package com.tt.ocean.common;

public class CommonList {
    public Object     object;

    public CommonList pre;
    public CommonList next;

    public CommonList(){
        object = null;
        pre = this;
        next = this;
    }

    public void addChild(CommonList list){
        list.pre = this;
        list.next.pre = list;

        list.next = this.next;
        this.next = list;
    }

    public void removeFromParent(){
        pre.next = next;
        next.pre = pre;
        pre = null;
        next = null;
    }

    public void setObject(Object object){
        this.object = object;
    }
}
