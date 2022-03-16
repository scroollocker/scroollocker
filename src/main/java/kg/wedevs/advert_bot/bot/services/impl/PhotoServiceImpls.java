package kg.wedevs.advert_bot.bot.services.impl;

import kg.wedevs.advert_bot.bot.repository.PhotoRepository;
import kg.wedevs.advert_bot.bot.services.PhotoService;
import kg.wedevs.advert_bot.models.PhotoModel;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PhotoServiceImpls implements PhotoService {

    final PhotoRepository photoRepository;

    @Override
    public List<PhotoModel> getPhotosByAdvertsId(Long id) {
        return photoRepository.findAllByAdvertId(id);
    }
}
