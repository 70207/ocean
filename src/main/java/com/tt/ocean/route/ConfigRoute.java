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
    public  void onRoute(Long conId, ChannelHandlerContext ctx, ConfigProto.ConfigMessage message){
        log.info("on route con id:"+ conId);
        if(!message.hasConfigRequest()){
            log.warn("route but not has config request");
            return;
        }

        if(message.getConfigRequest().hasAuth()){
            onAuthing(conId, message);
        }
        else if(message.getConfigRequest().hasGetNodes()){
            onGetNodes(conId, message);
        }
        else{
            log.warn("route but not has deal request");
            return;
        }
    }

    @Override
    public void onAuthing(Long conId, ConfigProto.ConfigMessage msg) {
        log.info("on authing con id:" + conId);
        service.onAuthing(conId, msg);
    }

    @Override
    public void onGetNodes(Long conId, ConfigProto.ConfigMessage msg) {
        log.info("on get nodes, con id:" + conId);
        service.onGetNodes(conId, msg);
    }
}
