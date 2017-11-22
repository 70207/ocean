package com.tt.ocean.client;



import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ResourceBundle;

public class Client {

    private static Logger log = LogManager.getLogger(Client.class.getName());
    private static  String __config_server_ip = "127.0.0.1";
    private static  int __config_server_port = 1213;


    static {
        ResourceBundle bundle = ResourceBundle.getBundle("config");
        if (bundle == null) {
            throw new IllegalArgumentException(
                    "[cache.properties] is not found!");
        }
        String ip = bundle.getString("config.server.ip");
        int port = Integer.valueOf(bundle.getString("config.server.port"));

        log.info("server ip:" + ip);
        log.info("server port:" + port);


        if(port > 0){
            __config_server_port = port;
        }

        if(ip != null && ip.length() > 0){
            __config_server_ip = ip;
        }

    }


    public void start(String ip, int port)
            throws Exception
    {


        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ClientIntializer());

//            Channel c = b.connect(ip, port).sync().channel();
//
//            HandlerForConfig handler = c.pipeline().get(HandlerForConfig.class);
//            handler.sendResponse().addListener((ChannelFuture f)->{
//                log.info("channel send response, id:" + f.channel().id().asLongText());
//            });
//            handler.sendResponse().addListener((ChannelFuture f)->{
//                log.info("channel send response, id:" + f.channel().id().asLongText());
//            });
//            c.close();

//            c = b.connect(ip, port).sync().channel();
//
//            handler = c.pipeline().get(HandlerForConfig.class);
//            handler.sendResponse().addListener((ChannelFuture f)->{
//                            log.info("channel send response, id:" + f.channel().id().asLongText());
//                    });
//
//            handler.sendResponse().addListener((ChannelFuture f)->{
//                log.info("channel send response, id:" + f.channel().id().asLongText());
//            });
//
//            c.close().addListener(f->{
//                log.info("close connection");
//            });

        } finally {

            group.shutdownGracefully();
            log.info("client end");
        }

    }

    public static void main(String[] args)
            throws Exception
    {
       // Route.setInstance(new RouteForConfig());
        Client client = new Client();
        log.info("connect to ip:" + __config_server_ip);
        log.info("connect to port:" + __config_server_port);

        client.start(__config_server_ip, __config_server_port);
    }
}
