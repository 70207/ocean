package com.tt.ocean.config.cache;

import com.tt.ocean.common.CommonList;

public class ConnContext {
    public Long     conId;
    public boolean  authed;


    public CommonList       typeList;
    public String           addr;
    public int              port;
    public int              pieceID;
    public long             prsID;
    public int              version;

    private int             reqTimes;
    private String          type;

    public ConnContext(Long conId){
        reqTimes = 0;
        this.conId = conId;
        authed = false;
        typeList = new CommonList();
        typeList.setObject(this);
    }

    public int getReqTimes() {
        return reqTimes;
    }

    public void incrReqTimes() {
        reqTimes++;
    }

    public void auth(String type){
        this.authed = true;
        this.type = type;
    }

    public void clearTypeList(){
        typeList.removeFromParent();
    }


    public void clear(){
        typeList.removeFromParent();
    }

    public String getType(){
        return type;
    }


}
