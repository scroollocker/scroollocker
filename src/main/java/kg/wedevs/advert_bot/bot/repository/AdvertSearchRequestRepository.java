package kg.wedevs.advert_bot.bot.repository;

import kg.wedevs.advert_bot.models.AdvertSearchRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdvertSearchRequestRepository extends JpaRepository<AdvertSearchRequest, Long> {

}
