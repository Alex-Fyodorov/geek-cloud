package ru.gb.cloud.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.BufferedOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AuthHandler extends ChannelInboundHandlerAdapter {

    Logger logger = LogManager.getLogger(AuthHandler.class);

    public enum State {
        IDLE, NAME_LENGTH, NAME, PASS_LENGTH, PASS
    }

    private State currentState = State.IDLE;
    private String username;
    private String password;
    private byte command;
    private int nextLength;
    private long fileLength;
    private long receivedFileLength;
    //private BufferedOutputStream out;


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        while (buf.readableBytes() > 0) {
            if (currentState == State.IDLE) {
                command = buf.readByte();
                if (command == (byte) 11 || command == (byte) 12) {
                    currentState = State.NAME_LENGTH;
//                    receivedFileLength = 0L;
//                    System.out.println("State: Start file receiving.");
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
//                    System.out.println("State: Filename received: " +
//                            new String(fileName, StandardCharsets.UTF_8));
//                    out = new BufferedOutputStream(Files.newOutputStream(Paths.get(
//                            "./geek-cloud-server/src/main/java/ru/gb/cloud/server/" +
//                                    new String(fileName, StandardCharsets.UTF_8))));
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
//                    System.out.println("State: Filename received: " +
//                            new String(fileName, StandardCharsets.UTF_8));
//                    out = new BufferedOutputStream(Files.newOutputStream(Paths.get(
//                            "./geek-cloud-server/src/main/java/ru/gb/cloud/server/" +
//                                    new String(fileName, StandardCharsets.UTF_8))));
                    password = new String(passBytes, StandardCharsets.UTF_8);
                    currentState = State.IDLE;
                    break;

//                    out.write(buf.readByte());
//                    receivedFileLength++;
//                    if (fileLength == receivedFileLength) {
//                        currentState = State.IDLE;
//                        System.out.println("File received");
//                        out.close();
//
//                    }
                }
            }

            if (buf.readableBytes() == 0) {
                buf.release();
            }
        }
        System.out.println(username);
        System.out.println(password);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
