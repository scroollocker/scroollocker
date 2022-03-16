package kg.wedevs.advert_bot.models;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.util.List;

@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Platform {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @Column(name = "name_v")
    String title;

    @Column(name = "code_v")
    String code;

    @Column(nullable = false)
    String channelId;

    @Column(name = "is_required_photo")
    boolean isRequiredPhoto;

    @Column
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "platform_id")
    @OrderBy(value = "id ASC")
    List<StepModel> stepFields;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "platform_id")
    List<AdvertModel> adverts;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "platform_id")
    List<AdvertSearchRequest> searches;

}

