package ru.gb.alex.cloud.client.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import ru.gb.alex.cloud.client.handlers.InClientHandler;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;

public class Network {
    private static Network ourInstance = new Network();

    public static Network getInstance() {
        return ourInstance;
    }

    public Network() {
    }

    private Channel currentChannel;

    public Channel getCurrentChannel() {
        return currentChannel;
    }

    public void start(CountDownLatch countDownLatch) {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap clientBootstrap = new Bootstrap();
            clientBootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress("localhost", 8189))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel socketChannel) {
                            socketChannel.pipeline().addLast(
                                    new InClientHandler()
                            );
                            currentChannel = (Channel) socketChannel;
                        }
                    });
            ChannelFuture future = clientBootstrap.connect().sync();
            countDownLatch.countDown();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                group.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void stop() {
        currentChannel.close();
    }
}
