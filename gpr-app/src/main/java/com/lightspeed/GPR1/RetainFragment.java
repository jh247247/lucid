// this file is kept specifically for config changes.
// note that it CANNOT keep references to the context, otherwise a
// whole bunch of memory leaks.

package com.lightspeed.GPR1;

import com.lightspeed.gpr.lib.DataInputInterface;
import android.support.v4.app.Fragment;
import android.app.Activity;
import android.os.Bundle;

import de.greenrobot.event.EventBus;

public class RetainFragment extends Fragment {
    private DataInputInterface m_input;
    private RenderElementManager m_manager;
    private RenderElementBlitter m_blitter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setRetainInstance(true);
	EventBus.getDefault().register(this);
    }


    public DataInputInterface getInput() {
        return m_input;
    }

    public RenderElementManager getManager() {
        return m_manager;
    }


    public RenderElementBlitter getBlitter() {
        return m_blitter;
    }

    // input changed! set via our handy function...
    public void onEvent(DataInputFragment.InputChangeEvent e) {
	m_input = e.input;
    }

    // set the renderElementManager...
    public void onEvent(RenderElementManager re) {
	m_manager = re;
    }

    // set the blitter
    public void onEvent(RenderElementBlitter rb) {
	m_blitter = rb;
    }
}
