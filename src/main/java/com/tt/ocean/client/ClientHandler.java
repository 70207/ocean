package com.tt.ocean.client;



import com.tt.ocean.proto.OceanProto.*;
import com.tt.ocean.proto.MessageProto;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class ClientHandler extends SimpleChannelInboundHandler<OceanMessage> {


    private static Logger log = LogManager.getLogger(ClientHandler.class.getName());
    private Channel channel;


    public ChannelFuture sendResponse(){
        OceanMessage cm = OceanMessage.newBuilder()
                .setHeader(
                        MessageProto.Header.newBuilder()
                        .setPieceId(1)
                        .setPrsId(1)
                        .setReqId(1)
                        .setRspId(0)
                        .setVersion(1)
                        .build()
                )
                .setHeartbeat(
                        MessageProto.HeartbeatReq.newBuilder()
                        .setCpuId(1)
                        .setCpuRate(1.0)
                        .setMemory(1)
                        .setPieceId(1)
                        .setProcessId(1)
                        .setMyType("server")
                        .build()
                )
                .build();


        log.info("send response");
        return channel.writeAndFlush(cm);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
        log.info("channel registered");
        channel = ctx.channel();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, OceanMessage msg)
            throws Exception {
        log.info("channel read 0");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}