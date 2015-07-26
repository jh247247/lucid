package com.lightspeed.GPR1;

import android.view.SurfaceHolder;
import android.util.AttributeSet;
import android.graphics.Canvas;
import android.view.SurfaceView;
import android.annotation.SuppressLint;

public class RenderThread extends Thread{
    private SurfaceHolder m_surfHold;
    private RenderView m_render;
    private boolean m_run = false;

    public RenderThread(SurfaceHolder surfHold, RenderView render) {
        m_surfHold = surfHold;
        m_render = render;
    }

    public void setSurfaceDims(int width,
			       int height) {
	m_surfHold.setFixedSize(width, height);
    }

    public void setRunning(boolean state) {
        m_run = state;
    }

    public boolean getRunning() {
        return m_run;
    }

    @SuppressLint("WrongCall")
    @Override
    public void run() {
        Canvas c = null;
        while(m_run) {
            try {
                c = m_surfHold.lockCanvas(null);
                synchronized (m_surfHold) {
                    m_render.onDraw(c);
                }
		yield();
	    } finally {
                // if exception is thrown above, make sure that
                // surface is not left in an inconsistent state.
                if(c != null) {
                    m_surfHold.unlockCanvasAndPost(c);
                }
            }
        }
    }
}
