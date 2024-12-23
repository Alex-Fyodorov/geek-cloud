package ru.gb.cloud.server.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.gb.cloud.server.constants.Constants;
import ru.gb.cloud.server.databases.AuthService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuthHandler extends ChannelInboundHandlerAdapter {
    private final AuthService authService;

    public AuthHandler(AuthService authService) {
        this.authService = authService;
    }

    Logger logger = LogManager.getLogger(AuthHandler.class);

    private enum State {
        IDLE, NAME_LENGTH, NAME, PASS_LENGTH, PASS
    }

    private enum MessageType {
        IDLE((byte) -1),
        AUTH((byte) 11),
        REG((byte) 12);

        byte firstMessageByte;

        MessageType(byte firstMessageByte) {
            this.firstMessageByte = firstMessageByte;
        }

        static MessageType getDataTypeFromByte(byte b) {
            if (b == AUTH.firstMessageByte) return AUTH;
            if (b == REG.firstMessageByte) return REG;
            return IDLE;
        }
    }

    private State currentState = State.IDLE;
    private MessageType messageType = MessageType.IDLE;
    private String username;
    private String password;
    private int nextLength;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        new Thread(() -> {
            ByteBuf buf = (ByteBuf) msg;
            while (buf.readableBytes() > 0) {
                if (currentState == State.IDLE) {
                    messageType = MessageType.getDataTypeFromByte(buf.readByte());
                    if (messageType == MessageType.AUTH || messageType == MessageType.REG) {
                        currentState = State.NAME_LENGTH;
                    } else {
                        logger.info("ERROR: Invalid first byte - " + command);
                    }
                }

                if (currentState == State.NAME_LENGTH) {
                    if (buf.readableBytes() >= 4) {
                        nextLength = buf.readInt();
                        currentState = State.NAME;
                    }
                }

                if (currentState == State.NAME) {
                    if (buf.readableBytes() >= nextLength) {
                        byte[] usernameBytes = new byte[nextLength];
                        buf.readBytes(usernameBytes);
                        username = new String(usernameBytes, StandardCharsets.UTF_8);
                        currentState = State.PASS_LENGTH;
                    }

                }

                if (currentState == State.PASS_LENGTH) {
                    if (buf.readableBytes() >= 4) {
                        nextLength = buf.readInt();
                        currentState = State.PASS;
                    }
                }

                if (currentState == State.PASS) {
                    if (buf.readableBytes() >= nextLength) {
                        byte[] passBytes = new byte[nextLength];
                        buf.readBytes(passBytes);
                        password = new String(passBytes, StandardCharsets.UTF_8);
                        currentState = State.IDLE;
                    }
                }

                if (buf.readableBytes() == 0) {
                    buf.release();
                }

                if (username != null && password != null) {
                    if (messageType == MessageType.AUTH) {
                        if (authService.authentification(username, password)) {
                            logger.info("The client " + username + " has connected.");
                            ctx.writeAndFlush(String.format("%sWelcome %s!", Constants.MESSAGE, username));

                            // TODO список файлов

                            mutatePipeline(ctx, buf, username);
                            break;
                        } else {
                            logger.info("The client " + username + " has not connected.");
                            ctx.writeAndFlush(String.format("%sIncorrect username or password.", Constants.MESSAGE));
                        }
                    }
                    if (messageType == MessageType.REG) {
                        if (authService.createNewAccount(username, password)) {
                            logger.info("A new client named " + username + " has signed up.");
                            ctx.writeAndFlush(String.format("%sWelcome %s!", Constants.MESSAGE, username));
                            try {
                                Files.createDirectory(Paths.get("./server_storage/" + username));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            mutatePipeline(ctx, buf, username);
                            break;
                        } else {
                            logger.info(String.format("Registration was unsuccessful. Username: %s", username));
                            ctx.writeAndFlush(String.format("%sRegistration was unsuccessful.", Constants.MESSAGE));
                        }
                    }
                }
            }
        }).start();

    }

    private void mutatePipeline(ChannelHandlerContext ctx, ByteBuf buf, String username) {
        ctx.pipeline().addLast(new ProtoHandler(username));
        ctx.fireChannelRead(buf);
        ctx.pipeline().remove(this);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
