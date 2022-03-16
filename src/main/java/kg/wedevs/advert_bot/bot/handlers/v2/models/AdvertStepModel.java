package kg.wedevs.advert_bot.bot.handlers.v2.models;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdvertStepModel {
    String step;
    boolean photosShown;
    List<PhotoModel> photos;
    List<ValueModel> values;
    List<StepModel> steps;

    public AdvertStepModel(String step, List<PhotoModel> photos, List<ValueModel> values, List<StepModel> steps) {
        this.step = step;
        this.photos = photos;
        this.values = values;
        this.steps = steps;
    }

    public StepModel getCurrentStep() {
        if (steps != null && steps.size() > 0) {
            StepModel selectedStep = null;
            if (step == null) {
                selectedStep = steps.stream().findFirst().get();
                step = selectedStep.getCode();
                return selectedStep;
            }
            Optional<StepModel> finded = steps.stream().filter(v -> v.getCode().equals(step)).findFirst();
            if (finded.isPresent()) {
                selectedStep = finded.get();
            }
            return selectedStep;
        }
        return null;
    }

    public StepModel getNextStep() {
        if (steps != null && steps.size() > 0) {
            StepModel selectedStep = null;

            for (int i = 0; i < steps.size(); i++) {
                StepModel stepModel = steps.get(i);
                if (stepModel.getCode().equals(step)) {
                    if (i + 1 < steps.size()) {
                        selectedStep = steps.get(i+1);
                    }
                    break;
                }
            }
            return  selectedStep;
        }
        return null;
    }

    public void setStepValue(String code, String value,String title) {
        ValueModel valueModel = new ValueModel(code, value, title);
        if (values == null) {
            values = new ArrayList<>();
        }
        values.add(valueModel);
    }

    public ValueModel getValueByCode(String code) {
        ValueModel valueModel = null;

        if (values != null) {
            Optional<ValueModel> finded = values.stream().filter(v -> v.getCode().equals(code)).findFirst();
            if (finded.isPresent()) {
                valueModel = finded.get();
            }
        }

        return valueModel;
    }

    public List<PresetModel> getAllPresets(StepModel step) {
        return step.getPresets().stream().filter(v -> {
            if (v.getParentCode() != null) {
                ValueModel value = getValueByCode(v.getParentCode());
                if (value != null && value.getValue().equals(v.getParentValue())) {
                    return true;
                }

            }
            else {
                return true;
            }
            return false;
        }).collect(Collectors.toList());
    }
}

