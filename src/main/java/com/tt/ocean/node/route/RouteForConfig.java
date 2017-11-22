package com.tt.ocean.node.route;

import com.tt.ocean.node.service.ConfigService;
import com.tt.ocean.route.Route;

import io.netty.channel.ChannelHandlerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.tt.ocean.proto.OceanProto.*;

public class RouteForConfig extends Route implements ConfigService {

    public static Logger log = LogManager.getLogger(RouteForConfig.class.getName());

    private ConfigService service = null;
    public RouteForConfig(ConfigService service){
        super();
        this.service = service;
    }




    @Override
    public  void onRouteConnected(Long conId, ChannelHandlerContext ctx)
    {
        onConfigConnected(conId, ctx);
    }



    @Override
    public  void onRouteDisconnected(Long conId, ChannelHandlerContext ctx)
    {
        onConfigDisconnected(conId, ctx);
    }


    @Override
    public  void onRoute(Long conId, ChannelHandlerContext ctx, OceanMessage req){


        if(!req.hasConfigResponse()){
            log.warn("route but not has config response");
            return;
        }

        if(onConfigResponse(conId, ctx, req)){
            return;
        }

        if(req.getConfigResponse().hasAuth()){
            onConfigAuthed(conId, ctx, req);
        }
        else if(req.getConfigResponse().hasGetNodes()){
            onConfigNodesGot(conId, ctx, req);
        }
        else if(req.getConfigResponse().hasNotifyNode()){
            onConfigNodesNotify(conId, ctx, req);
        }
        else {
            log.warn("route but not has deal request");
            return;
        }
    }

    @Override
    public  void onConfigConnected(Long conId, ChannelHandlerContext ctx)
    {
        log.info("on Connected con id:" + conId);
        service.onConfigConnected(conId, ctx);
    }

    @Override
    public  void onConfigDisconnected(Long conId, ChannelHandlerContext ctx)
    {
        log.info("on disconnected con id:" + conId);
        service.onConfigDisconnected(conId, ctx);
    }


    @Override
    public boolean onConfigResponse(Long conId, ChannelHandlerContext ctx, OceanMessage rsp) {
        log.info("on response authed, con id:" + conId);
        return service.onConfigResponse(conId, ctx, rsp);
    }

    @Override
    public boolean onConfigAuthed(Long conId,  ChannelHandlerContext ctx, OceanMessage rsp) {
        log.info("on config authed, con id:" + conId);
        return service.onConfigAuthed(conId, ctx, rsp);
    }

    @Override
    public boolean onConfigNodesNotify(Long conId,  ChannelHandlerContext ctx, OceanMessage rsp) {
        log.info("on config node notify, con id:" + conId);
        return service.onConfigNodesNotify(conId, ctx, rsp);
    }

    @Override
    public boolean onConfigNodesGot(Long conId,  ChannelHandlerContext ctx, OceanMessage rsp) {
        log.info("on node got, con id:" + conId);
        return service.onConfigNodesGot(conId, ctx, rsp);
    }


}
