package kg.wedevs.advert_bot.bot.handlers.v2.models;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StepModel {
    String code;
    String title;
    boolean isRequired = false;
    boolean isNumber = false;
    boolean isPreset = false;
    boolean needCheckPreset = false;
    List<PresetModel> presets;

    public StepModel(String code, boolean isRequired, boolean isNumber, List<PresetModel> presets, String title, boolean needCheckPreset) {
        this.code = code;
        this.isRequired = isRequired;
        this.isNumber = isNumber;
        this.presets = presets;
        this.title = title;
        this.needCheckPreset = needCheckPreset;
    }
}
