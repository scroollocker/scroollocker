package kg.wedevs.advert_bot.bot.helpers;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BotKeyboardBuilder {
    List<KeyboardRow> keyboardRow = new ArrayList<>();

    KeyboardRow keyboardSingleRow = new KeyboardRow();

    boolean isFlex = false;
    boolean isClosable = false;

    public static BotKeyboardBuilder create() {
        return new BotKeyboardBuilder();
    }

    public BotKeyboardBuilder addRowAsButtons(List<KeyboardButton> buttons) {
        KeyboardRow row = new KeyboardRow();
        row.addAll(buttons);
        keyboardRow.add(row);
        return this;
    }

    public BotKeyboardBuilder addRowAsString(List<String> buttons) {
        List<KeyboardButton> keyboardButtons = buttons.stream().map(item -> {
            KeyboardButton button = new KeyboardButton();
            button.setText(item);
            return button;
        }).collect(Collectors.toList());

        KeyboardRow row = new KeyboardRow();
        row.addAll(keyboardButtons);
        keyboardRow.add(row);
        return this;
    }

    public BotKeyboardBuilder addRowAsArray(String[] buttons) {
        List<KeyboardButton> keyboardButtons = Arrays.stream(buttons).map(item -> {
            KeyboardButton button = new KeyboardButton();
            button.setText(item);
            return button;
        }).collect(Collectors.toList());

        KeyboardRow row = new KeyboardRow();
        row.addAll(keyboardButtons);
        keyboardRow.add(row);
        return this;
    }

    public BotKeyboardBuilder addButton(String button) {
        KeyboardButton keyboardButton = new KeyboardButton();
        keyboardButton.setText(button);
        keyboardSingleRow.add(keyboardButton);
        return this;
    }

    public BotKeyboardBuilder appendRow() {
        keyboardRow.add(keyboardSingleRow);
        keyboardSingleRow = new KeyboardRow();
        return this;
    }

    public BotKeyboardBuilder setIsClosable(boolean closable) {
        isClosable = closable;
        return this;

    }

    public BotKeyboardBuilder setIsFlex(boolean flex) {
        isFlex = flex;
        return this;

    }

    public ReplyKeyboardMarkup build() {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup(keyboardRow);
        markup.setOneTimeKeyboard(isClosable);
        markup.setResizeKeyboard(isFlex);

        return markup;
    }

}
