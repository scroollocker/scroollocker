package kg.wedevs.advert_bot.bot.services;

import kg.wedevs.advert_bot.models.AdvertModel;
import kg.wedevs.advert_bot.models.ValueModel;

import java.util.List;
import java.util.Set;

public interface AdvertService {
    AdvertModel getAdvertById(long id);
    AdvertModel addAdvert(AdvertModel advert);
    AdvertModel saveAdvert(AdvertModel advertModel);
    List<AdvertModel> getAdvertsByPlatformCode(String platformCode);
    List<AdvertModel> getAdvertsByUserId(Long userId, boolean isSend);
    void deleteAllUnsaved(Long userId);
    List<AdvertModel> getAdvertsPagination(Long userId, int limit, int offset);

    long advertCount();
    long advertSoldCount();
    long advertCountByPlatform(String code);
    Set<AdvertModel> getFilteredAdverts(List<ValueModel> values, String platformCode);
}