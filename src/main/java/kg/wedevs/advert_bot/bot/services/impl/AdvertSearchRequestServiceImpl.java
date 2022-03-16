package kg.wedevs.advert_bot.bot.services.impl;

import kg.wedevs.advert_bot.bot.repository.AdvertSearchRequestRepository;
import kg.wedevs.advert_bot.bot.services.AdvertSearchRequestService;
import kg.wedevs.advert_bot.models.AdvertSearchRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AdvertSearchRequestServiceImpl implements AdvertSearchRequestService {
    AdvertSearchRequestRepository repository;

    public AdvertSearchRequestServiceImpl(AdvertSearchRequestRepository repository) {
        this.repository = repository;
    }

    @Override
    public AdvertSearchRequest saveSearch(AdvertSearchRequest search) {
        return repository.save(search);
    }
}
