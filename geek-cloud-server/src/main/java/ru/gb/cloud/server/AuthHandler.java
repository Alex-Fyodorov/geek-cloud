package ru.gb.cloud.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.BufferedOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AuthHandler extends ChannelInboundHandlerAdapter {

    public enum State {
        IDLE, NAME_LENGTH, NAME, FILE_LENGTH, FILE
    }

    private ProtoHandler.State currentState = ProtoHandler.State.IDLE;
    private int nextLength;
    private long fileLength;
    private long receivedFileLength;
    private BufferedOutputStream out;


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        while (buf.readableBytes() > 0) {
            byte command = buf.readByte();
            if (command == (byte) 11) {

            }
        }




        while (buf.readableBytes() > 0) {
            if (currentState == ProtoHandler.State.IDLE) {
                byte readed = buf.readByte();
                if (readed == (byte) 25) {
                    currentState = ProtoHandler.State.NAME_LENGTH;
                    receivedFileLength = 0L;
                    System.out.println("State: Start file receiving.");
                } else {
                    System.out.println("ERROR: Invalid first byte - " + readed);
                }
            }

            if (currentState == ProtoHandler.State.NAME_LENGTH) {
                if (buf.readableBytes() >= 4) {
                    System.out.println("State: Get filename length.");
                    nextLength = buf.readInt();
                    currentState = ProtoHandler.State.NAME;
                }

            }

            if (currentState == ProtoHandler.State.NAME) {
                if (buf.readableBytes() >= nextLength) {
                    byte[] fileName = new byte[nextLength];
                    buf.readBytes(fileName);
                    System.out.println("State: Filename received: " +
                            new String(fileName, StandardCharsets.UTF_8));
                    out = new BufferedOutputStream(Files.newOutputStream(Paths.get(
                            "./geek-cloud-server/src/main/java/ru/gb/cloud/server/" +
                                    new String(fileName, StandardCharsets.UTF_8))));
                    currentState = ProtoHandler.State.FILE_LENGTH;
                }

            }

            if (currentState == ProtoHandler.State.FILE_LENGTH) {
                if (buf.readableBytes() >= 8) {
                    fileLength = buf.readLong();
                    System.out.println("State: File length: " + fileLength);
                    currentState = ProtoHandler.State.FILE;
                }
            }

            if (currentState == ProtoHandler.State.FILE) {
                while (buf.readableBytes() > 0) {
                    out.write(buf.readByte());
                    receivedFileLength++;
                    if (fileLength == receivedFileLength) {
                        currentState = ProtoHandler.State.IDLE;
                        System.out.println("File received");
                        out.close();
                        break;
                    }
                }
            }

            if (buf.readableBytes() == 0) {
                buf.release();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
