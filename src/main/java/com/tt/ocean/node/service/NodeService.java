package com.tt.ocean.node.service;

import com.tt.ocean.proto.OceanProto.*;
import io.netty.channel.ChannelHandlerContext;

public interface NodeService {




    public void onNodeConnected(Long conId, ChannelHandlerContext ctx);
    public void onNodeDisconnected(Long conId, ChannelHandlerContext ctx);


    public boolean onNodeResponse(Long conId, ChannelHandlerContext ctx, OceanMessage rsp);


    public boolean onNodeAuthing(Long conId, ChannelHandlerContext ctx, OceanMessage rsp);
    public boolean onNodeAuthed(Long conId, ChannelHandlerContext ctx, OceanMessage rsp);

}
