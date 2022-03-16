package kg.wedevs.advert_bot.models;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdvertSetting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    int cheatedAdvertCount;
    int cheatedSoldCount;
    int requestCount;
    int cheatedRequestCount;
}
