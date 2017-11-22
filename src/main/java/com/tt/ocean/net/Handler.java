package com.tt.ocean.net;

import com.tt.ocean.proto.OceanProto;
import com.tt.ocean.route.Route;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Handler extends SimpleChannelInboundHandler<OceanProto.OceanMessage> {
    private static final Logger log = LogManager.getLogger(Handler.class.getName());


    Route         route;

    public void setRoute(Route route)
    {
        this.route = route;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
        log.info("channel registered");

    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, OceanProto.OceanMessage msg)
            throws Exception {
        log.info("channel read 0");
        Long id = Long.parseLong(ctx.channel().id().asShortText(), 16);
        route.onRoute(id, ctx, msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Long id = Long.parseLong(ctx.channel().id().asShortText(), 16);
        route.onRouteConnected(id, ctx);

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {

        Long id = Long.parseLong(ctx.channel().id().asShortText(), 16);
        route.onRouteDisconnected(id, ctx);
    }
}