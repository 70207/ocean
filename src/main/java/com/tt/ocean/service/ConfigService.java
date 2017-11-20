package com.tt.ocean.service;

import com.tt.ocean.proto.ConfigProto.ConfigMessage;

public interface ConfigService {

    public void onConnected(Long conId);
    public void onDisconnected(Long conId);
    public void onAuthing(Long conId, ConfigMessage msg);
    public void onGetNodes(Long conId, ConfigMessage msg);

}
