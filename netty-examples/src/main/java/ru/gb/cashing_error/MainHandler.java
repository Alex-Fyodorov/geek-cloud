package ru.gb.cashing_error;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainHandler extends ChannelInboundHandlerAdapter {
    private ExecutorService executorService;

    public MainHandler() {
        this.executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FileRequest) {
            executorService.execute(() -> {
                try {
                    File file = new File("./chat/space.png");
                    int bufSize = 10 * 1024 * 1024;
                    int partsCount = new Long(file.length() / bufSize).intValue();
                    if (file.length() % bufSize != 0) partsCount++;
                    FileMessage out = new FileMessage("space.png", -1,
                            partsCount, new byte[bufSize]);
                    FileInputStream in = new FileInputStream(file);
                    for (int i = 0; i < partsCount; i++) {
                        int readedBytes = in.read(out.data);
                        out.partNumber = i + 1;
                        if (readedBytes < bufSize) {
                            out.data = Arrays.copyOfRange(out.data, 0, readedBytes);
                        }
                        ChannelFuture channelFuture = ctx.writeAndFlush(out);
                        System.out.println("Отправлена часть №" + (i + 1));
                    }
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
