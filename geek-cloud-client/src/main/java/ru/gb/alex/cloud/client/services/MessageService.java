package ru.gb.alex.cloud.client.services;

import io.netty.buffer.ByteBuf;
import ru.gb.alex.cloud.client.callbacks.MessageCallback;

import java.nio.ByteBuffer;

public class MessageService {

    private enum State {
        MESSAGE_LENGTH, MESSAGE
    }

    private State currentState = State.MESSAGE_LENGTH;
    private int messageLength;
    private int currentMessageLength;
    private StringBuilder stringBuilder;
    private final byte[] bytes = new byte[4];
    private int index = 0;

    public void readMessage(ByteBuf buf, MessageCallback messageCallback) {

        if (currentState == State.MESSAGE_LENGTH) {
            while (buf.readableBytes() > 0) {
                bytes[index] = buf.readByte();
                index++;
                if (index == 4) {
                    messageLength = (ByteBuffer.wrap(bytes)).getInt();
                    index = 0;
                    currentMessageLength = 0;
                    stringBuilder = new StringBuilder();
                    currentState = State.MESSAGE;
                    break;
                }
            }
        }

        if (currentState == State.MESSAGE) {
            while (buf.readableBytes() > 0) {
                stringBuilder.append((char) buf.readByte());
                currentMessageLength++;
                if (currentMessageLength == messageLength) {
                    currentState = State.MESSAGE_LENGTH;
                    messageCallback.callback(stringBuilder.toString());
                    break;
                }
            }
        }
    }
}
