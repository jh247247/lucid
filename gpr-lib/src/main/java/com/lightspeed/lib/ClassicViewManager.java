package com.lightspeed.gpr.lib;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Range;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.ContiguousSet;
import com.google.common.util.concurrent.ListenableFuture;


import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.lang.IndexOutOfBoundsException;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;


import com.lightspeed.gpr.lib.Element;
import com.lightspeed.gpr.lib.EventBusHandler;

/**
 * This file implements a platform agnostic view manager for the data given
 * Note that this is the "classic" view, as in the data is displayed
 * in a 1D fashion
 */

public class ClassicViewManager
    extends AbstractViewManager {


    private final int CACHE_SIZE = 1000;
    private final int VIEW_WIDTH = 100; // defaults...
    private final int VIEW_HEIGHT = 255;

    // get the view dpi to calculate view size
    // should aim for 1x1mm pixels?
    private int m_viewDpi;

    private ArrayList<ListenableFuture<Element>> m_currentView = new ArrayList();

    LoadingCache<Integer, ListenableFuture<Element>> m_elementCache =
        CacheBuilder.newBuilder()
        .maximumSize(CACHE_SIZE)
        .build(new CacheLoader<Integer, ListenableFuture<Element>>() {
                @Override
                public ListenableFuture<Element> load(Integer index) {
                    if(m_input != null) {
                        return m_input.getElement(index);
                    }
                    return null; // need to do something smarter...
                }
            }
            );



    public ClassicViewManager() {
        super();

        m_viewWidth = VIEW_WIDTH;
        m_viewHeight = VIEW_HEIGHT;

        renewView();
    }

    // returns the current view as a list to be rendered
    @Override
    public List<ListenableFuture<Element>> getView() {
        return m_currentView;
    }

    // move the view by some amount
    public void moveView(int amount) {
        goToIndex(m_viewIndex + amount);
    }

    // go to a specific index
    public void goToIndex(int index) {
        m_viewIndex = index;
        if(m_viewIndex < 0) { // trying to go earlier than the start
            m_viewIndex = 0;
        } else if(m_input != null && m_viewIndex+m_viewWidth >
                  m_input.getCurrentIndex()) { // trying to go off the end
            m_viewIndex = Math.max(0,m_input.getCurrentIndex()-m_viewWidth);
        }

        //refreshElementCache();
    }


    @Override
    public void setInput(AbstractDataInput in) {
        super.setInput(in);

        // clear the cache since we are changing inputs, they are useless.
        m_elementCache.invalidateAll();
	renewView();
        //refreshElementCache();
    }

    @Override
    public void onNewElement(Element e, int i) {
        if(m_startLock) {
            m_viewIndex = Math.max(i-m_viewWidth,0);
        }

        // check if view needs renewing
        try {
            if(m_currentView.get(0) != m_elementCache.get(m_viewIndex)) {
                renewView();
            }
        }
	catch(Exception ex) {
	    System.out.println("Error checking view: " + ex);
	}

    }

    /////////////////////
    // PRIVATE METHODS //
    /////////////////////

    private void renewView() {
        m_currentView = new ArrayList();
        for(int i = m_viewIndex; i < m_viewIndex+m_viewWidth; i++) {
            try {
                m_currentView.add(m_elementCache.get(i));
            }
            catch(Exception e) {
                System.out.println("Error populating view: " + e);
            }
        }
    }


}
