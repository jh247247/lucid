// this file is kept specifically for config changes.
// note that it CANNOT keep references to the context, otherwise a
// whole bunch of memory leaks.

package com.lightspeed.GPR1;

import com.lightspeed.gpr.lib.DataInputInterface;
import android.support.v4.app.Fragment;
import android.app.Activity;
import android.os.Bundle;

public class RetainFragment extends Fragment {
    private DataInputInterface m_input;
    private RenderElementManager m_manager;
    private RenderElementBlitter m_blitter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setRetainInstance(true);
    }

    public void setInput(DataInputInterface in) {
	m_input = in;
    }

    public DataInputInterface getInput() {
	return m_input;
    }

    public void setManager(RenderElementManager re) {
	m_manager = re;
    }

    public RenderElementManager getManager() {
	return m_manager;
    }

    public void setBlitter(RenderElementBlitter rb) {
	m_blitter = rb;
    }

    public RenderElementBlitter getBlitter() {
	return m_blitter;
    }
}
