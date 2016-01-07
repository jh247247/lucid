package com.lightspeed.gpr.lib;

import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class AbstractPacketParser {
    public boolean parse(ByteBuffer bb) throws IOException {
        boolean status;
        while(bb.hasRemaining()) {
	    status = parse(bb.get());
	    if(status == true) return true;
        }
        return false;
    }

    // Should return true only on packet end, otherwise return false.
    public abstract boolean parse(byte b) throws IOException;
}
