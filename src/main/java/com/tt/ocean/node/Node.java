package com.tt.ocean.node;

import com.tt.ocean.proto.ConfigProto;
import com.tt.ocean.service.NodeService;

public class Node implements NodeService {


    public Node(){

    }

    @Override
    public void onConnected(Long conId) {

    }

    @Override
    public void onDisconnected(Long conId) {

    }

    @Override
    public void onAuthed(Long conId, ConfigProto.ConfigMessage msg) {

    }

    @Override
    public void onNodeNotify(Long conId, ConfigProto.ConfigMessage msg) {

    }

    @Override
    public void onNodeGot(Long conId, ConfigProto.ConfigMessage msg) {

    }
}
