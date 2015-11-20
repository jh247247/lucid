package com.lightspeed.gpr.lib;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.eventbus.EventBus;

import java.util.List;
import java.util.ArrayList;
import java.lang.IndexOutOfBoundsException;

import com.lightspeed.gpr.lib.Element;
import com.lightspeed.gpr.lib.EventBusHandler;

/**
 * This file implements a platform agnostic view manager for the data given
 * Note that this is the "classic" view, as in the data is displayed
 * in a 1D fashion
 */

public class ClassicViewManager
    extends AbstractViewManager
    implements AbstractDataInput.NewElementListener {

    private final int CACHE_SIZE = 1000;
    private final int VIEW_WIDTH = 100; // defaults...
    private final int VIEW_HEIGHT = 255;

    private final EventBus m_bus = EventBusHandler.getEventBus();

    // get the view dpi to calculate view size
    // should aim for 1x1mm pixels?
    private int m_viewDpi;

    LoadingCache<Integer, Element> m_elementCache;

    public ClassicViewManager() {
	super();
        m_elementCache = CacheBuilder.newBuilder()
            .maximumSize(CACHE_SIZE)
            .build(new CacheLoader<Integer, Element>() {
                    @Override public Element load(Integer index)
			throws IndexOutOfBoundsException {
                        if(m_input != null) {
                            return m_input.getElement(index);
                        }
                        throw new IndexOutOfBoundsException();
                    }
                }
                );

	// register on the eventbus...
	m_bus.register(this);

	m_viewWidth = VIEW_WIDTH;
	m_viewHeight = VIEW_HEIGHT;
    }

    // returns the current view as a list to be rendered
    @Override
    public List<Element> getView() {
	if(m_startLock) {
	    m_viewIndex = Math.max(m_input.getCurrentIndex()-m_viewWidth,0);
	}

        ArrayList<Element> ret = new ArrayList<Element>();

        // populate list with viewport
        for(int i = m_viewIndex; i < m_viewIndex+m_viewWidth; i++) {
            try {
                ret.add(m_elementCache.get(i));
            }
            catch(Exception e) {
                // no input, nothing to draw...
		System.out.println("Terminating at index: " + i +
				   "\nReason: " + e);

                return ret;
            }

        }

        return ret;
    }

    // move the view by some amount
    public void moveView(int amount) {
        goToIndex(m_viewIndex + amount);
    }

    // go to a specific index
    public void goToIndex(int index) {
        m_viewIndex = index;
        if(m_viewIndex == 0) {
            m_viewIndex = 0;
        }

        // do I have to preempt the caching?
    }

    @Override
    public void setInput(AbstractDataInput in) {
        super.setInput(in);

        // clear the cache since we are changing inputs, they are useless.
        m_elementCache.invalidateAll();
    }

    // TODO: handle viewport changes via eventbus
    // TODO: handle viewport scrolling via eventbus

    @Override
    public void onNewElement(Element e) {

    }
}
