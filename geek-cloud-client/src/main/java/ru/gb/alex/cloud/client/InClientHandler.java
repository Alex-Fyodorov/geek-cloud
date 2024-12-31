package ru.gb.alex.cloud.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.gb.alex.cloud.client.services.FileService;
import ru.gb.alex.cloud.client.services.MessageService;
import ru.gb.alex.cloud.common.CommandForClient;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InClientHandler extends ChannelInboundHandlerAdapter {

    Logger logger = LogManager.getLogger(InClientHandler.class);

    public enum State {
        IDLE, GET_MESSAGE, MESSAGE_END, GET_FILE
    }

    public InClientHandler() {
        executorService = Executors.newSingleThreadExecutor();
        messageService = new MessageService();
        fileService = new FileService();
    }

    private final ExecutorService executorService;
    private final MessageService messageService;
    private final FileService fileService;
    private static final String CLIENT_STORAGE = "./client_storage/";
    private State currentState = State.IDLE;
    private CommandForClient command;
    private String message;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        executorService.execute(() -> {
            ByteBuf buf = (ByteBuf) msg;
            while (buf.readableBytes() > 0) {
                if (currentState == State.IDLE) {
                    byte firstByte = buf.readByte();
                    if (checkFirstByte(firstByte)) {
                        command = CommandForClient.getDataTypeFromByte(firstByte);
                        currentState = State.GET_MESSAGE;
                    } else {
                        logger.info("ERROR: Invalid first byte - " + firstByte);
                    }
                }

                if (currentState == State.GET_MESSAGE) {
                    messageService.readMessage(buf, (m -> {
                        message = m;
                        logger.info(message);
                        currentState = State.MESSAGE_END;
                    }));
                }

                if (currentState == State.MESSAGE_END) {
                    if (command == CommandForClient.FILE) {
                        command = CommandForClient.IDLE;
                        currentState = State.GET_FILE;
                    } else if (command == CommandForClient.FILE_LIST) {
                        currentState = State.IDLE;
                    } else if (command == CommandForClient.MESSAGE) {
                        currentState = State.IDLE;
                    }
                }

                if (currentState == State.GET_FILE) {
                    try {
                        fileService.getFile(buf, CLIENT_STORAGE + message, (() -> {
                            currentState = State.IDLE;
                            logger.info(String.format("File %s received", message));
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
