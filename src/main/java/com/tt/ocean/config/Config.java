package com.tt.ocean.config;

import com.tt.ocean.common.CommonList;
import com.tt.ocean.config.cache.ConnCache;
import com.tt.ocean.config.cache.ConnContext;
import com.tt.ocean.config.cache.TypeCache;
import com.tt.ocean.proto.ConfigUtil;
import com.tt.ocean.proto.OceanProto.*;
import com.tt.ocean.proto.ConfigProto.*;

import com.tt.ocean.config.service.ConfigService;
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
        ConnContext conn = __connCache.getContext(conId);
        if(conn == null){
            log.warn("on disconnected but not have conn, con id:" + conId );
            return;
        }
        __connCache.removeContext(conId);


        log.info("notify offline");
        notifyNode(conn, ConfigNotifyNode.ConfigNotifyType.OFFLINE);


    }

    public void notifyNode(ConnContext conn, ConfigNotifyNode.ConfigNotifyType type){
        ConfigNotifyNode msg = ConfigNotifyNode.newBuilder()
                .setPieceId(conn.pieceID)
                .setAddr(conn.addr)
                .setAddr(conn.addr)
                .setPort(conn.port)
                .setConnNum(0)
                .setPrsId(conn.prsID)
                .setVersion(conn.version)
                .setNodeType(conn.getType())
                .setNotifyType(type)
                .build();

        OceanMessage rsp = ConfigUtil.createNotifyNodeChange(msg);

        TypeCache tc = __typeCache.get(conn.getType());


        if(tc == null){
            log.warn("notify node but no these type cache");
            return;
        }

        int count = 0;
        CommonList list = tc.listForSubscribe.next;
        while(list.object != null) {
            ConnContext cc = (ConnContext) list.object;
            cc.writeAndFlush(rsp);
            log.info("notify to node, piece id:", cc.pieceID);
            count++;

            list = list.next;
        }

        if(type == ConfigNotifyNode.ConfigNotifyType.ONLINE) {
            log.info("notify online, to count:" + count);
        }
        else{
            log.info("notify offline, to count:" + count);
        }

    }



    @Override
    public void onAuthing(Long conId, ChannelHandlerContext ctx, OceanMessage req) {
        ConnContext conn = __connCache.getContext(conId);

        if(ctx == null){
            log.warn("on authing, con id:" + conId);


            OceanMessage rsp = ConfigUtil.createAuthRsp(req,
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
            OceanMessage rsp = ConfigUtil.createAuthRsp(req,
                    ConfigAuthRsp.newBuilder()
                            .setStatus(Status.PARAM_ERROR)
                            .build());

            ctx.writeAndFlush(rsp);

            return;
        }

        if(conn.addr == null || conn.addr.equals("127.0.0.1")){
            conn.addr = req.getConfigRequest().getAuth().getServiceIp();
        }
        conn.port = req.getConfigRequest().getAuth().getServicePort();

        conn.pieceID = req.getHeader().getPieceId();
        conn.prsID = req.getHeader().getPrsId();
        conn.version = req.getHeader().getVersion();

        TypeCache tc = __typeCache.get(type);
        if(tc == null){
            tc = new TypeCache(type);
            __typeCache.put(type, tc);
        }

        tc.addContext(conn);
        conn.auth(ctx, req.getConfigRequest().getAuth().getNodeType());

        OceanMessage rsp = ConfigUtil.createAuthRsp(req,
                ConfigAuthRsp.newBuilder()
                        .setStatus(Status.OK)
                        .build());

        ctx.writeAndFlush(rsp);


        notifyNode(conn, ConfigNotifyNode.ConfigNotifyType.ONLINE);

    }

    @Override
    public void onGetNodes(Long conId, ChannelHandlerContext ctx, OceanMessage req) {
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
            builder.addNodes(ConfigNodeInfo.newBuilder()
                    .setPieceId(cc.pieceID)
                    .setAddr(cc.addr)
                    .setAddr(cc.addr)
                    .setPort(cc.port)
                    .setConnNum(0)
                    .setPrsId(cc.prsID)
                    .setVersion(cc.version)
                    .setNodeType(cc.getType())
                    .build());

            list = list.next;
        }


        ctx.writeAndFlush(ConfigUtil.createGetNodesRsp(req, builder.build()));

    }

    @Override
    public void onSubscribe(Long conId, ChannelHandlerContext ctx, OceanMessage req) {
        ConnContext conn = __connCache.getContext(conId);
        if(ctx == null){
            log.warn("on subscibe, con id:" + conId);
            ctx.writeAndFlush(ConfigUtil.createResponse(req));
            return;
        }

        onFilter(conn);
        String type = req.getConfigRequest().getSubscribe().getNodeType();
        if(type == null || type.length() <= 0){
            log.info("on subscribe nodes failed, type is null, con id:" + conId);
            ctx.writeAndFlush(ConfigUtil.createResponse(req));
            return;
        }

        conn.subscribe(type);


        TypeCache tc = __typeCache.get(type);
        if(tc == null){
            tc = new TypeCache(type);
            __typeCache.put(type, tc);
        }

        tc.addSubscribe(conn);

        ctx.writeAndFlush(ConfigUtil.createResponse(req));
    }



}
