package com.tt.ocean.node.cache;

import io.netty.channel.ChannelHandlerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.tt.ocean.proto.OceanProto.*;
import com.tt.ocean.proto.ConfigProto.*;

public class NodeCache {


    private HashMap<Long, ConfigNode>         __configNodes;
    private ArrayList<Long>                   __waitConfigNodes;

    private HashMap<Long, Long>               __serverIDByPiece;
    private HashMap<Long, NodeInfo>           __serverByID;


    private static final Logger log = LogManager.getLogger(NodeCache.class.getName());

    public NodeCache(){
        __configNodes = new HashMap<>();
        __waitConfigNodes = new ArrayList<>();
        __serverIDByPiece = new HashMap<>();
        __serverByID = new HashMap<>();
    }

    public void onNodesChanged(ConfigGetNodesRsp nodes){

        List<ConfigNodeInfo> list = nodes.getNodesList();
        for(ConfigNodeInfo info : list){
            Long pieceID = Long.valueOf(info.getPieceId());
            ConfigNode node = __configNodes.get(pieceID);
            if(node == null){
                __waitConfigNodes.add(pieceID);
                addConfigNode(info);
            }

        }

    }

    public NodeInfo getNode(Long conId){
        return __serverByID.get(conId);
    }




    public NodeInfo createNodeUnSafe(Long conId, ChannelHandlerContext ctx, String type, int pieceID, long prsID, int version){
        NodeInfo node = new NodeInfo(type);
        InetSocketAddress addr = (InetSocketAddress) ctx.channel().remoteAddress();
        node.addr = addr.getAddress().getHostAddress();
        node.port = addr.getPort();
        node.pieceID = pieceID;
        node.prsID =  prsID;
        node.version = version;

        __serverByID.put(conId, node);

        Long piece = Long.valueOf(pieceID);
        __serverIDByPiece.remove(piece);
        __serverIDByPiece.put(piece, conId);

        return node;

    }

    private void addConfigNode(ConfigNodeInfo info){
        log.info("add config node, piece id:" + info.getPieceId() );

        ConfigNode node = new ConfigNode();
        node.type = info.getNodeType();
        node.addr = info.getAddr();
        node.pieceID = info.getPieceId();
        node.port = info.getPort();
        node.prsID = info.getPrsId();
        node.version = info.getVersion();

        __configNodes.put(Long.valueOf(info.getPieceId()), node);
    }

    private void addConfigNode(ConfigNotifyNode info){
        log.info("add config node, piece id:" + info.getPieceId() );

        ConfigNode node = new ConfigNode();
        node.type = info.getNodeType();
        node.addr = info.getAddr();
        node.pieceID = info.getPieceId();
        node.port = info.getPort();
        node.prsID = info.getPrsId();
        node.version = info.getVersion();

        __configNodes.put(Long.valueOf(info.getPieceId()), node);
    }



    public void onNodeNotifyOnLine(ConfigNotifyNode info){
        log.info("on node notify on line, piece id:" + info.getPieceId());
        Long pieceID = Long.valueOf(info.getPieceId());
        ConfigNode node = __configNodes.get(pieceID);
        if(node == null){
            __waitConfigNodes.add(pieceID);
            addConfigNode(info);
        }


    }


    public void onNodeNotifyOffLine(ConfigNotifyNode info){
        log.info("on node notify off line, piece id:" + info.getPieceId());
        Long pieceID = Long.valueOf(info.getPieceId());
        __configNodes.remove(pieceID);
        __waitConfigNodes.remove(pieceID);
    }

    public List<Long> getWaitConfigNodes() {
        if(__waitConfigNodes.isEmpty()){
            return null;
        }
        return __waitConfigNodes;
    }

    public ConfigNode getConfigNode(Long pieceID){
        return __configNodes.get(pieceID);
    }

    public NodeInfo getNodeByPiece(Long pieceID){
        Long id = __serverIDByPiece.get(pieceID);
        if(id == null){
            return null;
        }

        NodeInfo info = __serverByID.get(id);
        if(info == null){
            __serverIDByPiece.remove(pieceID);
            return null;
        }

        return info;
    }

    public void removeNodeUnsafe(NodeInfo node){
        __serverByID.remove(node.conId);
    }

    public void clearWait(){
        __waitConfigNodes.clear();
    }
}
