package kg.wedevs.advert_bot.bot.services;

import kg.wedevs.advert_bot.models.TelegramUser;


public interface TelegramUserService {

    TelegramUser getByTelegramId(Long telegramId);

    TelegramUser getByTelegramUsername(String username);

    TelegramUser addUser(TelegramUser user);

    TelegramUser editUser(TelegramUser user);
}
