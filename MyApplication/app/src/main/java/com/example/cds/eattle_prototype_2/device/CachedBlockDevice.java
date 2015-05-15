package com.example.cds.eattle_prototype_2.device;

/**
 * Created by hyeonguk on 15. 5. 8..
 */
public interface CachedBlockDevice {
    public abstract void readBlock(int lba, byte[] buffer);

    public abstract void writeBlock(int lba, byte[] buffer);

    public abstract long getLastLogicalBlockAddress();

    public abstract long getBlockLength();

    public abstract void flush();
}
