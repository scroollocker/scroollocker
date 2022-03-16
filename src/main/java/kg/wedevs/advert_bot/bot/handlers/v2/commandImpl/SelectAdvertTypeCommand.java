package kg.wedevs.advert_bot.bot.handlers.v2.commandImpl;

import kg.wedevs.advert_bot.bot.handlers.enums.CommandResultType;
import kg.wedevs.advert_bot.bot.handlers.v2.BotCommandResult;
import kg.wedevs.advert_bot.bot.handlers.v2.WorkerCommand;
import kg.wedevs.advert_bot.bot.helpers.BotKeyboardBuilder;
import kg.wedevs.advert_bot.bot.services.TelegramUserService;
import kg.wedevs.advert_bot.models.TelegramUser;
import kg.wedevs.advert_bot.bot.handlers.enums.BaseStateEnums;
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
public class SelectAdvertTypeCommand implements WorkerCommand {

    TelegramUser telegramUser;
    Update update;
    TelegramLongPollingBot botService;

    Logger logger = Logger.getLogger(SelectAdvertTypeCommand.class.getName());

    private static final String commandCreateAdvert = "Создать заявку";
    private static final String commandSearchAdvert = "Поиск заявки";
    private static final String commandToMain = "На главную";

    @Override
    public void setBotService(TelegramLongPollingBot botService) {
        this.botService = botService;
    }

    SendMessage getDefaultMessage(Long chatId) {
        SendMessage message = new SendMessage();
        message.setText("Вы можете искать объявления или создать свое новое объявление. Ваше действие?");
        message.setChatId(chatId.toString());
        ReplyKeyboardMarkup markup = BotKeyboardBuilder.create()
                .setIsFlex(true)
                .setIsClosable(true)
                .addButton(commandCreateAdvert)
                .addButton(commandSearchAdvert)
                .appendRow()
                .addButton(commandToMain)
                .appendRow()
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

        if (telegramUser.getLastState() != BaseStateEnums.SELECT_ACTION_ADVERT_SCREEN || !update.hasMessage()) {
            return result;
        }

        Long chatId = update.getMessage().getChatId();

        try {
            if (update.hasMessage()) {
                Message message = update.getMessage();
                String textMessage = message.getText();
                switch (textMessage) {
                    case commandCreateAdvert: {
                        result.setType(CommandResultType.STATE_UPDATE);
                        result.setState(BaseStateEnums.CREATE_ADVERT_SCREEN);
                        break;
                    }
                    case commandSearchAdvert: {
                        result.setType(CommandResultType.STATE_UPDATE);
                        result.setState(BaseStateEnums.SEARCH_ADVERT_SCREEN);
                        break;
                    }
                    case commandToMain: {
                        result.setType(CommandResultType.STATE_UPDATE);
                        result.setState(BaseStateEnums.MAIN_SCREEN);
                        break;
                    }
                    default: {
                        throw new Exception("Command not found");
                    }

                }
            }
            else {
                throw new Exception("Empty message");
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
        if (update.hasMessage() && telegramUser.getLastState() == BaseStateEnums.SELECT_ACTION_ADVERT_SCREEN) {
            return getDefaultMessage(update.getMessage().getChatId());
        }
        return null;
    }
}
