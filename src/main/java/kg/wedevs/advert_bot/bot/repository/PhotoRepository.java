package kg.wedevs.advert_bot.bot.repository;

import kg.wedevs.advert_bot.models.PhotoModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PhotoRepository extends JpaRepository<PhotoModel, String> {
    List<PhotoModel> findAllByAdvertId(Long id);
}
