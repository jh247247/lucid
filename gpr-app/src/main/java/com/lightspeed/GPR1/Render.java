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


public class Render extends SurfaceView
    implements SurfaceHolder.Callback{
    private static final String LOGTAG = "Render";

    private RenderThread m_renderThread;

    public Render(Context ctx) {
        super(ctx);
        threadInit();
    }

    public Render(Context ctx, AttributeSet attrs) {
	super(ctx, attrs);
        threadInit();
    }

    public Render(Context ctx, AttributeSet attrs, int defStyle) {
        super(ctx, attrs, defStyle);
        threadInit();
    }

    private void threadInit() {
        getHolder().addCallback(this);
        m_renderThread = new RenderThread(getHolder(), this);
        setFocusable(true); // make sure we get events...
	Log.v(LOGTAG,"Finished init!");
    }

    @Override
    protected void onDraw(Canvas c) {
        Paint p = new Paint();
        p.setColor(Color.WHITE);
        c.drawRect(50,50,200,200,p);
        Log.v(LOGTAG,"Drawing to canvas!");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder,
                               int arg0,
                               int arg1,
                               int arg2) {
        // TODO: do something with this...
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        m_renderThread.setRunning(true);
        m_renderThread.start();
        Log.v(LOGTAG,"Surface created");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        m_renderThread.setRunning(false);

        boolean retry = true;
        while(retry) {
            try {
                m_renderThread.join();
                retry = false;
            }
            catch(InterruptedException e) {
                // TODO:
            }
        }
        Log.v(LOGTAG,"Surface destroyed");
    }
}
