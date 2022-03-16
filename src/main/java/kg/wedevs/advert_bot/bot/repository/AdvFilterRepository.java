package kg.wedevs.advert_bot.bot.repository;


import kg.wedevs.advert_bot.models.AdvertModel;
import kg.wedevs.advert_bot.models.ValueModel;

import java.util.List;
import java.util.Set;

public interface AdvFilterRepository {
    Set<AdvertModel> filterAdverts(List<ValueModel> values, String platformCode);
}
