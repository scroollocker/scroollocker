package kg.wedevs.advert_bot.bot.handlers.v2;

import kg.wedevs.advert_bot.bot.handlers.enums.CommandResultType;
import kg.wedevs.advert_bot.bot.services.TelegramUserService;
import kg.wedevs.advert_bot.models.TelegramUser;
import kg.wedevs.advert_bot.bot.handlers.enums.BaseStateEnums;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class BotCommandWorker {
    TelegramUser telegramUser;
    TelegramUserService service;
    TelegramLongPollingBot botInstance;

    Logger logger = Logger.getLogger(BotCommandWorker.class.getName());

    public BotCommandWorker(TelegramUser telegramUser, TelegramUserService service, TelegramLongPollingBot botInstance) {
        this.telegramUser = telegramUser;
        this.service = service;
        this.botInstance = botInstance;
    }

    List<WorkerCommand> listOfCommands = new ArrayList<>();

    public void registerWorker(WorkerCommand command) {
        listOfCommands.add(command);
    }

    public BotCommandResult execute(Update update) {
        if (listOfCommands.isEmpty()) {
            logger.log(Level.INFO, "Empty list of commands");
            return null;
        }

        LocalDateTime date = telegramUser.getLastActivity();

        logger.log(Level.INFO, "User start command");
        logger.log(Level.INFO, "Username " + telegramUser.getUserName());
        logger.log(Level.INFO, "State " + telegramUser.getLastState());

        LocalDateTime now = LocalDateTime.now();

        if (date == null || date.isBefore(now)) {
            System.out.println(date);
            now = now.plusMinutes(5);

            logger.log(Level.WARNING, "New Date is " + now);
            logger.log(Level.WARNING, "User last activity wrong");

            telegramUser.setLastState(BaseStateEnums.MAIN_SCREEN);
            telegramUser.setLastActivity(now);
            telegramUser.setStateJson(null);
            telegramUser = service.editUser(telegramUser);
        }

        BotCommandResult result = new BotCommandResult();

        for (int i = 0; i < listOfCommands.size(); i++) {
            WorkerCommand command = listOfCommands.get(i);
            command.setUpdate(update);
            command.setTelegramUser(telegramUser);
            command.setBotService(botInstance);
            BotCommandResult commandResult = command.execute();
            if (commandResult == null) {
                continue;
            }
            if (commandResult.getType() == CommandResultType.COMPLETE) {
                if (!commandResult.getMessages().isEmpty()) {
                    result.getMessages().addAll(commandResult.getMessages());
                }
                break;
            } else if (commandResult.getType() == CommandResultType.RESTART_COMMANDS) {
                if (!commandResult.getMessages().isEmpty()) {
                    result.getMessages().addAll(commandResult.getMessages());
                }
                // Restart command worker
                i = 0;
//                break;
            } else if (commandResult.getType() == CommandResultType.CONTINUE) {
                if (commandResult.getState() != null) {
                    telegramUser.setLastState(commandResult.getState());
                    telegramUser.setLastActivity(LocalDateTime.now().plusMinutes(5));
                    telegramUser = service.editUser(telegramUser);
                    if (!commandResult.getMessages().isEmpty()) {
                        result.getMessages().addAll(commandResult.getMessages());
                    }
                }
                break;
            } else if (commandResult.getType() == CommandResultType.STATE_UPDATE) {
                if (commandResult.getState() != null) {
                    telegramUser.setLastState(commandResult.getState());
                    telegramUser.setLastActivity(LocalDateTime.now().plusMinutes(5));
                    telegramUser.setStateJson(null);
                    telegramUser = service.editUser(telegramUser);

                    if (!commandResult.getMessages().isEmpty()) {
                        result.getMessages().addAll(commandResult.getMessages());
                    }
                }
                break;
            }
        }

        if (telegramUser.getLang() != null) {
            for (WorkerCommand command : listOfCommands) {
                command.setUpdate(update);
                command.setTelegramUser(telegramUser);

                SendMessage defaultMessage = command.sendDefaultMessage(update);
                if (defaultMessage != null) {
                    result.getMessages().add(defaultMessage);
                    break;
                }

            }
        }

        return result;
    }


}
