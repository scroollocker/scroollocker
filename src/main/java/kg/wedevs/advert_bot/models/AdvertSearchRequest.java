package kg.wedevs.advert_bot.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name="advert_searches")
public class AdvertSearchRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name="json", columnDefinition = "TEXT")
    String requestData;

    @ManyToOne
    @JoinColumn(name = "user_id")
    TelegramUser user;

    @ManyToOne
    @JoinColumn(name = "platform_id")
    Platform platform;

}
