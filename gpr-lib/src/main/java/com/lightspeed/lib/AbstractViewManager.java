package com.lightspeed.gpr.lib;



import com.lightspeed.gpr.lib.Element;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.lang.ref.WeakReference;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.ListenableFuture;

public abstract class AbstractViewManager
    implements AbstractDataInput.NewElementListener {

    // this is the input to the manager
    protected AbstractDataInput m_input;
    protected WeakReference<AbstractRenderer> m_renderer;
    protected final EventBus m_bus = EventBusHandler.getEventBus();

    protected double m_pixelSize;
    protected double m_scrollAccumulatorX;
    protected double m_scrollAccumulatorY;


    // index from the start of the input
    protected int m_viewIndex;

    // if set, locks the view to the start of the input.
    // i.e: makes sure that the view index is the same as the current
    // input index
    protected boolean m_startLock;


    // internal dims for the view.
    // can be seen as the "amount of elements to show"
    protected int m_viewWidth;
    // can be seen as the "amount of data points to show"
    protected int m_viewHeight;

    public AbstractViewManager() {
        m_viewIndex = 0;
        m_viewWidth = 1; // magic numbers get fixed later on.
        m_viewHeight = 1;
        m_pixelSize = 1;
        m_startLock = false;
        m_input = null;

        // register on the eventbus...
        m_bus.register(this);
    }

    // should get the current view
    abstract public List<ListenableFuture<Element>> getView();

    // move the view by some amount
    abstract public void moveView(int amount);

    // go to a specific index
    abstract public void goToIndex(int index);

    public void setStartLock(boolean sl) {
        m_startLock = sl;
    }

    public boolean getStartLock() {
        return m_startLock;
    }

    // view dims
    public void setViewWidth(int w) {
        m_viewWidth = w;
    }

    public int getViewWidth() {
        return m_viewWidth;
    }

    public void setViewHeight(int h) {
        m_viewHeight = h;
    }

    public int getViewHeight() {
        return m_viewHeight;
    }

    public void setInput(AbstractDataInput in) {
        if(m_input != null) {
            m_input.setNewElementListener(null);
        }
        in.setNewElementListener(this);
        m_input = in;
        m_viewIndex = 0;
    }

    public void setRenderer(AbstractRenderer r) {
        m_renderer = new WeakReference<AbstractRenderer>(r);
    }



    @Subscribe
    public void surfaceChanged(AbstractRenderer.SurfaceChangedEvent e) {
        // assume that the height of the data does not change, only
        // the width
        m_pixelSize = (int)(e.h/m_viewHeight);
        m_viewWidth = (int)(e.w/m_pixelSize);

        if(m_renderer.get() != null) {
            m_renderer.get().render();
        }
    }

    @Subscribe
    public void surfaceScroll(AbstractRenderer.SurfaceScrolledEvent e) {
        m_scrollAccumulatorX += e.dX;
        m_scrollAccumulatorX += e.dX;

        int xscroll = (int)(m_scrollAccumulatorX/m_pixelSize);
        if(xscroll != 0 && !getStartLock()) {
            moveView(xscroll/2);
            m_scrollAccumulatorX -= xscroll*m_pixelSize;

            if(m_renderer.get() != null) {
                m_renderer.get().render();
            }
        }
    }

    @Subscribe
    public void ScrollAccumulatorReset(AbstractRenderer.ResetScrollEvent e) {
        m_scrollAccumulatorX = 0;
        m_scrollAccumulatorY = 0;
    }
}
