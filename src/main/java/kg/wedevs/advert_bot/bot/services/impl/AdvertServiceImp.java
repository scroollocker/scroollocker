package kg.wedevs.advert_bot.bot.services.impl;

import kg.wedevs.advert_bot.bot.repository.AdvertRepository;
import kg.wedevs.advert_bot.bot.repository.ValueRepository;
import kg.wedevs.advert_bot.bot.services.AdvertService;
import kg.wedevs.advert_bot.models.AdvertModel;
import kg.wedevs.advert_bot.models.ValueModel;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class AdvertServiceImp implements AdvertService {
    AdvertRepository repository;
    ValueRepository valueRepository;

    public AdvertServiceImp(AdvertRepository repository) {
        this.repository = repository;
    }

    @Override
    public AdvertModel getAdvertById(long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public AdvertModel addAdvert(AdvertModel advert) {
        advert = repository.save(advert);
        return advert;
    }

    @Override
    public AdvertModel saveAdvert(AdvertModel advertModel) {
        return repository.save(advertModel);
    }

    @Override
    public List<AdvertModel> getAdvertsByPlatformCode(String platformCode) {
        return repository.getAdvertsByPlatformCode(platformCode);
    }

    @Override
    public List<AdvertModel> getAdvertsByUserId(Long userId, boolean isSend) {
        return repository.getAdvertsByUserId(userId, isSend);
    }

    @Override
    public void deleteAllUnsaved(Long userId) {
        List<AdvertModel> adverts = getAdvertsByUserId(userId, false);
        if (adverts != null && !adverts.isEmpty()) {
            repository.deleteAll(adverts);
        }

    }

    @Override
    public List<AdvertModel> getAdvertsPagination(Long userId, int limit, int offset) {
        Page<AdvertModel> page = repository.getAdvertsPagination(userId, PageRequest.of(offset, limit));

        return page.toList();
    }

    @Override
    public long advertCount() {
        return repository.advertCount();
    }

    @Override
    public long advertSoldCount() {
        return repository.advertSoldCount();
    }

    @Override
    public long advertCountByPlatform(String code) {
        return repository.advertCountByPlatform(code);
    }

    @Override
    public Set<AdvertModel> getFilteredAdverts(List<ValueModel> values, String platformCode) {

        return repository.filterAdverts(values, platformCode);
    }

}
