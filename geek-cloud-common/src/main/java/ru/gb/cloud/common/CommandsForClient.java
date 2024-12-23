package ru.gb.cloud.common;

public enum CommandsForClient {
    MESSAGE((byte) 11),
    FILE_LIST((byte) 21),
    FILE((byte) 25);

    private byte firstMessageByte;

    CommandsForClient(byte firstMessageByte) {
        this.firstMessageByte = firstMessageByte;
    }

    public byte getFirstMessageByte() {
        return firstMessageByte;
    }

    static CommandsForClient getDataTypeFromByte(byte b) {
        if (b == MESSAGE.firstMessageByte) return MESSAGE;
        if (b == FILE_LIST.firstMessageByte) return FILE_LIST;
        if (b == FILE.firstMessageByte) return FILE;
        return null;
    }
}

