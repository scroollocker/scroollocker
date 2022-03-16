package kg.wedevs.advert_bot.bot.handlers.v2;

import kg.wedevs.advert_bot.models.TelegramUser;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface WorkerCommand {

    void setTelegramUser(TelegramUser user);
    void setUpdate(Update update);
    void setBotService(TelegramLongPollingBot botService);
    BotCommandResult execute();

    SendMessage sendDefaultMessage(Update update);
}
