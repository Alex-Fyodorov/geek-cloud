package ru.gb.cloud.server.handlers;

@FunctionalInterface
public interface MessageCallback {
    void callback(String message);
}
