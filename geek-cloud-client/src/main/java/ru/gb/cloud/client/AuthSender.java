package ru.gb.cloud.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class AuthSender {
    public static void sendFile(String username, String password, Channel channel) throws IOException {
//        FileRegion region = new DefaultFileRegion(path.toFile(), 0, Files.size(path));
        // Альтернативный вариант не с файлом, а с каналом.
        //FileRegion region = new DefaultFileRegion(new FileInputStream(path.toFile()).getChannel(), 0, Files.size(path));

        byte[] usernameBytes = username.getBytes(StandardCharsets.UTF_8);
        byte[] passBytes = password.getBytes(StandardCharsets.UTF_8);
        ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(1 + 4 + usernameBytes.length + 4 + passBytes.length + 1);

        buf.writeByte((byte) 11);
        buf.writeInt(usernameBytes.length);
        buf.writeBytes(usernameBytes);
        buf.writeInt(passBytes.length);
        buf.writeBytes(passBytes);
        buf.writeByte((byte) 11);
        channel.writeAndFlush(buf);

//        ChannelFuture transferOperationFuture = channel.writeAndFlush(region);
//        if (finishListener != null) {
//            transferOperationFuture.addListener(finishListener);
//        }
    }
}
