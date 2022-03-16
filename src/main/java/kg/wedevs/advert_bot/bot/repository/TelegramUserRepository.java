package kg.wedevs.advert_bot.bot.repository;

import kg.wedevs.advert_bot.models.TelegramUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TelegramUserRepository extends JpaRepository<TelegramUser, Long> {
//    @Query("select u from TelegramUser u join fetch u.searches where u.telegramId = ?1")
    Optional<TelegramUser> findByTelegramId(Long telegramId);

//    @Query("select u from TelegramUser u where u.userName = ?1")
    Optional<TelegramUser> findByUserName(String username);
}
