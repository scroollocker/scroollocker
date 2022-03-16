package kg.wedevs.advert_bot.bot.repository;

import kg.wedevs.advert_bot.models.AdvertSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdvertSettingRepository extends JpaRepository<AdvertSetting, Long> {


}
