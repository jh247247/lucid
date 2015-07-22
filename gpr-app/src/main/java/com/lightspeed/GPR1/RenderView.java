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
import android.view.MotionEvent;
import android.widget.Toast;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.support.v4.view.GestureDetectorCompat;

import java.lang.Math;


public class RenderView extends SurfaceView
    implements SurfaceHolder.Callback{
    private static final String LOGTAG = "RenderView";

    private RenderThread m_renderThread;

    private RandomDataInput m_in;
    private RenderElementBlitter m_blitter;
    private RenderElementManager m_manager;

    private Context m_ctx;
    private GestureDetectorCompat m_gdetector;

    public RenderView(Context ctx) {
        super(ctx);
        m_ctx = ctx;
        threadInit();
        uiInit();
        renderInit();
    }

    public RenderView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        m_ctx = ctx;
        threadInit();
        uiInit();
        renderInit();
    }

    public RenderView(Context ctx, AttributeSet attrs, int defStyle) {
        super(ctx, attrs, defStyle);
        m_ctx = ctx;
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

        // handle those events
        m_gdetector = new GestureDetectorCompat(m_ctx, new
                                                RenderGestureListener());

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

    public void startView() {
        if(m_renderThread != null) {
            m_renderThread.setRunning(true);
        }

    }

    private class RenderGestureListener extends
                                            GestureDetector.SimpleOnGestureListener
    {
        private static final String GESLIN_LOGTAG =
            "RenderGestureListener";

        private float m_dXacc = 0; // accumulator for dX
        private float m_dYacc = 0;

        @Override
        public boolean onDown(MotionEvent e) {
            Log.d(GESLIN_LOGTAG,"onDown: " + e.toString());
            // reset accumulator so that we can track this scroll (if
            // it is one...)
            m_dXacc = 0;
            m_dYacc = 0; // TODO: Y scrolling...

            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float dX, float dY) {
            Log.d(GESLIN_LOGTAG,"onScroll: " + dX + " " + dY);
            m_dXacc += dX;
            m_dYacc += dY;

            //find out how many elements to move by

            float pixelSize = (float)RenderView.this.getWidth()/(float)m_manager.getMaxCurrentData();

            int xscroll = -(int)(m_dXacc/pixelSize);
            if(xscroll != 0) { // have to move!
                Log.d(GESLIN_LOGTAG,"onScroll x by: " + xscroll);
                m_manager.moveCurrent(xscroll); // move viewport

                // remove from accumulator
                m_dXacc += xscroll*pixelSize;
            }
            return true;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        //this.mDetector.onTouchEvent(event);
        // Be sure to call the superclass implementation
        boolean handled = super.onTouchEvent(event);
        if(!handled) {
            m_gdetector.onTouchEvent(event);
        }

        return true;
    }
}
