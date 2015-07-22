package com.lightspeed.GPR1;

import com.lightspeed.gpr.lib.DataInputInterface;
import com.lightspeed.gpr.lib.Element;
import java.util.ArrayList;
import java.lang.ref.SoftReference;
import java.lang.Math;
import android.util.Log;
import android.view.View.OnTouchListener;

public class RenderElementManager {
    static final String LOGTAG = "RenderElementManager";

    RenderElementBlitter m_blitter;

    /**
     * This keeps hold of the data before the current screen
     * Should be garbage collected since people don't generally scroll
     * back too often.
     */
    ArrayList<SoftReference<RenderElement>> m_olderData;

    /**
     * These keep track of the data that is current and newer than the
     * current. Strong references since we really don't want to lose this data.
     */
    ArrayList<RenderElement> m_currentData;
    int m_maxCurrentData = 0;
    ArrayList<RenderElement> m_newerData;
    int m_maxNewerData = 0;

    /**
     * This is where all the data comes in, can come from anything, we
     * really don't care.
     */

    DataInputInterface m_input;

    public RenderElementManager() {
        m_input = null;

        m_olderData = new ArrayList<SoftReference<RenderElement>>();
        m_currentData = new ArrayList<RenderElement>();
        m_newerData = new ArrayList<RenderElement>();

        m_blitter = new RenderElementBlitter(m_currentData);
    }

    public RenderElementManager(int maxCurrentData,
                                int maxNewerData) {
        this();
        setMaxCurrentData(maxCurrentData);
        setMaxNewerData(maxNewerData);
    }

    public RenderElementManager(DataInputInterface in,
                                int maxCurrentData,
                                int maxNewerData) {
        this(maxCurrentData, maxNewerData);
        m_input = in;
    }

    public void updateInput() {
        if(m_input == null) {
            return;
        }
        while(m_input.hasNext()) {
            Log.v(LOGTAG,"Getting another element...");
            m_newerData.add(new RenderElement(m_input.getNext()));

            // check limitations
            if(m_newerData.size() > m_maxNewerData) {
                moveNewerToCurrent(1);
            }
            if(m_currentData.size() > m_maxCurrentData) {
                moveCurrentToOlder(1);
            }
        }
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

    /**
     * This changes the "viewport" of the renderer.
     *
     * Positive offset shows older data, negative newer data.
     * Returns the successful amount of offset applied.
     */
    public int moveCurrent(int offset) {
        if(offset < 0) {
            offset = moveNewerToCurrent(-offset);
            offset = moveCurrentToOlder(-offset);
            m_maxNewerData -= offset;
        } else {
            offset = moveOlderToCurrent(offset);
            offset = moveCurrentToNewer(offset);
            m_maxNewerData -= offset;
        }
        return offset;
    }

    private int moveCurrentToNewer(int amount) {
        // get the amount to actually move
        amount = Math.min(amount, m_currentData.size());

        // move sublist at end of current of length amount to start of newer
        m_newerData.addAll(0, m_currentData.subList(m_currentData.size() - amount,
                                                    m_currentData.size()));
        m_currentData.subList(m_currentData.size() - amount,
                              m_currentData.size()).clear();
        return amount;
    }

    private int moveNewerToCurrent(int amount) {
        // get the amount to actually move
        amount = Math.min(amount, m_newerData.size());

        // move sublist from start of newer of length amount to end of current
        m_currentData.addAll(m_currentData.size(), m_newerData.subList(0, amount));
        m_newerData.subList(0, amount).clear();

        // too many elements to render!
        if(m_currentData.size() > m_maxCurrentData) {
            moveCurrentToOlder(m_currentData.size()-m_maxCurrentData);
        }

        // return amount actually moved
        return amount;

    }

    private int moveOlderToCurrent(int amount) {
        int ret = 0;
        for(int i = 0; i < amount; i++) {
            RenderElement re = getNextOlder();
            if(re == null) {
                return ret;
            }
            ret++;
            m_currentData.add(0,re);
        }

        return amount;
    }

    private int moveCurrentToOlder(int amount) {
        amount = Math.min(amount, m_currentData.size());
        for(int i = 0; i < amount; i++) {
            SoftReference<RenderElement> wre = new SoftReference<RenderElement>(m_currentData.remove(0));
            m_olderData.add(wre);
        }
        trimOlder();
        return amount;
    }

    // gets the next older element, one way or another.
    private RenderElement getNextOlder() {
        trimOlder();
        if(m_olderData.size() == 0) {
            // all weakreferences are expired! resort to reading from disk.
            Log.d(LOGTAG, "No more references, read from file");

            return new RenderElement(m_input.getPrevious(m_newerData.size() + m_currentData.size()));
        }

        // get least old element
	RenderElement re = null;
	try {
	    re = m_olderData.remove(m_olderData.size()).get();
	} catch(Exception e) {
	    // TODO: handle exception
	}

	if(re == null) {
	    Log.d(LOGTAG, "Reference expired, read from file");
	    // latest weakreference got gc'd, resort to disk.
	    return new RenderElement(m_input.getPrevious(m_newerData.size() + m_currentData.size()));
	}
        // can actually use weakreference! woo!
        Log.d(LOGTAG, "Reference exists!");
        return re;
    }

    // trim expired older elements
    private synchronized void trimOlder() {
        while(m_olderData.size() > 0 && // still have elements
              m_olderData.get(0).get() == null || // start element is expired
              m_olderData.size() > m_maxNewerData) { // force max of one screen storage.
            try {
                m_olderData.remove(0);
            } catch(Exception e) {
                Log.d(LOGTAG, "Miss trim");
                return;
                // TODO: handle?
            }
        }
    }

    public void setDataInput(DataInputInterface in) {
        m_input = in;
    }

    public void setMaxCurrentData(int max){
        m_maxCurrentData = max;
        m_blitter.setMaxElements(max);
    }

    public int getMaxCurrentData() {
        return m_maxCurrentData;
    }

    public void setMaxNewerData(int max){
        m_maxNewerData = max;
    }

    public int getMaxNewerData() {
        return m_maxNewerData;
    }



}
