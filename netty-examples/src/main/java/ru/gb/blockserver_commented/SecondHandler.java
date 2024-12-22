package ru.gb.blockserver_commented;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.Arrays;

public class SecondHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        byte[] array = (byte[]) msg;
        for (int i = 0; i < 3; i++) {
            array[i]++;
        }
        System.out.println("Второй шаг: " + Arrays.toString(array));
        // Проталкивает массив дальше по конвееру.
        ctx.fireChannelRead(array);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
