package kg.wedevs.advert_bot.bot.handlers.v2.commandImpl;

import com.google.gson.Gson;
import kg.wedevs.advert_bot.bot.handlers.enums.CommandResultType;
import kg.wedevs.advert_bot.bot.handlers.enums.InlineStateEnum;
import kg.wedevs.advert_bot.bot.handlers.v2.BotCommandResult;
import kg.wedevs.advert_bot.bot.handlers.v2.WorkerCommand;
import kg.wedevs.advert_bot.bot.handlers.v2.models.InlineCommandState;
import kg.wedevs.advert_bot.bot.handlers.v2.models.search.SearchModel;
import kg.wedevs.advert_bot.bot.helpers.BotKeyboardBuilder;
import kg.wedevs.advert_bot.bot.services.AdvertService;
import kg.wedevs.advert_bot.bot.services.PhotoService;
import kg.wedevs.advert_bot.bot.services.PlatformService;
import kg.wedevs.advert_bot.bot.services.TelegramUserService;
import kg.wedevs.advert_bot.models.*;
import kg.wedevs.advert_bot.bot.handlers.enums.BaseStateEnums;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component
public class SearchPeriodCommand implements WorkerCommand {
    TelegramUser telegramUser;
    Update update;
    TelegramLongPollingBot botService;
    AdvertService advertService;
    PhotoService photoService;
    PlatformService platformService;
    TelegramUserService userService;
    Set<AdvertSearchRequest> requestList;

    @Value("${bot.platformCode}")
    private String botPlatformCode;

    SearchModel searchModel;

    Gson gson = new Gson();

    Logger logger = Logger.getLogger(SearchPeriodCommand.class.getName());

    final int elementsPerPage = 5;

//    private static final String commandPeriod3Day = "3 дня";
//    private static final String commandPeriodWeek = "Неделя";
//    private static final String commandPeriodMonth = "Месяц";

    private static final String commandToMainPage = "На главную";
    private static final String commandLoadMore = "Подгрузить";

    public SearchPeriodCommand(AdvertService advertService, PhotoService photoService,
                               PlatformService platformService, TelegramUserService userService) {
        this.advertService = advertService;
        this.photoService = photoService;
        this.platformService = platformService;
        this.userService = userService;
    }

    @Override
    public void setBotService(TelegramLongPollingBot botService) {
        this.botService = botService;
    }

    SendMessage getDefaultMessage(Long chatId) {
        SendMessage message = new SendMessage();

        message.setText("Выполните действие ниже");
        message.setChatId(chatId.toString());

        ReplyKeyboardMarkup markup = BotKeyboardBuilder.create()
                .setIsClosable(true)
                .setIsFlex(true)
//                .addButton(commandPeriod3Day)
//                .addButton(commandPeriodWeek)
//                .addButton(commandPeriodMonth)
                .addButton(commandLoadMore)
                .appendRow()
                .addButton(commandToMainPage)
                .appendRow()
                .build();

        message.setReplyMarkup(markup);

        return message;
    }

    @Override
    public void setTelegramUser(TelegramUser user) {
        this.telegramUser = user;
        if (telegramUser.getLastState() == BaseStateEnums.SELECT_PERIOD_SCREEN) {
            String jsonData = user.getStateJson();
            if (jsonData == null) {
                requestList = user.getSearches();
                if (requestList != null && !requestList.isEmpty()) {
                    AdvertSearchRequest lastRequest = new ArrayList<>(requestList).get(requestList.size()-1);
                    jsonData = lastRequest.getRequestData();
                }
            }

            if (jsonData != null) {
                searchModel = gson.fromJson(jsonData, SearchModel.class);
            }


        }
    }

    @Override
    public void setUpdate(Update update) {
        this.update = update;
    }

