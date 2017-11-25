package com.tt.ocean.conf;

import com.tt.ocean.proto.MessageUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Conf {
    private static final Logger log = LogManager.getLogger(Conf.class.getName());



    public void init(int pieceID) throws IllegalArgumentException{
        MessageUtil.init(pieceID);
    }
}
