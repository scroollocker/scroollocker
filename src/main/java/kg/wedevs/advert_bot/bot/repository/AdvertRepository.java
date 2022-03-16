package kg.wedevs.advert_bot.bot.repository;

import kg.wedevs.advert_bot.models.AdvertModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdvertRepository extends JpaRepository<AdvertModel, Long>, AdvFilterRepository {

    @Query(value = "select a from AdvertModel a where a.platformCode = ?1")
    List<AdvertModel> getAdvertsByPlatformCode(String platformCode);

    @Query(value = "select a from AdvertModel a join fetch a.values where a.user.id = ?1 and a.isSend = ?2")
    List<AdvertModel> getAdvertsByUserId(Long userId, boolean isSend);

    @Modifying(clearAutomatically = true)
    @Query( value = "delete from AdvertModel a where a.isSend = false and a.user.id = ?1")
    void deleteAllUnsaved(Long id);

    @Query(value = "select a from AdvertModel a where a.user.id = ?1 and a.isSold = false")
    Page<AdvertModel> getAdvertsPagination(Long userId, Pageable pageable);

    @Query("select count(a) from AdvertModel a")
    long advertCount();

    @Query("select count(a) from AdvertModel a where a.isSold = true")
    long advertSoldCount();

    @Query("select count(a) from AdvertModel a where a.platformCode = ?1")
    long advertCountByPlatform(String code);
}
