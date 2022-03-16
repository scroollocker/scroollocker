package kg.wedevs.advert_bot.bot.services.impl;

import kg.wedevs.advert_bot.bot.repository.PlatformRepository;
import kg.wedevs.advert_bot.bot.services.PlatformService;
import kg.wedevs.advert_bot.models.Platform;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PlatformServiceImpl implements PlatformService {
    PlatformRepository platformRepository;

    public PlatformServiceImpl(PlatformRepository platformRepository) {
        this.platformRepository = platformRepository;
    }

    @Override
    public Platform getPlatformByCode(String code) {
//        List<Platform> platforms = platformRepository.getPlatformsByCode(code);
//        System.out.println(platforms.size());
//        return platforms.stream().findFirst().get();
        return platformRepository.findByCodeContaining(code);
    }
}
