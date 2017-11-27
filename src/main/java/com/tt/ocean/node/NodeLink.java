package com.tt.ocean.node;


import com.tt.ocean.conf.Conf;
import com.tt.ocean.net.Handler;
import com.tt.ocean.node.cache.ConfigConnContext;
import com.tt.ocean.node.cache.NodeCache;
import com.tt.ocean.node.cache.NodeInfo;
import com.tt.ocean.node.route.RouteForConfig;
import com.tt.ocean.node.route.RouteForNode;
import com.tt.ocean.node.service.ConfigService;
import com.tt.ocean.node.service.NodeService;
import com.tt.ocean.proto.*;
import com.tt.ocean.route.Route;
import com.tt.ocean.status.Status;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.naming.ConfigurationException;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

public abstract class NodeLink implements NodeService, ConfigService {

    protected static final Logger log = LogManager.getLogger(NodeLink.class.getName());

    ConfigConnContext           __configConn;
    NodeCache                   __nodeCache;

    Bootstrap                   __configStrap;
    Bootstrap                   __nodeStrap;

    protected static  String      __config_server_ip = "127.0.0.1";
    protected static  int         __config_server_port = 1213;



    protected String               __type;
    protected String               __subscribeType;

    protected int                  __pieceID;

    protected boolean              __inited = false;

    protected String               __config_auth_key;
    protected String               __config_auth_secrect;

    protected String               __inner_server_ip;
    protected int                  __inner_server_port;


    protected String               __node_auth_key;
    protected String               __node_auth_secrect;

    protected Conf __conf;



    public NodeLink(String type, String subsribeType, int pieceID, String ip, int port) throws ConfigurationException, IllegalArgumentException {
        parseArgs(type, subsribeType, pieceID, ip, port);
        parseBundle();

        create();
    }


    public void parseArgs(String type, String subsribeType, int pieceID, String ip, int port) throws IllegalArgumentException{
        if(type == null || type.isEmpty()){
            log.error("node type should be set");
            throw new IllegalArgumentException("node type should be set");
        }

        if(subsribeType == null || subsribeType.isEmpty()){
            log.error("node subscribe type should be set");
            throw new IllegalArgumentException("node subscribe type should be set");
        }

        if(pieceID <= 0){
            log.error("piece id type should be set");
            throw new IllegalArgumentException("piece id should be set");
        }

        if(ip == null || ip.isEmpty()){
            log.error("node ip should be set");
            throw new IllegalArgumentException("node ip should be set");
        }

        if(port <= 0){
            log.error("node port should be set");
            throw new IllegalArgumentException("node port should be set");
        }



        __pieceID = pieceID;

        __inner_server_ip = ip;
        __inner_server_port = port;


        this.__type = type;
        this.__subscribeType = subsribeType;

    }

    private void parseBundle() throws ConfigurationException{
        ResourceBundle bundle = ResourceBundle.getBundle("config");
        if (bundle == null) {
            throw new ConfigurationException(
                    "[cache.properties] is not found!");
        }

        String configIp = bundle.getString("config.server.ip");
        int configPort = Integer.valueOf(bundle.getString("config.server.port"));

        log.info("config server ip:" + configIp);
        log.info("config server port:" + configPort);

        if(configIp == null || configIp.isEmpty()){
            throw new ConfigurationException("[config.server.ip] is not found");
        }

        if(configPort < 0){
            throw new ConfigurationException("[config.server.port] is not found");
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

        __config_server_ip = configIp;
        __config_server_port = configPort;

    }


    private void create()
    {
        __configConn = new ConfigConnContext();
        __nodeCache = new NodeCache();

        __configStrap = new Bootstrap();
        __nodeStrap = new Bootstrap();



        __conf = new Conf();
        __conf.init(__pieceID);
    }

    public void init(EventLoopGroup group){
        if(__inited){
            throw new IllegalArgumentException("node has been inited before");
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
                        p.addLast(new ProtobufDecoder(OceanProto.OceanMessage.getDefaultInstance()));

                        p.addLast(new ProtobufVarint32LengthFieldPrepender());
                        p.addLast(new ProtobufEncoder());

                        Handler configHandler = new Handler();
                        configHandler.setRoute(route);
                        p.addLast(configHandler);

                    }
                });


        Route nodeRoute = new RouteForNode(this);


        __nodeStrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>(){

                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline p = socketChannel.pipeline();

                        Handler nodeHandler = new Handler();
                        nodeHandler.setRoute(nodeRoute);

                        p.addLast(new ProtobufVarint32FrameDecoder());
                        p.addLast(new ProtobufDecoder(OceanProto.OceanMessage.getDefaultInstance()));

                        p.addLast(new ProtobufVarint32LengthFieldPrepender());
                        p.addLast(new ProtobufEncoder());


                        p.addLast(nodeHandler);

                    }
                });

        connectConfig();
    }

    private void connectConfig(){
        log.info("connect to config");
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
    public boolean onConfigResponse(Long conId, ChannelHandlerContext ctx, OceanProto.OceanMessage rsp) {
        return false;
    }

    @Override
    public boolean onConfigAuthed(Long conId, ChannelHandlerContext ctx, OceanProto.OceanMessage rsp) {
        if(rsp.getConfigResponse().getAuth().getStatus() == Status.OK){
            __configConn.state = ConfigConnContext.STATE_AUTHED;
        }
        else{
            __configConn.state = ConfigConnContext.STATE_CONNECTED;
        }
        return true;
    }

    @Override
    public boolean onConfigNodesNotify(Long conId, ChannelHandlerContext ctx, OceanProto.OceanMessage rsp) {
        if(rsp.getConfigResponse().getNotifyNode().getNotifyType() == ConfigProto.ConfigNotifyNode.ConfigNotifyType.ONLINE){
            __nodeCache.onNodeNotifyOnLine(rsp.getConfigResponse().getNotifyNode());
        }
        else{
            __nodeCache.onNodeNotifyOffLine(rsp.getConfigResponse().getNotifyNode());
        }


        return true;
    }

    @Override
    public boolean onConfigNodesGot(Long conId, ChannelHandlerContext ctx, OceanProto.OceanMessage rsp) {
        __nodeCache.onNodesChanged(rsp.getConfigResponse().getGetNodes());

        return true;
    }

    @Override
    public void onNodeConnected(Long conId, ChannelHandlerContext ctx) {

    }

    @Override
    public void onNodeDisconnected(Long conId, ChannelHandlerContext ctx) {

    }

    @Override
    public boolean onNodeResponse(Long conId, ChannelHandlerContext ctx, OceanProto.OceanMessage rsp) {
        return false;
    }

    @Override
    public boolean onNodeAuthing(Long conId, ChannelHandlerContext ctx, OceanProto.OceanMessage req) {

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

            OceanProto.OceanMessage msg = NodeUtil.createAuthRsp(req, rsp);

            ctx.channel().writeAndFlush(msg);
            return true;
        }
        node.state = NodeInfo.STATE_AUTHED;

        NodeProto.NodeAuthRsp rsp = NodeProto.NodeAuthRsp.newBuilder()
                .setStatus(Status.OK)
                .setNodeType(__type)
                .build();

        OceanProto.OceanMessage msg = NodeUtil.createAuthRsp(req, rsp);

        ctx.channel().writeAndFlush(msg);

        return true;

    }

    @Override
    public boolean onNodeAuthed(Long conId, ChannelHandlerContext ctx, OceanProto.OceanMessage rsp) {

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


}
