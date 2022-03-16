package kg.wedevs.advert_bot.bot.handlers.v2.commandImpl;

import com.google.gson.Gson;
import kg.wedevs.advert_bot.bot.handlers.enums.CommandResultType;
import kg.wedevs.advert_bot.bot.handlers.enums.InlineStateEnum;
import kg.wedevs.advert_bot.bot.handlers.v2.BotCommandResult;
import kg.wedevs.advert_bot.bot.handlers.v2.WorkerCommand;
import kg.wedevs.advert_bot.bot.handlers.v2.models.InlineCommandState;
import kg.wedevs.advert_bot.bot.helpers.BotHelper;
import kg.wedevs.advert_bot.bot.helpers.BotLogger;
import kg.wedevs.advert_bot.bot.services.AdvertService;
import kg.wedevs.advert_bot.bot.services.PhotoService;
import kg.wedevs.advert_bot.bot.services.PlatformService;
import kg.wedevs.advert_bot.models.*;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Log
@Component
public class InlineCommandWorker implements WorkerCommand {
    private TelegramUser user;
    private Update update;
    private TelegramLongPollingBot botService;

    @Value("${bot.platformCode}")
    private String botPlatformCode;

    private AdvertService advertService;
    private PhotoService photoService;
    private PlatformService platformService;


    public InlineCommandWorker(AdvertService advertService, PhotoService photoService, PlatformService platformService) {
        this.advertService = advertService;
        this.platformService = platformService;
        this.photoService = photoService;
    }

    @Override
    public void setTelegramUser(TelegramUser user) {
        this.user = user;
    }

    @Override
    public void setUpdate(Update update) {
        this.update = update;
    }

    @Override
    public void setBotService(TelegramLongPollingBot botService) {
        this.botService = botService;
    }

