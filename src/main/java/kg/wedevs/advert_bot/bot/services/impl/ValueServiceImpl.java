package kg.wedevs.advert_bot.bot.services.impl;

import kg.wedevs.advert_bot.bot.repository.ValueRepository;
import kg.wedevs.advert_bot.bot.services.ValueService;
import kg.wedevs.advert_bot.models.ValueModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ValueServiceImpl implements ValueService {

    private ValueRepository repository;

    @Override
    public ValueModel saveValue(ValueModel value) {
        return repository.save(value);
    }

    @Override
    public List<ValueModel> saveAllValues(List<ValueModel> values) {
        return repository.saveAll(values);
    }

    @Override
    public void deleteAllValuesByAdvertId(long advertId) {
        repository.deleteByAdvertId(advertId);
    }
}
