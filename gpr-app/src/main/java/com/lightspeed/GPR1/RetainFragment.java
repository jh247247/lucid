// this file is kept specifically for config changes.
// note that it CANNOT keep references to the context, otherwise a
// whole bunch of memory leaks.

package com.lightspeed.GPR1;

import com.lightspeed.gpr.lib.AbstractDataInput;
import com.lightspeed.gpr.lib.AbstractViewManager;
import com.lightspeed.gpr.lib.ClassicViewManager;
import com.lightspeed.gpr.lib.EventBusHandler;

import android.support.v4.app.Fragment;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class RetainFragment extends Fragment {
    private AbstractDataInput m_input;
    private RenderElementBlitter m_blitter;
    private AbstractViewManager m_viewManager;

    public RetainFragment() {
        m_viewManager = new ClassicViewManager();
        m_blitter = new RenderElementBlitter();
        m_blitter.setViewManager(m_viewManager);
        // TODO: have this saved offline so we can reset to previous settings...
	setInput(new RandomDataInput());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public AbstractDataInput getInput() {
        return m_input;
    }

    public void setInput(AbstractDataInput in) {
	if(in != null) {
	    m_input = in;
	    EventBusHandler.getEventBus().post(new AbstractViewManager.InputChangeEvent(m_input));
	}
    }

    public RenderElementBlitter getBlitter() {
        return m_blitter;
    }
}
