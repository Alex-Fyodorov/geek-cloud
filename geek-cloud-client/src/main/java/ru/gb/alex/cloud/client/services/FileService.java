package ru.gb.alex.cloud.client.services;

import io.netty.buffer.ByteBuf;
import ru.gb.alex.cloud.client.callbacks.FileCallback;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileService {

    private enum State {
        FILE_LENGTH, FILE
    }

    private State currentState = State.FILE_LENGTH;
    private long fileLength;
    private long receivedFileLength;
    private BufferedOutputStream out;

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
                while (buf.readableBytes() > 0) {
                    out.write(buf.readByte());
                    receivedFileLength++;
                    if (fileLength == receivedFileLength) {
                        currentState = State.FILE_LENGTH;
                        out.close();
                        fileCallback.callback();
                        break;
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
