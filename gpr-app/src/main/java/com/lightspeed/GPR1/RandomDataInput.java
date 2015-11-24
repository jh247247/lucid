package com.lightspeed.GPR1;

import com.lightspeed.gpr.lib.AbstractDataInput;
import com.lightspeed.gpr.lib.Element;

import java.util.ArrayList;
import java.lang.ref.WeakReference;
import java.lang.Math;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Callable;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import java.lang.Thread;
import java.lang.Runnable;

import android.util.Log;



public class RandomDataInput extends AbstractDataInput {
    final int START_ELEMENT = 0;
    final int END_ELEMENT = 255;
    final int MAX_VAL = 255;
    final int ELEMENT_RATE = 50;

    ListeningExecutorService m_executor =
	MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());

    AtomicInteger m_index;
    int m_oldIndex = 0;

    // generator thread
    Thread m_genThread;
    boolean m_genThreadRun;


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
			Log.d("RandomDataInput","New element: " + m_index.addAndGet(1));

                        if(m_elementListener != null) {
                            m_elementListener.onNewElement(null, getCurrentIndex());
                        }
                    }
                }
            };
        m_genThread = new Thread(r);
        m_genThread.start();
    }

    public int getCurrentIndex() {
        return m_index.get();
    }

    public boolean exists(int index) {
        return index >= 0 && index < m_index.get();
    }

    private class ElementGetter implements Callable<Element> {
	int m_index;
	public ElementGetter(int index) {
	    m_index = index;
	}

        @Override
        public Element call() {
	    // this needs fixing...
	    if(!exists(m_index)) {
		Thread.yield();
	    }
            // read in the data here
	    // a real implementation would read from file or cache.
            Element ret = new Element(START_ELEMENT,END_ELEMENT);
            for(int i = START_ELEMENT; i < END_ELEMENT; i++) {
                ret.setSample(i,Math.random()*MAX_VAL);
            }
            return ret;
        }
    }

    // get an older element, probably one from file.
    public ListenableFuture<Element> getElement(int index) {
	return m_executor.submit(new ElementGetter(index));
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
