package com.tt.ocean.config;

import com.tt.ocean.proto.ConfigProto;
import com.tt.ocean.service.ConfigService;

public class Config implements ConfigService {
    public Config(){

    }



    @Override
    public void onConnected(Long conId) {

    }

    @Override
    public void onDisconnected(Long conId) {

    }

    @Override
    public void onAuthing(Long conId, ConfigProto.ConfigMessage msg) {

    }

    @Override
    public void onGetNodes(Long conId, ConfigProto.ConfigMessage msg) {

    }
}
