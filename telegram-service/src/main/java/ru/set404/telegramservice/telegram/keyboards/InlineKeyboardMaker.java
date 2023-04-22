package ru.set404.telegramservice.telegram.keyboards;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.set404.telegramservice.constants.ActionDefinitionEnum;
import ru.set404.telegramservice.constants.ActionPartEnum;

import java.util.ArrayList;
import java.util.List;


@Component
public class InlineKeyboardMaker {
    public InlineKeyboardMarkup getServiceInlineButton() {
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(getButton(
                "Название",
                ActionPartEnum.SERVICE_.name() + ActionDefinitionEnum.NAME.name()
        ));
        rowList.add(getButton(
                "Описание",
                ActionPartEnum.SERVICE_.name() + ActionDefinitionEnum.DESCRIPTION.name()        ));
        rowList.add(getButton(
                "Длительность",
                ActionPartEnum.SERVICE_.name() + ActionDefinitionEnum.DURATION.name()        ));
        rowList.add(getButton(
                "Цена",
                ActionPartEnum.SERVICE_.name() + ActionDefinitionEnum.PRICE.name()        ));

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getAgentInlineButton() {
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(getButton(
                "Ф.И.О.",
                ActionPartEnum.AGENT_.name() + ActionDefinitionEnum.NAME.name()
        ));
        rowList.add(getButton(
                "Пароль",
                ActionPartEnum.AGENT_.name() + ActionDefinitionEnum.PASSWORD.name()
        ));
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getScheduleInlineButton() {
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(getButton(
                "Изменить",
                ActionPartEnum.SCHEDULE_.name() + ActionDefinitionEnum.TIME.name()
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
