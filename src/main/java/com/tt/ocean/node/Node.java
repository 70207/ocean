package com.tt.ocean.node;


import com.tt.ocean.node.cache.ConfigNode;
import com.tt.ocean.node.cache.NodeInfo;
import com.tt.ocean.proto.ConfigUtil;
import com.tt.ocean.proto.NodeProto;
import com.tt.ocean.proto.NodeUtil;
import com.tt.ocean.status.Status;
import io.netty.channel.*;
import com.tt.ocean.proto.OceanProto.*;
import com.tt.ocean.proto.ConfigProto.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.naming.ConfigurationException;
import java.util.List;

import java.util.concurrent.TimeUnit;

public class Node extends NodeLink {


    private static final Logger log = LogManager.getLogger(Node.class.getName());




    public Node(String type, String subsribeType, int pieceID, String ip, int port) throws ConfigurationException, IllegalArgumentException {
        super(type, subsribeType, pieceID, ip, port);
    }



    private void authConfig(Channel ch){
        log.info("auth to config");
        OceanMessage msg = ConfigUtil.createAuthReq(
                ConfigAuthReq.newBuilder()
                .setServiceIp(__inner_server_ip)
                .setServicePort(__inner_server_port)
                .setAuthKey(__config_auth_key)
                .setAuthSecret(__config_auth_secrect)
                .setNodeType(__type)
                .build());

        ch.writeAndFlush(msg);
    }

    private void getNodes(Channel ch, String type){
        log.info("get nodes to config, type:" + type);
        OceanMessage msg = ConfigUtil.createGetNodesReq(ConfigGetNodesReq.newBuilder()
        .setNodeType(type)
        .build());

        ch.writeAndFlush(msg);
    }

    private void subsribe(Channel ch, String type){
        log.info("subscribe to config, type:" + type);
        OceanMessage msg = ConfigUtil.createSubsribeReq(ConfigSubscribeReq.newBuilder()
                .setNodeType(type)
                .build());

        ch.writeAndFlush(msg);
    }

    @Override
    public void onConfigConnected(Long conId, ChannelHandlerContext ctx) {
        super.onConfigConnected(conId, ctx);

        authConfig(ctx.channel());
    }

    @Override
    public void onConfigDisconnected(Long conId, ChannelHandlerContext ctx) {
        super.onConfigDisconnected(conId, ctx);

    }

    @Override
    public boolean onConfigResponse(Long conId, ChannelHandlerContext ctx, OceanMessage rsp) {
        return super.onConfigResponse(conId, ctx, rsp);
    }

    @Override
    public boolean onConfigAuthed(Long conId, ChannelHandlerContext ctx, OceanMessage rsp) {
        super.onConfigAuthed(conId, ctx, rsp);

        if(rsp.getConfigResponse().getAuth().getStatus() == Status.OK){
            subsribe(ctx.channel(), __subscribeType);
            getNodes(ctx.channel(), __subscribeType);
        }

        return true;
    }

    @Override
    public boolean onConfigNodesNotify(Long conId, ChannelHandlerContext ctx, OceanMessage rsp) {
        super.onConfigNodesNotify(conId, ctx, rsp);

        checkWaitConfigNode();
        return true;
    }

    @Override
    public boolean onConfigNodesGot(Long conId, ChannelHandlerContext ctx, OceanMessage rsp) {
        super.onConfigNodesGot(conId, ctx, rsp);
        checkWaitConfigNode();

        return true;
    }



    private void checkWaitConfigNode(){


        List<Long> waitConfigNode = __nodeCache.getWaitConfigNodes();
        if(waitConfigNode == null){
            return;
        }

        log.info("check wait config node, node count:" + waitConfigNode.size());


        for(Long pieceID : waitConfigNode){
            if(pieceID == __pieceID){
                continue;
            }
            ConfigNode node = __nodeCache.getConfigNode(pieceID);
            NodeInfo info = __nodeCache.getNodeByPiece(pieceID);
            if(info == null){
                connectNode(node);
            }
        }

        __nodeCache.clearWait();
    }


    private void connectNode(ConfigNode node){
        log.info("connect to node, piece id:" + node.pieceID);
        log.info("node addr:" + node.addr);
        log.info("node port:" + node.port);

        node.state = ConfigNode.STATE_CONNECTING;
        __nodeStrap.connect(node.addr, node.port).addListener((ChannelFuture f)->{
            if(!f.isSuccess()){
                log.warn("connect to node failed, times:" + node.reconnectTimes + ", piece id:" + node.pieceID);
                if(node.reconnectTimes < 3){
                    f.channel().eventLoop().schedule(()->{
                        connectNode(node);
                    }, 4, TimeUnit.SECONDS);
                    node.reconnectTimes++;
                }
                else {
                    log.info("stop to connect to piece, piece id:" + node.pieceID);
                    node.state = ConfigNode.STATE_NOT_CONNECTED;
                }

            }
            else{
                log.info("connect to node success, piece id:" + node.pieceID);
                node.state = ConfigNode.STATE_CONNECTED;
                node.reconnectTimes = 0;
                authNode(node, f.channel());
            }
        });
    }

    private void authNode(ConfigNode node, Channel channel){
        log.info("auth to node, piece id:" + node.pieceID);

        node.state = ConfigNode.STATE_AUTHING;


        NodeProto.NodeAuthReq auth = NodeProto.NodeAuthReq.newBuilder()
                .setNodeType(__type)
                .setAuthKey(__node_auth_key)
                .setAuthSecret(__node_auth_secrect)
                .build();

        OceanMessage req = NodeUtil.createAuthReq(auth);

        channel.writeAndFlush(req).addListener((ChannelFuture f)->{
            if(f.isSuccess()){
                log.info("auth connect to node piece:" + node.pieceID);
            }
            else{
                log.info("auth connect failed to node piece:" + node.pieceID);
            }
        });


    }
}
