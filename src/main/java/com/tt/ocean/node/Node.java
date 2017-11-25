package com.tt.ocean.node;

import com.sun.xml.internal.ws.handler.HandlerException;

import com.tt.ocean.conf.Conf;
import com.tt.ocean.node.cache.ConfigConnContext;
import com.tt.ocean.node.cache.ConfigNode;
import com.tt.ocean.node.cache.NodeCache;
import com.tt.ocean.node.cache.NodeInfo;
import com.tt.ocean.net.Handler;
import com.tt.ocean.node.route.RouteForConfig;

import com.tt.ocean.node.service.ConfigService;
import com.tt.ocean.node.service.NodeService;
import com.tt.ocean.proto.MessageProto;
import com.tt.ocean.proto.NodeProto;
import com.tt.ocean.proto.NodeUtil;
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
import sun.util.resources.cldr.ar.CalendarData_ar_MA;


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

    private String               __type;

    private int                  __pieceID;

    private boolean              __inited = false;

    private String               __config_auth_key;
    private String               __config_auth_secrect;


    private String               __node_auth_key;
    private String               __node_auth_secrect;

    private Conf                 __conf;

    public Node(String type, int pieceID) throws ConfigurationException, IllegalArgumentException{

        __pieceID = pieceID;
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


        String configAuthKey = bundle.getString("config.auth.key");
        String configAuthSecret = bundle.getString("config.auth.secret");
        String nodeAuthKey = bundle.getString("node.auth.key");
        String nodeAuthSecret = bundle.getString("node.auth.secret");


        if(configAuthKey == null || configAuthKey.isEmpty()){
            throw new ConfigurationException("[config.auth.key] is not found");
        }

        if(configAuthSecret == null || configAuthSecret.isEmpty()){
            throw new ConfigurationException("[config.auth.secret] is not found");
        }

        if(nodeAuthKey == null || nodeAuthKey.isEmpty()){
            throw new ConfigurationException("[node.auth.key] is not found");
        }

        if(nodeAuthSecret == null || nodeAuthSecret.isEmpty()){
            throw new ConfigurationException("[node.auth.secret] is not found");
        }


        __config_auth_key = configAuthKey;
        __config_auth_secrect = configAuthSecret;
        __node_auth_key = nodeAuthKey;
        __node_auth_secrect = nodeAuthSecret;


        this.__type = type;

        __config_server_ip = ip;
        __config_server_port = port;

        __configConn = new ConfigConnContext();
        __nodeCache = new NodeCache();

        __configStrap = new Bootstrap();
        __nodeStrap = new Bootstrap();



        __conf = new Conf();
        __conf.init(pieceID);
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
    public boolean onNodeAuthing(Long conId, ChannelHandlerContext ctx, OceanMessage req) {

        Long pieceID = Long.valueOf(req.getHeader().getPieceId());
        MessageProto.Header header = req.getHeader();
        NodeInfo node = __nodeCache.getNodeByPiece(pieceID);
        if(node != null) {
            if(node.conId != conId){
                log.info("on node authing but the piece channel has been created before");
                log.info("pieceid:" + pieceID);
                ctx.channel().close();

                NodeInfo n2 = __nodeCache.getNode(conId);
                if(n2 != null){
                    log.info("node cache find the node and remove it");
                    __nodeCache.removeNodeUnsafe(n2);
                }
                return true;
            }

        }
        else{
            node = __nodeCache.getNode(conId);
        }

        if(node == null){

            node = __nodeCache.createNodeUnSafe(conId, ctx, req.getNodeRequest().getAuth().getNodeType(),
                    header.getPieceId(), header.getPrsId(), header.getVersion());


        }

        if(node == null){
            log.error("node is null, which should not be");
            NodeProto.NodeAuthRsp rsp = NodeProto.NodeAuthRsp.newBuilder()
                    .setStatus(Status.FAIL)
                    .setNodeType(__type)
                    .build();

            OceanMessage msg = NodeUtil.createAuthRsp(req, rsp);

            ctx.channel().writeAndFlush(msg);
            return true;
        }
        node.state = NodeInfo.STATE_AUTHED;

        NodeProto.NodeAuthRsp rsp = NodeProto.NodeAuthRsp.newBuilder()
                .setStatus(Status.OK)
                .setNodeType(__type)
                .build();

        OceanMessage msg = NodeUtil.createAuthRsp(req, rsp);

        ctx.channel().writeAndFlush(msg);

        return true;

    }

    @Override
    public boolean onNodeAuthed(Long conId, ChannelHandlerContext ctx, OceanMessage rsp) {

        Long pieceID = Long.valueOf(rsp.getHeader().getPieceId());
        MessageProto.Header header = rsp.getHeader();
        NodeInfo node = __nodeCache.getNodeByPiece(pieceID);
        if(node != null) {
            if(node.conId != conId){
                log.info("on node authing but the piece channel has been created before");
                log.info("pieceid:" + pieceID);
                ctx.channel().close();

                NodeInfo n2 = __nodeCache.getNode(conId);
                if(n2 != null){
                    log.info("node cache find the node and remove it");
                    __nodeCache.removeNodeUnsafe(n2);
                }
                return true;
            }

        }
        else{
            node = __nodeCache.getNode(conId);
        }

        if(node == null){

            node = __nodeCache.createNodeUnSafe(conId, ctx, rsp.getNodeResponse().getAuth().getNodeType(),
                    header.getPieceId(), header.getPrsId(), header.getVersion());


        }

        if(node == null){
            log.error("node is null, which should not be");
            return true;
        }
        node.state = NodeInfo.STATE_AUTHED;

        return true;
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
