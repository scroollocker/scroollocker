package kg.wedevs.advert_bot.bot;

import kg.wedevs.advert_bot.bot.handlers.v2.BotCommandResult;
import kg.wedevs.advert_bot.bot.handlers.v2.BotCommandWorker;
import kg.wedevs.advert_bot.bot.handlers.v2.commandImpl.*;
import kg.wedevs.advert_bot.bot.services.TelegramUserService;
import kg.wedevs.advert_bot.models.TelegramUser;
import kg.wedevs.advert_bot.bot.handlers.enums.BaseStateEnums;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.List;

@Component
public class AdvertBot extends TelegramLongPollingBot {

    TelegramUserService telegramUserService;
    TelegramLongPollingBot botCommandInstance;

    // LIST OF COMMaNDS
    LangCommand langCommand;
    CreateAdvertCommand createAdvertCommand;
    ConfirmAdvertCommand confirmAdvertCommand;
    MainCommand mainCommand;
    MyAdvertsList myAdvertsListCommand;
    InlineCommandWorker inlineCommandWorker;
    StatisticCommand statisticCommand;
    SearchAdverts searchAdvertsCommand;
    SearchPeriodCommand searchPeriodCommand;
    SelectAdvertTypeCommand selectAdvertTypeCommand;
    SelectBasePhotoCommand selectBasePhotoCommand;
    UploadPhotoCommand uploadPhotoCommand;

    public AdvertBot(TelegramUserService telegramUserService, LangCommand langCommand, CreateAdvertCommand createAdvertCommand,
                     ConfirmAdvertCommand confirmAdvertCommand, MyAdvertsList myAdvertsListCommand,
                     InlineCommandWorker inlineCommandWorker, StatisticCommand statisticCommand,
                     SearchAdverts searchAdvertsCommand, SearchPeriodCommand searchPeriodCommand,
                     SelectAdvertTypeCommand selectAdvertTypeCommand, UploadPhotoCommand uploadPhotoCommand,
                     MainCommand mainCommand, SelectBasePhotoCommand selectBasePhotoCommand) {
        super();
        this.langCommand = langCommand;
        this.telegramUserService = telegramUserService;
        this.createAdvertCommand = createAdvertCommand;
        this.confirmAdvertCommand = confirmAdvertCommand;
        this.botCommandInstance = this;
        this.myAdvertsListCommand = myAdvertsListCommand;
        this.mainCommand = mainCommand;
        this.inlineCommandWorker = inlineCommandWorker;
        this.statisticCommand = statisticCommand;
        this.searchAdvertsCommand = searchAdvertsCommand;
        this.searchPeriodCommand = searchPeriodCommand;
        this.selectAdvertTypeCommand =selectAdvertTypeCommand;
        this.selectBasePhotoCommand = selectBasePhotoCommand;
        this.uploadPhotoCommand = uploadPhotoCommand;
    }

    @Value("${bot.name}")
    private String botName;

    @Value("${bot.token}")
    private String botToken;

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {

            User user;

            if (update.hasMessage()) {
                user = update.getMessage().getFrom();
            } else if (update.hasCallbackQuery()) {
                user = update.getCallbackQuery().getFrom();
            } else {
                return;
            }

            TelegramUser telegramUser = telegramUserService.getByTelegramId(user.getId());
            if (telegramUser.getTelegramId() == null) {
                telegramUser = new TelegramUser();
                telegramUser.setLastState(BaseStateEnums.MAIN_SCREEN);
                telegramUser.setUserName(user.getUserName());
                telegramUser.setTelegramId(user.getId());
                telegramUser = telegramUserService.addUser(telegramUser);
            }
            BotCommandWorker commandWorker = new BotCommandWorker(telegramUser, telegramUserService, botCommandInstance);

            commandWorker.registerWorker(langCommand);
            commandWorker.registerWorker(mainCommand);
            commandWorker.registerWorker(statisticCommand);
            commandWorker.registerWorker(searchPeriodCommand);
            commandWorker.registerWorker(selectAdvertTypeCommand);
            commandWorker.registerWorker(createAdvertCommand);
            commandWorker.registerWorker(selectBasePhotoCommand);
            commandWorker.registerWorker(uploadPhotoCommand);
            commandWorker.registerWorker(confirmAdvertCommand);
            commandWorker.registerWorker(myAdvertsListCommand);
            commandWorker.registerWorker(inlineCommandWorker);
            commandWorker.registerWorker(searchAdvertsCommand);

            BotCommandResult result = commandWorker.execute(update);

            List<SendMessage> messages = result.getMessages();

            if (!messages.isEmpty()) {
                for (SendMessage m : messages) {
                    execute(m);
                }
            }

        } catch (Exception err) {
            err.printStackTrace();
        }


    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        super.onUpdatesReceived(updates);
    }
}
