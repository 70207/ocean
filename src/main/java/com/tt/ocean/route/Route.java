package com.tt.ocean.route;

import com.tt.ocean.proto.OceanProto;

import io.netty.channel.ChannelHandlerContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Route {


    public static Logger log = LogManager.getLogger(Route.class.getName());



    public  void onRouteConnected(Long conId, ChannelHandlerContext ctx)
    {
        log.info("on Connected but not implemented");
    }

    public  void onRouteDisconnected(Long conId, ChannelHandlerContext ctx)
    {
        log.info("on disconnected but not implemented");
    }

    public  void onRoute(Long conId, ChannelHandlerContext ctx, OceanProto.OceanMessage req){
        log.info("on route but not implemented");
    }




}
