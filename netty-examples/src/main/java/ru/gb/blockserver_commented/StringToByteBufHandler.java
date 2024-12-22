package ru.gb.blockserver_commented;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

public class StringToByteBufHandler extends ChannelOutboundHandlerAdapter {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        String str = (String) msg;
        byte[] array = str.getBytes();
        ByteBuf byteBuf = ctx.alloc().buffer(array.length);
        byteBuf.writeBytes(array);
        ctx.writeAndFlush(byteBuf);
        //byteBuf.release(); // После отправки релиз делать не надо, нетти делает это сам.
    }
}
