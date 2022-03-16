package kg.wedevs.advert_bot.bot.handlers.v2.commandImpl;

import com.google.gson.Gson;
import kg.wedevs.advert_bot.bot.handlers.enums.BaseStateEnums;
import kg.wedevs.advert_bot.bot.handlers.enums.CommandResultType;
import kg.wedevs.advert_bot.bot.handlers.v2.BotCommandResult;
import kg.wedevs.advert_bot.bot.handlers.v2.WorkerCommand;
import kg.wedevs.advert_bot.bot.handlers.v2.models.AdvertStepModel;
import kg.wedevs.advert_bot.bot.handlers.v2.models.PhotoModel;
import kg.wedevs.advert_bot.bot.helpers.BotKeyboardBuilder;
import kg.wedevs.advert_bot.bot.helpers.BotLogger;
import kg.wedevs.advert_bot.bot.services.TelegramUserService;
import kg.wedevs.advert_bot.models.TelegramUser;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;
import java.util.logging.Level;

@Log
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SelectBasePhotoCommand implements WorkerCommand {

    TelegramUser telegramUser;
    Update update;
    TelegramLongPollingBot botService;

    final TelegramUserService service;

    AdvertStepModel advertSteps;

    final Gson gson = new Gson();

    static final String commandToMainScreen = "На главную";
    static final String commandReady = "Готово";
    static final String textSelectBasePhoto = "Выберите основное фото";
    static final String buttonSelect = "Выбрать";
    static final String buttonSelected = "Выбрано!";

    @Override
    public void setTelegramUser(TelegramUser user) {
        this.telegramUser = user;
        if (telegramUser.getLastState() != BaseStateEnums.SELECT_BASE_PHOTO_SCREEN) {
            return;
        }

        String jsonAdvert = telegramUser.getStateJson();


        if (jsonAdvert == null) {
            BotLogger.logWarning(log, telegramUser, "jsonAdvert is null");
            return;
        }
        advertSteps = gson.fromJson(jsonAdvert, AdvertStepModel.class);
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
        if (telegramUser.getLastState() != BaseStateEnums.SELECT_BASE_PHOTO_SCREEN) {
            return result;
        }

        if (update.hasMessage()) {
            if (update.hasMessage() && update.getMessage().hasText()) {
                String messageText = update.getMessage().getText();
                if (Objects.equals(messageText, commandToMainScreen)) {
                    result.setType(CommandResultType.STATE_UPDATE);
                    result.setState(BaseStateEnums.MAIN_SCREEN);
                    return result;
                }
            }

            if (update.getMessage().hasText() && isSelectedPrimaryPhoto()) {
                String messageText = update.getMessage().getText();
                if (Objects.equals(messageText, commandReady)) {
                    advertSteps.setPhotosShown(false);
                    result.setType(CommandResultType.CONTINUE);
                    result.setState(BaseStateEnums.CREATE_ADVERT_SCREEN);
                    return result;
                }
            }
        }

        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            Message message = update.getCallbackQuery().getMessage();

            try {
                List<PhotoModel> photos = advertSteps.getPhotos();
                for (int i = 0; i < photos.size(); i++) {
                    PhotoModel photoModel = photos.get(i);
                    log.info(photoModel.getMessageId() == null ? "photo messageId with index " + i + ": null" : photoModel.getMessageId().toString());

                    if (i == Integer.parseInt(callbackQuery.getData())) {
                        photoModel.setMessageId(message.getMessageId());
                        photoModel.setChatId(message.getChatId());
                        photoModel.setPrimary(true);
                        editReplyMarkup(i, photoModel, buttonSelected);

                    } else {
                        if (photoModel.getMessageId() == null) continue;

                        editReplyMarkup(i, photoModel, buttonSelect);

                    }
                }

                String jsonAdvert = gson.toJson(advertSteps);
                log.log(Level.INFO, jsonAdvert);
                telegramUser.setStateJson(jsonAdvert);
                service.editUser(telegramUser);

            } catch (Exception err) {
                BotLogger.logWarning(log, telegramUser, err.getMessage());
            }


            return result;
        }

        return result;
    }

    private boolean isSelectedPrimaryPhoto() {
        return advertSteps.getPhotos().stream().anyMatch(PhotoModel::isPrimary);
    }

    private void editReplyMarkup(int i, PhotoModel photoModel, String buttonSelect) throws TelegramApiException {
        InlineKeyboardButton button = InlineKeyboardButton.builder()
                .text(buttonSelect)
                .callbackData(String.valueOf(i))
                .build();

        InlineKeyboardMarkup keyboardMarkup = InlineKeyboardMarkup.builder()
                .keyboardRow(Collections.singletonList(button))
                .build();


        EditMessageReplyMarkup editMarkup = EditMessageReplyMarkup.builder()
                .chatId(photoModel.getChatId().toString())
                .messageId(photoModel.getMessageId())
                .replyMarkup(keyboardMarkup)
                .build();

        botService.execute(editMarkup);
    }

    @Override
    public SendMessage sendDefaultMessage(Update update) {
        if (telegramUser.getLastState() != BaseStateEnums.SELECT_BASE_PHOTO_SCREEN || (!update.hasMessage() && !update.hasCallbackQuery())) {
            return null;
        }


        SendMessage message = new SendMessage();

        String chatId;

        if (update.hasMessage()) {
            chatId = update.getMessage().getChatId().toString();
            message.setText(textSelectBasePhoto);
        } else {
            chatId = update.getCallbackQuery().getMessage().getChatId().toString();
            message.setText("Выбрано фото " + (Integer.parseInt(update.getCallbackQuery().getData()) + 1));
        }

        message.setChatId(chatId);

        if (!advertSteps.isPhotosShown()) {
            sendPhotosWithReplyMarkup(chatId);
            advertSteps.setPhotosShown(true);
            telegramUser.setStateJson(gson.toJson(advertSteps));
            service.editUser(telegramUser);
            BotLogger.logInfo(log, telegramUser, "Photos shown to user");

        }


        BotKeyboardBuilder keyboardBuilder = BotKeyboardBuilder.create()
                .setIsClosable(true)
                .setIsFlex(true);
        if (isSelectedPrimaryPhoto()) {
            keyboardBuilder.addButton(commandReady).appendRow();
        }

        keyboardBuilder.addButton(commandToMainScreen).appendRow();

        message.setReplyMarkup(keyboardBuilder.build());

        return message;
    }

    private void sendPhotosWithReplyMarkup(String chatId) {
        if (advertSteps == null) {
            BotLogger.logWarning(log, telegramUser, "advertSteps is null");
            return;
        }

        if (advertSteps.getPhotos() == null) {
            BotLogger.logWarning(log, telegramUser, "advertSteps.getPhotos() is null");
            return;
        }
        if (advertSteps.getPhotos().isEmpty()) {
            BotLogger.logWarning(log, telegramUser, "advertSteps.getPhotos() is empty");
            return;
        }

        try {
            List<PhotoModel> photos = advertSteps.getPhotos();
            for (int i = 0; i < photos.size(); i++) {
                PhotoModel photoModel = photos.get(i);

                String fileId = photoModel.getFileId();
                BotLogger.logInfo(log, telegramUser, "Try send photo with id: " + fileId);

                SendPhoto sendPhoto = new SendPhoto(chatId, new InputFile(fileId));

                InlineKeyboardButton button = InlineKeyboardButton.builder()
                        .text(buttonSelect)
                        .callbackData(String.valueOf(i))
                        .build();

                InlineKeyboardMarkup keyboardMarkup = InlineKeyboardMarkup.builder()
                        .keyboardRow(Collections.singletonList(button))
                        .build();

                sendPhoto.setReplyMarkup(keyboardMarkup);

                botService.execute(sendPhoto);

                botService.execute(new SendMessage(chatId, "."));
            }
        } catch (Exception err) {
            BotLogger.logWarning(log, telegramUser, err.getMessage());
        }
    }
}
