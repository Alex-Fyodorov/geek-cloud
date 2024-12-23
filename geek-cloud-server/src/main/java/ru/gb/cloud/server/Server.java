package ru.gb.cloud.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.gb.cloud.server.databases.AuthService;
import ru.gb.cloud.server.databases.MySQLAuthService;
import ru.gb.cloud.server.databases.SessionFactoryUtils;
import ru.gb.cloud.server.handlers.AuthHandler;
import ru.gb.cloud.server.handlers.OutServerHandler;

public class Server {
    Logger logger = LogManager.getLogger(Server.class);
    public void run(AuthService authService) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler( new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(
                                    new OutServerHandler(),
                                    new AuthHandler(authService)
                            );
                        }
                    });
            //.childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture future = serverBootstrap.bind(8189).sync();
            logger.info("The server is waiting for connection.");
            future.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        AuthService authService = new MySQLAuthService(new SessionFactoryUtils());
        authService.start();
        new Server().run(authService);
    }
}
