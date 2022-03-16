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
import kg.wedevs.advert_bot.models.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class ConfirmAdvertCommand implements WorkerCommand {
    TelegramUser telegramUser;
    Update update;
    TelegramLongPollingBot botService;
    AdvertService advertService;
    PhotoService photoService;
    PlatformService platformService;

    @Value("${bot.platformCode}")
    private String botPlatformCode;

    Gson gson = new Gson();

    private Logger logger = Logger.getLogger(ConfirmAdvertCommand.class.getName());

    public ConfirmAdvertCommand(AdvertService advertService, PhotoService photoService, PlatformService platformService) {
        this.advertService = advertService;
        this.photoService = photoService;
        this.platformService = platformService;
    }

    private static final String commandConfirm = "Подтвердить";
    private static final String commandCancel = "Отменить";

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
        this.botService = botService;
    }

    private void sendMessageToChat() throws Exception {
        logger.log(Level.INFO, "Start send to chat");
//        logger.log(Level.INFO, "@"+botChannel);
        SendMessage message = new SendMessage();
        StringBuilder messageBuilder = new StringBuilder();

        List<AdvertModel> advertModel = advertService.getAdvertsByUserId(telegramUser.getId(), false);
        AdvertModel advertModelData = null;
        if (advertModel != null && !advertModel.isEmpty()) {
            advertModelData = advertModel.stream().findFirst().orElse(null);
        }
        if (advertModelData != null) {
            Platform platform = platformService.getPlatformByCode(botPlatformCode);
            if (platform == null) {
                return;
            }

            String platformChannelId = platform.getChannelId();
            logger.log(Level.INFO, "@" + platformChannelId);
            message.setChatId("@" + platformChannelId);
//            message.setChatId("@"+botChannel);

            List<StepModel> steps = platform.getStepFields();
            List<ValueModel> values = advertModelData.getValues();

            messageBuilder.append("\n");

            messageBuilder.append(BotHelper.getAdvertMessageRequired(values, steps));  // getAdvertMessageRequired было

            messageBuilder.append("\n\nЧтобы проверить объявления, подпишитесь на бота @" + botService.getMe().getUserName());
            message.setText(messageBuilder.toString());


            List<InlineKeyboardButton> rowKeyboards = new ArrayList<>();

            InlineCommandState inlineCommandState = new InlineCommandState();
            inlineCommandState.setState(InlineStateEnum.SHOW_ADVERT_TO_USER);
            inlineCommandState.setMessageId(advertModelData.getId());

            rowKeyboards.add(InlineKeyboardButton.builder().text("Посмотреть объявление").callbackData(gson.toJson(inlineCommandState)).build());

            message.setReplyMarkup(InlineKeyboardMarkup.builder().keyboardRow(rowKeyboards).build());

            if (platform.isRequiredPhoto()) {
                sendAdvertPrimaryPhoto(advertModelData.getId(), "@" + platformChannelId);
            }

            Message execute = botService.execute(message);
            advertModelData.setTelegramMessageId(execute.getMessageId());
            advertModelData.setSend(true);
            advertService.saveAdvert(advertModelData);
        }

    }

    private void sendAdvertPrimaryPhoto(Long advertId, String chatId) throws Exception {

        List<PhotoModel> photoModels = photoService.getPhotosByAdvertsId(advertId);

        if (photoModels == null || photoModels.isEmpty()) {
            throw new Exception("Advert with id: " + advertId + " photos not found");
        }

        String fileId = photoModels.stream()
                .filter(PhotoModel::isPrimary)
                .findAny()
                .orElseThrow(() -> new Exception("Advert with id: " + advertId + " no primary photo")).getFileId();

        botService.execute(new SendPhoto(chatId, new InputFile(fileId)));
    }

    private SendMessage getDefaultMessage(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        StringBuilder builder = new StringBuilder();
        builder.append("Подтвердите объявление\n");

        List<AdvertModel> advertModel = advertService.getAdvertsByUserId(telegramUser.getId(), false);
        if (advertModel != null && !advertModel.isEmpty()) {
            AdvertModel advertModelData = advertModel.stream().findFirst().orElse(null);

            Platform platform = platformService.getPlatformByCode(botPlatformCode);
            if (platform == null) {
                return null;
            }

            if (platform.isRequiredPhoto()) {
                try {
                    sendAdvertPrimaryPhoto(advertModelData.getId(), chatId.toString());
                } catch (Exception err) {
                    BotLogger.logWarning(logger, telegramUser, err.getMessage());
                }
            }

            List<StepModel> steps = platform.getStepFields();

            List<ValueModel> values = advertModelData.getValues();

            builder.append(BotHelper.getAdvertMessage(values, steps));

        }

        message.setText(builder.toString());

        ReplyKeyboardMarkup markup = BotKeyboardBuilder.create()
                .addButton(commandConfirm)
                .addButton(commandCancel)
                .appendRow()
                .setIsFlex(true)
                .setIsClosable(true)
                .build();

        message.setReplyMarkup(markup);
        return message;
    }

    @Override
    public BotCommandResult execute() {
        BotCommandResult result = new BotCommandResult();
        if (!update.hasMessage() || telegramUser.getLastState() != BaseStateEnums.CONFIRM_ADVERT_SCREEN) {
            return result;
        }

        Long chatId = update.getMessage().getChatId();

        try {
            switch (update.getMessage().getText()) {
                case commandConfirm: {
                    sendMessageToChat();
                    SendMessage message = new SendMessage();
                    message.setChatId(chatId.toString());
                    message.setText("Сообщение успешно отправлено");

                    result.setMessages(Collections.singletonList(message));
                    result.setType(CommandResultType.STATE_UPDATE);
                    result.setState(BaseStateEnums.MAIN_SCREEN);
                    break;
                }
                case commandCancel: {
                    SendMessage message = new SendMessage();
                    message.setChatId(chatId.toString());
                    message.setText("Отправка отменена");

                    result.setMessages(Collections.singletonList(message));
                    result.setType(CommandResultType.STATE_UPDATE);
                    result.setState(BaseStateEnums.MAIN_SCREEN);
                    break;
                }
                default: {
                    throw new Exception("No commands");
                }
            }
        } catch (Exception err) {
            logger.log(Level.WARNING, err.getMessage());
            logger.log(Level.WARNING, "Username " + telegramUser.getUserName());
            logger.log(Level.WARNING, "State " + telegramUser.getLastState());
            result.setMessages(Collections.singletonList(getDefaultMessage(chatId)));
        }


        return result;
    }

    @Override
    public SendMessage sendDefaultMessage(Update update) {
        if (!update.hasMessage() || telegramUser.getLastState() != BaseStateEnums.CONFIRM_ADVERT_SCREEN) {
            return null;
        }
        return getDefaultMessage(update.getMessage().getChatId());
    }
}
