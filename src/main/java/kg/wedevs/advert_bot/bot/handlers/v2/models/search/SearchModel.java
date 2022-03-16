package kg.wedevs.advert_bot.bot.handlers.v2.models.search;

import kg.wedevs.advert_bot.bot.handlers.enums.SearchEnum;
import kg.wedevs.advert_bot.bot.handlers.v2.models.ValueModel;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SearchModel implements Serializable {
    SearchEnum period;
    int page = 0;
    List<ValueModel> valueList;
}
