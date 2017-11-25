package com.tt.ocean.node.route;

import com.tt.ocean.node.service.NodeService;
import com.tt.ocean.proto.OceanProto;
import com.tt.ocean.route.Route;
import io.netty.channel.ChannelHandlerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RouteForNode extends Route implements NodeService {

    public static Logger log = LogManager.getLogger(RouteForConfig.class.getName());

    private NodeService service = null;
    public RouteForNode(NodeService service){
        super();
        this.service = service;
    }

    @Override
    public  void onRouteConnected(Long conId, ChannelHandlerContext ctx)
    {
        log.info("on Connected con id:" + conId);
        onNodeConnected(conId, ctx);
    }



    @Override
    public  void onRouteDisconnected(Long conId, ChannelHandlerContext ctx)
    {
        log.info("on disconnected con id:" + conId);
        onNodeDisconnected(conId, ctx);
    }


    @Override
    public  void onRoute(Long conId, ChannelHandlerContext ctx, OceanProto.OceanMessage req){
        log.info("on route con id:" + conId);


        if(req.hasNodeResponse()){
            if(onNodeResponse(conId, ctx, req)){
                return;
            }

            if(req.getNodeResponse().hasAuth()){
                onNodeAuthed(conId, ctx, req);
            }
        }
        else if(req.hasNodeRequest()){
            if(req.getNodeRequest().hasAuth()){
                onNodeAuthing(conId, ctx, req);
            }
        }
        else{
              log.warn("node route but not has deal request");
              return;
        }


    }

    @Override
    public void onNodeConnected(Long conId, ChannelHandlerContext ctx) {
        log.info("on node Connected con id:" + conId);
        service.onNodeConnected(conId, ctx);
    }

    @Override
    public void onNodeDisconnected(Long conId, ChannelHandlerContext ctx) {
        log.info("on node disConnected con id:" + conId);
        service.onNodeDisconnected(conId, ctx);
    }

    @Override
    public boolean onNodeResponse(Long conId, ChannelHandlerContext ctx, OceanProto.OceanMessage rsp) {
        log.info("on node response con id:" + conId);
        return service.onNodeResponse(conId, ctx, rsp);
    }

    @Override
    public boolean onNodeAuthing(Long conId, ChannelHandlerContext ctx, OceanProto.OceanMessage req) {
        log.info("on node authing con id:" + conId);
        return service.onNodeAuthing(conId, ctx, req);
    }

    @Override
    public boolean onNodeAuthed(Long conId, ChannelHandlerContext ctx, OceanProto.OceanMessage rsp) {
        log.info("on node authed con id:" + conId);
        return service.onNodeAuthed(conId, ctx, rsp);
    }


}
