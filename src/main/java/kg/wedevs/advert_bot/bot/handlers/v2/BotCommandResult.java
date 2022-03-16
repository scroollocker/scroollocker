package kg.wedevs.advert_bot.bot.handlers.v2;

import kg.wedevs.advert_bot.bot.handlers.enums.CommandResultType;
import kg.wedevs.advert_bot.bot.handlers.enums.BaseStateEnums;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.ArrayList;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BotCommandResult {
    CommandResultType type;
    List<SendMessage> messages;
    BaseStateEnums state;

    public BotCommandResult() {
        type = CommandResultType.NEXT;
        messages = new ArrayList<>();
    }

}
