package ru.gb.dinamic_pipeline;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;

import java.net.Socket;

public class DynamicServer implements Runnable {

    private static class AuthHandler extends ChannelInboundHandlerAdapter {
        private boolean authOk = false;

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            String input = (String) msg;
            if (authOk) {
                ctx.fireChannelRead(input);
                return;
            }
            if (input.split(" ")[0].equals("/auth")) {
                String username = input.split(" ")[1];
                authOk = true;
                ctx.pipeline().addLast(new MainHandler(username));
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }
    }

    private static class MainHandler extends ChannelInboundHandlerAdapter {
        private String username;

        public MainHandler(String username) {
            this.username = username;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            String input = (String) msg;
            System.out.println(username + ": " + input);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }
    }

    @Override
    public void run() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(
                                    new ObjectDecoder(1024 * 1024, null),
                                    new AuthHandler()
                            );
                        }
                    });
            //.childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture future = serverBootstrap.bind(8189).sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        new Thread(new DynamicServer()).start();
        Thread.sleep(2000);
        Socket socket = new Socket("localhost", 8189);
        ObjectEncoderOutputStream out = new ObjectEncoderOutputStream(socket.getOutputStream());
        out.writeObject("/auth myNameIsClient");
        out.writeObject("Hello!");
        out.close();
        socket.close();
    }
}
