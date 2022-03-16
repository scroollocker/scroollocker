package kg.wedevs.advert_bot.models;

import kg.wedevs.advert_bot.bot.handlers.enums.BaseStateEnums;
import kg.wedevs.advert_bot.models.enums.LangEnum;
import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "telegram_users")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TelegramUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name="telegram_id")
    Long telegramId;

    @Column(name="user_name")
    String userName;

    @Column(name="activity_date")
    LocalDateTime lastActivity;

    @Enumerated(EnumType.STRING)
    @Column(name="state")
    BaseStateEnums lastState;

    @Enumerated(EnumType.STRING)
    @Column(name="lang")
    LangEnum lang;

    @Column(name="json", columnDefinition = "TEXT")
    String stateJson;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    List<AdvertModel> adverts;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "searches_id")
    Set<AdvertSearchRequest> searches;

    public TelegramUser(Long telegramId, String userName, LocalDateTime lastActivity, BaseStateEnums lastState, LangEnum lang, String stateJson) {
        this.telegramId = telegramId;
        this.userName = userName;
        this.lastActivity = lastActivity;
        this.lastState = lastState;
        this.lang = lang;
        this.stateJson = stateJson;
    }
}
