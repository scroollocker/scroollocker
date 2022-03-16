package kg.wedevs.advert_bot.bot.handlers.v2.models;

import kg.wedevs.advert_bot.bot.handlers.enums.InlineStateEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class InlineCommandState {
    InlineStateEnum state;
    Long messageId;
}
