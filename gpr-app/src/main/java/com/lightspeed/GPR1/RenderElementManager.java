package com.lightspeed.GPR1;

import com.lightspeed.gpr.lib.DataInputInterface;
import com.lightspeed.gpr.lib.Element;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.lang.ref.SoftReference;
import java.lang.Math;
import android.util.Log;
import java.util.Collections;

public class RenderElementManager {
    static final String LOGTAG = "RenderElementManager";

    RenderElementBlitter m_blitter;

    RenderElementCachedStack m_olderData;
    RenderElementCachedStack m_newerData;

    /**
     * These keep track of the data that is current and newer than the
     * current. Strong references since we really don't want to lose this data.
     */
    List<RenderElement> m_currentData;
    int m_maxCurrentData = 0;
    int m_currentIndex = 0;


    /**
     * This is where all the data comes in, can come from anything, we
     * really don't care.
     */

    DataInputInterface m_input;

    public RenderElementManager() {
        m_input = null;

        m_olderData = new RenderElementCachedStack(new olderInputRequest());
        m_currentData = Collections.synchronizedList(new LinkedList<RenderElement>());
        m_newerData = new RenderElementCachedStack(new newerInputRequest());

        m_blitter = new RenderElementBlitter(m_currentData);
    }

    public RenderElementManager(int maxCurrentData) {
        this();
        setMaxCurrentData(maxCurrentData);

    }

    public RenderElementManager(DataInputInterface in,
                                int maxCurrentData) {
        this(maxCurrentData);
        setDataInput(in);
    }

    public RenderElementBlitter getBlitter() {
        return m_blitter;
        // FIXME: privacy leak
    }

    public int getMaxElementLength() {
        int maxCurrentElementLength = 0;
        for (RenderElement re : m_currentData) {
            maxCurrentElementLength = Math.max(re.getElementHeight(),
                                               maxCurrentElementLength);
        }
        return maxCurrentElementLength;
    }

    public void updateInput() {
        if(m_input == null) {
            return;
        }
        int index = m_input.getCurrentIndex()-m_currentData.size();
        if(index > m_currentIndex) {
            m_currentIndex += moveNewerToCurrent(index-m_currentIndex);
        }
        if(m_currentData.size() > m_maxCurrentData) {
            moveCurrentToOlder(m_currentData.size() - m_maxCurrentData);
        }
    }

    /**
     * This changes the "viewport" of the renderer.
     *
     * Positive offset shows older data, negative newer data.
     * Returns the successful amount of offset applied.
     */
    public synchronized int moveCurrent(int offset) {
        Log.d(LOGTAG, "moveCurrent: move " + offset + " elements");
        if(offset < 0) {
            offset = moveNewerToCurrent(-offset);
            offset = moveCurrentToOlder(-offset);
        } else {
            offset = moveOlderToCurrent(offset);
            offset = moveCurrentToNewer(offset);
        }
        m_currentIndex = Math.max(0, m_currentIndex - offset);
        Log.d(LOGTAG, "Current index: " + m_currentIndex);
        return offset;
    }

    private synchronized int moveCurrentToNewer(int amount) {
        Log.d(LOGTAG, "Attempt to move " + amount + " elements to newer");
        amount = Math.min(amount, m_currentData.size());
        if(amount <= 0) {
            return 0;
        }

        // since it is a stack, add backwards
        for(int i = 0; i < amount; i++) {
            m_newerData.push(m_currentData.remove(m_currentData.size()-1));
        }

        return amount;
    }

    private synchronized int moveCurrentToOlder(int amount) {
        Log.d(LOGTAG, "Attempt to move " + amount + " elements to older");
        amount = Math.min(amount, m_currentData.size());
        if(amount <= 0) {
            return 0;
        }

        for(int i = 0; i < amount; i++) {
            m_olderData.push(m_currentData.remove(0));
        }
        return amount;
    }

    private synchronized int moveOlderToCurrent(int amount) {
        RenderElement re = null;
        for(int i = 0; i < amount; i++) {
            re = m_olderData.pop();
            if(re == null) { // no more elements in older
                return i;
            }
            m_currentData.add(0, re);
        }
        return amount;
    }

    private synchronized int moveNewerToCurrent(int amount) {
        RenderElement re = null;
        for(int i = 0; i < amount; i++) {
            re = m_olderData.pop();
            if(re == null) { // no more elements in older
                return i;
            }
            m_currentData.add(m_currentData.size(),
                              re);
        }
        return amount;
    }

    public void setDataInput(DataInputInterface in) {
        m_input = in;
    }

    public void setMaxCurrentData(int max){
        m_maxCurrentData = max;
        m_blitter.setMaxElements(max);
        m_newerData.setHardBufferSize(max);
        m_olderData.setHardBufferSize(max);
    }

    public int getMaxCurrentData() {
        return m_maxCurrentData;
    }

    private class newerInputRequest implements RenderElementCachedStack.InputRequest {
        public Element getOlder(int offset, int length) {
            // input not defined, return null
            if(m_input == null) {
                return null;
            }

            return m_input.getPrevious(m_currentIndex - length - offset);
        }
    }

    private class olderInputRequest implements RenderElementCachedStack.InputRequest {
        public Element getOlder(int offset, int length) {
            // input not defined, return null
            if(m_input == null) {
                return null;
            }

            return m_input.getPrevious(m_currentIndex +
                                       m_currentData.size() +
                                       length +
                                       offset);
        }
    }
}
