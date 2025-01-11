package ru.gb.alex.cloud.common.constants;

public enum CommandForServer {
    IDLE((byte) -1),
    AUTH((byte) 11),
    REG((byte) 12),
    GET_FILE((byte) 21),
    SEND_FILE((byte) 22),
    RENAME((byte) 23),
    DELETE((byte) 24),
    FILE_LIST((byte) 25),
    EXIT((byte) 30);

    private final byte firstMessageByte;

    public byte getFirstMessageByte() {
        return firstMessageByte;
    }

    CommandForServer(byte firstMessageByte) {
        this.firstMessageByte = firstMessageByte;
    }

    public static CommandForServer getDataTypeFromByte(byte b) {
        if (b == AUTH.firstMessageByte) return AUTH;
        if (b == REG.firstMessageByte) return REG;
        if (b == GET_FILE.firstMessageByte) return GET_FILE;
        if (b == SEND_FILE.firstMessageByte) return SEND_FILE;
        if (b == RENAME.firstMessageByte) return RENAME;
        if (b == DELETE.firstMessageByte) return DELETE;
        if (b == FILE_LIST.firstMessageByte) return FILE_LIST;
        if (b == EXIT.firstMessageByte) return EXIT;
        return IDLE;
    }
}
