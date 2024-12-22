package ru.gb.blockserver_commented;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.Arrays;

public class GatewayHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        byte[] array = (byte[]) msg;
        int sum = 0;
        for (int i = 0; i < 3; i++) {
            sum += array[i];
        }
        if (sum == 66) {
            ctx.fireChannelRead(array);
        } else {
            System.out.println("Сообщение сломано: " + Arrays.toString(array));
            ctx.writeAndFlush("Битое сообшение.\n");
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
