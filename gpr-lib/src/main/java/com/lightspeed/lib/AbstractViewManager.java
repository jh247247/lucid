package com.lightspeed.gpr.lib;



import com.lightspeed.gpr.lib.Element;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.lang.ref.WeakReference;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public abstract class AbstractViewManager
    implements AbstractDataInput.NewElementListener {

    // this is the input to the manager
    protected AbstractDataInput m_input;
    protected WeakReference<AbstractRenderer> m_renderer;
    protected final EventBus m_bus = EventBusHandler.getEventBus();


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
	m_viewWidth = 100; // FIXME: magics
	m_viewHeight = 100; // FIXME: magics
	m_startLock = true;
	m_input = null;

	// register on the eventbus...
	m_bus.register(this);
    }

    // should get the current view
    abstract public List<Element> getView();

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

    @Override
    public void onNewElement(Element e) {
	if(m_renderer.get() != null) {
	    m_renderer.get().render();
	}
    }

    @Subscribe
    public void surfaceChanged(AbstractRenderer.SurfaceChangedEvent e) {
	System.out.println("SURFACE CHANGED");
    }

    @Subscribe
    public void surfaceScrolled(AbstractRenderer.SurfaceChangedEvent e) {
	System.out.println("SURFACE SCROLLED");
    }
}
