package ru.set404.telegramservice.telegram.keyboards;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;


@Component
public class InlineKeyboardMaker {

    public InlineKeyboardMarkup getInlineMessageButtonsWithTemplate(String prefix, boolean isUserDictionaryNeed) {
        InlineKeyboardMarkup inlineKeyboardMarkup = getInlineMessageButtons(prefix, isUserDictionaryNeed);
        inlineKeyboardMarkup.getKeyboard().add(getButton(
                "Шаблон",
                prefix + "Name3"
        ));
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getInlineMessageButtons(String prefix, boolean isButtonsNeeded) {
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();

        if (isButtonsNeeded) {
            rowList.add(getButton(
                    "Название:",
                    prefix + "ServiceName"
            ));
            rowList.add(getButton(
                    "Описание:",
                    prefix + "ServiceDescription"
            ));
            rowList.add(getButton(
                    "Длительность:",
                    prefix + "ServiceDuration"
            ));
            rowList.add(getButton(
                    "Цена:",
                    prefix + "ServicePrice"
            ));
        }

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    private List<InlineKeyboardButton> getButton(String buttonName, String buttonCallBackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(buttonName);
        button.setCallbackData(buttonCallBackData);

        List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
        keyboardButtonsRow.add(button);
        return keyboardButtonsRow;
    }
}
