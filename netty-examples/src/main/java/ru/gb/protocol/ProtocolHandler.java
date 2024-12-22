package ru.gb.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ProtocolHandler extends ChannelInboundHandlerAdapter {
    private enum DataType {
        EMPTY((byte) -1),
        FiLE((byte) 15),
        COMMAND((byte) 16);

        byte firstMessageByte;

        DataType(byte firstMessageByte) {
            this.firstMessageByte = firstMessageByte;
        }

        static DataType getDataTypeFromByte(byte b) {
            if (b == FiLE.firstMessageByte) return FiLE;
            if (b == COMMAND.firstMessageByte) return COMMAND;
            return EMPTY;
        }
    }

    private int state = -1;
    private int reqLen = -1;
    private DataType type = DataType.EMPTY;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        if (state == -1) {
            byte firstByte = buf.readByte();
            type = DataType.getDataTypeFromByte(firstByte);
            state = 0;
            reqLen = 4;
            System.out.println(type);
        }

        if (state == 0) {
            if (buf.readableBytes() < reqLen) {
                return;
            }
            reqLen = buf.readInt();
            state = 1;
            System.out.println("text size " + reqLen);
        }

        if (state == 1) {
            if (buf.readableBytes() < reqLen) {
                return;
            }
            byte[] data = new byte[reqLen];
            buf.readBytes(data);
            String str = new String(data);
            System.out.println(type + " " + str);
            state = -1;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
