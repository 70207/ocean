package com.tt.ocean.route;

import com.tt.ocean.proto.ConfigProto.ConfigMessage;
import io.netty.channel.ChannelHandlerContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Route {


    public static Logger log = LogManager.getLogger(Route.class.getName());

    public static Route instance = null;

    public static Route getInstance(){
        return new Route();
    }

    public static void setInstance(Route route){
        instance = route;
    }

    public  void onConnected(Long conId)
    {
        log.info("on Connected but not implemented");
    }

    public  void onDisconnected(Long conId)
    {
        log.info("on disconnected but not implemented");
    }

    public  void onRoute(Long conId, ChannelHandlerContext ctx, ConfigMessage req){
        log.info("on route but not implemented");
    }


}
