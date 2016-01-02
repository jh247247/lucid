package com.lightspeed.GPR1;

import com.lightspeed.gpr.lib.AbstractDataInput;
import com.lightspeed.gpr.lib.AbstractRenderer;
import com.lightspeed.gpr.lib.EventBusHandler;
import com.lightspeed.gpr.lib.Element;

import com.google.common.eventbus.EventBus;

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

public class RenderView extends SurfaceView
    implements SurfaceHolder.Callback {

    private static final String LOGTAG = "RenderView";

    private EventBus m_bus = EventBusHandler.getEventBus();

    /**
     * These should be in their own fragment, so they get retained on
     * configuration changes.
     */
    RenderElementBlitter m_blitter;

    /**
     * I don't know what happens with this, does it get destroyed? I
     * dunno, since it has a reference to the context...
     */
    private GestureDetectorCompat m_gdetector;

    public RenderView(Context ctx) {
        super(ctx);
    }

    public RenderView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
    }

    public RenderView(Context ctx, AttributeSet attrs, int defStyle) {
        super(ctx, attrs, defStyle);
    }

    // call this if we have some retained managers
    public void setBlitter(RenderElementBlitter rb) {
        m_blitter = rb;

        if(m_blitter == null) {
            // somebody made a boo-boo..
            Log.e(LOGTAG, "blitter is null in init!");
            // don't start rendering if blitter not set
            return;
        } else {
            //m_blitter.setSurfaceHolder(getHolder());
            m_blitter.setRenderView(this);
        }

        uiInit();
        initCanvas();
    }

    private void uiInit() {
        setFocusable(true); // make sure we get events...
        getHolder().addCallback(this);

        // TODO: make this get recreated on config change
        m_gdetector = new GestureDetectorCompat(getContext(), new
                                                RenderGestureListener());

        Log.v(LOGTAG,"Finished init!");
    }

    private void initCanvas() {
        AbstractRenderer.SurfaceChangedEvent sufEv =
            new AbstractRenderer.SurfaceChangedEvent(this.getWidth(),
                                                     this.getHeight());
        // send out the new surface dims via the event bus

        setWillNotDraw(false);

        Log.v(LOGTAG,"New canvas dims: " + sufEv.w + " x " + sufEv.h);
        m_bus.post(sufEv);
        SurfaceHolder sh = getHolder();
        sh.setFixedSize(m_blitter.getWidth(),
                        m_blitter.getHeight());
    }

    @Override
    protected void onDraw(Canvas c) {
        if(c == null) {
            // this can still be called when the surface is destroyed,
            // so make sure that we aren't passed a null canvas
            Log.e(LOGTAG, "Surface draw failed!: canvas was null");
            return;
        }

        if(m_blitter == null) {
            Log.e(LOGTAG, "Surface draw failed!");
            return;
        }
        Log.d(LOGTAG, "Surface drawing!");
        m_blitter.blitToCanvas(c);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder,
                               int arg0,
                               int arg1,
                               int arg2) {
        initCanvas();

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.v(LOGTAG,"Surface created");
        initCanvas();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.v(LOGTAG,"Surface destroyed");
    }

    // TODO: Make this an external class?
    // I don't like how this clutters up the class, so it might be a
    // good idea to make it external...
    private class RenderGestureListener
        extends GestureDetector.SimpleOnGestureListener {
        private static final String GESLIN_LOGTAG =
            "RenderGestureListener";

        @Override
        public boolean onDown(MotionEvent e) {
            Log.d(GESLIN_LOGTAG,"onDown: " + e.toString());
            m_bus.post(new AbstractRenderer.ResetScrollEvent());
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float dX, float dY) {

            AbstractRenderer.SurfaceScrolledEvent se =
                new AbstractRenderer.SurfaceScrolledEvent(dX,dY);

            m_bus.post(se);
            return true;
        }
    }



    @Override
    public boolean onTouchEvent(MotionEvent event){
        //this.mDetector.onTouchEvent(event);
        // Be sure to call the superclass implementation
        boolean handled = super.onTouchEvent(event);

        // detect finger up for idle
        if(!handled && event.getAction() == 1) { // on up
            AbstractRenderer.SurfaceIdleStartEvent ie =
		new AbstractRenderer.SurfaceIdleStartEvent();
	    m_bus.post(ie);
	    return true;
        }

        if(!handled) {
            m_gdetector.onTouchEvent(event);
        }

        return true;
    }
}
