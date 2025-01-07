package ru.gb.alex.cloud.server.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.gb.alex.cloud.server.constants.CommandForServer;
import ru.gb.alex.cloud.server.constants.OutMessageType;
import ru.gb.alex.cloud.server.services.FileService;
import ru.gb.alex.cloud.server.services.MessageService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
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

    public enum State {IDLE, GET_MESSAGE, MESSAGE_END, GET_FILE}
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
                        ctx.writeAndFlush(String.format("%sERROR: Invalid first byte: %d.",
                                OutMessageType.MESSAGE, firstByte));
                        logger.info(String.format("ERROR: Invalid first byte: %d, user: %s",
                                firstByte, username));
                    }
                }

                if (currentState == State.GET_MESSAGE) {
                    messageService.readMessage(buf, (m -> {
                        message = m;
                        logger.info(String.format("%s: %s - %s", username, command, message));
                        path = String.format("%s%s/%s", SERVER_STORAGE, username, message);
                        currentState = State.MESSAGE_END;
                    }));
                }

                if (currentState == State.MESSAGE_END) {
                    if (command == CommandForServer.GET_FILE) {
                        currentState = State.GET_FILE;
                    }
                    if (command == CommandForServer.SEND_FILE) {
                        CountDownLatch countDownLatch = new CountDownLatch(1);
                        ctx.writeAndFlush(OutMessageType.FILE + path,
                                ctx.newPromise().addListener(f -> countDownLatch.countDown()));
                        try {
                            countDownLatch.await();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        ctx.writeAndFlush(OutMessageType.LIST + username);
                        currentState = State.IDLE;
                    }
                    if (command == CommandForServer.RENAME) {
                        String[] names = message.split("\\s");
                        path = String.format("%s%s/%s", SERVER_STORAGE, username, names[0]);
                        if (checkRenamableFiles(ctx, names[0], names[1])) {
                            try {
                                Files.move(Paths.get(path), Paths.get(path).resolveSibling(names[1]));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            logger.info(String.format("File \"%s/%s\" was renamed to \"%s\".",
                                    username, names[0], names[1]));
                            ctx.writeAndFlush(OutMessageType.LIST + username);
                        }
                        currentState = State.IDLE;
                    }
                    if (command == CommandForServer.DELETE) {
                        try {
                            if (Files.deleteIfExists(Paths.get(path))) {
                                Thread.sleep(50);
                                ctx.writeAndFlush(OutMessageType.LIST + username);
                                logger.info(String.format("File \"%s/%s\" was deleted.",
                                        username, message));
                            } else {
                                ctx.writeAndFlush(String.format("%sFile \"%s\" does not exist.",
                                        OutMessageType.MESSAGE, message));
                            }
                        } catch (IOException | InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        currentState = State.IDLE;
                    }
                    if (command == CommandForServer.FILE_LIST) {
                        ctx.writeAndFlush(OutMessageType.LIST + username);
                        currentState = State.IDLE;
                    }
                }

                if (currentState == State.GET_FILE) {
                    try {
                        fileService.getFile(buf, path, (() -> {
                            currentState = State.IDLE;
                            ctx.writeAndFlush(OutMessageType.LIST + username);
                            logger.info(String.format("The \"%s/%s\" file was successfully " +
                                    "received on the server", username, message));
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
        if (firstByte == CommandForServer.GET_FILE.getFirstMessageByte()) return true;
        if (firstByte == CommandForServer.SEND_FILE.getFirstMessageByte()) return true;
        if (firstByte == CommandForServer.RENAME.getFirstMessageByte()) return true;
        if (firstByte == CommandForServer.DELETE.getFirstMessageByte()) return true;
        if (firstByte == CommandForServer.FILE_LIST.getFirstMessageByte()) return true;
        return false;
    }

    private boolean checkRenamableFiles(ChannelHandlerContext ctx, String oldName, String newName) {
        int count = 0;
        String oldPath = String.format("%s%s/%s", SERVER_STORAGE, username, oldName);
        String newPath = String.format("%s%s/%s", SERVER_STORAGE, username, newName);
        if (Files.exists(Paths.get(newPath))) {
            count++;
            ctx.writeAndFlush(String.format("%sFile \"%s\" already exists.",
                    OutMessageType.MESSAGE, newName));
        }
        if (!Files.exists(Paths.get(oldPath))) {
            count++;
            ctx.writeAndFlush(String.format("%sFile \"%s\" does not exist.",
                    OutMessageType.MESSAGE, oldName));
        }
        return count == 0;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
