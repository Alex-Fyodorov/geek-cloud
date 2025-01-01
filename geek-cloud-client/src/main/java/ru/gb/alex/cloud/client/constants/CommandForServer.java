package ru.gb.alex.cloud.client.constants;

public enum CommandForServer {
    IDLE((byte) -1),
    AUTH((byte) 11),
    REG((byte) 12),
    GET_FILE_FROM_CLIENT((byte) 21),
    SEND_FILE_TO_CLIENT((byte) 22),
    RENAME((byte) 23),
    DELETE((byte) 24),
    FILE_LIST((byte) 25);

    private byte firstMessageByte;

    public byte getFirstMessageByte() {
        return firstMessageByte;
    }

    CommandForServer(byte firstMessageByte) {
        this.firstMessageByte = firstMessageByte;
    }

    public static CommandForServer getDataTypeFromByte(byte b) {
        if (b == AUTH.firstMessageByte) return AUTH;
        if (b == REG.firstMessageByte) return REG;
        if (b == GET_FILE_FROM_CLIENT.firstMessageByte) return GET_FILE_FROM_CLIENT;
        if (b == SEND_FILE_TO_CLIENT.firstMessageByte) return SEND_FILE_TO_CLIENT;
        if (b == RENAME.firstMessageByte) return RENAME;
        if (b == DELETE.firstMessageByte) return DELETE;
        if (b == FILE_LIST.firstMessageByte) return FILE_LIST;
        return IDLE;
    }
}
