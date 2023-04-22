package ru.set404.telegramservice.telegram.keyboards;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class ReplyKeyboardMaker {

    public ReplyKeyboardMarkup getMainMenuKeyboard() {
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("Расписание"));
        row1.add(new KeyboardButton("Записи"));

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("Личные данные"));
        row2.add(new KeyboardButton("Редактирование услуг"));

        KeyboardRow row3 = new KeyboardRow();
        row3.add(new KeyboardButton("Код для сайта"));

        List<KeyboardRow> keyboard = new ArrayList<>();
        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);

        final ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setKeyboard(keyboard);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup getSingleButtonKeyboard(String text) {
        KeyboardRow row1 = new KeyboardRow();
        KeyboardButton keyboardButton = new KeyboardButton(text);
        keyboardButton.setRequestContact(true);
        row1.add(keyboardButton);

        final ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();


        replyKeyboardMarkup.setKeyboard(Collections.singletonList(row1));
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        return replyKeyboardMarkup;
    }
}
