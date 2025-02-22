package ru.gb.alex.cloud.client.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.gb.alex.cloud.client.front.Represent;
import ru.gb.alex.cloud.common.constants.CommandForClient;
import ru.gb.alex.cloud.common.constants.StringConstants;
import ru.gb.alex.cloud.common.services.FileService;
import ru.gb.alex.cloud.common.services.MessageService;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InClientHandler extends ChannelInboundHandlerAdapter {

    private enum State {
        IDLE, GET_MESSAGE, MESSAGE_END, GET_FILE
    }

    private final ExecutorService executorService;
    private final MessageService messageService;
    private final FileService fileService;
    private final Represent represent;
    private final Logger logger;
    private State currentState;
    private CommandForClient command;
    private String message;

    public InClientHandler(Represent represent) {
        this.represent = represent;
        executorService = Executors.newSingleThreadExecutor();
        messageService = new MessageService();
        fileService = new FileService();
        logger = LogManager.getLogger(InClientHandler.class);
        currentState = State.IDLE;
    }

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
                        buf.release();
                        break;
                    }
                }

                if (currentState == State.GET_MESSAGE) {
                    messageService.readMessage(buf, (m -> {
                        message = m;
                        logger.info(String.format("%s - %s", command, message));
                        currentState = State.MESSAGE_END;
                    }));
                }

                if (currentState == State.MESSAGE_END) {
                    if (command == CommandForClient.FILE) {
                        command = CommandForClient.IDLE;
                        currentState = State.GET_FILE;
                    } else if (command == CommandForClient.FILE_LIST) {
                        represent.showServerFileList(message);
                        currentState = State.IDLE;
                    } else if (command == CommandForClient.MESSAGE) {
                        represent.showMessage(message);
                        currentState = State.IDLE;
                    } else if (command == CommandForClient.CONFIRM) {
                        represent.confirmLogin(message.equals(StringConstants.CONFIRM));
                        currentState = State.IDLE;
                    }
                }

                if (currentState == State.GET_FILE) {
                    try {
                        fileService.getFile(buf, StringConstants.CLIENT_STORAGE + message, (() -> {
                            represent.showClientFileList();
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
        if (firstByte == CommandForClient.CONFIRM.getFirstMessageByte()) return true;
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
