package com.tt.ocean.config.cache;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;

public class ConnCache {

    private static Logger log = LogManager.getLogger(ConnCache.class.getName());

    HashMap<Long, ConnContext> _connMap;


    public ConnCache(){
        _connMap = new HashMap<>();
    }

    public ConnContext createContext(Long conId){
        ConnContext t = getContext(conId);
        if(t != null){
            log.error("create context, but exist before, con id:" + conId);
            return t;
        }

        t = new ConnContext(conId);
        _connMap.put(conId, t);
        return t;
    }

    public ConnContext getContext(Long conId){
        return _connMap.get(conId);
    }

    public void removeContext(Long conId){
        ConnContext t = getContext(conId);
        if(t == null){
            return;
        }

        t.clear();
        _connMap.remove(conId);
    }


}
