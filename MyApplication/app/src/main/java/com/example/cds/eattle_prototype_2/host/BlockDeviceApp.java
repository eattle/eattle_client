package com.example.cds.eattle_prototype_2.host;

import com.example.cds.eattle_prototype_2.device.BlockDevice;

/**
 * Created by hyeonguk on 15. 2. 20..
 */
public interface BlockDeviceApp {
    public void onConnected(BlockDevice blockDevice);
}