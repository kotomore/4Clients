package ru.set404.telegramservice.telegram.keyboards;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.set404.telegramservice.enums.CallbackActionDefinitionEnum;
import ru.set404.telegramservice.enums.CallbackActionPartsEnum;

import java.util.ArrayList;
import java.util.List;


@Component
public class InlineKeyboardMaker {
    public InlineKeyboardMarkup getServiceInlineButton() {
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(getButton(
                "Название",
                CallbackActionPartsEnum.SERVICE_.name() + CallbackActionDefinitionEnum.NAME.name()
        ));
        rowList.add(getButton(
                "Описание",
                CallbackActionPartsEnum.SERVICE_.name() + CallbackActionDefinitionEnum.DESCRIPTION.name()        ));
        rowList.add(getButton(
                "Длительность",
                CallbackActionPartsEnum.SERVICE_.name() + CallbackActionDefinitionEnum.DURATION.name()        ));
        rowList.add(getButton(
                "Цена",
                CallbackActionPartsEnum.SERVICE_.name() + CallbackActionDefinitionEnum.PRICE.name()        ));

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getAgentInlineButton() {
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(getButton(
                "Ф.И.О.",
                CallbackActionPartsEnum.AGENT_.name() + CallbackActionDefinitionEnum.NAME.name()
        ));
        rowList.add(getButton(
                "Пароль",
                CallbackActionPartsEnum.AGENT_.name() + CallbackActionDefinitionEnum.PASSWORD.name()
        ));
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getScheduleInlineButton() {
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(getButton(
                "Изменить",
                CallbackActionPartsEnum.SCHEDULE_.name() + CallbackActionDefinitionEnum.TIME.name()
        ));

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
