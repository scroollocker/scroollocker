package kg.wedevs.advert_bot.bot.handlers.v2.commandImpl;

import com.google.gson.Gson;
import kg.wedevs.advert_bot.bot.handlers.enums.BaseStateEnums;
import kg.wedevs.advert_bot.bot.handlers.enums.CommandResultType;
import kg.wedevs.advert_bot.bot.handlers.enums.InlineStateEnum;
import kg.wedevs.advert_bot.bot.handlers.v2.BotCommandResult;
import kg.wedevs.advert_bot.bot.handlers.v2.WorkerCommand;
import kg.wedevs.advert_bot.bot.handlers.v2.models.InlineCommandState;
import kg.wedevs.advert_bot.bot.helpers.BotHelper;
import kg.wedevs.advert_bot.bot.helpers.BotKeyboardBuilder;
import kg.wedevs.advert_bot.bot.helpers.BotLogger;
import kg.wedevs.advert_bot.bot.services.AdvertService;
import kg.wedevs.advert_bot.bot.services.PhotoService;
import kg.wedevs.advert_bot.bot.services.PlatformService;
import kg.wedevs.advert_bot.bot.services.TelegramUserService;
import kg.wedevs.advert_bot.models.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class MyAdvertsList implements WorkerCommand {
    TelegramUser telegramUser;
    Update update;
    TelegramLongPollingBot telegramService;
    AdvertService service;
    PhotoService photoService;
    TelegramUserService userService;
    PlatformService platformService;

    Logger logger = Logger.getLogger(MyAdvertsList.class.getName());

    Gson gson = new Gson();

    int elementsPerPage = 5;

    @Value("${bot.platformCode}")
    private String botPlatformCode;


    private static final String commandLoadMore = "Подгрузить";
    private static final String commandHome = "На главную";

    public MyAdvertsList(AdvertService service, TelegramUserService userService,
                         PlatformService platformService, PhotoService photoService) {
        this.service = service;
        this.userService = userService;
        this.platformService = platformService;
        this.photoService = photoService;
    }

    void loadPaginationElements(int page) throws TelegramApiException {

        List<AdvertModel> adverts = service.getAdvertsPagination(telegramUser.getId(), elementsPerPage, page);
        String chatId = telegramUser.getTelegramId().toString();

        if (adverts == null || adverts.isEmpty()) {
            SendMessage resultMessage = new SendMessage();
            resultMessage.setChatId(chatId);
            resultMessage.setText("Нет больше сообщений для отображения");
            telegramService.execute(resultMessage);
        } else {
            Platform platform = platformService.getPlatformByCode(botPlatformCode);
            for (AdvertModel advert : adverts) {
                if (advert.isSold()) continue;
                SendMessage message = new SendMessage();

                if (platform == null) {
                    continue;
                }

                sendAdvertPhotos(platform, advert, chatId);

                List<StepModel> steps = platform.getStepFields();
                List<ValueModel> values = advert.getValues();

                StringBuilder messageBuilder = new StringBuilder();

                messageBuilder.append("\n");

                messageBuilder.append(BotHelper.getAdvertMessage(values, steps));

                messageBuilder.append("\n\nЧтобы проверить объявления, подпишитесь на бота @" + telegramService.getMe().getUserName());
                message.setText(messageBuilder.toString());
                message.setChatId(telegramUser.getTelegramId().toString());

                List<InlineKeyboardButton> rowKeyboards = new ArrayList<>();

                InlineCommandState inlineCommandState = new InlineCommandState();
                inlineCommandState.setState(InlineStateEnum.SOLD_ADVERT);
                inlineCommandState.setMessageId(advert.getId());

                InlineCommandState inlineUpCommandState = new InlineCommandState();
                inlineUpCommandState.setState(InlineStateEnum.UP_MESSAGE);
                inlineUpCommandState.setMessageId(advert.getId());

                rowKeyboards.add(InlineKeyboardButton.builder().text("Не актуально").callbackData(gson.toJson(inlineCommandState)).build());
                rowKeyboards.add(InlineKeyboardButton.builder().text("Поднять").callbackData(gson.toJson(inlineUpCommandState)).build());

                message.setReplyMarkup(InlineKeyboardMarkup.builder().keyboardRow(rowKeyboards).build());

                telegramService.execute(message);

            }
            SendMessage message = new SendMessage();

            message.setChatId(telegramUser.getTelegramId().toString());

            ReplyKeyboardMarkup markup = BotKeyboardBuilder.create()
                    .addButton(commandLoadMore)
                    .appendRow()
                    .addButton(commandHome)
                    .appendRow()
                    .setIsFlex(true)
                    .setIsClosable(true)
                    .build();

            message.setText("Загрузить еще?");
            message.setReplyMarkup(markup);
            telegramService.execute(message);
        }
    }

    private void sendAdvertPhotos(Platform platform, AdvertModel advert, String chatId) throws TelegramApiException {
        Long advertId = advert.getId();

        List<PhotoModel> photoModels = photoService.getPhotosByAdvertsId(advertId);

        if (photoModels != null && !photoModels.isEmpty()) {
            PhotoModel primaryPhoto = photoModels.stream().filter(PhotoModel::isPrimary).findAny().orElse(null);
            if (primaryPhoto != null) {
                telegramService.execute(new SendPhoto(chatId, new InputFile(primaryPhoto.getFileId())));
            } else {
                logger.info("Advert id: " + advertId + " no primary Photo");
            }

            for (PhotoModel photo : photoModels) {
                if (!photo.isPrimary()) {
                    telegramService.execute(new SendPhoto(chatId, new InputFile(photo.getFileId())));
                }
            }
        } else if (platform.isRequiredPhoto()) {
            BotLogger.logWarning(logger, telegramUser, "Advert id: " + advertId +
                    " required photo not found");
        }
    }

    @Override
    public void setTelegramUser(TelegramUser user) {
        this.telegramUser = user;
    }

    @Override
    public void setUpdate(Update update) {
        this.update = update;
    }

    @Override
    public void setBotService(TelegramLongPollingBot botService) {
        this.telegramService = botService;
    }

    @Override
    public BotCommandResult execute() {
        BotCommandResult result = new BotCommandResult();
        if (!update.hasMessage() || telegramUser.getLastState() != BaseStateEnums.MY_ADVERTS_SCREEN) {
            return result;
        }

        try {
            switch (update.getMessage().getText()) {
                case commandLoadMore: {

                    String jsonData = telegramUser.getStateJson();
                    Integer page = 0;
                    if (jsonData != null) {
                        page = gson.fromJson(jsonData, Integer.class);
                    }

                    loadPaginationElements(page);
                    page += 1;
                    telegramUser.setStateJson(gson.toJson(page));
                    userService.editUser(telegramUser);
                    result.setType(CommandResultType.COMPLETE);
                    break;
                }
                case commandHome: {
                    result.setType(CommandResultType.STATE_UPDATE);
                    result.setState(BaseStateEnums.MAIN_SCREEN);
                    break;
                }
            }
        } catch (Exception err) {
            err.printStackTrace();
            logger.log(Level.WARNING, err.getMessage());
            logger.log(Level.WARNING, "Username " + telegramUser.getUserName());
            logger.log(Level.WARNING, "State " + telegramUser.getLastState());
            SendMessage defaultMessage = sendDefaultMessage(update);
            result.setMessages(Collections.singletonList(defaultMessage));
        }

        return result;
    }

    @Override
    public SendMessage sendDefaultMessage(Update update) {
        if (update.hasMessage() && telegramUser.getLastState() == BaseStateEnums.MY_ADVERTS_SCREEN) {
            SendMessage message = new SendMessage();

            message.setChatId(telegramUser.getTelegramId().toString());
            message.setText("В данном разделе отображаются Ваши заявки\n" +
                    "Воспользуйтесь командами ниже");

            ReplyKeyboardMarkup markup = BotKeyboardBuilder.create()
                    .addButton(commandLoadMore)
                    .appendRow()
                    .addButton(commandHome)
                    .appendRow()
                    .setIsFlex(true)
                    .setIsClosable(true)
                    .build();

            message.setReplyMarkup(markup);

            return message;

        }
        return null;
    }
}
