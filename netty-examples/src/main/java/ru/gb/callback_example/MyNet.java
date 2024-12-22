package ru.gb.callback_example;

import java.nio.file.Path;
import java.nio.file.Paths;

public class MyNet {
    public static void sendMessage(Callback finishCallback) {
        // всё отправлено
        finishCallback.callback();
    }

    public static void fileReceived(FileReceivedCallback fileReceivedCallback) {
        // ...
        Path path = Paths.get("1.txt");
        fileReceivedCallback.callback(path);
    }
}
