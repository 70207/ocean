package com.tt.ocean.service;

import com.tt.ocean.proto.ConfigProto.ConfigMessage;
import io.netty.channel.ChannelHandlerContext;

public interface ConfigService {

    public void onConnected(Long conId, ChannelHandlerContext ctx);
    public void onDisconnected(Long conId, ChannelHandlerContext ctx);
    public void onAuthing(Long conId, ChannelHandlerContext ctx, ConfigMessage req);
    public void onGetNodes(Long conId, ChannelHandlerContext ctx, ConfigMessage req);

}
