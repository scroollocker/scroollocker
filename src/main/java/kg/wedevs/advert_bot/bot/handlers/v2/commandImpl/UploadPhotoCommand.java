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
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import java.util.logging.Level;

@Log
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UploadPhotoCommand implements WorkerCommand {

    TelegramUser telegramUser;
    Update update;

    final TelegramUserService service;

    AdvertStepModel advertSteps;

    final Gson gson = new Gson();

    static final String commandToMainScreen = "На главную";
    static final String commandReady = "Готово";
    static final String commandUploadAgain = "Загрузить снова";

    @Override
    public void setTelegramUser(TelegramUser user) {
        this.telegramUser = user;

        if (telegramUser.getLastState() != BaseStateEnums.UPLOAD_PHOTO_SCREEN) {
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
    }

    @Override
    public BotCommandResult execute() {
        BotCommandResult result = new BotCommandResult();
        if (telegramUser.getLastState() != BaseStateEnums.UPLOAD_PHOTO_SCREEN || !update.hasMessage()) {
            return result;
        }

        try {
            if (advertSteps.getPhotos() == null) {
                advertSteps.setPhotos(new ArrayList<>());
            }

            int numberOfPhotos = advertSteps.getPhotos().size();

            if (update.getMessage().hasText()) {
                String messageText = update.getMessage().getText();
                if (Objects.equals(messageText, commandToMainScreen)) {
                    result.setType(CommandResultType.STATE_UPDATE);
                    result.setState(BaseStateEnums.MAIN_SCREEN);
                    return result;
                }

                if (numberOfPhotos >= 2 && numberOfPhotos <= 4 && Objects.equals(messageText, commandReady)) {
                    String jsonAdvert = gson.toJson(advertSteps);
                    log.log(Level.INFO, jsonAdvert);
                    telegramUser.setStateJson(jsonAdvert);
                    result.setType(CommandResultType.CONTINUE);
                    result.setState(BaseStateEnums.SELECT_BASE_PHOTO_SCREEN);
                    return result;
                }

                if (numberOfPhotos > 4 && Objects.equals(messageText, commandUploadAgain)) {
                    advertSteps.setPhotos(new ArrayList<>());
                    return result;
                }
            }

            if (update.getMessage().hasPhoto()) {
                PhotoSize photo = update
                        .getMessage()
                        .getPhoto()
                        .stream()
                        .max(Comparator.comparing(PhotoSize::getFileSize))
                        .orElse(null);
                assert photo != null;
                advertSteps.getPhotos().add(new PhotoModel(photo.getFileId(), photo.getFileUniqueId()));

                String jsonAdvert = gson.toJson(advertSteps);
                log.log(Level.INFO, jsonAdvert);
                telegramUser.setStateJson(jsonAdvert);
                service.editUser(telegramUser);

                numberOfPhotos = advertSteps.getPhotos().size();

                log.log(Level.INFO, numberOfPhotos + " photo loaded");

            } else {
                BotLogger.logWarning(log, telegramUser, "Photo not found");
            }
        } catch (Exception err) {
            BotLogger.logWarning(log, telegramUser, err.getMessage());
        }

        return result;
    }

    @Override
    public SendMessage sendDefaultMessage(Update update) {
        if (!update.hasMessage() || telegramUser.getLastState() != BaseStateEnums.UPLOAD_PHOTO_SCREEN) {
            return null;
        }

        SendMessage message = new SendMessage();

        message.setChatId(update.getMessage().getChatId().toString());

        StringBuilder builder = new StringBuilder();
        builder.append("Загрузите от 2 до 4 фото");

        BotKeyboardBuilder keyboardBuilder = BotKeyboardBuilder.create()
                .setIsClosable(true)
                .setIsFlex(true);

        if (telegramUser.getStateJson() != null && advertSteps.getPhotos() != null) {
            int numberOfPhotos = advertSteps.getPhotos().size();
            builder.append("\nЗагружено ").append(advertSteps.getPhotos().size()).append(" фото");

            if (numberOfPhotos >= 2 && numberOfPhotos <= 4) {
                keyboardBuilder.addButton(commandReady).appendRow();
            } else if (numberOfPhotos > 4) {
                keyboardBuilder.addButton(commandUploadAgain).appendRow();
            }
        }

        keyboardBuilder.addButton(commandToMainScreen).appendRow();

        message.setText(builder.toString());
        message.setReplyMarkup(keyboardBuilder.build());

        return message;
    }
}
