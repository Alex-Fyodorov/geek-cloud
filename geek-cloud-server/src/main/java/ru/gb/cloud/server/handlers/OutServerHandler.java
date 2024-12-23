package ru.gb.cloud.server.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import ru.gb.cloud.server.constants.Constants;

public class OutServerHandler extends ChannelOutboundHandlerAdapter {

    //Logger logger = LogManager.getLogger(OutHandler.class);

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        String message = (String) msg;
        if (message.startsWith(Constants.MESSAGE)) {
            message = message.substring(Constants.MESSAGE.length());
            System.out.println(message);
            byte[] messageBytes = message.getBytes();
            ByteBuf byteBuf = ctx.alloc().buffer(1 + 4 + messageBytes.length);
            byteBuf.writeByte(11);
            byteBuf.writeInt(messageBytes.length);
            byteBuf.writeBytes(messageBytes);
            ctx.writeAndFlush(byteBuf);
        }
    }
}
