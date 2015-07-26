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

    InputUpdateCallback m_callback;


    final int START_ELEMENT = 0;
    final int END_ELEMENT = 255;
    final int MAX_VAL = 255;
    final int ELEMENT_RATE = 100;


    public RandomDataInput() {
        m_index = new AtomicInteger(0);
        Runnable r = new Runnable() {
                public void run() {
                    while(true) {
                        try {
                            Thread.sleep(1000/ELEMENT_RATE);
                        } catch(Exception e) {
                            // TODO: handle?
                        }
                        Log.d("RANDOM","generating element, index: " +
                              m_index.get());
                        m_index.addAndGet(1);
                        if(m_callback != null) {
                            m_callback.updateInput();
                        }

                    }
                }
            };
        new Thread(r).start();
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

    // get an older element, probably one from file.
    public Element getPrevious(int offset) {
        if(offset < 0 || offset > m_index.get()) {
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
        return;
    }

    public void setUpdateCallback(DataInputInterface.InputUpdateCallback call) {
        m_callback = call;
    }

    public String getName() {
        return "Random Data";
    }
}
