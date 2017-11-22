package com.tt.ocean.config.cache;

import com.tt.ocean.common.CommonList;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;

public class ConnContext {
    public Long     conId;
    public boolean  authed;


    public CommonList       typeList;
    public CommonList       listInSubscribe;


    public String           addr;
    public int              port;
    public int              pieceID;
    public long             prsID;
    public int              version;

    private int             reqTimes;
    private String          type;

    private String          typeSubscribe;


    public ChannelHandlerContext   ctx;

    public ConnContext(Long conId){
        reqTimes = 0;
        this.conId = conId;
        authed = false;
        typeList = new CommonList();
        typeList.setObject(this);

        listInSubscribe = new CommonList();
        listInSubscribe.setObject(this);
    }

    public int getReqTimes() {
        return reqTimes;
    }

    public void incrReqTimes() {
        reqTimes++;
    }

    public void auth(ChannelHandlerContext ctx, String type){
        this.authed = true;
        this.type = type;
        this.ctx = ctx;
    }

    public void clearTypeList(){
        typeList.removeFromParent();
    }

    public void clearSubscribeList(){
        listInSubscribe.removeFromParent();
    }

    public void clear(){
        typeList.removeFromParent();
        listInSubscribe.removeFromParent();
    }

    public String getType(){
        return type;
    }


    public String getSubscribe(){
        return typeSubscribe;
    }

    public void subscribe(String type){
        this.typeSubscribe = type;
    }

    public ChannelFuture writeAndFlush(java.lang.Object o){
        return ctx.writeAndFlush(o);
    }
}
