package kg.wedevs.advert_bot.models;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StepModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @Column
    String title;
    @Column
    String code;
    @Column
    boolean isRequired;
    @Column
    boolean isPreset;
    @Column
    boolean needPresetCheck;
    @Column
    boolean isNumber;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "field_id")
    List<StepPreset> stepPresets;

    @OneToMany(mappedBy = "field", cascade = CascadeType.ALL)
    List<StepValue> stepValues;

}
