package com.tt.ocean.node.cache;

public class ConfigNode {

    public static final int STATE_NOT_CONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;
    public static final int STATE_AUTHING = 3;
    public static final int STATE_AUTHED = 4;

    public String           addr;
    public int              port;
    public int              pieceID;
    public long             prsID;
    public int              version;


    public String           type;

    public int              state;


    public int              reconnectTimes;


    public ConfigNode(){
        state = STATE_NOT_CONNECTED;
    }
}
