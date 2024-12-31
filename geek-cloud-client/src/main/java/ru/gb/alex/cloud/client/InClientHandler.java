package ru.gb.alex.cloud.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.gb.alex.cloud.common.CommandForClient;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InClientHandler extends ChannelInboundHandlerAdapter {

    Logger logger = LogManager.getLogger(InClientHandler.class);

    public enum State {
        IDLE, MSG_LENGTH, MESSAGE, FILE_LENGTH, FILE
    }

    public InClientHandler() {
        executorService = Executors.newSingleThreadExecutor();
    }

    private final ExecutorService executorService;
    private static final String CLIENT_STORAGE = "./client_storage/";
    private State currentState = State.IDLE;
    private CommandForClient command;
    private String message;
    private int nextLength;
    private long fileLength;
    private long receivedFileLength;
    private BufferedOutputStream out;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        executorService.execute(() -> {
            ByteBuf buf = (ByteBuf) msg;
            while (buf.readableBytes() > 0) {
                if (currentState == State.IDLE) {
                    byte firstByte = buf.readByte();
                    if (checkFirstByte(firstByte)) {
                        command = CommandForClient.getDataTypeFromByte(firstByte);
                        currentState = State.MSG_LENGTH;
                    } else {
                        logger.info("ERROR: Invalid first byte - " + firstByte);
                    }
                }

                if (currentState == State.MSG_LENGTH) {
                    if (buf.readableBytes() >= 4) {
                        nextLength = buf.readInt();
                        currentState = State.MESSAGE;
                    }
                }

                if (currentState == State.MESSAGE) {
                    if (buf.readableBytes() >= nextLength) {
                        byte[] messageBytes = new byte[nextLength];
                        buf.readBytes(messageBytes);
                        message = new String(messageBytes, StandardCharsets.UTF_8);

                        logger.info(message);
                    }
                }

                if (command == CommandForClient.FILE) {
                    try {
                        command = CommandForClient.IDLE;
                        currentState = State.FILE_LENGTH;
                        receivedFileLength = 0L;
                        out = new BufferedOutputStream(Files.newOutputStream(
                                Paths.get(CLIENT_STORAGE + message)));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else if (command == CommandForClient.FILE_LIST) {
                    currentState = State.IDLE;
                } else if (command == CommandForClient.MESSAGE) {
                    currentState = State.IDLE;
                }

                if (currentState == State.FILE_LENGTH) {
                    if (buf.readableBytes() >= 8) {
                        fileLength = buf.readLong();
                        currentState = State.FILE;
                    }
                }

                if (currentState == State.FILE) {
                    try {
                        while (buf.readableBytes() > 0) {
                            out.write(buf.readByte());
                            receivedFileLength++;
                            if (fileLength == receivedFileLength) {
                                currentState = State.IDLE;

                                // TODO message to user
                                logger.info(String.format("File %s received", message));
                                out.close();
                                break;
                            }
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                if (buf.readableBytes() == 0) {
                    buf.release();
                }
            }
        });
    }

    private boolean checkFirstByte(byte firstByte) {
        if (firstByte == CommandForClient.MESSAGE.getFirstMessageByte()) return true;
        if (firstByte == CommandForClient.FILE_LIST.getFirstMessageByte()) return true;
        if (firstByte == CommandForClient.FILE.getFirstMessageByte()) return true;
        return false;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
