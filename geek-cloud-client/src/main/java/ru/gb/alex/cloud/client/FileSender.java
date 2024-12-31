package ru.gb.alex.cloud.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileSender {
    public static void sendFile(Path path, Channel channel,
                                ChannelFutureListener finishListener) throws IOException {
        FileRegion region = new DefaultFileRegion(path.toFile(), 0, Files.size(path));
        // Альтернативный вариант не с файлом, а с каналом.
        //FileRegion region = new DefaultFileRegion(new FileInputStream(path.toFile()).getChannel(), 0, Files.size(path));

        byte[] fileNameBytes = path.getFileName().toString().getBytes(StandardCharsets.UTF_8);
        ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(1 + 4 + fileNameBytes.length + 8);

        buf.writeByte((byte) 21);
        buf.writeInt(path.getFileName().toString().length());
        buf.writeBytes(fileNameBytes);
        buf.writeLong(Files.size(path));
        channel.writeAndFlush(buf);

        ChannelFuture transferOperationFuture = channel.writeAndFlush(region);
        if (finishListener != null) {
            transferOperationFuture.addListener(finishListener);
        }
    }

    public static void sendAuth(String username, String password, Channel channel) {
        byte[] usernameBytes = username.getBytes(StandardCharsets.UTF_8);
        byte[] passBytes = password.getBytes(StandardCharsets.UTF_8);
        ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(1 + 4 + usernameBytes.length + 4 + passBytes.length);

        buf.writeByte((byte) 11);
        buf.writeInt(usernameBytes.length);
        buf.writeBytes(usernameBytes);
        buf.writeInt(passBytes.length);
        buf.writeBytes(passBytes);
        channel.writeAndFlush(buf);
    }

    public static void fileRequest(String filename, Channel channel) {
        byte[] filenameBytes = filename.getBytes(StandardCharsets.UTF_8);
        ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(1 + 4 + filenameBytes.length);

        buf.writeByte((byte) 22);
        buf.writeInt(filenameBytes.length);
        buf.writeBytes(filenameBytes);
        channel.writeAndFlush(buf);
    }

    public static void renameFile(String filename, Channel channel) {
        byte[] filenameBytes = filename.getBytes(StandardCharsets.UTF_8);
        ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(1 + 4 + filenameBytes.length);

        buf.writeByte((byte) 23);
        buf.writeInt(filenameBytes.length);
        buf.writeBytes(filenameBytes);
        channel.writeAndFlush(buf);
    }

    public static void deleteFile(String filename, Channel channel) {
        byte[] filenameBytes = filename.getBytes(StandardCharsets.UTF_8);
        ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(1 + 4 + filenameBytes.length);

        buf.writeByte((byte) 24);
        buf.writeInt(filenameBytes.length);
        buf.writeBytes(filenameBytes);
        channel.writeAndFlush(buf);
    }
}
