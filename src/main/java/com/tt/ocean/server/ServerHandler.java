package com.tt.ocean.server;


import com.tt.ocean.proto.ConfigProto;
import com.tt.ocean.route.Route;
import io.netty.channel.ChannelHandlerContext;

import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;




public class ServerHandler extends  SimpleChannelInboundHandler<ConfigProto.ConfigMessage> {



    private static Logger log = LogManager.getLogger(ServerHandler.class.getName());
    @Override
    public void channelRead0(ChannelHandlerContext ctx, ConfigProto.ConfigMessage message) throws Exception {
        long currentTime = System.currentTimeMillis();
        log.info("channel read 0");
        log.info("channel long id:" + ctx.channel().id().asLongText());
        log.info("channel short id:" + ctx.channel().id().asShortText());
       // log.info("message:" + message.toString());

        Long id = Long.parseLong(ctx.channel().id().asShortText(), 16);

        Route.getInstance().onRoute(id, ctx, message);

    }




    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }




}
