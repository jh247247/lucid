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
     * These should be in their own fragment, so they get retained on
     * configuration changes.
     */
    private RenderElementBlitter m_blitter;

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
    public void start(RenderElementBlitter rb) {
        m_blitter = rb;

        if(m_blitter == null) {
            // somebody made a boo-boo..
            Log.e(LOGTAG, "blitter is null in init!");
            // don't start rendering if blitter not set
            // TODO: error checking...
        } else {
	    m_blitter.setSurfaceHolder(getHolder());
	}

        uiInit();
        initCanvas();
	postInvalidate();
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
        int width = this.getWidth();
        int height = this.getHeight();
        // send out the new surface dims via the event bus
        EventBus.getDefault().post(new
                                   SurfaceChangedEvent(width, height));

        setWillNotDraw(false);

        Log.v(LOGTAG,"Previous canvas dims: " + width + " x " +
              height);
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
            Log.e(LOGTAG, "Surface draw failed!: blitter was null");
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

        private float m_dXacc = 0; // accumulator for dX
        private float m_dYacc = 0;

        @Override
        public boolean onDown(MotionEvent e) {
            Log.d(GESLIN_LOGTAG,"onDown: " + e.toString());

            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float dX, float dY) {

            //Log.d(GESLIN_LOGTAG,"onScroll: " + dX + " " + dY);
            m_dXacc += dX;
            m_dYacc += dY;

            // can't do anything, no blitter or no elements to render
            if(m_blitter == null || m_blitter.getMaxElements() == 0) {
                return true;
            }

            //find out how many elements to move by

            float pixelSize =
                (float)RenderView.this.getWidth()/(float)m_blitter.getMaxElements();

            int xscroll = -(int)(m_dXacc/pixelSize);
            //Log.d(GESLIN_LOGTAG,"onScroll x by: " + xscroll);
            if(xscroll != 0) {
                // send scroll event to element manager
                EventBus.getDefault().post(new
                                           SurfaceScrolledEvent(xscroll,
                                                                0));
                // TODO: figure out if we need to do y scroll...
                // remove from accumulator
                m_dXacc += xscroll*pixelSize;
                postInvalidate();
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


    /**
     * This object is sent via the eventbus whenever the surface in
     * this view is changed.
     */
    public class SurfaceChangedEvent {
        public final int w;
        public final int h;

        public SurfaceChangedEvent(int wi, int hi) {
            w = wi;
            h = hi;
        }
    }

    public class SurfaceScrolledEvent {
        public final int dX;
        public final int dY;

        public SurfaceScrolledEvent(int x, int y) {
            dX = x;
            dY = y;
        }
    }
}
