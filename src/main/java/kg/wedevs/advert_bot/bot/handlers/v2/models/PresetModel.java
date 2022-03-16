package kg.wedevs.advert_bot.bot.handlers.v2.models;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PresetModel {
    String parentCode;
    String parentValue;
    String value;

    public PresetModel(String parentCode, String parentValue, String value) {
        this.parentCode = parentCode;
        this.parentValue = parentValue;
        this.value = value;
    }
}
