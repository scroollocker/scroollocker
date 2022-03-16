package kg.wedevs.advert_bot.bot.repository;

import kg.wedevs.advert_bot.models.ValueModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ValueRepository extends JpaRepository<ValueModel, Long> {
    @Modifying
    @Query("delete from ValueModel v where v.advert.id = ?1")
    void deleteByAdvertId(long id);
}
