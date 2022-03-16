package kg.wedevs.advert_bot.bot.repository.impl;


import kg.wedevs.advert_bot.bot.repository.AdvFilterRepository;
import kg.wedevs.advert_bot.models.AdvertModel;
import kg.wedevs.advert_bot.models.Platform;
import kg.wedevs.advert_bot.models.ValueModel;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AdvFilterRepositoryImpl implements AdvFilterRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public Set<AdvertModel> filterAdverts(List<ValueModel> values, String platformCode) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<AdvertModel> query = cb.createQuery(AdvertModel.class);
        Root<AdvertModel> advertRoot = query.from(AdvertModel.class);
        Join<AdvertModel, Platform> platformJoin = advertRoot.join("platform");


        Path<String> platformPath = platformJoin.get("code");

        List<Predicate> predicates = new ArrayList<>();
        for (ValueModel value : values) {
            Subquery<ValueModel> subquery = query.subquery(ValueModel.class);
            Root<ValueModel> valuesRoot = subquery.from(ValueModel.class);
            Path<String> valuePathP = valuesRoot.get("value");
            Path<String> codePathP = valuesRoot.get("fieldCode");

            subquery.select(valuesRoot.get("advert"))
                    .distinct(true)
                    .where(cb.like(valuePathP, value.getValue()), cb.like(codePathP, value.getFieldCode()));

            predicates.add(cb.isTrue(advertRoot.get("isSend")));
            predicates.add(cb.isFalse(advertRoot.get("isSold")));
            predicates.add(cb.in(advertRoot.get("id")).value(subquery));
        }

        query.select(advertRoot).distinct(true).where(cb.and(cb.equal(platformPath, platformCode), cb.and(predicates.toArray(new Predicate[predicates.size()]))));

        List<AdvertModel> list = entityManager.createQuery(query)
                .getResultList();

        //collect distinct users as list
        return list.stream()
                //operators to remove duplicates based on person name
                .collect(Collectors.groupingBy(AdvertModel::getId))
                .values()
                .stream()
                //cut short the groups to size of 1
                .flatMap(group -> group.stream().limit(1)).collect(Collectors.toSet());
    }
}
