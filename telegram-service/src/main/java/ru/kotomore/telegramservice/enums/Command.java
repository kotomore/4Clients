package ru.kotomore.telegramservice.enums;

public enum Command {
    START("/start"),
    EDIT_SERVICES("Редактирование услуг"),
    PERSONAL_DATA("Личные данные"),
    SCHEDULE("Расписание"),
    APPOINTMENTS("Записи"),
    WEBSITE_CODE("Код для сайта");

    private final String commandText;

    Command(String commandText) {
        this.commandText = commandText;
    }

    public String getCommandText() {
        return commandText;
    }

    public static Command fromString(String text) {
        for (Command command : Command.values()) {
            if (command.getCommandText().equalsIgnoreCase(text)) {
                return command;
            }
        }
        return null;
    }
}
