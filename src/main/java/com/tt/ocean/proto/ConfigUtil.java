package com.tt.ocean.proto;

import com.tt.ocean.proto.OceanProto.*;
import com.tt.ocean.proto.ConfigProto.*;

public class ConfigUtil {


    public static OceanMessage createAuthReq(ConfigAuthReq msg){
        return OceanMessage.newBuilder()
                .setHeader(MessageUtil.createHeader())
                .setConfigRequest(ConfigRequest.newBuilder()
                        .setAuth(msg)
                        .build())
                .build();
    }

    public static OceanMessage createGetNodesReq(ConfigGetNodesReq msg){
        return OceanMessage.newBuilder()
                .setHeader(MessageUtil.createHeader())
                .setConfigRequest(ConfigRequest.newBuilder()
                        .setGetNodes(msg)
                        .build())
                .build();
    }

    public static OceanMessage createSubsribeReq(ConfigSubscribeReq msg){
        return OceanMessage.newBuilder()
                .setHeader(MessageUtil.createHeader())
                .setConfigRequest(ConfigRequest.newBuilder()
                        .setSubscribe(msg)
                        .build())
                .build();
    }

    public static OceanMessage createAuthRsp(OceanMessage req, ConfigAuthRsp msg){
        return OceanMessage.newBuilder()
                .setHeader(MessageUtil.getRspHeader(req.getHeader()))
                .setConfigResponse(ConfigResponse.newBuilder()
                                    .setAuth(msg).build())
                .build();
    }


    public static OceanMessage createGetNodesRsp(OceanMessage req, ConfigGetNodesRsp msg){
        return OceanMessage.newBuilder()
                .setHeader(MessageUtil.getRspHeader(req.getHeader()))
                .setConfigResponse(ConfigResponse.newBuilder()
                .setGetNodes(msg).build())
                .build();
    }


    public static OceanMessage createNotifyNodeChange(ConfigNotifyNode msg){
        return OceanMessage.newBuilder()
                .setHeader(MessageUtil.createHeader())
                .setConfigResponse(ConfigResponse.newBuilder()
                .setNotifyNode(msg).build())
                .build();
    }



    public static OceanMessage createResponse(OceanMessage req){
        return OceanMessage.newBuilder()
                .setHeader(MessageUtil.getRspHeader(req.getHeader()))
                .build();
    }
}
