package ru.gb.cloud.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.gb.cloud.common.CommandForClient;

import java.nio.charset.StandardCharsets;

public class InClientHandler extends ChannelInboundHandlerAdapter {

    Logger logger = LogManager.getLogger(InClientHandler.class);

    public enum State {
        IDLE, MSG_LENGTH, MESSAGE, FILE_LENGTH, FILE
    }

    private State currentState = State.IDLE;
    private CommandForClient command;
    private String username;
    private String password;
    private int nextLength;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
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
                    String message = new String(messageBytes, StandardCharsets.UTF_8);
                    System.out.println(message);
                }
            }

            if (command == CommandForClient.FILE) {
                currentState = State.FILE_LENGTH;
            } else if (command == CommandForClient.FILE_LIST) {
                currentState = State.IDLE;
            } else if (command == CommandForClient.MESSAGE) {
                currentState = State.IDLE;
            }
//                while (buf.readableBytes() > 0) {
//                    out.write(buf.readByte());
//                    receivedFileLength++;
//                    if (fileLength == receivedFileLength) {
//                        currentState = State.IDLE;
//                        System.out.println("File received");
//                        out.close();
//                        break;
//                    }
//                }



//            if (currentState == State.MESSAGE) {
//                if (buf.readableBytes() >= nextLength) {
//                    byte[] usernameBytes = new byte[nextLength];
//                    buf.readBytes(usernameBytes);
//                    username = new String(usernameBytes, StandardCharsets.UTF_8);
//                    currentState = State.PASS_LENGTH;
//                }
//
//            }

//            if (currentState == State.PASS_LENGTH) {
//                if (buf.readableBytes() >= 4) {
//                    nextLength = buf.readInt();
//                    currentState = State.PASS;
//                }
//            }
//
//            if (currentState == State.PASS) {
//                if (buf.readableBytes() >= nextLength) {
//                    byte[] passBytes = new byte[nextLength];
//                    buf.readBytes(passBytes);
//                    password = new String(passBytes, StandardCharsets.UTF_8);
//                    currentState = State.IDLE;
//                }
//            }

            if (buf.readableBytes() == 0) {
                buf.release();
            }
        }
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
