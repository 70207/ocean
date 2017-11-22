package com.tt.ocean.node.service;

import com.tt.ocean.proto.OceanProto;
import io.netty.channel.ChannelHandlerContext;

public interface ConfigService {

    public void onConfigConnected(Long conId, ChannelHandlerContext ctx);
    public void onConfigDisconnected(Long conId, ChannelHandlerContext ctx);


    public boolean onConfigResponse(Long conId, ChannelHandlerContext ctx, OceanProto.OceanMessage rsp);
    public boolean onConfigAuthed(Long conId, ChannelHandlerContext ctx, OceanProto.OceanMessage rsp);
    public boolean onConfigNodesNotify(Long conId, ChannelHandlerContext ctx, OceanProto.OceanMessage rsp);
    public boolean onConfigNodesGot(Long conId, ChannelHandlerContext ctx, OceanProto.OceanMessage rsp);
}
