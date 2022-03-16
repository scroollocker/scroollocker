package kg.wedevs.advert_bot.bot.services.impl;

import kg.wedevs.advert_bot.bot.repository.TelegramUserRepository;
import kg.wedevs.advert_bot.bot.services.TelegramUserService;
import kg.wedevs.advert_bot.models.TelegramUser;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TelegramUserServiceImpl implements TelegramUserService {
    private TelegramUserRepository repository;

    public TelegramUserServiceImpl(TelegramUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public TelegramUser getByTelegramId(Long telegramId) {
        return repository.findByTelegramId(telegramId).orElse(new TelegramUser());
    }

    @Override
    public TelegramUser getByTelegramUsername(String username) {
        return repository.findByUserName(username).orElse(new TelegramUser());
    }

    @Override
    public TelegramUser addUser(TelegramUser user) {
        return repository.save(user);
    }

    @Override
    public TelegramUser editUser(TelegramUser user) {
        return repository.save(user);
    }
}
