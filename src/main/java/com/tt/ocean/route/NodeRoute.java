package com.tt.ocean.route;

import com.tt.ocean.node.Node;
import com.tt.ocean.proto.ConfigProto;
import com.tt.ocean.service.NodeService;
import io.netty.channel.ChannelHandlerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NodeRoute extends Route implements NodeService {

    public static Logger log = LogManager.getLogger(NodeRoute.class.getName());

    private NodeService service = null;
    public NodeRoute(){
        service = new Node();
    }

    @Override
    public  void onConnected(Long conId)
    {
        log.info("on Connected con id:" + conId);
        service.onConnected(conId);
    }

    @Override
    public  void onDisconnected(Long conId)
    {
        log.info("on disconnected con id:" + conId);
        service.onDisconnected(conId);
    }



    @Override
    public  void onRoute(Long conId, ChannelHandlerContext ctx, ConfigProto.ConfigMessage req){
        log.info("on route con id:" + conId);

        if(!req.hasConfigResponse()){
            log.warn("route but not has config response");
            return;
        }

        if(req.getConfigResponse().hasAuth()){
            onAuthed(conId, req);
        }
        else if(req.getConfigResponse().hasGetNodes()){
            onNodeGot(conId, req);
        }
        else if(req.getConfigResponse().hasNotifyNodes()){
            onNodeNotify(conId, req);
        }
        else {
            log.warn("route but not has deal request");
            return;
        }
    }

    @Override
    public void onAuthed(Long conId, ConfigProto.ConfigMessage req) {
        log.info("on config authed, con id:" + conId);
        service.onAuthed(conId, req);
    }

    @Override
    public void onNodeNotify(Long conId, ConfigProto.ConfigMessage req) {
        log.info("on config node notify, con id:" + conId);
        service.onNodeNotify(conId, req);
    }

    @Override
    public void onNodeGot(Long conId, ConfigProto.ConfigMessage req) {
        log.info("on node got, con id:" + conId);
        service.onNodeGot(conId, req);
    }
}