    @Override
    public BotCommandResult execute() {
        BotCommandResult result = new BotCommandResult();

        if (telegramUser.getLastState() != BaseStateEnums.SELECT_PERIOD_SCREEN || !update.hasMessage()) {
            return result;
        }



        Long chatId = update.getMessage().getChatId();

        try {
            if (update.hasMessage()) {
                String textMessage = update.getMessage().getText();
                switch (textMessage) {
//                    case commandPeriod3Day: {
//                        break;
//                    }
//                    case commandPeriodWeek: {
//                        break;
//                    }
//                    case commandPeriodMonth: {
//                        break;
//                    }
                    case commandToMainPage: {
                        result.setType(CommandResultType.STATE_UPDATE);
                        result.setState(BaseStateEnums.MAIN_SCREEN);
                        break;
                    }
                    case commandLoadMore: {
                        if (requestList.isEmpty()) {
                            return result;
                        }

                        if (searchModel == null) {
                            return result;
                        }

                        Platform platform = platformService.getPlatformByCode(botPlatformCode);

                        if (platform == null) {
                            return result;
                        }
                        Set<AdvertModel> adverts = advertService.getFilteredAdverts(searchModel.getValueList().stream().map(e -> {
                            ValueModel model = new ValueModel();
                            model.setFieldCode(e.getCode());
                            model.setValue(e.getValue());
                            return model;
                        }).collect(Collectors.toList()), botPlatformCode);

                        int page = searchModel.getPage();
                        int offset = page * elementsPerPage;

                        if (adverts.size() <= offset) {
                            SendMessage resultMessage = new SendMessage();
                            resultMessage.setChatId(telegramUser.getTelegramId().toString());
                            resultMessage.setText("Нет записей для отображения");
                            botService.execute(resultMessage);
                        }
                        else {
                            int limit = offset + elementsPerPage;
                            if (limit > adverts.size()) {
                                limit = adverts.size();
                            }
                            List<AdvertModel> subAdverts = new ArrayList<>(adverts).subList(offset, limit);
                            if (!subAdverts.isEmpty()) {
                                for (AdvertModel advert : subAdverts) {
                                    ForwardMessage forwardMessage = new ForwardMessage();
                                    forwardMessage.setChatId(telegramUser.getTelegramId().toString());
                                    forwardMessage.setMessageId(Long.valueOf(advert.getTelegramMessageId()).intValue());
                                    forwardMessage.setFromChatId("@"+platform.getChannelId());

                                    if (platform.isRequiredPhoto()) {
                                        sendPrimaryPhoto(advert.getId(), telegramUser.getTelegramId().toString());
                                    }

                                    botService.execute(forwardMessage);

                                    SendMessage message = new SendMessage();
                                    message.setChatId(telegramUser.getTelegramId().toString());
                                    message.setText("Посмотреть детали объявления");

                                    List<InlineKeyboardButton> rowKeyboards = new ArrayList<>();

                                    InlineCommandState inlineCommandState = new InlineCommandState();
                                    inlineCommandState.setState(InlineStateEnum.SHOW_ADVERT_TO_USER);
                                    inlineCommandState.setMessageId(advert.getId());

                                    rowKeyboards.add(InlineKeyboardButton.builder().text("Посмотреть объявление").callbackData(gson.toJson(inlineCommandState)).build());

                                    message.setReplyMarkup(InlineKeyboardMarkup.builder().keyboardRow(rowKeyboards).build());
                                    botService.execute(message);
                                }
                                searchModel.setPage(searchModel.getPage() + 1);
                                String jsonData = gson.toJson(searchModel);
                                telegramUser.setStateJson(jsonData);
                                userService.editUser(telegramUser);
                                result.setType(CommandResultType.COMPLETE);
                            }
                        }


                        break;
                    }

                    default: {
                        throw  new Exception("Command not found");
                    }
                }
            }
            else {
                throw new Exception("No message");
            }
        }
        catch (Exception err) {
            logger.log(Level.WARNING, err.getMessage());
            logger.log(Level.WARNING, "Username " + telegramUser.getUserName());
            logger.log(Level.WARNING, "State " + telegramUser.getLastState());
            result.setMessages(Collections.singletonList(getDefaultMessage(chatId)));
        }

        return result;
    }

    private void sendPrimaryPhoto(Long advertId, String chatId) throws Exception {
        List<PhotoModel> photoModels = photoService.getPhotosByAdvertsId(advertId);
        if (photoModels == null || photoModels.isEmpty()) {
            throw new Exception("Advert with id: " + advertId + " photos not found");
        }

        String fileId = photoModels.stream()
                .filter(PhotoModel::isPrimary)
                .findAny()
                .orElseThrow(() -> new Exception("Advert with id: " + advertId + " no primary photo")).getFileId();

        SendPhoto sendPhoto = new SendPhoto(chatId, new InputFile(fileId));
        botService.execute(sendPhoto);
    }

    @Override
    public SendMessage sendDefaultMessage(Update update) {
        if (update.hasMessage() && telegramUser.getLastState() == BaseStateEnums.SELECT_PERIOD_SCREEN) {
            return getDefaultMessage(update.getMessage().getChatId());
        }
        return null;
    }
}
