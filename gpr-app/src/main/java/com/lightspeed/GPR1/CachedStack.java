package com.lightspeed.GPR1;

import com.lightspeed.gpr.lib.DataInputInterface;
import com.lightspeed.gpr.lib.Element;

import java.lang.ref.SoftReference;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.lang.Thread;
import java.lang.Runnable;

import android.util.Log;

public class CachedStack<T> {
    List<T> m_hardData;
    int m_hardDataAmount;
    InputRequest m_request;

    boolean m_threadRunning;


    public CachedStack(InputRequest r) {
        // hard data is synced, since it is referenced more often
        m_hardData = Collections.synchronizedList(new ArrayList());
        m_request = r;

    }

    public CachedStack(InputRequest r,
		       int hardAmount) {
        this(r);
        setHardBufferSize(hardAmount);
        Log.d("RECS", "INIT Wanted size: " + m_hardDataAmount);
    }

    public void push(T re) {
        m_hardData.add(0,re);
	setHardBufferSize(m_hardDataAmount);
    }

    public T pop() {
        setHardBufferSize(m_hardDataAmount);
        if(m_hardData.size() > 0) {
            return m_hardData.remove(0);
        }
        return null;
    }

    // set the callback for when we need more info
    public void setInputRequestCallback(InputRequest<T> r) {
        m_request = r;
    }

    public void setHardBufferSize(int size) {
        m_hardDataAmount = size;

        // change hard data list size by amount in a seperate thread.
        if(!m_threadRunning) {

            Thread run = new Thread(new Runnable(){
                    public void run() {
                        int delta = m_hardDataAmount - m_hardData.size();

                        Log.d("RECS", "DELTA: " + delta);
                        if(delta > 0) {
                            moveSoftToHard(delta);
                        } else {
                            moveHardToSoft(-delta);
                        }
                        m_threadRunning = false;
                    }
                });
            run.start();
            m_threadRunning = true;
        }
    }


    private int moveHardToSoft(int amount) {

        amount = Math.min(amount, m_hardData.size());
        for(int i = 0; i < amount; i++) {
            m_hardData.remove(0);
        }
        Log.d("RECS", "removed " + amount + " from hard buffer");
        Log.d("RECS", "Current size: " + m_hardData.size());
        return amount;
    }


    // move the requested amount from the soft buffer to the hard
    // buffer or the amount in the soft buffer, whichever is smaller.
    // return the amount moved
    private int moveSoftToHard(int amount) {

        if(m_request == null) {
            return 0;
        }
        int ret = 0;
        for(int i = 0; i < amount; i++) {
            T re = getNextSoft();
            if(re == null) {
                return ret;
            }
            ret++;
            m_hardData.add(re);
        }
        Log.d("RECS", "Added " + amount + " to hard buffer");
        Log.d("RECS", "Current size: " + m_hardData.size());
        return ret;
    }

    private T getNextSoft() {
        Log.d("RECS", "Getting next soft element");
        return (T)m_request.getOlder(0,m_hardData.size());

    }

    public interface InputRequest<T> {
        // attempt to read from file, given our offset from the end of
        // the hardData list and the total length of the hardData list
        // should return null if at the end of the input
        public T getOlder(int offset, int length);
    }

}
