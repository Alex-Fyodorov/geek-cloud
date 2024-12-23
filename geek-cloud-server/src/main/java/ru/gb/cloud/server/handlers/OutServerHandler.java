package ru.gb.cloud.server.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import ru.gb.cloud.common.CommandsForClient;
import ru.gb.cloud.server.constants.Constants;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OutServerHandler extends ChannelOutboundHandlerAdapter {
    private final ExecutorService executorService;

    public OutServerHandler() {
        executorService = Executors.newSingleThreadExecutor();
    }

    //Logger logger = LogManager.getLogger(OutHandler.class);

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        executorService.execute(() -> {
            String message = (String) msg;
            if (message.startsWith(Constants.MESSAGE)) {
                message = message.substring(Constants.MESSAGE.length());
                sendText(ctx, message, CommandsForClient.MESSAGE);
            }

            if (message.startsWith(Constants.LIST)) {
                String username = message.substring(Constants.LIST.length());
                File[] filesInCurrentDir = new File("./server_storage/" + username).listFiles();
                if (filesInCurrentDir != null && filesInCurrentDir.length > 0) {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (File file : filesInCurrentDir) {
                        stringBuilder.append(file.getName());
                        stringBuilder.append(" ");
                    }
                    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                    sendText(ctx, stringBuilder.toString(), CommandsForClient.FILE_LIST);
                } else {
                    sendText(ctx, "", CommandsForClient.FILE_LIST);
                }
            }
        });
    }

    private void sendText(ChannelHandlerContext ctx, String message, CommandsForClient command) {
        byte[] messageBytes = message.getBytes();
        ByteBuf byteBuf = ctx.alloc().buffer(1 + 4 + messageBytes.length);
        byteBuf.writeByte(command.getFirstMessageByte());
        byteBuf.writeInt(messageBytes.length);
        byteBuf.writeBytes(messageBytes);
        ctx.writeAndFlush(byteBuf);
    }
}
