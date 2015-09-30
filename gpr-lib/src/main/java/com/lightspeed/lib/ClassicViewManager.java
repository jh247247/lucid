package com.lightspeed.gpr.lib;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import com.lightspeed.gpr.lib.Element;

import java.util.List;
import java.util.ArrayList;
import java.lang.IndexOutOfBoundsException;

/**
 * This file implements a platform agnostic view manager for the data given
 * Note that this is the "classic" view, as in the data is displayed
 * in a 1D fashion
 */

public class ClassicViewManager extends AbstractViewManager {
    private int CACHE_SIZE = 1000;

    LoadingCache<Long, Element> m_elementCache;

    public ClassicViewManager() {
	m_elementCache = CacheBuilder.newBuilder()
	    .maximumSize(CACHE_SIZE)
	    .build(new CacheLoader<Long, Element>() {
		    @Override public Element load(Long index) {
			// TODO: fix api for datainputinterface
			Element ret = m_input.getElement(index);
			if(m_input != null && ret != null) {
			    return ret;
			}
			throw new IndexOutOfBoundsException();
		    }
		}
		);
    }

    // returns the current view as a list to be rendered
    @Override
    public List<Element> getView() {
	ArrayList<Element> ret = new ArrayList<Element>();

	// populate list with viewport
	for(long i = m_viewIndex; i < m_viewIndex+m_viewWidth; i++) {
	    try {
		ret.add(m_elementCache.get(i));
	    }
	    catch(Exception e) {
		// no input, nothing to draw...
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
    public void goToIndex(long index) {
	m_viewIndex = index;
	if(m_viewIndex == 0) {
	    m_viewIndex = 0;
	}

	// do I have to preempt the caching?
    }

    @Override
    public void setInput(DataInputInterface in) {
	super.setInput(in);

	// clear the cache since we are changing inputs, they are useless.
	m_elementCache.invalidateAll();
    }

}
