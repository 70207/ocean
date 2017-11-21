package com.tt.ocean.config;

import com.tt.ocean.common.CommonList;
import com.tt.ocean.config.cache.ConnCache;
import com.tt.ocean.config.cache.ConnContext;
import com.tt.ocean.config.cache.TypeCache;
import com.tt.ocean.proto.ConfigProto.*;
import com.tt.ocean.proto.ConfigUtil;
import com.tt.ocean.proto.ConfigProto.ConfigMessage;

import com.tt.ocean.service.ConfigService;
import com.tt.ocean.status.Status;
import io.netty.channel.ChannelHandlerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;

import java.util.HashMap;

public class Config implements ConfigService {


    private  static final Logger log = LogManager.getLogger(ConfigService.class.getName());

    ConnCache __connCache = null;


    HashMap<String, TypeCache> __typeCache = null;

    public Config(){
        __connCache = new ConnCache();
        __typeCache = new HashMap<>();
    }



    private void onFilter(ConnContext ctx){
        ctx.incrReqTimes();
    }

    @Override
    public void onConnected(Long conId, ChannelHandlerContext ctx) {
        ConnContext conn = __connCache.createContext(conId);
        InetSocketAddress addr = (InetSocketAddress) ctx.channel().remoteAddress();
        conn.addr = addr.getAddress().getHostAddress();
        conn.port = addr.getPort();


        onFilter(conn);
    }

    @Override
    public void onDisconnected(Long conId, ChannelHandlerContext ctx) {
        ConnContext conn = __connCache.createContext(conId);
        if(conn == null){
            log.warn("on disconnected but not have conn, con id:" + conId );
            return;
        }
        __connCache.removeContext(conId);


        notifyNode(conn, ConfigNotifyNodes.ConfigNotifyType.OFFLINE);


    }

    public void notifyNode(ConnContext conn, ConfigNotifyNodes.ConfigNotifyType type){
        ConfigNotifyNodes msg = ConfigNotifyNodes.newBuilder()
                .setPieceId(conn.pieceID)
                .setPort(conn.port)
                .setIp(conn.addr)
                .setNodeType(conn.getType())
                .setNotifyType(type)
                .build();

        ConfigMessage rsp = ConfigUtil.createNotifyNodeChange(msg);


    }






    @Override
    public void onAuthing(Long conId, ChannelHandlerContext ctx, ConfigMessage req) {
        ConnContext conn = __connCache.getContext(conId);

        if(ctx == null){
            log.warn("on authing, con id:" + conId);


            ConfigMessage rsp = ConfigUtil.createAuthRsp(req,
                    ConfigAuthRsp.newBuilder()
                    .setStatus(Status.OK)
                    .build());

            ctx.writeAndFlush(rsp);

            return;
        }

        onFilter(conn);

        String type = req.getConfigRequest().getAuth().getNodeType();
        if(type == null || type.length() <= 0){
            log.info("on authing failed, type is null, con id:" + conId);
            ConfigMessage rsp = ConfigUtil.createAuthRsp(req,
                    ConfigAuthRsp.newBuilder()
                            .setStatus(Status.PARAM_ERROR)
                            .build());

            ctx.writeAndFlush(rsp);

            return;
        }

        conn.pieceID = req.getHeader().getPieceId();
        conn.prsID = req.getHeader().getPrsId();
        conn.version = req.getHeader().getVersion();

        TypeCache tc = __typeCache.get(type);
        if(tc == null){
            tc = new TypeCache(type);
            __typeCache.put(type, tc);
        }

        tc.addContext(conn);
        conn.auth(req.getConfigRequest().getAuth().getNodeType());

        ConfigMessage rsp = ConfigUtil.createAuthRsp(req,
                ConfigAuthRsp.newBuilder()
                        .setStatus(Status.OK)
                        .build());

        ctx.writeAndFlush(rsp);


        notifyNode(conn, ConfigNotifyNodes.ConfigNotifyType.ONLINE);

    }

    @Override
    public void onGetNodes(Long conId, ChannelHandlerContext ctx, ConfigMessage req) {
        ConnContext conn = __connCache.getContext(conId);
        if(ctx == null){
            log.warn("on authing, con id:" + conId);
            ctx.writeAndFlush(ConfigUtil.createGetNodesRsp(req, ConfigGetNodesRsp.newBuilder()
            .build()));

            return;
        }

        onFilter(conn);
        String type = req.getConfigRequest().getGetNodes().getNodeType();
        if(type == null || type.length() <= 0){
            log.info("on get nodes failed, type is null, con id:" + conId);
            ctx.writeAndFlush(ConfigUtil.createGetNodesRsp(req, ConfigGetNodesRsp.newBuilder()
                    .build()));
            return;
        }


        TypeCache tc = __typeCache.get(type);
        if(tc == null){
            return;
        }

        ConfigGetNodesRsp.Builder builder = ConfigGetNodesRsp.newBuilder();



        CommonList list = tc.list.next;
        while(list.object != null){
            ConnContext cc = (ConnContext)list.object;
            builder.addNodes(ConfigNodesInfo.newBuilder()
                    .setPieceId(cc.pieceID)
                    .setPort(cc.port)
                    .setConnNum(0)
                    .setPrsId(cc.prsID)
                    .setVersion(cc.version)
                    .setNodeType(cc.getType())
                    .build());
        }


        ctx.writeAndFlush(ConfigUtil.createGetNodesRsp(req, builder.build()));

    }
}
