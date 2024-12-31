package ru.gb.alex.cloud.server.services;

import io.netty.buffer.ByteBuf;
import ru.gb.alex.cloud.server.callbacks.MessageCallback;

import java.nio.charset.StandardCharsets;

public class MessageService {

    private enum State {
        MESSAGE_LENGTH, MESSAGE
    }

    private State currentState = State.MESSAGE_LENGTH;
    private int messageLength;

    public void readMessage(ByteBuf buf, MessageCallback messageCallback) {

        if (currentState == State.MESSAGE_LENGTH) {
            if (buf.readableBytes() >= 4) {
                messageLength = buf.readInt();
                currentState = State.MESSAGE;
            }
        }

        if (currentState == State.MESSAGE) {
            if (buf.readableBytes() >= messageLength) {
                byte[] messageBytes = new byte[messageLength];
                buf.readBytes(messageBytes);
                String message = new String(messageBytes, StandardCharsets.UTF_8);
                currentState = State.MESSAGE_LENGTH;
                messageCallback.callback(message);
            }
        }
    }
}
