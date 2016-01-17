package com.lightspeed.gpr.lib;


import com.annimon.stream.Stream;
import com.google.common.io.BaseEncoding;
import com.lightspeed.gpr.lib.AbstractPacketParser;
import com.lightspeed.gpr.lib.Element;
import java.io.IOException;
import com.migcomponents.migbase64.Base64;

import java.nio.ByteBuffer;
public class DataPacketParser extends AbstractPacketParser {
    public final static int PACKET_START = 1;
    public final static int PACKET_END = 2;
    public final static int PACKET_BPP = 3;
    public final static int PACKET_DATA = 4;

    static final boolean DEBUG = false;

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

    // this method assumes that we are at the start of the packet, just after the type byte.
    // what do we do if we aren't???
    @Override
    public boolean parse(ByteBuffer buf) throws IOException {
        m_dataStart = buf.getShort();
        m_dataEnd = buf.getShort();
        m_dataBpp = buf.get();

        if(DEBUG){
            System.out.println("Start: " + m_dataStart);
            System.out.println("End: " + m_dataEnd);
            System.out.println("Bpp: " + m_dataBpp);
        }

        m_dataLength = (m_dataEnd-m_dataStart)*m_dataBpp;
        int inSize = m_dataLength + ((m_dataLength%3)!=0 ? (3-(m_dataLength%3)):0);
        int b64Size = (inSize/3)*4;
        m_currSampleEncoded = new byte[b64Size];
        if(DEBUG) System.out.println("Packet size: " + m_currSampleEncoded.length);

        buf.get(m_currSampleEncoded);
        byte[] decoded = Base64.decode(m_currSampleEncoded);
        digestBuffer(ByteBuffer.wrap(decoded));

        init();
        m_currState = PACKET_START;
        return true;
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

                // decode bytes, wrap in bytebuffer for decoding
                byte[] decoded = Base64.decode(m_currSampleEncoded);
                ByteBuffer buf = ByteBuffer.wrap(decoded);

                digestBuffer(buf);
                // reset state machine
                init();
                m_currState = PACKET_START;
                return true;
            }
            break;
        }
        return false;
    }

    // this method expects the byte buffer holding the raw decoded data.
    private void digestBuffer(ByteBuffer buf) {
        int[] out = new int[buf.remaining()/m_dataBpp];

        if(DEBUG)System.out.println("Setting element: "+ m_currIndex);

        switch(m_dataBpp) {
        case 1:
            byte[] outb = new byte[buf.remaining()];
            buf.get(outb);
            for(int i = 0; i < outb.length; i++) {
                out[i] = (int)outb[i];
            }

            break;
        case 2:
            short[] outs = new short[buf.remaining()/m_dataBpp];
            buf.asShortBuffer().get(outs);
            for(int i = 0; i < outs.length; i++) {
		out[i] = (int)outs[i];
	    }
	    break;
	case 4:
	    buf.asIntBuffer().get(out);
	    break;
	case 8:
	    double[] outd = new double[buf.remaining()/m_dataBpp];
	    buf.asDoubleBuffer().get(outd);
            for(int i = 0; i < outd.length; i++) {
		out[i] = (int)outd[i];
	    }
	    break;
	default:
	    System.out.println("WARNING: unknown bytes per pixel!");
	    break;
	}
	m_currElement = new Element(m_dataStart,out);
    }

}
