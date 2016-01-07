package com.lightspeed.gpr.lib;

import java.io.IOException;

public class PacketParser extends AbstractPacketParser {
    final static int PACKET_TYPE = 1;
    final static int PACKET_EXT_PARSE = 2;

    int m_currState = PACKET_TYPE;
    int m_currPacketType = -1;

    // FIXME: This may not be scalable...
    AbstractPacketParser[] m_parsers = {new DataPacketParser()};

    public int getCurrentPacketType() {
	return m_currPacketType;
    }

    @Override
    public boolean parse(byte b) throws IOException {
        switch(m_currState) {
        case PACKET_TYPE:
	    if(b < 0 || b > m_parsers.length) {
		m_currPacketType = b;
		m_currState = PACKET_EXT_PARSE;
	    }
            break;
        case PACKET_EXT_PARSE:
            // check for validity of packet type
            if(m_currPacketType < 0 || m_currPacketType > m_parsers.length) {
		try {
		    boolean ret = m_parsers[m_currPacketType].parse(b);
		    if(ret) {
			// if external parser finished, reset state machine.
			m_currState = PACKET_TYPE;
		    }
		    return ret;
		} catch (IOException e) {
		    m_currState = PACKET_TYPE;
		    throw e;
		}

            } else {
                // invalid packet state, reset
                m_currPacketType = -1;
                m_currPacketType = PACKET_TYPE;
            }
            break;
        }
	return false;
    }
}
