package kg.wedevs.advert_bot.bot.services;

import kg.wedevs.advert_bot.models.Platform;

public interface PlatformService {
    Platform getPlatformByCode(String code);
}
