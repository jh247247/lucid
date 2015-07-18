package com.lightspeed.GPR1;

import com.lightspeed.gpr.lib.DataInputInterface;
import com.lightspeed.gpr.lib.Element;
import java.util.ArrayList;
import java.lang.ref.SoftReference;
import java.lang.Math;
import io.palaima.smoothbluetooth.SmoothBluetooth;
import android.content.Context;

public class BluetoothDataInput implements DataInputInterface {
    ArrayList<SoftReference<Element>> m_previous;
    ArrayList<Element> m_new;
    // TODO: write to file

    public BluetoothDataInput() {
        m_previous = new ArrayList<SoftReference<Element>>();
        m_new = new ArrayList<Element>();
    }

    public boolean hasNext() {
        return !m_new.isEmpty();
    }

    public Element getNext() {
        // fifo, so remove from start
	if(m_new.size() > 0) {
	    return m_new.remove(0);
	}
	return null;
    }

    private void trimPrevious() {
        while(m_previous.size() > 0 && // still have elements
              m_previous.get(m_previous.size()-1).get() == null) { // start element is expired
            m_previous.remove(m_previous.size()-1);
        }
    }


    public Element getPrevious(int offset) {
        trimPrevious();
        Element ret = null;
        if(offset < 0) {
            return null; // cannot grab from future...
        }

        // definitely not stored. have to reload... (FIXME)
        if(offset > m_previous.size()-1 ||
           m_previous.get(offset).get() == null) {
            // would load it from file, but cbf generate random var.
            ret = new Element(0,255);
            for(int i = 0; i < 255; i++) {
                ret.setSample(i,Math.random()*255);
            }
            return ret;
        }

        // woo! we still have it! return it.
        return m_previous.get(offset).get();
    }



    public boolean open() {
	return true;
    }

    public void close() {

    }

    public String getName() {
        return "Bluetooth"; // TODO: make this settable in xml
    }
}
