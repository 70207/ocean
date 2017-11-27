package com.tt.ocean.config.route;

import com.tt.ocean.config.Config;
import com.tt.ocean.route.Route;
import com.tt.ocean.config.service.ConfigService;
import io.netty.channel.ChannelHandlerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.tt.ocean.proto.OceanProto.*;

public class ConfigRoute extends Route implements ConfigService{

    public static Logger log = LogManager.getLogger(ConfigRoute.class.getName());

    private ConfigService service = null;
    public ConfigRoute(ConfigService service){
        this.service = service;
    }

    @Override
    public  void onRouteConnected(Long conId, ChannelHandlerContext ctx)
    {
        log.info("on Connected con id:" + conId);
        onConnected(conId, ctx);
    }



    @Override
    public  void onRouteDisconnected(Long conId, ChannelHandlerContext ctx)
    {
        log.info("on disconnected con id:" + conId);
        onDisconnected(conId, ctx);
    }

    @Override
    public  void onRoute(Long conId, ChannelHandlerContext ctx, OceanMessage req){
        log.info("on route con id:"+ conId);
        if(!req.hasConfigRequest()){
            log.warn("route but not has config request");
            return;
        }

        if(req.getConfigRequest().hasAuth()){
            onAuthing(conId, ctx, req);
        }
        else if(req.getConfigRequest().hasGetNodes()){
            onGetNodes(conId, ctx, req);
        }
        else if(req.getConfigRequest().hasSubscribe()){
            onSubscribe(conId, ctx, req);
        }
        else{
            log.warn("route but not has deal request");
            return;
        }
    }

    @Override
    public void onConnected(Long conId, ChannelHandlerContext ctx) {
        service.onConnected(conId, ctx);
    }

    @Override
    public void onDisconnected(Long conId, ChannelHandlerContext ctx) {
        service.onDisconnected(conId, ctx);
    }

    @Override
    public void onAuthing(Long conId, ChannelHandlerContext ctx, OceanMessage req) {
        log.info("on authing con id:" + conId);
        service.onAuthing(conId, ctx, req);
    }

    @Override
    public void onGetNodes(Long conId, ChannelHandlerContext ctx, OceanMessage req) {
        log.info("on get nodes, con id:" + conId);
        service.onGetNodes(conId, ctx, req);
    }



    @Override
    public void onSubscribe(Long conId, ChannelHandlerContext ctx, OceanMessage req) {
        log.info("on subscribe nodes, con id:" + conId);
        service.onSubscribe(conId, ctx, req);
    }
}