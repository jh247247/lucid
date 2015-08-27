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
import java.util.concurrent.atomic.AtomicBoolean;
import de.greenrobot.event.EventBus;

public class RenderElementManager implements
                                      DataInputInterface.InputUpdateCallback {
    static final String LOGTAG = "RenderElementManager";
    static final int MAX_CACHE = 1000;
    static final int DEFAULT_MAX_ELEMENTS = 100;

    /**
     * This is the cache for what is "Left" and "Right" of the
     * screen. Has callbacks to the class to actually load past and
     * future from the input. We have to act as a "translator" between
     * these and the input.
     */
    CachedStack<RenderElement> m_olderData;
    CachedStack<RenderElement> m_newerData;

    /**
     * This is a list of whatever is on the screen. "Has" to be passed
     * to the renderer somehow. Should probably be passed in by
     * eventbus.
     * Is that a privacy leak? Idk.
     */
    List<RenderElement> m_currentData;

    /**
     * Amount of elements we should limit the list to.
     */
    int m_maxCurrentData = 0;

    /**
     * The current index for whatever is on the "left" of the screen
     * (i.e the start of the list). Should enforce this somehow...
     */
    int m_currentIndex = 0;


    /**
     * This is where all the data comes in, can come from anything, we
     * really don't care. Should only be ever set by eventbus. MARK
     */

    DataInputInterface m_input;

    // this flag makes the manager keep the data at the start of the input
    boolean m_startLock;

    public RenderElementManager() {
	m_maxCurrentData = DEFAULT_MAX_ELEMENTS;
	m_input = null;

        m_olderData = new CachedStack<RenderElement>(new
                                                     olderInputRequest(), MAX_CACHE);
        m_currentData = Collections.synchronizedList(new LinkedList<RenderElement>());
        m_newerData = new CachedStack<RenderElement>(new
                                                     newerInputRequest(),
                                                     MAX_CACHE);

        // FIXME: Should have this handled externally...

        m_startLock = false;

        // TODO: Remember to unregister...
        EventBus.getDefault().register(this);
    }

    // FIXME: Deprecated! Get rid of this entirely.
    public RenderElementBlitter getBlitter() {
        return new RenderElementBlitter(m_currentData, m_maxCurrentData);
    }

    /**
     * Get the maximum current element length shown on the screen.
     */
    public int getMaxElementLength() {
        int maxCurrentElementLength = 0;
        for (RenderElement re : m_currentData) {
            maxCurrentElementLength = Math.max(re.getElementHeight(),
                                               maxCurrentElementLength);
        }
        return maxCurrentElementLength;
    }

    /**
     * Update whatever is displayed on the screen, should probably be
     * made more modular. MARK
     */
    public void updateInput() {
        if(m_input == null) {
            return;
        }
        int deltaIndex = m_input.getCurrentIndex() - m_currentIndex;

        // make sure the screen is full of data
        synchronized(m_currentData) {
            if(m_startLock && // following data
               deltaIndex != 0 && // new data
               m_currentData.size() == m_maxCurrentData) { // screen full
                m_currentIndex += moveNewerToCurrent(deltaIndex);
            }
            if(m_currentData.size() < m_maxCurrentData) {
                moveNewerToCurrent(m_maxCurrentData - m_currentData.size());
            }
            if(m_currentData.size() > m_maxCurrentData) {
                moveCurrentToOlder(m_currentData.size() - m_maxCurrentData);
            }
        }
    }

    /**
     * This changes the "viewport" of the renderer.
     *
     * Positive offset shows older data, negative newer data.
     * Returns the successful amount of offset applied.
     */
    public synchronized int moveCurrent(int offset) {
        updateInput();

        Log.d(LOGTAG, "moveCurrent: move " + offset + " elements");
        synchronized(m_currentData) {
            if(offset < 0) {
                offset = moveNewerToCurrent(-offset);
                offset = moveCurrentToOlder(offset);
                m_currentIndex += offset;
            } else {
                offset = moveOlderToCurrent(offset);
                offset = moveCurrentToNewer(offset);
                m_currentIndex -= offset;
            }
        }
        m_currentIndex = Math.max(0, m_currentIndex);
        Log.d(LOGTAG, "Current index: " + m_currentIndex);
        return offset;
    }

    /**
     * Next 4 functions should really be made so that we can move from
     * X to Y since we pretty much have 2 copies of 2 functions, which
     * kinda grates on me right now.
     */
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
            re = m_newerData.pop();
            if(re == null) { // no more elements in older
                return i;
            }
            m_currentData.add(m_currentData.size(),
                              re);
        }
        return amount;
    }


    /**
     * Set the input to a new data stream. Is it worth is making new
     * objects for the caches? Simplest solution, I guess the simplest
     * solution is the best.
     */
    public void setDataInput(DataInputInterface in) {
        m_input = in;

        // clean out data from old interface
        // TODO: fix magic numbers
        m_olderData = new CachedStack<RenderElement>(new
                                                     olderInputRequest(), MAX_CACHE);
        m_newerData = new CachedStack<RenderElement>(new
                                                     newerInputRequest(), MAX_CACHE);

        m_currentData.clear();
        m_currentIndex = 0;

        if(m_input != null) {
            m_input.setUpdateCallback(this);
        }
    }

    /**
     * Set the maximum amount of data to show on the screen.
     */
    public void setMaxCurrentData(int max){
        m_maxCurrentData = max;
        // TODO: communicate new maximum with blitter...

        //m_blitter.setMaxElements(max);
    }

    public int getMaxCurrentData() {
        return m_maxCurrentData;
    }

    // passing in true to this sets the manager to lock the renderer
    // to the start of the incoming data
    public void setStartLock(boolean lock){
        m_startLock = lock;
    }

    public boolean getStartLock() {
        return m_startLock;
    }

    /**
     * Callbacks for getting past and future elements from the input.
     * TODO: Elements are currently rendered as they come in, is there
     * a better (faster) way to do this/
     */
    private class newerInputRequest implements CachedStack.InputRequest<RenderElement> {
        public RenderElement getOlder(int offset, int length) {
            // input not defined, return null
            if(m_input == null) {
                return null;
            }


            Element e = m_input.getPrevious(m_currentIndex +
                                            m_currentData.size() +
                                            length +
                                            offset + 1);

            if(e != null) {
                RenderElement re = new RenderElement(e);
                re.renderElement();
                return re;
            }
            return null;
        }
    }

    private class olderInputRequest implements CachedStack.InputRequest<RenderElement> {
        public RenderElement getOlder(int offset, int length) {
            // input not defined, return null
            if(m_input == null) {
                return null;
            }

            // TODO: render?
            Element e = m_input.getPrevious(m_currentIndex - length -
                                            offset - 1);
            if(e != null) {
                RenderElement re = new RenderElement(e);
                re.renderElement();
                return re;
            }
            return null;
        }
    }

    // input changed! set via our handy function...
    public void
    onEventBackgroundThread(DataInputFragment.InputChangeEvent e) {
	if(e.input != m_input) {
	    setDataInput(e.input);
	    updateInput();
	}
    }

    public void
        onEventBackgroundThread(RenderView.SurfaceScrolledEvent e) {
	moveCurrent(e.dX);
    }

}
