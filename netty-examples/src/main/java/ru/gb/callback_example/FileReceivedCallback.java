package ru.gb.callback_example;

import java.nio.file.Path;

public interface FileReceivedCallback {
    void callback(Path path);
}
