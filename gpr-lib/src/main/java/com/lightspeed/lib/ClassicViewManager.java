package com.lightspeed.gpr.lib;

import com.annimon.stream.Collectors;
import com.annimon.stream.Exceptional;
import com.annimon.stream.Stream;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Range;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.ContiguousSet;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.SettableFuture;

import java.io.IOException;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.lang.IndexOutOfBoundsException;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Callable;


import com.lightspeed.gpr.lib.Element;
import com.lightspeed.gpr.lib.EventBusHandler;

/**
 * This file implements a platform agnostic view manager for the data given
 * Note that this is the "classic" view, as in the data is displayed
 * in a 1D fashion
 */

public class ClassicViewManager
    extends AbstractViewManager {


    private final int CACHE_SIZE = 5000;
    private final int VIEW_WIDTH = 100; // defaults...
    private final int VIEW_HEIGHT = 255;

    // get the view dpi to calculate view size
    // should aim for 1x1mm pixels?
    private int m_viewDpi;

    private List<ListenableFuture<Element>> m_currentView = new ArrayList();

    LoadingCache<Integer, ListenableFuture<Element>> m_elementCache =
        CacheBuilder.newBuilder()
        .maximumSize(CACHE_SIZE)
        .concurrencyLevel(1)
        .build(new CacheLoader<Integer, ListenableFuture<Element>>() {
                @Override
                public ListenableFuture<Element> load(Integer index) throws IOException {
                    if(m_input != null) {
                        return m_input.getElement(index);
                    }
		    throw new IOException();
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
        amount = Math.max(amount, -m_viewIndex); // amount to move left
        amount = Math.min(amount, // amount to move right
                          m_input.getCurrentIndex()-(m_viewIndex+m_viewWidth));

        goToIndex(m_viewIndex + amount);

        try {
            if(Math.abs(amount) < m_viewWidth) { // moving less than one screen
                for(int i = 0; i < Math.abs(amount); i++) {
                    if(amount > 0) { // moving right
                        m_currentView.remove(0);
                        // add new element to rhs
                        m_currentView.add(m_elementCache.get(m_viewIndex+m_viewWidth-amount+i));
                    } else {
                        m_currentView.remove(m_currentView.size()-1);
                        m_currentView.add(0,m_elementCache.get(m_viewIndex-amount-i));
                    }
                }
            } else {
                renewView();
            }
        }
        catch(Exception e){
            // TODO:
        }
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
        if(m_renderer.get() != null) {
            m_renderer.get().render();
        }
    }

    @Override
    public void onNewElement(Element e, int i) {
        System.out.println("Received element of index: " + i);

        SettableFuture<Element> se = SettableFuture.create();
        se.set(e);
        m_elementCache.put(new Integer(i),se);

        if(m_startLock) {
            m_viewIndex = Math.max(i-m_viewWidth,0);
        }

        // check if view needs renewing
        try {
            renewView();
        }
        catch(Exception ex) {
            System.out.println("Error checking view: " + ex);
        }
        if(m_renderer.get() != null) {
            m_renderer.get().render();
        }

    }

    @Override
    public void
        scrollAccumulatorReset(AbstractRenderer.ResetScrollEvent e) {
        super.scrollAccumulatorReset(e);
    }

    @Override
    public void surfaceScroll(AbstractRenderer.SurfaceScrolledEvent e) {
        super.surfaceScroll(e);

        // check if we are at the start of the data, enable start lock, else disable.
        m_startLock = (m_viewIndex == Math.max(0,m_input.getCurrentIndex()-m_viewWidth));
    }


    @Override
    public void surfaceChanged(AbstractRenderer.SurfaceChangedEvent e) {
        renewView();
        super.surfaceChanged(e);
    }

    @Subscribe
    public void surfaceIdle(AbstractRenderer.SurfaceIdleStartEvent e) {
        precache();
    }

    /////////////////////
    // PRIVATE METHODS //
    /////////////////////

    private void renewView() {
	if(m_input == null) return; // FIXME:

	precache();

        m_currentView = Stream.ofRange(m_viewIndex, m_viewIndex+m_viewWidth)
	    .map((i) -> m_elementCache.getUnchecked(i))
	    .collect(Collectors.toList());
    }

    private void precache() {
	if(m_input == null)  return;

	ArrayList<ListenableFuture<Element>> tcache = new ArrayList();

        // todo: test if in cache already?
        Stream.ofRange(m_viewIndex-m_viewWidth,
                       m_viewIndex+m_viewWidth*2)
	    .filter((i) -> m_elementCache.getIfPresent(i) == null)
            .forEach((i) -> tcache.add(m_elementCache.getUnchecked(i)));

        if(m_renderer.get() != null) {
            m_renderer.get().cache(tcache);
        }
    }
}
