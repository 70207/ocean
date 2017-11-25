package com.tt.ocean.proto;

public class NodeUtil {

    public static OceanProto.OceanMessage createAuthRsp(OceanProto.OceanMessage req, NodeProto.NodeAuthRsp msg){
        return OceanProto.OceanMessage.newBuilder()
                .setHeader(MessageUtil.getRspHeader(req.getHeader()))
                .setNodeResponse(NodeProto.NodeResponse.newBuilder()
                        .setAuth(msg).build())
                .build();
    }


    public static OceanProto.OceanMessage createAuthReq( NodeProto.NodeAuthReq msg){
        return OceanProto.OceanMessage.newBuilder()
                .setHeader(MessageUtil.getRspHeader(MessageUtil.createHeader()))
                .setNodeRequest(NodeProto.NodeRequest.newBuilder()
                        .setAuth(msg)
                        .build())
                .build();
    }

}
