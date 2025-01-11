package ru.gb.alex.cloud.common.callbacks;

@FunctionalInterface
public interface MessageCallback {
    void callback(String message);
}
