package kg.wedevs.advert_bot.bot.handlers.v2.commandImpl;

import kg.wedevs.advert_bot.bot.handlers.enums.CommandResultType;
import kg.wedevs.advert_bot.bot.handlers.v2.BotCommandResult;
import kg.wedevs.advert_bot.bot.handlers.v2.WorkerCommand;
import kg.wedevs.advert_bot.bot.helpers.BotKeyboardBuilder;
import kg.wedevs.advert_bot.models.TelegramUser;
import kg.wedevs.advert_bot.bot.handlers.enums.BaseStateEnums;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


@Component
public class MainCommand implements WorkerCommand {
    TelegramUser telegramUser;
    Update update;
    TelegramLongPollingBot botService;

    Logger logger = Logger.getLogger(MainCommand.class.getName());

    //    private static final String commandCreateAdvert = "Оставить заявку";
    private static final String commandMyAdverts = "Мои заявки";
    private static final String commandStatistic = "Статистика";
    private static final String commandShare = "Поделится";
    private static final String commandCreateAdvertNew = "Создать заявку";
    private static final String commandSearchAdvertNew = "Поиск заявки";
    private static final String commandInstruction = "Инструкция";

    @Override
    public void setBotService(TelegramLongPollingBot botService) {
        this.botService = botService;
    }

    SendMessage getDefaultMessage(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Главное меню, выберите действие");

        ReplyKeyboardMarkup markup = BotKeyboardBuilder.create()
                .addButton(commandCreateAdvertNew)
                .addButton(commandSearchAdvertNew)
                .appendRow()
                .addButton(commandMyAdverts)
                .appendRow()
                .addButton(commandShare)
                .addButton(commandInstruction)
                .appendRow()
                .setIsFlex(true)
                .setIsClosable(true)
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

        if (telegramUser.getLastState() != BaseStateEnums.MAIN_SCREEN || !update.hasMessage()) {
            return result;
        }

        Long chatId = update.getMessage().getChatId();

        try {
            if (!update.hasMessage()) {
                throw new Exception("Empty message");
            }
            String message = update.getMessage().getText();
            switch (message) {

                case commandCreateAdvertNew: {
                    result.setType(CommandResultType.STATE_UPDATE);
                    result.setState(BaseStateEnums.CREATE_ADVERT_SCREEN);
                    break;
                }
                case commandSearchAdvertNew: {
                    result.setType(CommandResultType.STATE_UPDATE);
                    result.setState(BaseStateEnums.SEARCH_ADVERT_SCREEN);
                    break;
                }
                case commandMyAdverts: {
                    result.setType(CommandResultType.STATE_UPDATE);
                    result.setState(BaseStateEnums.MY_ADVERTS_SCREEN);
                    break;
                }
                case commandInstruction: {
                    result.setType(CommandResultType.COMPLETE);
                    SendMessage msg1 = new SendMessage();
                    msg1.setChatId(telegramUser.getTelegramId().toString());
                    msg1.setText("Здесь будет инструкция по пользованию ботом");


                    List<SendMessage> listOfMessages = new ArrayList<>();
                    listOfMessages.add(msg1);

                    result.setMessages(listOfMessages);
                    break;
                }
                case commandShare: {
                    result.setType(CommandResultType.COMPLETE);
                    SendMessage msg1 = new SendMessage();
                    SendMessage msg2 = new SendMessage();
                    msg1.setChatId(telegramUser.getTelegramId().toString());
                    msg1.setText("Чтобы ваши друзья могли подписаться на бота, " +
                            "поделитесь данной ссылкой с друзьям");

                    msg2.setChatId(telegramUser.getTelegramId().toString());
                    msg2.setText("https://t.me/" + botService.getMe().getUserName());

                    List<SendMessage> listOfMessages = new ArrayList<>();
                    listOfMessages.add(msg1);
                    listOfMessages.add(msg2);

                    result.setMessages(listOfMessages);
                    break;
                }
                default: {
                    throw new Exception("Command not found");
                }
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
        if (update.hasMessage() && telegramUser.getLastState() == BaseStateEnums.MAIN_SCREEN) {
            return getDefaultMessage(update.getMessage().getChatId());
        }

        return null;
    }
}
