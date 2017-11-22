package com.tt.ocean.config.service;

import com.tt.ocean.proto.OceanProto;
import io.netty.channel.ChannelHandlerContext;

public interface ConfigService {
    public void onConnected(Long conId, ChannelHandlerContext ctx);
    public void onDisconnected(Long conId, ChannelHandlerContext ctx);
    public void onAuthing(Long conId, ChannelHandlerContext ctx, OceanProto.OceanMessage req);
    public void onGetNodes(Long conId, ChannelHandlerContext ctx, OceanProto.OceanMessage req);
    public void onSubscribe(Long conId, ChannelHandlerContext ctx, OceanProto.OceanMessage req);
}
