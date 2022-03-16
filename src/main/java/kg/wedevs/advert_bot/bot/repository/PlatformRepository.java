package kg.wedevs.advert_bot.bot.repository;

import kg.wedevs.advert_bot.models.Platform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlatformRepository extends JpaRepository<Platform, Long> {
//    @Query(value = "select p from Platform p join fetch p.stepFields where p.code like %:code%")
//    List<Platform> getPlatformsByCode(@Param("code") String code);

    @Query("select p from Platform p join fetch p.stepFields where p.code like %?1%")
    Platform findByCodeContaining(String code);
}
