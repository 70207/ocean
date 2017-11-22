package com.tt.ocean.node.cache;

import io.netty.channel.ChannelHandlerContext;

public class ConfigConnContext {
    public static final int STATE_NOT_CONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;
    public static final int STATE_AUTHING = 3;
    public static final int STATE_AUTHED = 4;


    public Long              conId;
    public int               state;
    public boolean           authed;

    public ChannelHandlerContext    ctx;


    public ConfigConnContext(){
        state = STATE_NOT_CONNECTED;
    }
}
