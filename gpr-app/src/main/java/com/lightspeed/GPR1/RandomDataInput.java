package com.lightspeed.GPR1;

import com.lightspeed.gpr.lib.DataInputInterface;
import com.lightspeed.gpr.lib.Element;
import java.util.ArrayList;
import java.lang.ref.WeakReference;
import java.lang.Math;
import java.util.concurrent.atomic.AtomicInteger;

import java.lang.Thread;
import java.lang.Runnable;

import android.util.Log;

public class RandomDataInput implements DataInputInterface{
    AtomicInteger m_index;
    int m_oldIndex = 0;

    // generator thread
    Thread m_genThread;
    boolean m_genThreadRun;


    final int START_ELEMENT = 0;
    final int END_ELEMENT = 255;
    final int MAX_VAL = 255;
    final int ELEMENT_RATE = 100;


    public RandomDataInput() {
        m_index = new AtomicInteger(0);

	// setup thread
	m_genThreadRun = true;
	Runnable r = new Runnable() {
		public void run() {
		    while(m_genThreadRun) {
			try {
			    Thread.sleep(1000/ELEMENT_RATE);
			} catch(Exception e) {
			    // TODO: handle?
			}
			m_index.addAndGet(1);
		    }
		}
	    };
        m_genThread = new Thread(r);
        m_genThread.start();
    }

    public int getCurrentIndex() {
        return m_index.get();
    }

    public boolean hasNext() {
        boolean ret = m_index.get() == m_oldIndex;
        m_oldIndex = m_index.get();
        return !ret;
    }

    public Element getNext(){
        Element ret = new Element(START_ELEMENT,END_ELEMENT);
        for(int i = START_ELEMENT; i < END_ELEMENT; i++) {
            ret.setSample(i,Math.random()*MAX_VAL);
        }
        return ret;
    }

    public boolean exists(int index) {
	return index > 0 && index < m_index.get();
    }

    // get an older element, probably one from file.
    public Element getElement(int index) {
	if(!exists(index)) {
            return null;
        }

        // would load it from file, but cbf
        Element ret = ret = new Element(START_ELEMENT,END_ELEMENT);
        for(int i = START_ELEMENT; i < END_ELEMENT; i++) {
            ret.setSample(i,Math.random()*MAX_VAL);
        }
        return ret;
    }

    public boolean open() {
        return true;
    }

    public void close() {
        m_genThreadRun = false;
        while(m_genThread != null) {
            try {
                m_genThread.join();
		m_genThread = null;
            }
            catch(InterruptedException e) {
                // TODO:
            }
        }
        Log.v("RANDOM","Random data thread stopped");
	return;
    }

    public String getName() {
        return "Random Data";
    }
}