    @Override
    public BotCommandResult execute() {
        BotCommandResult result = new BotCommandResult();
        if (!update.hasCallbackQuery()) {
            return result;
        }

        if (update.getCallbackQuery().getData().length() == 1 && update.getCallbackQuery().getData().matches("\\d")) {
            return result;
        }

        try {
            Gson gson = new Gson();

            InlineCommandState command = gson.fromJson(update.getCallbackQuery().getData(), InlineCommandState.class);

            if (command == null) {
                return result;
            }

            switch (command.getState()) {
                case SOLD_ADVERT: {
                    AdvertModel advertModel = advertService.getAdvertById(command.getMessageId());
                    if (advertModel.isSold()) {
                        break;
                    }
                    Platform platform = platformService.getPlatformByCode(botPlatformCode);
                    if (platform == null) {
                        break;
                    }

                    EditMessageText editMessageText = new EditMessageText();
                    editMessageText.setChatId("@" + platform.getChannelId());
                    editMessageText.setMessageId(Long.valueOf(advertModel.getTelegramMessageId()).intValue());

                    StringBuilder builder = new StringBuilder();

                    List<StepModel> steps = platform.getStepFields();
                    List<ValueModel> values = advertModel.getValues();

                    builder.append("\n");

                    builder.append(BotHelper.getAdvertMessage(values, steps));

                    builder.append("\n\nНе актуально");
                    editMessageText.setText(builder.toString());
                    editMessageText.setReplyMarkup(null);

                    botService.execute(editMessageText);
                    advertModel.setSold(true);
                    advertService.addAdvert(advertModel);

                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setText("Объявление успешно помечено как не актуальное");
                    sendMessage.setChatId(user.getTelegramId().toString());
                    botService.execute(sendMessage);

                    result.setType(CommandResultType.COMPLETE);

                    break;
                }
                case SHOW_ADVERT_TO_USER: {
                    AdvertModel advertModel = advertService.getAdvertById(command.getMessageId());

                    if (advertModel.isSold()) {
                        botService.execute(new SendMessage(user.getTelegramId().toString(), "Извните, объявление не актуально"));
                        break;
                    }

                    Platform platform = platformService.getPlatformByCode(botPlatformCode);
                    if (platform == null) {
                        break;
                    }

                    List<StepModel> steps = platform.getStepFields();

                    List<ValueModel> values = advertModel.getValues();

                    StringBuilder messageBuilder = new StringBuilder();
                    messageBuilder.append("Актуальное объявление");
                    messageBuilder.append("\n");

                    messageBuilder.append(BotHelper.getAdvertMessage(values, steps));

                    List<InlineKeyboardButton> rowKeyboards = new ArrayList<>();

                    InlineCommandState inlineCommandState = new InlineCommandState();
                    inlineCommandState.setState(InlineStateEnum.SHOW_ADVERT_TO_USER);
                    inlineCommandState.setMessageId(advertModel.getId());

                    String username = user.getUserName();
                    ValueModel phoneValue = values.stream().filter(v -> v.getFieldCode().equalsIgnoreCase("phone")).findFirst().orElse(null);


                    String phoneNumber = null;
                    String phone = null;
                    if (phoneValue != null) {
                        phoneNumber = phoneValue.getValue();
                        phone = "tel:+" + phoneValue.getValue().replaceAll("\\+", "");
                    }

                    String writeAuthor = "https://t.me/" + username;


                    if (username != null) {
                        rowKeyboards.add(InlineKeyboardButton.builder().text("Написать").url(writeAuthor).build());
                    }
                    if (phone != null) {
                        messageBuilder.append("\n\nПозвонить => [+" + phoneNumber + "](" + phone + ") <= \n\n или Вы можете написать продавцу");
                    }

                    if (platform.isRequiredPhoto()) {
                        sendAdvertPhotos(advertModel.getId(), user.getTelegramId().toString());
                    }

                    SendMessage message = new SendMessage();
                    message.setParseMode(ParseMode.MARKDOWN);
                    message.setText(messageBuilder.toString());
                    message.setChatId(user.getTelegramId().toString());
                    message.setReplyMarkup(InlineKeyboardMarkup.builder().keyboardRow(rowKeyboards).build());

                    botService.execute(message);

                    break;
                }
                case UP_MESSAGE: {
                    AdvertModel advertModel = advertService.getAdvertById(command.getMessageId());
                    if (advertModel.getCreatedDate().plusHours(2).isAfter(LocalDateTime.now())) {

                        break;
                    }
                    Platform platform = platformService.getPlatformByCode(botPlatformCode);
                    if (platform == null) {
                        break;
                    }

                    SendMessage message = new SendMessage();

                    StringBuilder builder = new StringBuilder();

                    message.setChatId("@" + platform.getChannelId());

                    List<StepModel> steps = platform.getStepFields();
                    List<ValueModel> values = advertModel.getValues();

                    builder.append("\n");

                    builder.append(BotHelper.getAdvertMessage(values, steps));

                    builder.append("\n\nЧтобы проверить объявления, подпишитесь на бота @" + botService.getMe().getUserName());
                    message.setText(builder.toString());

                    List<InlineKeyboardButton> rowKeyboards = new ArrayList<>();

                    InlineCommandState inlineCommandState = new InlineCommandState();
                    inlineCommandState.setState(InlineStateEnum.SHOW_ADVERT_TO_USER);
                    inlineCommandState.setMessageId(advertModel.getId());

                    rowKeyboards.add(InlineKeyboardButton.builder().text("Посмотреть объявление").callbackData(gson.toJson(inlineCommandState)).build());

                    message.setReplyMarkup(InlineKeyboardMarkup.builder().keyboardRow(rowKeyboards).build());


                    if (platform.isRequiredPhoto()) {
                        sendAdvertPrimaryPhoto(advertModel.getId(), "@" + platform.getChannelId());
                    }

                    Message sendedMessage = botService.execute(message);

                    advertModel.setTelegramMessageId(sendedMessage.getMessageId());
                    advertModel.setSold(false);
                    advertService.addAdvert(advertModel);

                    message = new SendMessage();
                    message.setText("Объявление успешно поднятно");
                    message.setChatId(user.getTelegramId().toString());

                    botService.execute(message);
                }
            }

        } catch (Exception error) {
            BotLogger.logWarning(log, user, error.getMessage());
        }

        return result;
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

    private void sendAdvertPhotos(Long advertId, String chatId) throws Exception {

        List<PhotoModel> photoModels = photoService.getPhotosByAdvertsId(advertId);

        if (photoModels == null || photoModels.isEmpty()) {
            throw new Exception("Advert with id: " + advertId + " photos not found");
        }

        String fileId = photoModels.stream()
                .filter(PhotoModel::isPrimary)
                .findAny()
                .orElseThrow(() -> new Exception("Advert with id: " + advertId + " no primary photo")).getFileId();

        botService.execute(new SendPhoto(chatId, new InputFile(fileId)));

        for (PhotoModel photo : photoModels) {
            if (!photo.isPrimary()) {
                botService.execute(new SendPhoto(chatId, new InputFile(photo.getFileId())));
            }
        }
    }

    @Override
    public SendMessage sendDefaultMessage(Update update) {
        return null;
    }
}
