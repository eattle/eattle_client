package com.example.cds.eattle_prototype_2.device;

/**
 * Created by hyeonguk on 15. 2. 19..
 */
public interface UsbSerialDevice {

    public abstract void write(byte[] data);

    public abstract void read(byte[] data);
}