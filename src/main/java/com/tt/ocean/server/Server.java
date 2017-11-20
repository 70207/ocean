package com.tt.ocean.server;

import com.tt.ocean.route.ConfigRoute;
import com.tt.ocean.route.Route;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Echoes back any received data from a client.
 */
public final class Server {

    private static Logger log = LogManager.getLogger(Server.class.getName());
    private static  int __config_server_port = 1213;

    static {
        ResourceBundle bundle = ResourceBundle.getBundle("config");
        if (bundle == null) {
            throw new IllegalArgumentException(
                    "[cache.properties] is not found!");
        }
        int port = Integer.valueOf(bundle.getString("config.server.port"));

        log.info("server port:" + port);
        if(port > 0){
            __config_server_port = port;
        }

    }



    public void start(int port)
            throws Exception
    {


        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ServerIntializer());

            b.bind(port).sync().channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }

    public static void main(String[] args)
            throws Exception
    {
        Route.setInstance(new ConfigRoute());
        Server server = new Server();
        log.info("Http Server listening on " + __config_server_port);
        server.start(__config_server_port);
    }
}
