package ru.gb.alex.cloud.server.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.gb.alex.cloud.common.constants.CommandForServer;
import ru.gb.alex.cloud.common.constants.StringConstants;
import ru.gb.alex.cloud.common.services.MessageService;
import ru.gb.alex.cloud.server.constants.OutMessageType;
import ru.gb.alex.cloud.server.services.AuthService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AuthHandler extends ChannelInboundHandlerAdapter {

    private enum State {
        IDLE, GET_NAME, GET_PASSWORD
    }

    private final AuthService authService;
    private final MessageService messageService;
    private final Logger logger;
    private State currentState;
    private CommandForServer messageType;
    private String username;
    private String password;

    public AuthHandler(AuthService authService) {
        this.authService = authService;
        messageService = new MessageService();
        logger = LogManager.getLogger(AuthHandler.class);
        currentState = State.IDLE;
        messageType = CommandForServer.IDLE;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        new Thread(() -> {
            ByteBuf buf = (ByteBuf) msg;
            while (buf.readableBytes() > 0) {
                if (currentState == State.IDLE) {
                    byte firstByte = buf.readByte();
                    messageType = CommandForServer.getDataTypeFromByte(firstByte);
                    if (messageType == CommandForServer.AUTH || messageType == CommandForServer.REG) {
                        currentState = State.GET_NAME;
                    } else {
                        logger.info("ERROR: Invalid first byte - " + firstByte);
                    }
                }

                if (currentState == State.GET_NAME) {
                    messageService.readMessage(buf, (m -> {
                        username = m;
                        currentState = State.GET_PASSWORD;
                    }));
                }

                if (currentState == State.GET_PASSWORD) {
                    messageService.readMessage(buf, (m -> {
                        password = m;
                        currentState = State.IDLE;
                    }));
                }

                if (buf.readableBytes() == 0) {
                    buf.release();
                }

                if (username != null && password != null) {
                    if (messageType == CommandForServer.AUTH) {
                        if (authService.authentication(username, password)) {
                            logger.info("The client " + username + " has connected.");
                            ctx.writeAndFlush(OutMessageType.CONFIRM + StringConstants.CONFIRM);
                            ctx.writeAndFlush(String.format("%sWelcome %s!", OutMessageType.MESSAGE, username));
                            ctx.writeAndFlush(OutMessageType.LIST + username);
                            mutatePipeline(ctx, buf, username);
                            break;
                        } else {
                            logger.info("The client " + username + " has not connected.");
                            ctx.writeAndFlush(OutMessageType.CONFIRM + StringConstants.NOT_CONFIRM);
                            ctx.writeAndFlush(String.format("%sIncorrect username or password.", OutMessageType.MESSAGE));
                        }
                    }
                    if (messageType == CommandForServer.REG) {
                        if (authService.createNewAccount(username, password)) {
                            logger.info("A new client named " + username + " has signed up.");
                            ctx.writeAndFlush(OutMessageType.CONFIRM + StringConstants.CONFIRM);
                            ctx.writeAndFlush(String.format("%sWelcome %s!", OutMessageType.MESSAGE, username));
                            try {
                                Files.createDirectory(Paths.get("./server_storage/" + username));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            mutatePipeline(ctx, buf, username);
                            break;
                        } else {
                            ctx.writeAndFlush(OutMessageType.CONFIRM + StringConstants.NOT_CONFIRM);
                            logger.info(String.format("Registration was unsuccessful. Username: %s", username));
                            ctx.writeAndFlush(String.format("%sRegistration was unsuccessful.", OutMessageType.MESSAGE));
                        }
                    }
                }
            }
        }).start();
    }

    private void mutatePipeline(ChannelHandlerContext ctx, ByteBuf buf, String username) {
        ctx.pipeline().addLast(new RequestHandler(username));
        ctx.fireChannelRead(buf);
        ctx.pipeline().remove(this);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
