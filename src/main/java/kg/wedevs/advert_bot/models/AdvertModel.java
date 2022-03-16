package kg.wedevs.advert_bot.models;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

import static javax.persistence.FetchType.EAGER;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "adverts_data")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdvertModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String platformCode;
    long telegramMessageId;
    LocalDateTime createdDate;

    boolean isSold = false;
    boolean isSend = false;

    @ManyToOne
    @JoinColumn(name = "user_id")
    TelegramUser user;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "advert_id")
    List<PhotoModel> photos;

    @OneToMany(cascade = CascadeType.ALL, fetch = EAGER)
    @JoinColumn(name = "advert_id")
    List<ValueModel> values;

    @ManyToOne
    @JoinColumn(name = "platform_id")
    Platform platform;
}
