package kg.wedevs.advert_bot.bot.handlers.v2.commandImpl;

import kg.wedevs.advert_bot.bot.handlers.enums.CommandResultType;
import kg.wedevs.advert_bot.bot.handlers.v2.BotCommandResult;
import kg.wedevs.advert_bot.bot.handlers.v2.WorkerCommand;
import kg.wedevs.advert_bot.bot.helpers.BotKeyboardBuilder;
import kg.wedevs.advert_bot.bot.services.TelegramUserService;
import kg.wedevs.advert_bot.models.TelegramUser;
import kg.wedevs.advert_bot.bot.handlers.enums.BaseStateEnums;
import kg.wedevs.advert_bot.models.enums.LangEnum;
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
public class LangCommand implements WorkerCommand {
    Update update;
    TelegramUser telegramUser;
    TelegramUserService service;
    TelegramLongPollingBot botService;

    Logger logger = Logger.getLogger(LangCommand.class.getName());

    public LangCommand(TelegramUserService service) {
        this.service = service;
    }

    String[] availableCommands = new String[] {"RU", "KG", "KZ", "UZ"};

    @Override
    public void setBotService(TelegramLongPollingBot botService) {
        this.botService = botService;
    }

    @Override
    public void setTelegramUser(TelegramUser user) {
        this.telegramUser = user;
    }

    @Override
    public void setUpdate(Update update) {
        this.update = update;
    }

    SendMessage getDefaultMessage(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Выберите язык общения");

        ReplyKeyboardMarkup markup = BotKeyboardBuilder.create()
                .addRowAsArray(availableCommands)
                .setIsClosable(true)
                .setIsFlex(true)
                .build();

        message.setReplyMarkup(markup);
        return  message;
    }

    @Override
    public BotCommandResult execute() {
        BotCommandResult result = new BotCommandResult();
        boolean isLangSet = telegramUser.getLang() != null;
        if (!update.hasMessage()) {
            return result;
        }



        if (!isLangSet) {
            Message message = update.getMessage();
            Long chatId = message.getChatId();

            try {
                if (message.hasText()) {
                    String text = message.getText();
                    LangEnum langEnum = LangEnum.valueOf(text);
                    telegramUser.setLang(langEnum);
                    service.editUser(telegramUser);

                    result.setType(CommandResultType.STATE_UPDATE);
                    result.setState(BaseStateEnums.MAIN_SCREEN);
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setText("Язык выбран");
                    sendMessage.setChatId(chatId.toString());
                    result.setMessages(Collections.singletonList(sendMessage));
                } else {
                    throw new Exception("Empty text");
                }
            } catch (Exception err) {
                logger.log(Level.WARNING, err.getMessage());
                logger.log(Level.WARNING, "Username " + telegramUser.getUserName());
                logger.log(Level.WARNING, "State " + telegramUser.getLastState());
                result.setMessages(Collections.singletonList(getDefaultMessage(chatId)));
                result.setType(CommandResultType.COMPLETE);
            }


        }

        return result;
    }

    @Override
    public SendMessage sendDefaultMessage(Update update) {

        return null;
    }

}
