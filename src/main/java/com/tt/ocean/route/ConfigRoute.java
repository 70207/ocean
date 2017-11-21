package com.tt.ocean.route;

import com.tt.ocean.config.Config;
import com.tt.ocean.proto.ConfigProto;
import com.tt.ocean.service.ConfigService;
import io.netty.channel.ChannelHandlerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConfigRoute extends Route implements ConfigService{

    public static Logger log = LogManager.getLogger(ConfigRoute.class.getName());

    private ConfigService service = null;
    public ConfigRoute(){
        service = new Config();
    }

    @Override
    public  void onConnected(Long conId, ChannelHandlerContext ctx)
    {
        log.info("on Connected con id:" + conId);
        service.onConnected(conId, ctx);
    }

    @Override
    public  void onDisconnected(Long conId, ChannelHandlerContext ctx)
    {
        log.info("on disconnected con id:" + conId);
        service.onDisconnected(conId, ctx);
    }

    @Override
    public  void onRoute(Long conId, ChannelHandlerContext ctx, ConfigProto.ConfigMessage req){
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
        else{
            log.warn("route but not has deal request");
            return;
        }
    }

    @Override
    public void onAuthing(Long conId, ChannelHandlerContext ctx, ConfigProto.ConfigMessage req) {
        log.info("on authing con id:" + conId);
        service.onAuthing(conId, ctx, req);
    }

    @Override
    public void onGetNodes(Long conId, ChannelHandlerContext ctx, ConfigProto.ConfigMessage msg) {
        log.info("on get nodes, con id:" + conId);
        service.onGetNodes(conId, ctx, msg);
    }

}
