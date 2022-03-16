package kg.wedevs.advert_bot.bot.handlers.v2.commandImpl;

import com.google.gson.Gson;
import kg.wedevs.advert_bot.bot.handlers.enums.BaseStateEnums;
import kg.wedevs.advert_bot.bot.handlers.enums.CommandResultType;
import kg.wedevs.advert_bot.bot.handlers.v2.BotCommandResult;
import kg.wedevs.advert_bot.bot.handlers.v2.BotCommandWorker;
import kg.wedevs.advert_bot.bot.handlers.v2.WorkerCommand;
import kg.wedevs.advert_bot.bot.handlers.v2.models.AdvertStepModel;
import kg.wedevs.advert_bot.bot.handlers.v2.models.PresetModel;
import kg.wedevs.advert_bot.bot.handlers.v2.models.ValueModel;
import kg.wedevs.advert_bot.bot.handlers.v2.models.search.SearchModel;
import kg.wedevs.advert_bot.bot.helpers.BotHelper;
import kg.wedevs.advert_bot.bot.helpers.BotKeyboardBuilder;
import kg.wedevs.advert_bot.bot.services.*;
import kg.wedevs.advert_bot.models.AdvertSearchRequest;
import kg.wedevs.advert_bot.models.Platform;
import kg.wedevs.advert_bot.models.StepModel;
import kg.wedevs.advert_bot.models.TelegramUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component
public class SearchAdverts implements WorkerCommand {
    TelegramUser telegramUser;
    Update update;
    TelegramLongPollingBot botService;
    Platform platform;
    AdvertStepModel advertSteps;
    Gson gson = new Gson();
    Logger logger = Logger.getLogger(SearchAdverts.class.getName());

    AdvertService advertService;
    ValueService valueService;
    PlatformService platformService;
    TelegramUserService service;
    AdvertSearchRequestService advertSearchRequestService;

    @Value("${bot.platformCode}")
    private String botPlatformCode;

    private static final String commandEmptyValue = "Пропустить";
    private static final String commandToMainScreen = "На главную";

    public SearchAdverts(AdvertService advertService, ValueService valueService, PlatformService platformService, TelegramUserService service, AdvertSearchRequestService advertSearchRequestService) {
        this.advertService = advertService;
        this.valueService = valueService;
        this.platformService = platformService;
        this.service = service;
        this.advertSearchRequestService = advertSearchRequestService;
    }

    private void saveAdvertToDatabase(AdvertStepModel stepModel) {
        AdvertSearchRequest searchRequest = new AdvertSearchRequest();

        SearchModel searchModel = new SearchModel();

        searchModel.setValueList(stepModel.getValues());

        String valueJson = gson.toJson(searchModel);

        searchRequest.setPlatform(platform);
        searchRequest.setUser(telegramUser);
        searchRequest.setRequestData(valueJson);

        telegramUser.setSearches(new HashSet<>(Collections.singletonList(searchRequest)));

        advertSearchRequestService.saveSearch(searchRequest);
    }

    @Override
    public void setTelegramUser(TelegramUser user) {
        this.telegramUser = user;
        if (telegramUser.getLastState() != BaseStateEnums.SEARCH_ADVERT_SCREEN) {
            return;
        }
        String jsonString = telegramUser.getStateJson();
        if (jsonString == null) {
            platform = platformService.getPlatformByCode(botPlatformCode);

            if (platform == null) return;

            List<StepModel> steps = platform.getStepFields();
            advertSteps = new AdvertStepModel();
            advertSteps.setSteps(steps.stream().filter(StepModel::isRequired).map(s -> {

                kg.wedevs.advert_bot.bot.handlers.v2.models.StepModel step = new kg.wedevs.advert_bot.bot.handlers.v2.models.StepModel();
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
        } else {
            advertSteps = gson.fromJson(jsonString, AdvertStepModel.class);
        }
    }

    SendMessage getStepMessage(Long chatId) {
        SendMessage message = new SendMessage();

        if (advertSteps == null) return null;

        kg.wedevs.advert_bot.bot.handlers.v2.models.StepModel step = advertSteps.getCurrentStep();

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

        if (advertSteps.getNextStep() != null || advertSteps.getValues() != null) {
            keyboardBuilder = keyboardBuilder.addButton(commandEmptyValue)
                    .appendRow();

        }

        keyboardBuilder = keyboardBuilder.addButton(commandToMainScreen)
                .appendRow();

        message.setReplyMarkup(keyboardBuilder.build());



        return message;
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
        if (telegramUser.getLastState() != BaseStateEnums.SEARCH_ADVERT_SCREEN || !update.hasMessage()) {
            return result;
        }

        Long chatId = update.getMessage().getChatId();

        try {
            if (update.hasMessage()) {
                String messageText = update.getMessage().getText();
                if (messageText.equals(commandToMainScreen)) {
                    result.setType(CommandResultType.STATE_UPDATE);
                    result.setState(BaseStateEnums.MAIN_SCREEN);
                    return result;
                }


                kg.wedevs.advert_bot.bot.handlers.v2.models.StepModel step = advertSteps.getCurrentStep();

                if (step != null) {
                    if (messageText.equals(commandEmptyValue)) {
                        // CONTINUE THIS STEP
//                        advertSteps.setStepValue(step.getCode(), null, step.getTitle());
                    } else if (step.isNumber()) {
                        if (BotHelper.isNumeric(messageText)) {
                            advertSteps.setStepValue(step.getCode(), messageText, step.getTitle());
                        } else {

                            throw new Exception("Format exception");
                        }
                    } else if (step.isPreset() && step.isNeedCheckPreset()) {
                        List<PresetModel> presets = advertSteps.getAllPresets(step);
                        if (presets != null && !presets.isEmpty()) {
                            Optional<PresetModel> finded = presets.stream().filter(v -> v.getValue().equals(messageText)).findFirst();
                            if (finded.isPresent()) {
                                advertSteps.setStepValue(step.getCode(), messageText, step.getTitle());
                            } else {

                                throw new Exception("Format exception");
                            }
                        }
                    } else {
                        advertSteps.setStepValue(step.getCode(), messageText, step.getTitle());
                    }
                    kg.wedevs.advert_bot.bot.handlers.v2.models.StepModel nextStep = advertSteps.getNextStep();
                    if (nextStep != null) {
                        advertSteps.setStep(nextStep.getCode());
                        String jsonText = gson.toJson(advertSteps);
                        telegramUser.setStateJson(jsonText);
                        service.editUser(telegramUser);
                        throw new Exception("Show next step");
                    } else {
                        //Save advert to database


                        saveAdvertToDatabase(advertSteps);

                        result.setType(CommandResultType.STATE_UPDATE);
                        result.setState(BaseStateEnums.SELECT_PERIOD_SCREEN);
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
        if (update.hasMessage() && telegramUser.getLastState() == BaseStateEnums.SEARCH_ADVERT_SCREEN) {
            return getStepMessage(update.getMessage().getChatId());
        }
        return null;
    }
}
