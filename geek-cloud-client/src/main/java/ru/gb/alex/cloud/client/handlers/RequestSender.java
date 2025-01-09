package ru.gb.alex.cloud.client.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import ru.gb.alex.cloud.client.constants.CommandForServer;
import ru.gb.alex.cloud.client.network.Network;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class RequestSender {

    private final Network network;
    private final Channel channel;

    public RequestSender(Network network) {
        this.network = network;
        channel = network.getCurrentChannel();
    }

    public void sendFile(Path path, ChannelFutureListener finishListener) throws IOException {
        FileRegion region = new DefaultFileRegion(path.toFile(), 0, Files.size(path));
        // Альтернативный вариант не с файлом, а с каналом.
        //FileRegion region = new DefaultFileRegion(new FileInputStream(path.toFile()).getChannel(), 0, Files.size(path));

        byte[] fileNameBytes = path.getFileName().toString().getBytes(StandardCharsets.UTF_8);
        ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(1 + 4 + fileNameBytes.length + 8);

        buf.writeByte(CommandForServer.GET_FILE.getFirstMessageByte());
        buf.writeInt(path.getFileName().toString().length());
        buf.writeBytes(fileNameBytes);
        buf.writeLong(Files.size(path));
        channel.writeAndFlush(buf);

        ChannelFuture transferOperationFuture = channel.writeAndFlush(region);
        if (finishListener != null) {
            transferOperationFuture.addListener(finishListener);
        }
    }

    public void sendAuth(String username, String password, CommandForServer command) {
        byte[] usernameBytes = username.getBytes(StandardCharsets.UTF_8);
        byte[] passBytes = password.getBytes(StandardCharsets.UTF_8);
        ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(1 + 4 + usernameBytes.length + 4 + passBytes.length);

        buf.writeByte(command.getFirstMessageByte());
        buf.writeInt(usernameBytes.length);
        buf.writeBytes(usernameBytes);
        buf.writeInt(passBytes.length);
        buf.writeBytes(passBytes);
        channel.writeAndFlush(buf);
    }

    public void sendRequest(String filename, CommandForServer command) {
        byte[] filenameBytes = filename.getBytes(StandardCharsets.UTF_8);
        ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(1 + 4 + filenameBytes.length);

        buf.writeByte(command.getFirstMessageByte());
        buf.writeInt(filenameBytes.length);
        buf.writeBytes(filenameBytes);
        channel.writeAndFlush(buf);
    }

    public void exit() {
        network.stop();
    }
}
