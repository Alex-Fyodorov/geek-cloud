package ru.gb.cloud.common;

public enum CommandForClient {
    IDLE((byte) -1),
    MESSAGE((byte) 11),
    FILE_LIST((byte) 21),
    FILE((byte) 25);

    private byte firstMessageByte;

    CommandForClient(byte firstMessageByte) {
        this.firstMessageByte = firstMessageByte;
    }

    public byte getFirstMessageByte() {
        return firstMessageByte;
    }

    public static CommandForClient getDataTypeFromByte(byte b) {
        if (b == MESSAGE.firstMessageByte) return MESSAGE;
        if (b == FILE_LIST.firstMessageByte) return FILE_LIST;
        if (b == FILE.firstMessageByte) return FILE;
        return IDLE;
    }
}

