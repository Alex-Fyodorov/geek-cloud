package ru.gb.alex.cloud.server.handlers;

@FunctionalInterface
public interface MessageCallback {
    void callback(String message);
}
