package com.lightspeed.GPR1;

import com.lightspeed.gpr.lib.Element;

import java.util.ArrayList;
import java.util.List;
import android.graphics.Canvas;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.Paint;
import android.graphics.Matrix;
import android.view.SurfaceHolder;
import java.lang.Math;
import android.util.Log;
import java.util.LinkedList;
import android.graphics.Color;

// TODO: remove!
import de.greenrobot.event.EventBus;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;


public class RenderElementBlitter {
    static final String LOGTAG = "RenderElementBlitter";

    static final int MAX_PIXEL_SIZE = 3;

    static final int CACHE_SIZE = 1000;

    Bitmap m_bm;
    Canvas m_cbm;
    Paint m_paint;

    SurfaceHolder m_surfHold;

    public RenderElementBlitter() {
        m_bm = null;
        m_cbm = null;
        m_surfHold = null;

	// disable filtering...
        m_paint = new Paint();
        m_paint.setAntiAlias(false);
        m_paint.setFilterBitmap(false);

        // TODO: unsubscribe?
        EventBus.getDefault().register(this);
    }

    public void setSurfaceHolder(SurfaceHolder s) {
        m_surfHold = s;
    }

    public void blitToCanvas(Canvas c, List<Element> data) {
        Log.d("RenderElementBlitter", "Trying to render!");

        // if elements are empty, might as well clear all the pixels...
        if(data.isEmpty()) {
            c.drawColor(Color.BLACK);
            //Log.w("RenderElementBlitter","No elements!");
            return;
        }

        // create bitmap just in case we havent yet
        if(m_bm == null) {
	    	Bitmap.Config conf = Bitmap.Config.ARGB_8888;
		m_bm = Bitmap.createBitmap(c.getWidth(),
					   c.getHeight(),
					   conf);
        }

        long startTime = System.nanoTime();

        // start rendering here

        // create tmp bitmap to render to
        Bitmap tbm = null;

        // for(int i = data.size(); i >= 0; i--) {
        //     // render the bitmap
        //     tbm = data.get(i).getRenderedElement();
        //     if(tbm == null) break;

        //     // blit bitmap to canvas
        //     m_cbm.drawBitmap(tbm, m_cbm.getWidth()-data.size()+i-1, 0, null);
        // }
        // stop rendering

	Matrix matrix = new Matrix();
        matrix.postScale(((float)c.getWidth()/(float)m_bm.getWidth()),
                         ((float)c.getHeight()/(float)m_bm.getHeight()));

        c.drawBitmap(m_bm,matrix,m_paint);

        long endTime = System.nanoTime();
        long diff = (endTime-startTime)/1000000;
        if(diff > 16) {
            Log.w("RenderElementBlitter","Scene in: " + diff);
        }
    }
}
