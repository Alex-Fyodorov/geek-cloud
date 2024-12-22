package ru.gb.unlimited_reader;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

public class UnlimitedHandler extends ChannelInboundHandlerAdapter {
    private ByteBuf accumulator;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ByteBufAllocator allocator = ctx.alloc();
        accumulator = allocator.directBuffer(1 * 1024 * 1024, 5 * 1024 * 1024);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf input = (ByteBuf) msg;
        accumulator.writeBytes(input);
        input.release();

        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream("./netty-examples/src/main/resources/1.txt", true))) {
            while (accumulator.readableBytes() > 0) {
                out.write(accumulator.readByte());
            }
            accumulator.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
