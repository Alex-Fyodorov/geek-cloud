package ru.gb.alex.cloud.client.callbacks;

@FunctionalInterface
public interface MessageCallback {
    void callback(String message);
}
