package ru.gb.serialization;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class CloudServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client connected...");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client disconnected...");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //if (msg == null) return;
        System.out.println(msg.getClass().getName());
        if (msg instanceof MyMessage) {
            System.out.println("Client text message: " + ((MyMessage) msg).getText());
            ctx.writeAndFlush(new MyMessage("Hello Client!"));
        } else {
            System.out.println("Server received wrong object!");
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
