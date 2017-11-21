package com.tt.ocean.config.cache;

import com.tt.ocean.common.CommonList;

public class TypeCache {
    String      type;

    public CommonList  list;

    public TypeCache(String type){
        this.type = type;
        list = new CommonList();
    }

    public void addContext(ConnContext ctx){
        ctx.clearTypeList();
        list.addChild(ctx.typeList);
    }
}
