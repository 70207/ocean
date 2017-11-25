package com.tt.ocean.proto;

import com.sun.org.apache.xml.internal.security.utils.resolver.ResourceResolverException;
import com.tt.ocean.proto.MessageProto.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.lang.management.ManagementFactory;
import java.util.ResourceBundle;

public class MessageUtil {

    private static long  __reqID = 0;
    private static int   __prsID = 0;
    private static int   __pieceID = 0;
    private static int   __version = 0;


    private static Logger log = LogManager.getLogger(MessageUtil.class.getName());


    public static void init(int pieceID){
        __prsID =  Integer.valueOf(ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
        ResourceBundle bundle = ResourceBundle.getBundle("config");
        if(bundle == null){
            throw new IllegalArgumentException("config.properties not found");
        }

        String version = bundle.getString("ocean.version");
        if(version != null &&  !version.isEmpty()){
            __version = Integer.valueOf(version).intValue();
        }

        __pieceID = pieceID;
    }

    public static long incrReqID(){
        return ++__reqID;
    }

    public static Header getRspHeader(Header req){
        return Header.newBuilder()
                .setReqId(incrReqID())
                .setPieceId(__pieceID)
                .setPrsId(__prsID)
                .setVersion(__version)
                .setRspId(req.getReqId())
                .build();
    }


    public static Header createHeader(){
        return Header.newBuilder()
                .setReqId(incrReqID())
                .setPieceId(__pieceID)
                .setPrsId(__prsID)
                .setVersion(__version)
                .setRspId(0)
                .build();
    }
}
