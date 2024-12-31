package ru.gb.alex.cloud.server.callbacks;

@FunctionalInterface
public interface MessageCallback {
    void callback(String message);
}
