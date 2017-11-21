package com.tt.ocean.proto;

import com.tt.ocean.proto.ConfigProto.*;

public class ConfigUtil {

    public static ConfigMessage createAuthRsp(ConfigMessage req, ConfigAuthRsp msg){
        return ConfigMessage.newBuilder()
                .setHeader(MessageUtil.getRspHeader(req.getHeader()))
                .setConfigResponse(ConfigResponse.newBuilder()
                                    .setAuth(msg).build())
                .build();
    }


    public static ConfigMessage createGetNodesRsp(ConfigMessage req, ConfigGetNodesRsp msg){
        return ConfigMessage.newBuilder()
                .setHeader(MessageUtil.getRspHeader(req.getHeader()))
                .setConfigResponse(ConfigResponse.newBuilder()
                .setGetNodes(msg).build())
                .build();
    }


    public static ConfigMessage createNotifyNodeChange(ConfigNotifyNodes msg){
        return ConfigMessage.newBuilder()
                .setHeader(MessageUtil.createHeader())
                .setConfigResponse(ConfigResponse.newBuilder()
                .setNotifyNodes(msg).build())
                .build();
    }
}
