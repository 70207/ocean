package com.tt.ocean.config.cache;

import com.tt.ocean.common.CommonList;

public class TypeCache {
    String      type;

    public CommonList  list;
    public CommonList  listForSubscribe;

    public TypeCache(String type){
        this.type = type;
        list = new CommonList();
        listForSubscribe = new CommonList();
    }

    public void addContext(ConnContext ctx){
        ctx.clearTypeList();
        list.addChild(ctx.typeList);
    }

    public void addSubscribe(ConnContext ctx){
        ctx.clearSubscribeList();
        listForSubscribe.addChild(ctx.listInSubscribe);
    }
}
