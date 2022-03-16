package kg.wedevs.advert_bot.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "field_values")
public class ValueModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String fieldCode;

    String value;

    @ManyToOne
    @JoinColumn(name = "advert_id")
    AdvertModel advert;
}
