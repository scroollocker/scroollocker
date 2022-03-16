package kg.wedevs.advert_bot.bot.helpers;

import kg.wedevs.advert_bot.models.TelegramUser;

import java.util.logging.Level;
import java.util.logging.Logger;

public class BotLogger {
    public static void logWarning(Logger logger, TelegramUser user, String message) {
        logger.warning(message);
        logger.warning("Username " + user.getUserName());
        logger.warning("State " + user.getLastState());
    }

    public static void logInfo(Logger logger, TelegramUser user, String message) {
        logger.info(message);
        logger.info("Username " + user.getUserName());
        logger.info("State " + user.getLastState());
    }
}
