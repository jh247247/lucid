// this file is kept specifically for config changes.
// note that it CANNOT keep references to the context, otherwise a
// whole bunch of memory leaks.

package com.lightspeed.GPR1;

import com.lightspeed.gpr.lib.DataInputInterface;
import android.support.v4.app.Fragment;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import de.greenrobot.event.EventBus;

public class RetainFragment extends Fragment {
    private DataInputInterface m_input;
    private RenderElementBlitter m_blitter;

    public RetainFragment() {
        m_blitter = new RenderElementBlitter();
	EventBus.getDefault().register(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setRetainInstance(true);
    }

    public DataInputInterface getInput() {
        return m_input;
    }

    public RenderElementBlitter getBlitter() {
        return m_blitter;
    }

    // input changed! set via our handy function...
    public void onEvent(DataInputFragment.InputChangeEvent e) {
	m_input = e.input;
    }
}
