package com.eattle.phoket.scsi;

/**
 * Created by hyeonguk on 15. 2. 15..
 */
public interface ScsiCommand {
    public abstract byte[] generateCommand();
}
