package com.tt.ocean.node;

import com.sun.xml.internal.ws.handler.HandlerException;

import com.tt.ocean.node.cache.ConfigConnContext;
import com.tt.ocean.node.cache.ConfigNode;
import com.tt.ocean.node.cache.NodeCache;
import com.tt.ocean.node.cache.NodeInfo;
import com.tt.ocean.net.Handler;
import com.tt.ocean.node.route.RouteForConfig;

import com.tt.ocean.node.service.ConfigService;
import com.tt.ocean.node.service.NodeService;
import com.tt.ocean.route.Route;
import com.tt.ocean.status.Status;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;

import com.tt.ocean.proto.OceanProto.*;
import com.tt.ocean.proto.ConfigProto.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import javax.naming.ConfigurationException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

public class Node implements NodeService, ConfigService {



    ConfigConnContext           __configConn;
    NodeCache                   __nodeCache;

    Bootstrap                   __configStrap;
    Bootstrap                   __nodeStrap;

    private static  String      __config_server_ip = "127.0.0.1";
    private static  int         __config_server_port = 1213;

    private static final Logger log = LogManager.getLogger(Node.class.getName());

    private boolean              __inited = false;

    public Node() throws ConfigurationException{

        ResourceBundle bundle = ResourceBundle.getBundle("config");
        if (bundle == null) {
            throw new ConfigurationException(
                    "[cache.properties] is not found!");
        }
        String ip = bundle.getString("config.server.ip");
        int port = Integer.valueOf(bundle.getString("config.server.port"));

        log.info("server ip:" + ip);
        log.info("server port:" + port);


        if(port < 0){
            throw new ConfigurationException("[config.server.port] is not found");
        }

        if(ip == null || ip.isEmpty()){
            throw new ConfigurationException("[config.server.ip] is not found");
        }



        __config_server_ip = ip;
        __config_server_port = port;

        __configConn = new ConfigConnContext();
        __nodeCache = new NodeCache();

        __configStrap = new Bootstrap();
        __nodeStrap = new Bootstrap();


    }

    public void init(EventLoopGroup group){
        if(__inited){
            throw new HandlerException("node has been inited before");
        }


        Route route = new RouteForConfig(this);
        __inited = true;
        __configStrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>(){

                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline p = socketChannel.pipeline();

                        p.addLast(new ProtobufVarint32FrameDecoder());
                        p.addLast(new ProtobufDecoder(OceanMessage.getDefaultInstance()));

                        p.addLast(new ProtobufVarint32LengthFieldPrepender());
                        p.addLast(new ProtobufEncoder());

                        Handler configHandler = new Handler();
                        configHandler.setRoute(route);
                        p.addLast(configHandler);

                    }
                });


        Route nodeRoute = new RouteForConfig(this);


        __nodeStrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>(){

                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline p = socketChannel.pipeline();

                        p.addLast(new ProtobufVarint32FrameDecoder());
                        p.addLast(new ProtobufDecoder(OceanMessage.getDefaultInstance()));

                        p.addLast(new ProtobufVarint32LengthFieldPrepender());
                        p.addLast(new ProtobufEncoder());

                        Handler nodeHandler = new Handler();
                        nodeHandler.setRoute(nodeRoute);
                        p.addLast(nodeHandler);

                    }
                });

        connectConfig();
    }

    private void connectConfig(){
        __configStrap.connect(__config_server_ip, __config_server_port).addListener((ChannelFuture f)->{
            if(!f.isSuccess()) {
                f.channel().eventLoop().schedule(() -> {
                    connectConfig();
                }, 2, TimeUnit.SECONDS);
            }
        });

//        __configStrap.connect(__config_server_ip, __config_server_port).addListener(
//                new ChannelFutureListener() {
//                    @Override
//                    public void operationComplete(ChannelFuture future) throws Exception {
//
//                    }
//                }
//        );

    }
    @Override
    public void onConfigConnected(Long conId, ChannelHandlerContext ctx) {
        __configConn.state = ConfigConnContext.STATE_CONNECTED;
        __configConn.conId = conId;
        __configConn.ctx = ctx;
    }

    @Override
    public void onConfigDisconnected(Long conId, ChannelHandlerContext ctx) {
        __configConn.state = ConfigConnContext.STATE_NOT_CONNECTED;

    }

    @Override
    public boolean onConfigResponse(Long conId, ChannelHandlerContext ctx, OceanMessage rsp) {
        return false;
    }

    @Override
    public boolean onConfigAuthed(Long conId, ChannelHandlerContext ctx, OceanMessage rsp) {
        if(rsp.getConfigResponse().getAuth().getStatus() == Status.OK){
            __configConn.state = ConfigConnContext.STATE_AUTHED;
        }
        else{
            __configConn.state = ConfigConnContext.STATE_CONNECTED;
        }
        return true;
    }

    @Override
    public boolean onConfigNodesNotify(Long conId, ChannelHandlerContext ctx, OceanMessage rsp) {
        if(rsp.getConfigResponse().getNotifyNode().getNotifyType() == ConfigNotifyNode.ConfigNotifyType.ONLINE){
            __nodeCache.onNodeNotifyOnLine(rsp.getConfigResponse().getNotifyNode());
        }
        else{
            __nodeCache.onNodeNotifyOffLine(rsp.getConfigResponse().getNotifyNode());
        }

        checkWaitConfigNode();
        return true;
    }

    @Override
    public boolean onConfigNodesGot(Long conId, ChannelHandlerContext ctx, OceanMessage rsp) {
        __nodeCache.onNodesChanged(rsp.getConfigResponse().getGetNodes());
        checkWaitConfigNode();

        return true;
    }

    @Override
    public void onNodeConnected(Long conId, ChannelHandlerContext ctx) {

    }

    @Override
    public void onNodeDisconnected(Long conId, ChannelHandlerContext ctx) {

    }

    @Override
    public boolean onNodeResponse(Long conId, ChannelHandlerContext ctx, OceanMessage rsp) {
        return false;
    }

    @Override
    public boolean onNodeAuthing(Long conId, ChannelHandlerContext ctx, OceanMessage rsp) {
        return false;
    }

    @Override
    public boolean onNodeAuthed(Long conId, ChannelHandlerContext ctx, OceanMessage rsp) {
        return false;
    }

    private void checkWaitConfigNode(){
        List<Long> waitConfigNode = __nodeCache.getWaitConfigNodes();
        if(waitConfigNode == null){
            return;
        }

        for(Long pieceID : waitConfigNode){
            ConfigNode node = __nodeCache.getConfigNode(pieceID);
            NodeInfo info = __nodeCache.getNodeByPiece(pieceID);
            if(info == null){
                connectNode(node);
            }
        }

        __nodeCache.clearWait();
    }


    private void connectNode(ConfigNode node){
        node.state = ConfigNode.STATE_CONNECTING;
        __nodeStrap.connect(node.addr, node.port).addListener((ChannelFuture f)->{
            if(!f.isSuccess()){
                if(node.reconnectTimes < 3){
                    f.channel().eventLoop().schedule(()->{
                        connectNode(node);
                    }, 4, TimeUnit.SECONDS);
                    node.reconnectTimes++;
                }
                else {
                    node.state = ConfigNode.STATE_NOT_CONNECTED;
                }

            }
            else{
                node.state = ConfigNode.STATE_CONNECTED;
                node.reconnectTimes = 0;
                authConnect(node, f.channel());
            }
        });
    }

    private void authConnect(ConfigNode node, Channel channel){
        node.state = ConfigNode.STATE_AUTHING;


    }
}
