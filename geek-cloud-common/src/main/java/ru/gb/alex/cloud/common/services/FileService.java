package ru.gb.alex.cloud.common.services;

import io.netty.buffer.ByteBuf;
import ru.gb.alex.cloud.common.callbacks.FileCallback;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileService {

    private enum State {
        FILE_LENGTH, FILE
    }

    private State currentState;
    private long fileLength;
    private long receivedFileLength;
    private BufferedOutputStream out;
    private byte[] bytes;

    public FileService() {
        currentState = State.FILE_LENGTH;
    }

    public void getFile(ByteBuf buf, String path, FileCallback fileCallback) throws IOException {

        if (currentState == State.FILE_LENGTH) {
            if (buf.readableBytes() >= 8) {
                fileLength = buf.readLong();
                receivedFileLength = 0L;
                out = new BufferedOutputStream(Files.newOutputStream(Paths.get(path)));
                currentState = State.FILE;
            }
        }

        if (currentState == State.FILE) {
            try {
                int capacity = buf.readableBytes();
                if (fileLength - receivedFileLength > capacity) {
                    bytes = new byte[capacity];
                    buf.readBytes(bytes);
                    out.write(bytes);
                    receivedFileLength += capacity;
                } else {
                    while (buf.readableBytes() > 0) {
                        out.write(buf.readByte());
                        receivedFileLength++;
                        if (fileLength == receivedFileLength) {
                            currentState = State.FILE_LENGTH;
                            out.close();
                            fileCallback.callback();
                            return;
                        }
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
