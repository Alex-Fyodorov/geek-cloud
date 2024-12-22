package ru.gb.blockserver_commented;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.file.Files;
import java.nio.file.Paths;

public class FinalHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        byte[] array = (byte[]) msg;
        Files.write(Paths.get("netty-examples", "1.txt"), array);
        ctx.writeAndFlush("Java!\n"); // от обработчика.
        //ctx.channel().writeAndFlush("Java"); // от конца конвеера.
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
