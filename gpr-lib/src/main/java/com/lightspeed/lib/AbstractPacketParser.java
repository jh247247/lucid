package com.lightspeed.gpr.lib;

import java.io.IOException;

public abstract class AbstractPacketParser {
    // Should return true only on packet end, otherwise return false.
    public abstract boolean parse(byte b) throws IOException;
}
