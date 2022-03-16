package kg.wedevs.advert_bot.bot.handlers.v2.commandImpl;

import kg.wedevs.advert_bot.bot.handlers.enums.CommandResultType;
import kg.wedevs.advert_bot.bot.handlers.v2.BotCommandResult;
import kg.wedevs.advert_bot.bot.handlers.v2.WorkerCommand;
import kg.wedevs.advert_bot.bot.helpers.BotKeyboardBuilder;
import kg.wedevs.advert_bot.bot.services.AdvertService;
import kg.wedevs.advert_bot.bot.services.AdvertSettingService;
import kg.wedevs.advert_bot.bot.services.PlatformService;
import kg.wedevs.advert_bot.models.AdvertSetting;
import kg.wedevs.advert_bot.models.TelegramUser;
import kg.wedevs.advert_bot.bot.handlers.enums.BaseStateEnums;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class StatisticCommand implements WorkerCommand {
    TelegramUser telegramUser;
    Update update;
    TelegramLongPollingBot botService;
    AdvertService advertService;
    PlatformService platformService;
    AdvertSettingService settingService;

    @Value("${bot.platformCode}")
    private String botPlatformCode;

    Logger logger = Logger.getLogger(StatisticCommand.class.getName());

    private static final String commandToMainScreen = "На главную";

    public StatisticCommand(AdvertService advertService, PlatformService platformService, AdvertSettingService settingService) {
        this.advertService = advertService;
        this.platformService = platformService;
        this.settingService = settingService;
    }

    @Override
    public void setBotService(TelegramLongPollingBot botService) {
        this.botService = botService;
    }

    SendMessage getDefaultMessage(Long chatId) {
        SendMessage message = new SendMessage();

        AdvertSetting setting = settingService.getSettings();
        if (setting == null) setting = new AdvertSetting();

        long advertCount = advertService.advertCount() + setting.getCheatedAdvertCount();
        long soldCount = advertService.advertSoldCount() + setting.getCheatedSoldCount();

        long platformCount = advertService.advertCountByPlatform(botPlatformCode) + setting.getCheatedAdvertCount();
        message.setText("Статистика по боту\n\n" +
                "Объявлений всего:\n" +
                "Объявлений всего: " + advertCount + "\n" +
                "Количество проданных: " + soldCount + "\n" +
                "Количество объявлений на площадке: " + platformCount );

        message.setChatId(chatId.toString());
        ReplyKeyboardMarkup markup = BotKeyboardBuilder.create()
                .addButton(commandToMainScreen)
                .appendRow()
                .setIsClosable(true)
                .setIsFlex(true)
                .build();
        message.setReplyMarkup(markup);

        return message;
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
    public BotCommandResult execute() {
        BotCommandResult result = new BotCommandResult();
        if (telegramUser.getLastState() != BaseStateEnums.STATISTIC_SCREEN || !update.hasMessage()) {
            return result;
        }

        Long chatId = update.getMessage().getChatId();

        Message message = update.getMessage();

        try {
            if (message.hasText()) {
                String messageText = message.getText();
                switch (messageText) {
                    case commandToMainScreen: {
                        result.setState(BaseStateEnums.MAIN_SCREEN);
                        result.setType(CommandResultType.STATE_UPDATE);
                        break;
                    }
                    default: {
                        throw new Exception("Command not found");
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

    @Override
    public SendMessage sendDefaultMessage(Update update) {
        if (update.hasMessage() && telegramUser.getLastState() == BaseStateEnums.STATISTIC_SCREEN) {
            return getDefaultMessage(update.getMessage().getChatId());
        }
        return null;
    }
}
