package com.lightspeed.gpr.lib;



import com.lightspeed.gpr.lib.Element;

import java.util.List;
import java.util.concurrent.ExecutionException;

public abstract class AbstractViewManager {

    // this is the input to the manager
    protected DataInputInterface m_input;

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

    public void setInput(DataInputInterface in) {
	m_input = in;
	m_viewIndex = 0;
    }
}
