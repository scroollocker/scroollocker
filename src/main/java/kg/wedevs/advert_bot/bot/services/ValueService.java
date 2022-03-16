package kg.wedevs.advert_bot.bot.services;

import kg.wedevs.advert_bot.models.ValueModel;

import java.util.List;

public interface ValueService {
    ValueModel saveValue(ValueModel value);
    List<ValueModel> saveAllValues(List<ValueModel> values);
    void deleteAllValuesByAdvertId(long advertId);
}
