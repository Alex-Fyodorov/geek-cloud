package ru.gb.proto_file;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ProtoFileSender2 {
    public static void sendFile(Path path, Channel channel,
                                ChannelFutureListener finishListener) throws IOException {
        FileRegion region = new DefaultFileRegion(path.toFile(), 0, Files.size(path));
        // Альтернативный вариант не с файлом, а с каналом.
        //FileRegion region = new DefaultFileRegion(new FileInputStream(path.toFile()).getChannel(), 0, Files.size(path));

        byte[] fileNameBytes = path.getFileName().toString().getBytes(StandardCharsets.UTF_8);
        ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(1 + 4 + fileNameBytes.length + 8);

        buf.writeByte((byte) 25);
        buf.writeInt(path.getFileName().toString().length());
        buf.writeBytes(fileNameBytes);
        buf.writeLong(Files.size(path));
        channel.writeAndFlush(buf);

        ChannelFuture transferOperationFuture = channel.writeAndFlush(region);
        if (finishListener != null) {
            transferOperationFuture.addListener(finishListener);
        }
    }
}
