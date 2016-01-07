package com.lightspeed.gpr.lib;


import com.google.common.io.BaseEncoding;
import com.lightspeed.gpr.lib.AbstractPacketParser;
import com.lightspeed.gpr.lib.Element;
import java.io.IOException;

import java.nio.ByteBuffer;
public class DataPacketParser extends AbstractPacketParser {
    final static int PACKET_START = 1;
    final static int PACKET_END = 2;
    final static int PACKET_BPP = 3;
    final static int PACKET_DATA = 4;

    // Current packet decoding state
    int m_currState;

    // element attributes
    int m_dataLength;
    int m_dataStart;
    int m_dataEnd;
    int m_dataBpp;

    // what sample we are currently setting
    int m_currIndex;
    Element m_currElement;

    // current sample attributes
    int m_currEncodedIndex;
    byte[] m_currSampleEncoded;

    public DataPacketParser() {
        init();
    }

    private void init() {
        m_currState = PACKET_START;
        m_dataLength = -1;
        m_dataStart = -1;
        m_dataEnd = -1;
        m_dataBpp = -1;

        m_currIndex = 0;
        m_currEncodedIndex = 0;
        m_currSampleEncoded = null;
    }

    public boolean hasPacketSize() {
	if(m_)
    }

    @Override
    public boolean parse(byte b) throws IOException {
        switch(m_currState) {
            // attempt to read
        case PACKET_START:
            if(m_dataStart == -1) {
                m_dataStart = b;
            } else {
                m_dataStart = (m_dataStart<<8) + b;
                m_currState = PACKET_END;
            }
            break;

        case PACKET_END:
            if(m_dataStart == -1) {
                m_dataEnd = b;
            } else {
                m_dataEnd = m_dataEnd + (b<<8);
                if(m_dataEnd < m_dataStart) {
		    m_currState = PACKET_START;
                    throw new IOException("Element end value smaller than start value!");
                }
                m_currState = PACKET_BPP;
            }
            m_currElement = new Element(m_dataStart,m_dataEnd);
            break;

        case PACKET_BPP:
            m_dataBpp = b;
            m_dataLength = (m_dataEnd-m_dataStart)*m_dataBpp;

            // calculate base64 length, based on binary length
            int inSize = m_dataLength + ((m_dataLength%3)!=0 ? (3-(m_dataLength%3)):0);
            int b64Size = (inSize/3)*4;
            m_currSampleEncoded = new byte[b64Size];

            m_currState = PACKET_DATA;
            break;

        case PACKET_DATA:
            if(m_currEncodedIndex < m_currSampleEncoded.length) {
		m_currSampleEncoded[m_currEncodedIndex++] = b;
	    } else {
		// decode bytes, wrap in bytebuffer for decoding
		byte[] decoded = BaseEncoding.base64().decode(new String(m_currSampleEncoded));
		ByteBuffer buf = ByteBuffer.wrap(decoded);

		while(buf.hasRemaining() && m_currIndex < m_dataLength) {
		    switch(m_dataBpp) {
		    case 1:
			m_currElement.setSample(m_currIndex, buf.get());
		    case 2:
			m_currElement.setSample(m_currIndex, buf.getShort());
		    case 4:
			m_currElement.setSample(m_currIndex, buf.getInt());
		    case 8:
			m_currElement.setSample(m_currIndex, buf.getDouble());
		    }
		}

		// reset state machine
		init();
		m_currState = PACKET_START;
	    }
	    break;
	}
	return false;
    }
}
