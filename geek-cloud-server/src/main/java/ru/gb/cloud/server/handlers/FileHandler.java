package ru.gb.cloud.server.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.gb.cloud.common.CommandForServer;
import ru.gb.cloud.server.constants.OutMessageType;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileHandler extends ChannelInboundHandlerAdapter {
    private final String username;
    private final ExecutorService executorService;
    private static final String SERVER_STORAGE = "./server_storage/";

    Logger logger = LogManager.getLogger(FileHandler.class);

    public FileHandler(String username) {
        this.username = username;
        executorService = Executors.newSingleThreadExecutor();
    }

    public enum State {
        IDLE, MESSAGE_LENGTH, MESSAGE, FILE_LENGTH, FILE
    }

    private State currentState = State.IDLE;
    private CommandForServer command;
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
                        command = CommandForServer.getDataTypeFromByte(firstByte);
                        currentState = State.MESSAGE_LENGTH;
                    } else {
                        logger.info(String.format("ERROR: Invalid first byte: %d, user: %s", firstByte, username));
                    }
                }

                if (currentState == State.MESSAGE_LENGTH) {
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
                    }
                }

                if (command == CommandForServer.SEND) {
                    try {
                        receivedFileLength = 0L;
                        String path = String.format("%s%s/%s", SERVER_STORAGE, username, message);
                        out = new BufferedOutputStream(Files.newOutputStream(Paths.get(path)));
                        currentState = State.FILE_LENGTH;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else if (command == CommandForServer.LOAD) {
                    String path = String.format("%s%s%s/%s", OutMessageType.FILE, SERVER_STORAGE, username, message);
                    ctx.writeAndFlush(path);
                    currentState = State.IDLE;
                } else if (command == CommandForServer.RENAME) {
                    // TODO сообщение в OUT
                    currentState = State.IDLE;
                } else if (command == CommandForServer.DELETE) {
                    // TODO сообщение в OUT
                    currentState = State.IDLE;
                } else if (command == CommandForServer.FILE_LIST) {
                    // TODO сообщение в OUT
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
                                System.out.println("File received");
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
        if (firstByte == CommandForServer.SEND.getFirstMessageByte()) return true;
        if (firstByte == CommandForServer.LOAD.getFirstMessageByte()) return true;
        if (firstByte == CommandForServer.RENAME.getFirstMessageByte()) return true;
        if (firstByte == CommandForServer.DELETE.getFirstMessageByte()) return true;
        if (firstByte == CommandForServer.FILE_LIST.getFirstMessageByte()) return true;
        return false;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
