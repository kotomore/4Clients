package ru.kotomore.telegramservice.telegram.keyboards;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.kotomore.telegramservice.enums.DefinitionEnum;
import ru.kotomore.telegramservice.enums.EntityEnum;

import java.util.ArrayList;
import java.util.List;


@Component
public class InlineKeyboardMaker {
    public InlineKeyboardMarkup getServiceInlineButton() {
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(getButton(
                "Название",
                EntityEnum.SERVICE_.name() + DefinitionEnum.NAME.name()
        ));
        rowList.add(getButton(
                "Описание",
                EntityEnum.SERVICE_.name() + DefinitionEnum.DESCRIPTION.name()));
        rowList.add(getButton(
                "Длительность",
                EntityEnum.SERVICE_.name() + DefinitionEnum.DURATION.name()));
        rowList.add(getButton(
                "Цена",
                EntityEnum.SERVICE_.name() + DefinitionEnum.PRICE.name()));

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getAgentInlineButton() {
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(getButton(
                "Ф.И.О.",
                EntityEnum.AGENT_.name() + DefinitionEnum.NAME.name()
        ));
        rowList.add(getButton(
                "Пароль",
                EntityEnum.AGENT_.name() + DefinitionEnum.PASSWORD.name()
        ));
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getScheduleInlineButton(boolean isPageable) {
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        if (isPageable) {
            rowList.add(List.of(
                    getOneButton("<", EntityEnum.SCHEDULE_.name() + DefinitionEnum.PREV_PAGE.name()),
                    getOneButton("Изменить", EntityEnum.SCHEDULE_.name() + DefinitionEnum.TIME.name()),
                    getOneButton(">", EntityEnum.SCHEDULE_.name() + DefinitionEnum.NEXT_PAGE.name())
            ));
        }
        rowList.add(getButton(
                "Изменить",
                EntityEnum.SCHEDULE_.name() + DefinitionEnum.TIME.name()
        ));
        rowList.add(getButton(
                "Очистить",
                EntityEnum.SCHEDULE_.name() + DefinitionEnum.DELETE.name()
        ));

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getAppointmentDeleteInlineButton(String appointmentId) {
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(getButton(
                "Удалить запись",
                EntityEnum.APPOINTMENT_.name() + DefinitionEnum.DELETE.name() +
                        appointmentId
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

    private InlineKeyboardButton getOneButton(String buttonName, String buttonCallBackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(buttonName);
        button.setCallbackData(buttonCallBackData);
        return button;
    }

}
