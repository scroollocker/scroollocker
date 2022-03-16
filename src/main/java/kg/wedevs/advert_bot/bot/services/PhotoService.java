package kg.wedevs.advert_bot.bot.services;

import kg.wedevs.advert_bot.bot.repository.PhotoRepository;
import kg.wedevs.advert_bot.models.PhotoModel;

import java.util.List;

public interface PhotoService {
    List<PhotoModel> getPhotosByAdvertsId(Long id);
}
