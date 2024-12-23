package ru.gb.cloud.common;

public enum CommandsForServer {
    IDLE((byte) -1),
    AUTH((byte) 11),
    REG((byte) 12),
    SEND((byte) 21),
    LOAD((byte) 22),
    RENAME((byte) 23),
    DELETE((byte) 24),
    FILE_LIST((byte) 25);

    private byte firstMessageByte;

    public byte getFirstMessageByte() {
        return firstMessageByte;
    }

    CommandsForServer(byte firstMessageByte) {
        this.firstMessageByte = firstMessageByte;
    }

    public static CommandsForServer getDataTypeFromByte(byte b) {
        if (b == AUTH.firstMessageByte) return AUTH;
        if (b == REG.firstMessageByte) return REG;
        if (b == SEND.firstMessageByte) return SEND;
        if (b == LOAD.firstMessageByte) return LOAD;
        if (b == RENAME.firstMessageByte) return RENAME;
        if (b == DELETE.firstMessageByte) return DELETE;
        if (b == FILE_LIST.firstMessageByte) return FILE_LIST;
        return IDLE;
    }
}
