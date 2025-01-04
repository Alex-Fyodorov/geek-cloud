package ru.gb.alex.cloud.client.inter;

import ru.gb.alex.cloud.client.constants.CommandForServer;

public interface Represent {
    void showMessage(String message);
    void showServerFileList(String message);
    void showClientFileList();
    void login(String username, String password, CommandForServer command);
    void confirmLogin(boolean confirm);
}
