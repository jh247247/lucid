package com.lightspeed.gpr.lib;


import com.google.common.io.BaseEncoding;
import com.lightspeed.gpr.lib.AbstractPacketParser;
import com.lightspeed.gpr.lib.Element;
import java.io.IOException;

import java.nio.ByteBuffer;
public class DataPacketParser extends AbstractPacketParser {
    public final static int PACKET_START = 1;
    public final static int PACKET_END = 2;
    public final static int PACKET_BPP = 3;
    public final static int PACKET_DATA = 4;

    static final boolean DEBUG = true;

    // Current packet decoding state
    int m_currState;

    // element attributes
    int m_dataLength;
    int m_dataStart;
    int m_dataEnd;
    byte m_dataBpp;

    // what sample we are currently setting
    int m_currIndex;
    Element m_currElement;

    // current sample attributes
    int m_currEncodedIndex;
    byte[] m_currSampleEncoded;
    int m_encodedBufferSize;

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

    // return how much we actually expect to read in the next batch.
    public int getNextReadSize() {
        if(m_currSampleEncoded != null) {
            return m_currSampleEncoded.length - m_currEncodedIndex +1;
        }
        return 1;
    }

    public Element getDecodedElement() {
        return m_currElement;
    }

    public int getCurrentState() {
        return m_currState;
    }

    @Override
    public boolean parse(byte b) throws IOException {
        if(DEBUG) System.out.println("Passed in: " + b);

        switch(m_currState) {
            // attempt to read
        case PACKET_START:
            if(m_dataStart == -1) {
                m_dataStart = (b<<8);
            } else {
                m_dataStart = m_dataStart + b;
                m_currState = PACKET_END;
                if(DEBUG) System.out.println("Packet starts at sample: " + m_dataStart);
            }
            break;

        case PACKET_END:
            if(m_dataEnd == -1) {
                m_dataEnd = (b<<8);
            } else {
                m_dataEnd = m_dataEnd + b;
                if(m_dataEnd < m_dataStart) {
                    m_currState = PACKET_START;
                    throw new IOException("Element end value smaller than start value!");
                }
                m_currState = PACKET_BPP;
                if(DEBUG)System.out.println("Packet ends at sample: " + m_dataEnd);
            }

            break;

        case PACKET_BPP:
            m_dataBpp = b;
            m_dataLength = (m_dataEnd-m_dataStart)*m_dataBpp;

            // calculate base64 length, based on binary length
            int inSize = m_dataLength + ((m_dataLength%3)!=0 ? (3-(m_dataLength%3)):0);
            int b64Size = (inSize/3)*4;
            m_currSampleEncoded = new byte[b64Size];
            m_currState = PACKET_DATA;
            if(DEBUG)System.out.println("Packet contains " + m_dataBpp + " bytes per sample");

            break;

        case PACKET_DATA:
            if(m_currEncodedIndex < m_currSampleEncoded.length) {
                if(DEBUG)System.out.println("Reading sample " +
                                            (m_currEncodedIndex+1)+"out of"+
                                            m_currSampleEncoded.length);
                m_currSampleEncoded[m_currEncodedIndex++] = b;
            }
            // at final byte, decode.
            if(m_currEncodedIndex == m_currSampleEncoded.length) {
                if(DEBUG)System.out.println("Decoding: " + new String(m_currSampleEncoded));

                m_currElement = new Element(m_dataStart,m_dataEnd);

                // decode bytes, wrap in bytebuffer for decoding
                byte[] decoded = BaseEncoding.base64().decode(new String(m_currSampleEncoded));

                ByteBuffer buf = ByteBuffer.wrap(decoded);

                while(buf.hasRemaining() && m_currIndex < m_dataLength) {
                    if(DEBUG)System.out.println("Setting element: "+ m_currIndex);

                    switch(m_dataBpp) {
                    case 1:
                        m_currElement.setSample(m_currIndex++, buf.get());
                        break;
                    case 2:
                        m_currElement.setSample(m_currIndex++, buf.getShort());
                        break;
                    case 4:
                        m_currElement.setSample(m_currIndex++, buf.getInt());
                        break;
                    case 8:
                        m_currElement.setSample(m_currIndex++, buf.getDouble());
                        break;
		    default:
			System.out.println("WARNING: unknown bytes per pixel!");
			break;
                    }
                }

                // reset state machine
                init();
                m_currState = PACKET_START;
                return true;
            }
            break;
        }
        return false;
    }
}
