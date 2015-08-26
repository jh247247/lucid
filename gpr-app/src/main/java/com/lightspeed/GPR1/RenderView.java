package com.lightspeed.GPR1;

import com.lightspeed.gpr.lib.DataInputInterface;

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
import java.lang.ref.WeakReference;

import de.greenrobot.event.EventBus;

public class RenderView extends SurfaceView
    implements SurfaceHolder.Callback{
    private static final String LOGTAG = "RenderView";

    /**
     * Thread for rendering, artificially calls the onDraw method of
     * this view to blit all the elements to the screen.
     */


    private RenderThread m_renderThread;

    /**
     * These should be in their own fragment, so they get retained on
     * configuration changes.
     */
    private RenderElementBlitter m_blitter;
    private RenderElementManager m_manager;


    private WeakReference<Context> m_ctx;

    /**
     * I don't know what happens with this, does it get destroyed? I
     * dunno, since it has a reference to the context...
     */
    private GestureDetectorCompat m_gdetector;

    public RenderView(Context ctx) {
        super(ctx);
        m_ctx = new WeakReference<Context>(ctx);
        threadInit();
        uiInit();
        renderInit();
    }

    public RenderView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        m_ctx = new WeakReference<Context>(ctx);
        threadInit();
        uiInit();
        renderInit();

    }

    public RenderView(Context ctx, AttributeSet attrs, int defStyle) {
        super(ctx, attrs, defStyle);
        m_ctx = new WeakReference<Context>(ctx);
        threadInit();
        uiInit();
        renderInit();
    }

    private void threadInit() {
        getHolder().addCallback(this);
        m_renderThread = new RenderThread(getHolder(), this);
        m_renderThread.setPriority(Thread.MAX_PRIORITY);

        m_renderThread.setRunning(true);
        m_renderThread.start();
    }


    // call this one if we want to autostart
    public void start() {
        uiInit();
        renderInit();
        threadInit();
    }

    // call this if we have some retained managers
    public void start(RenderElementManager re,
                      RenderElementBlitter rb) {
        threadInit();
        uiInit();
        m_manager = re;
        m_blitter = rb;
        if(m_manager == null) { // somebody made a boo-boo..
            Log.e(LOGTAG, "manager is unexpectedly null in retained init!");
            renderInit();
        }
    }

    private void uiInit() {
        setFocusable(true); // make sure we get events...

        if(m_ctx.get() != null) {
            // handle those events
            m_gdetector = new GestureDetectorCompat(m_ctx.get(), new
                                                    RenderGestureListener());

        }
        Log.v(LOGTAG,"Finished init!");
    }

    private void renderInit() {
        // TODO: Make sure that max data set gets called upon surface creation/change...
        m_manager = new RenderElementManager();
        m_blitter = m_manager.getBlitter();

        EventBus.getDefault().post(m_manager);
        EventBus.getDefault().post(m_blitter);
    }

    private void initCanvas() {
        int width = this.getWidth();
        int height = this.getHeight();
        m_manager.setMaxCurrentData(100); // FIXME: have this scale
        // depending on display/elementsize

        Log.v(LOGTAG,"Previous canvas dims: " + width + " x " +
              height);

        int newWidth;
        int newHeight = Math.min(3*255,(height/255)*255); // FIXME: magicsss
        if(m_manager.getMaxCurrentData() != 0){
            newWidth = Math.min(m_manager.getMaxCurrentData()*3,
                                (width/m_manager.getMaxCurrentData())*m_manager.getMaxCurrentData());
        } else {
            // hackyyyy
            newWidth = newHeight;
        }


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
        m_manager.updateInput(); // TODO: REMOVE, have this dependent
        // on the input data rate!
        m_blitter.blitToCanvas(c);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder,
                               int arg0,
                               int arg1,
                               int arg2) {
        initCanvas();
        m_manager.setMaxCurrentData(255*this.getWidth()/this.getHeight());
        m_blitter.setMaxElements(m_manager.getMaxCurrentData());

        if(!m_renderThread.getRunning()) {
	    m_renderThread.kill();
	    threadInit();
        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.v(LOGTAG,"Surface created");
        initCanvas();

        // TODO: Setup proper calculations for this, eventbus?
        m_manager.setMaxCurrentData(255*this.getWidth()/this.getHeight());

        if(!m_renderThread.getRunning()) {
            threadInit();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
	stopView();
	Log.v(LOGTAG,"Surface destroyed");
    }

    // stop the thread from running temporarily
    public void stopView() {
        if(m_renderThread != null) {
            m_renderThread.setRunning(false);
            while(m_renderThread != null) {
                try {
                    m_renderThread.join();
		    m_renderThread.kill();
		    m_renderThread = null;
                }
                catch(InterruptedException e) {
                    // TODO:
                }
            }
        }
    }

    public void startView() {
        threadInit();
    }

    // TODO: Make this an external class?
    // I don't like how this clutters up the class, so it might be a
    // good idea to make it external...
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
            //Log.d(GESLIN_LOGTAG,"onScroll: " + dX + " " + dY);
            m_dXacc += dX;
            m_dYacc += dY;

            //find out how many elements to move by

            float pixelSize = (float)RenderView.this.getWidth()/(float)m_manager.getMaxCurrentData();

            int xscroll = -(int)(m_dXacc/pixelSize);
            if(xscroll != 0 && !m_manager.getStartLock()) { // have to move!
                //Log.d(GESLIN_LOGTAG,"onScroll x by: " + xscroll);
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

    public void setDataInput(DataInputInterface in) {
        m_manager.setDataInput(in);
    }
}
