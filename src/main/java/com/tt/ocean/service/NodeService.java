package com.tt.ocean.service;

import com.tt.ocean.proto.ConfigProto.ConfigMessage;

public interface NodeService {

    public void onConnected(Long conId);
    public void onDisconnected(Long conId);


    public void onAuthed(Long conId, ConfigMessage msg);
    public void onNodeNotify(Long conId, ConfigMessage msg);
    public void onNodeGot(Long conId, ConfigMessage msg);
}
