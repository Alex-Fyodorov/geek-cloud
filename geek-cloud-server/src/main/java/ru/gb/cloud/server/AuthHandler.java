package ru.gb.cloud.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;

public class AuthHandler extends ChannelInboundHandlerAdapter {
    private final AuthService authService;

    public AuthHandler(AuthService authService) {
        this.authService = authService;
    }

    Logger logger = LogManager.getLogger(AuthHandler.class);

    public enum State {
        IDLE, NAME_LENGTH, NAME, PASS_LENGTH, PASS
    }

    private State currentState = State.IDLE;
    private String username;
    private String password;
    private byte command;
    private int nextLength;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        while (buf.readableBytes() > 0) {
            if (currentState == State.IDLE) {
                command = buf.readByte();
                if (command == (byte) 11 || command == (byte) 12) {
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
                if (command == (byte) 11) {
                    if (authService.authentification(username, password)) {
                        System.out.println("auth!!!!");
                    }
                }
                if (command == (byte) 12) {
                    if (authService.createNewAccount(username, password)) {
                        System.out.println("create!!!!!");
                    }
                }
                ctx.pipeline().addLast(new ProtoHandler(username));
                ctx.fireChannelRead(buf);
                ctx.pipeline().remove(this);
                break;
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
