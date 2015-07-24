package com.lightspeed.GPR1;

import com.lightspeed.gpr.lib.DataInputInterface;
import com.lightspeed.gpr.lib.Element;

import java.lang.ref.SoftReference;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.lang.Thread;
import java.lang.Runnable;

public class RenderElementCachedStack {
    List<RenderElement> m_hardData;
    int m_hardDataAmount;
    InputRequest m_request;

    public RenderElementCachedStack(InputRequest r) {
        // make everything soft because we aren't given a value
        this(r,0);
    }

    public RenderElementCachedStack(InputRequest r,
                                    int hardAmount) {
        m_hardDataAmount = hardAmount;
        // hard data is synced, since it is referenced more often
        m_hardData = Collections.synchronizedList(new ArrayList());
        m_request = r;
    }

    public void push(RenderElement re) {
        setHardBufferSize(m_hardDataAmount);
        m_hardData.add(0,re);
        moveHardToSoft(1);
    }

    public RenderElement pop() {
        setHardBufferSize(m_hardDataAmount);
        moveSoftToHard(1);
        if(m_hardData.size() > 0) {
            return m_hardData.remove(0);
        }
        return null;
    }

    public void setInputRequestCallback(InputRequest r) {
	m_request = r;
    }

    public void setHardBufferSize(int size) {
        final int delta = size - m_hardData.size();
        m_hardDataAmount = size;

        // change hard data list size by amount in a seperate thread.
        Runnable r = new Runnable() {
                public void run() {
                    if(delta > 0) {
                        moveSoftToHard(delta);
                    } else {
                        moveHardToSoft(delta);
                    }
                }
            };
    }


    private int moveHardToSoft(int amount) {
        amount = Math.min(amount, m_hardData.size());
        for(int i = 0; i < amount; i++) {
            m_hardData.remove(0);
        }
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
            RenderElement re = getNextSoft();
            if(re == null) {
                return ret;
            }
            ret++;
            re.renderElement();
            m_hardData.add(re);
        }
        return ret;
    }

    private RenderElement getNextSoft() {
        // TODO: log
        Element e = m_request.getOlder(0,m_hardData.size());
        if(e != null) {
	    return new RenderElement(e);
        }
	return null;
    }

    public interface InputRequest {
        // attempt to read from file, given our offset from the end of
        // the hardData list and the total length of the hardData list
        // should return null if at the end of the input
        public Element getOlder(int offset, int length);
    }
}
