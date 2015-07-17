package com.lightspeed.GPR1;

import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.content.Context;
import android.util.AttributeSet;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.WindowManager;
import android.view.Display;

import java.lang.Math;


public class RenderView extends SurfaceView
    implements SurfaceHolder.Callback{
    private static final String LOGTAG = "RenderView";

    private RenderThread m_renderThread;

    private RandomDataInput m_in;
    private RenderElementBlitter m_blitter;
    private RenderElementManager m_manager;

    public RenderView(Context ctx) {
        super(ctx);
        threadInit();
        uiInit();
        renderInit();
    }

    public RenderView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        threadInit();
        uiInit();
        renderInit();
    }

    public RenderView(Context ctx, AttributeSet attrs, int defStyle) {
        super(ctx, attrs, defStyle);
        threadInit();
        uiInit();
        renderInit();
    }

    private void threadInit() {
        getHolder().addCallback(this);
        m_renderThread = new RenderThread(getHolder(), this);
    }

    private void uiInit() {
        setFocusable(true); // make sure we get events...
        Log.v(LOGTAG,"Finished init!");
    }

    private void renderInit() {
        m_in = new RandomDataInput();
        m_manager = new RenderElementManager(m_in,255*16/9,0);
        m_blitter = m_manager.getBlitter();
    }

    private void initCanvas() {
        int width = this.getWidth();
        int height = this.getHeight();

	Log.v(LOGTAG,"Previous canvas dims: " + width + " x " +
	      height);

        int newWidth = Math.min(m_manager.getMaxCurrentData()*3,
				(width/m_manager.getMaxCurrentData())*m_manager.getMaxCurrentData());
        int newHeight = Math.min(3*255,(height/255)*255); // FIXME: magicsss

	Log.v(LOGTAG,"New canvas dims: " + newWidth + " x " + newHeight);

	m_renderThread.setSurfaceDims(newWidth,newHeight);
    }

    @Override
    protected void onDraw(Canvas c) {
        if(c == null) {
            // this can still be called when the surface is destroyed,
            // so make sure that we aren't passed a null canvas
            return;
        }
        m_manager.updateInput();

        m_blitter.blitToCanvas(c);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder,
                               int arg0,
                               int arg1,
                               int arg2) {
        // TODO: do something with this...
	initCanvas();
	m_manager.setMaxCurrentData(255*this.getWidth()/this.getHeight());
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // switching back to app can recreate this...
        threadInit();
        m_renderThread.setRunning(true);
        m_renderThread.start();

        Log.v(LOGTAG,"Surface created");
	initCanvas();
	m_manager.setMaxCurrentData(255*this.getWidth()/this.getHeight());
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        m_renderThread.setRunning(false);

        while(m_renderThread != null) {
            try {
                m_renderThread.join();
                m_renderThread = null;
            }
            catch(InterruptedException e) {
                // TODO:
            }
        }
        Log.v(LOGTAG,"Surface destroyed");
    }

    // stop the thread from running temporarily
    public void stopView() {
        if(m_renderThread != null) {
            m_renderThread.setRunning(false);
        }
    }
}
