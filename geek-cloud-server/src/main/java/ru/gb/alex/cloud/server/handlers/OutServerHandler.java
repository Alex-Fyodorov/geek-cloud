package ru.gb.alex.cloud.server.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.gb.alex.cloud.server.constants.CommandForClient;
import ru.gb.alex.cloud.server.constants.OutMessageType;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OutServerHandler extends ChannelOutboundHandlerAdapter {
    private final ExecutorService executorService;

    public OutServerHandler() {
        executorService = Executors.newSingleThreadExecutor();
    }

    Logger logger = LogManager.getLogger(OutServerHandler.class);

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        executorService.execute(() -> {
            String message = (String) msg;
            if (message.startsWith(OutMessageType.MESSAGE)) {
                message = message.substring(OutMessageType.MESSAGE.length());
                sendText(ctx, message, CommandForClient.MESSAGE);
            }

            if (message.startsWith(OutMessageType.LIST)) {
                String username = message.substring(OutMessageType.LIST.length());
                File[] filesInCurrentDir = new File("./server_storage/" + username).listFiles();
                if (filesInCurrentDir != null && filesInCurrentDir.length > 0) {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (File file : filesInCurrentDir) {
                        stringBuilder.append(file.getName());
                        stringBuilder.append(" ");
                    }
                    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                    sendText(ctx, stringBuilder.toString(), CommandForClient.FILE_LIST);
                } else {
                    sendText(ctx, "", CommandForClient.FILE_LIST);
                }
            }

            if (message.startsWith(OutMessageType.FILE)) {
                try {
                    String filePath = message.substring(OutMessageType.FILE.length());
                    Path path = Paths.get(filePath);
                    FileRegion region = new DefaultFileRegion(path.toFile(), 0, Files.size(path));
                    byte[] fileNameBytes = path.getFileName().toString().getBytes(StandardCharsets.UTF_8);
                    ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(1 + 4 + fileNameBytes.length + 8);
                    buf.writeByte(CommandForClient.FILE.getFirstMessageByte());
                    buf.writeInt(fileNameBytes.length);
                    buf.writeBytes(fileNameBytes);
                    buf.writeLong(Files.size(path));
                    ctx.writeAndFlush(buf);

                    ChannelFuture transferOperationFuture = ctx.writeAndFlush(region);
                    transferOperationFuture.addListener(future -> {
                        if (future.isSuccess()) {
                            logger.info("The file has been sent successfully: " + filePath);
                        }
                        if (!future.isSuccess()) {
                            logger.info("Sending the file failed: " + filePath);
                        }
                    });
                } catch (NoSuchFileException e) {
                    String filePath = message.substring(OutMessageType.FILE.length());
                    String fileName = Paths.get(filePath).getFileName().toString();
                    String response = String.format("File \"%s\" not found.", fileName);
                    logger.info(response);
                    sendText(ctx, response, CommandForClient.MESSAGE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void sendText(ChannelHandlerContext ctx, String message, CommandForClient command) {
        byte[] messageBytes = message.getBytes();
        ByteBuf byteBuf = ctx.alloc().buffer(1 + 4 + messageBytes.length);
        byteBuf.writeByte(command.getFirstMessageByte());
        byteBuf.writeInt(messageBytes.length);
        byteBuf.writeBytes(messageBytes);
        ctx.writeAndFlush(byteBuf);
    }
}
