package kg.wedevs.advert_bot.bot.handlers.v2.models;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ValueModel {
    String code;
    String value;
    String title;

    public ValueModel(String code, String value, String title) {
        this.code = code;
        this.value = value;
        this.title = title;
    }
}
