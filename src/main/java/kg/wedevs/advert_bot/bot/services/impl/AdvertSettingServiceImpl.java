package kg.wedevs.advert_bot.bot.services.impl;

import kg.wedevs.advert_bot.bot.repository.AdvertSettingRepository;
import kg.wedevs.advert_bot.bot.services.AdvertSettingService;
import kg.wedevs.advert_bot.models.AdvertSetting;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdvertSettingServiceImpl implements AdvertSettingService {
    private AdvertSettingRepository repository;

    public AdvertSettingServiceImpl(AdvertSettingRepository repository) {
        this.repository = repository;
    }

    @Override
    public AdvertSetting getSettings() {
        return repository.findAll().stream().findFirst().orElse(null);
    }
}
