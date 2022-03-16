package kg.wedevs.advert_bot.bot.handlers.v2.models;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PhotoModel {
    String fileId;
    String fileName;
    Integer messageId;
    Long chatId;
    boolean isPrimary;

    public PhotoModel(String fileId, String fileName) {
        this.fileId = fileId;
        this.fileName = fileName;
    }
}
