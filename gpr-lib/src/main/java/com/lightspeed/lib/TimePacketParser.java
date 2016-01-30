package com.lightspeed.gpr.lib;


import com.lightspeed.gpr.lib.AbstractPacketParser;
import java.nio.ByteBuffer;

public class TimePacketParser extends AbstractPacketParser {
    final static int PACKET_LEN = 8;
    int m_currIndex;
    byte[] m_currTimeBuffer;

    long m_prevDecoded;

    TimePacketParser() {
	init();
    }

    void init() {
	m_currIndex = 0;
	m_currTimeBuffer = new byte[PACKET_LEN];
    }

    @Override
    public boolean parse(byte b) {
	m_currTimeBuffer[m_currIndex++] = b;

	if(m_currIndex == PACKET_LEN) {
	    m_prevDecoded = ByteBuffer.wrap(m_currTimeBuffer).getLong();
	    System.out.println("Parsed unix timestamp: " + m_prevDecoded);

	    init();
	    return true;
	}
	return false;
    }
}
