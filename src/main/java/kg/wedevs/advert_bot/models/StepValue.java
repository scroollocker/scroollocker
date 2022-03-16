package kg.wedevs.advert_bot.models;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StepValue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @Column(name="code_v")
    String code;

    @Column(name="value_v")
    String value;

    @ManyToOne
    @JoinColumn(name="field_id")
    StepModel field;

}
