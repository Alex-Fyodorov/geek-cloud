package ru.gb.alex.cloud.server.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.gb.alex.cloud.server.constants.OutMessageType;
import ru.gb.alex.cloud.common.CommandForServer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileHandler extends ChannelInboundHandlerAdapter {
    private final String username;
    private final ExecutorService executorService;
    private static final String SERVER_STORAGE = "./server_storage/";
    private final MessageService messageService;
    private final FileService fileService;

    Logger logger = LogManager.getLogger(FileHandler.class);

    public FileHandler(String username) {
        this.username = username;
        executorService = Executors.newSingleThreadExecutor();
        messageService = new MessageService();
        fileService = new FileService();
    }

    public enum State {
        IDLE, GET_MESSAGE, MESSAGE_END, GET_FILE
    }

    private State currentState = State.IDLE;
    private CommandForServer command;
    private String message;
    private String path;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        executorService.execute(() -> {
            ByteBuf buf = (ByteBuf) msg;
            while (buf.readableBytes() > 0) {
                if (currentState == State.IDLE) {
                    byte firstByte = buf.readByte();
                    if (checkFirstByte(firstByte)) {
                        command = CommandForServer.getDataTypeFromByte(firstByte);
                        currentState = State.GET_MESSAGE;
                    } else {
                        // TODO может быть послать сообщение пользователю?
                        logger.info(String.format("ERROR: Invalid first byte: %d, user: %s", firstByte, username));
                    }
                }

                if (currentState == State.GET_MESSAGE) {
                    messageService.readMessage(buf, (m -> {
                        message = m;
                        path = String.format("%s%s/%s", SERVER_STORAGE, username, message);
                        currentState = State.MESSAGE_END;
                    }));
                }

                if (currentState == State.MESSAGE_END) {
                    if (command == CommandForServer.GET_FILE_FROM_CLIENT) {
                        command = CommandForServer.IDLE;
                        currentState = State.GET_FILE;
                    } else if (command == CommandForServer.SEND_FILE_TO_CLIENT) {
                        ctx.writeAndFlush(OutMessageType.FILE + path);
                        currentState = State.IDLE;
                    } else if (command == CommandForServer.RENAME) {
                        // TODO сообщение в OUT
                        currentState = State.IDLE;
                    } else if (command == CommandForServer.DELETE) {
                        try {
                            Files.deleteIfExists(Paths.get(path));
                            // TODO проверить команду delete
                            // TODO сообщение в OUT
                            currentState = State.IDLE;
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } else if (command == CommandForServer.FILE_LIST) {
                        // TODO сообщение в OUT
                        currentState = State.IDLE;
                    }
                }

                if (currentState == State.GET_FILE) {
                    try {
                        fileService.getFile(buf, path, (() -> {
                            currentState = State.IDLE;
                            ctx.writeAndFlush(String.format(String.format("%sThe \"%s\" file was " +
                                            "successfully received on the server",
                                    OutMessageType.MESSAGE, message)));
                        }));
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
        if (firstByte == CommandForServer.GET_FILE_FROM_CLIENT.getFirstMessageByte()) return true;
        if (firstByte == CommandForServer.SEND_FILE_TO_CLIENT.getFirstMessageByte()) return true;
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
