package kg.wedevs.advert_bot.bot.helpers;


import kg.wedevs.advert_bot.bot.handlers.v2.models.AdvertStepModel;
import kg.wedevs.advert_bot.bot.handlers.v2.models.PresetModel;
import kg.wedevs.advert_bot.bot.handlers.v2.models.StepModel;
import kg.wedevs.advert_bot.models.StepValue;
import kg.wedevs.advert_bot.models.ValueModel;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class BotHelper {
    private static Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        return pattern.matcher(strNum).matches();
    }

    public static AdvertStepModel generateSteps() {
        AdvertStepModel advertStepModel = new AdvertStepModel();
        StepModel firstStep = new StepModel();
        firstStep.setRequired(true);
        firstStep.setCode("MARK");
        firstStep.setTitle("Марка");
        firstStep.setPreset(true);

        PresetModel honda = new PresetModel();
        honda.setValue("Honda");
        PresetModel audi = new PresetModel();
        audi.setValue("Audi");
        PresetModel subaru = new PresetModel();

        subaru.setValue("Subaru");
        PresetModel toyota = new PresetModel();
        toyota.setValue("Toyota");
        firstStep.setPresets(Arrays.asList(honda, toyota, audi, subaru));

        StepModel secondStep = new StepModel();
        secondStep.setNeedCheckPreset(true);
        PresetModel accord = new PresetModel();
        accord.setParentCode("MARK");
        accord.setParentValue("Honda");
        accord.setValue("Accord");

        PresetModel ist = new PresetModel();
        ist.setParentCode("MARK");
        ist.setParentValue("Toyota");
        ist.setValue("ist");
        secondStep.setPresets(Arrays.asList(accord, ist));

        secondStep.setRequired(false);
        secondStep.setCode("MODEL");
        secondStep.setTitle("Модель");
        secondStep.setPreset(true);

        StepModel threadStep = new StepModel();
        threadStep.setRequired(false);
        threadStep.setNumber(true);
        threadStep.setCode("ENGINE");
        threadStep.setTitle("Объем");
        threadStep.setPreset(false);

        advertStepModel.setSteps(Arrays.asList(firstStep, secondStep, threadStep)
        );

        return advertStepModel;
    }

    public static String getAdvertMessageRequired(List<ValueModel> stepValues, List<kg.wedevs.advert_bot.models.StepModel> stepModels) {
        StringBuilder builder = new StringBuilder();

        if (stepModels != null && stepValues != null) {
            for (ValueModel value : stepValues) {

                kg.wedevs.advert_bot.models.StepModel stepModel = stepModels.stream().filter(s -> s.getCode().equals(value.getFieldCode())).findFirst().orElse(null);

                if (stepModel != null) {
                    if (!stepModel.isRequired()) continue;
                    if (stepModel.getCode().equals("PHONE")) continue;
                    builder.append(stepModel.getTitle());
                } else {
                    builder.append(value.getFieldCode());
                }
                builder.append(" - ");
                builder.append(value.getValue());
                builder.append("\n");
            }


        }

        return builder.toString();
    }

    public static String getAdvertMessage(List<ValueModel> stepValues, List<kg.wedevs.advert_bot.models.StepModel> stepModels) {
        StringBuilder builder = new StringBuilder();

        if (stepModels != null && stepValues != null) {
            for (ValueModel value : stepValues) {

                kg.wedevs.advert_bot.models.StepModel stepModel = stepModels.stream().filter(s -> s.getCode().equals(value.getFieldCode())).findFirst().orElse(null);
                if (stepModel != null) {
                    builder.append(stepModel.getTitle());
                } else {
                    builder.append(value.getFieldCode());
                }
                builder.append(" - ");
                builder.append(value.getValue());
                builder.append("\n");
            }


        }

        return builder.toString();
    }

}
