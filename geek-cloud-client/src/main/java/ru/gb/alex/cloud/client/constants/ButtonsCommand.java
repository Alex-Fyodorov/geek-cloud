package ru.gb.alex.cloud.client.constants;

public enum ButtonsCommand {
    COPY("copy"),
    MOVE("move"),
    DELETE("delete");

    private String message;

    ButtonsCommand(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
