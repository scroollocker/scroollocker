package kg.wedevs.advert_bot.bot.handlers.v2.commandImpl;

import com.google.gson.Gson;
import kg.wedevs.advert_bot.bot.handlers.enums.BaseStateEnums;
import kg.wedevs.advert_bot.bot.handlers.enums.CommandResultType;
import kg.wedevs.advert_bot.bot.handlers.v2.BotCommandResult;
import kg.wedevs.advert_bot.bot.handlers.v2.WorkerCommand;
import kg.wedevs.advert_bot.bot.handlers.v2.models.AdvertStepModel;
import kg.wedevs.advert_bot.bot.handlers.v2.models.PresetModel;
import kg.wedevs.advert_bot.bot.handlers.v2.models.StepModel;
import kg.wedevs.advert_bot.bot.helpers.BotHelper;
import kg.wedevs.advert_bot.bot.helpers.BotKeyboardBuilder;
import kg.wedevs.advert_bot.bot.services.AdvertService;
import kg.wedevs.advert_bot.bot.services.PlatformService;
import kg.wedevs.advert_bot.bot.services.TelegramUserService;
import kg.wedevs.advert_bot.models.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateAdvertCommand implements WorkerCommand {

    TelegramUser telegramUser;
    Update update;
    AdvertStepModel advertSteps;

    final TelegramUserService service;
    final AdvertService advertService;
    final PlatformService platformService;

    @Value("${bot.platformCode}")
    private String botPlatformCode;
    Platform platform;

    Gson gson = new Gson();

    Logger logger = Logger.getLogger(CreateAdvertCommand.class.getName());

    private static final String commandToMainScreen = "На главную";
    private static final String commandEmptyValue = "Пропустить поле";


    @Override
    public void setBotService(TelegramLongPollingBot botService) {
    }

    private void saveAdvertToDatabase(AdvertStepModel step) {
        AdvertModel advertModel = new AdvertModel();

        if (step.getPhotos() != null && !step.getPhotos().isEmpty()) {
            advertModel.setPhotos(step.getPhotos().stream().map(p -> {
                PhotoModel photoModel = new PhotoModel();
                photoModel.setFileId(p.getFileId());
                return photoModel;
            }).collect(Collectors.toList()));
            advertModel.getPhotos().get(0).setPrimary(true);
        }
        if (step.getValues() != null && !step.getValues().isEmpty()) {

            List<ValueModel> values = step.getValues().stream().map(v -> {
                ValueModel valueModel = new ValueModel();
                valueModel.setValue(v.getValue());
                valueModel.setFieldCode(v.getCode());
                return valueModel;
            }).collect(Collectors.toList());


            advertModel.setValues(values);
        }

        advertModel.setCreatedDate(LocalDateTime.now());
        advertModel.setUser(telegramUser);
        advertModel.setPlatformCode(platform.getCode());
        advertModel.setPlatform(platform);

        advertService.deleteAllUnsaved(telegramUser.getId());
        advertService.addAdvert(advertModel);

    }

    SendMessage getStepMessage(Long chatId) {
        SendMessage message = new SendMessage();

        if (advertSteps == null) return null;

        StepModel step = advertSteps.getCurrentStep();

        if (step == null) {
            return null;
        }

        message.setChatId(chatId.toString());
        message.setText("Заполните поле: " + step.getTitle());

        BotKeyboardBuilder keyboardBuilder = BotKeyboardBuilder.create()
                .setIsClosable(true)
                .setIsFlex(true);


        if (step.isPreset()) {
            List<PresetModel> presets = advertSteps.getAllPresets(step);
            if (presets != null && !presets.isEmpty()) {
                int j = 0;
                for (PresetModel preset : presets) {
                    keyboardBuilder = keyboardBuilder.addButton(preset.getValue());
                    j++;
                    if (j == 3) {
                        j = 0;
                        keyboardBuilder = keyboardBuilder.appendRow();
                    }
                }
                if (j > 0) {
                    keyboardBuilder = keyboardBuilder.appendRow();
                }
            }
        }

        if (!step.isRequired()) {
            keyboardBuilder = keyboardBuilder.addButton(commandEmptyValue)
                    .appendRow();

        }

        keyboardBuilder = keyboardBuilder.addButton(commandToMainScreen)
                .appendRow();

        message.setReplyMarkup(keyboardBuilder.build());


        return message;
    }

    @Override
    public void setTelegramUser(TelegramUser user) {
        this.telegramUser = user;
        if (telegramUser.getLastState() != BaseStateEnums.CREATE_ADVERT_SCREEN) {
            return;
        }

        platform = platformService.getPlatformByCode(botPlatformCode);

        String jsonString = telegramUser.getStateJson();
        if (jsonString == null) {

            if (platform == null) return;

            List<kg.wedevs.advert_bot.models.StepModel> steps = platform.getStepFields();
            advertSteps = new AdvertStepModel();
            advertSteps.setSteps(steps.stream().map(s -> {
                StepModel step = new StepModel();
                step.setCode(s.getCode());
                step.setPreset(s.isPreset());
                step.setRequired(s.isRequired());
                step.setTitle(s.getTitle());
                step.setNumber(s.isNumber());
                step.setNeedCheckPreset(s.isNeedPresetCheck());
                step.setPresets(s.getStepPresets().stream().map(preset -> {
                    PresetModel presetModel = new PresetModel();
                    presetModel.setValue(preset.getValue());
                    presetModel.setParentCode(preset.getParentCode());
                    presetModel.setParentValue(preset.getParentValue());
                    return presetModel;
                }).collect(Collectors.toList()));
                return step;
            }).collect(Collectors.toList()));

            if (platform.isRequiredPhoto()) {
                String jsonText = gson.toJson(advertSteps);
                telegramUser.setStateJson(jsonText);
                telegramUser.setLastState(BaseStateEnums.UPLOAD_PHOTO_SCREEN);
                service.editUser(telegramUser);
            }
//            advertSteps = BotHelper.generateSteps();
        } else {
            advertSteps = gson.fromJson(jsonString, AdvertStepModel.class);
            System.out.println(jsonString);
        }

    }

    @Override
    public void setUpdate(Update update) {
        this.update = update;
    }

    @Override
    public BotCommandResult execute() {
        BotCommandResult result = new BotCommandResult();
        if (telegramUser.getLastState() != BaseStateEnums.CREATE_ADVERT_SCREEN || !update.hasMessage()) {
            return result;
        }

        Long chatId = update.getMessage().getChatId();
        try {
            if (update.hasMessage()) {
                String messageText = update.getMessage().getText();
                if (Objects.equals(messageText, commandToMainScreen)) {
                    result.setType(CommandResultType.STATE_UPDATE);
                    result.setState(BaseStateEnums.MAIN_SCREEN);
                    return result;
                }

                StepModel step = advertSteps.getCurrentStep();

                if (step != null) {
                    if (Objects.equals(messageText, commandEmptyValue)) {
                        advertSteps.setStepValue(step.getCode(), "-", step.getTitle());
                    } else if (step.isNumber()) {
                        if (BotHelper.isNumeric(messageText)) {
                            advertSteps.setStepValue(step.getCode(), messageText, step.getTitle());
                        } else {

                            throw new Exception("Format exception");
                        }
                    } else if (step.isPreset() && step.isNeedCheckPreset()) {
                        List<PresetModel> presets = advertSteps.getAllPresets(step);
                        if (presets != null && !presets.isEmpty()) {
                            Optional<PresetModel> finded = presets.stream().filter(v -> Objects.equals(v.getValue(), messageText)).findFirst();
                            if (finded.isPresent()) {
                                advertSteps.setStepValue(step.getCode(), messageText, step.getTitle());
                            } else {

                                throw new Exception("Format exception");
                            }
                        }
                    } else {
                        advertSteps.setStepValue(step.getCode(), messageText, step.getTitle());
                    }
                    StepModel nextStep = advertSteps.getNextStep();
                    if (nextStep != null) {
                        advertSteps.setStep(nextStep.getCode());
                        String jsonText = gson.toJson(advertSteps);
                        telegramUser.setStateJson(jsonText);
                        service.editUser(telegramUser);
                        throw new Exception("Show next step");
                    } else {
                        //Save advert to database
                        String jsonAdvert = gson.toJson(advertSteps);
                        logger.log(Level.INFO, jsonAdvert);
                        saveAdvertToDatabase(advertSteps);

                        result.setType(CommandResultType.STATE_UPDATE);
                        result.setState(BaseStateEnums.CONFIRM_ADVERT_SCREEN);
                    }

                } else {
                    throw new Exception("No steps");
                }

            } else {
                throw new Exception("No message");
            }
        } catch (Exception err) {
            logger.log(Level.WARNING, err.getMessage());
            logger.log(Level.WARNING, "Username " + telegramUser.getUserName());
            logger.log(Level.WARNING, "State " + telegramUser.getLastState());

            SendMessage message = getStepMessage(chatId);
            if (message == null) {
                result.setType(CommandResultType.STATE_UPDATE);
                result.setState(BaseStateEnums.MAIN_SCREEN);

            } else {
                result.setMessages(Collections.singletonList(message));
            }

        }

        return result;
    }

    @Override
    public SendMessage sendDefaultMessage(Update update) {
        if (update.hasMessage() && telegramUser.getLastState() == BaseStateEnums.CREATE_ADVERT_SCREEN) {
            logger.info("isPhotosShown check: " + advertSteps.isPhotosShown());
            return getStepMessage(update.getMessage().getChatId());
        }
        return null;
    }
}
