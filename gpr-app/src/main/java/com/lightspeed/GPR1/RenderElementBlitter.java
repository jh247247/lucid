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

import com.lightspeed.gpr.lib.AbstractViewManager;

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

    AbstractViewManager m_viewManager;

    LoadingCache<Element, RenderElement> m_renderElementCache =
        CacheBuilder.newBuilder()
        .maximumSize(CACHE_SIZE)
        .build(new CacheLoader<Element, RenderElement>() {
                @Override public RenderElement load(Element e) {
		    return new RenderElement(e);
                }
            }
            );

    public RenderElementBlitter() {
        m_bm = null;
	m_cbm = null;
        m_surfHold = null;
        m_viewManager = null;

        // disable filtering...
        m_paint = new Paint();
        m_paint.setAntiAlias(false);
        m_paint.setFilterBitmap(false);
    }

    public void setViewManager(AbstractViewManager viewman) {
        m_viewManager = viewman;
	initBitmap();

        // todo: caching...
    }

    public void initBitmap() {
        // create bitmap just in case we havent yet
	if(m_viewManager == null) return;

        if(m_bm == null) {
            Bitmap.Config conf = Bitmap.Config.ARGB_8888;
            m_bm = Bitmap.createBitmap(m_viewManager.getViewWidth(),
                                       m_viewManager.getViewHeight(),
                                       conf);
	    m_cbm = new Canvas(m_bm);
        } else if(m_bm.getWidth() != m_viewManager.getViewWidth()) {
	    m_bm.setWidth(m_viewManager.getViewWidth());
	} else if(m_bm.getHeight() != m_viewManager.getViewHeight()) {
	    m_bm.setHeight(m_viewManager.getViewHeight());
	}
    }

    public void setSurfaceHolder(SurfaceHolder s) {
        m_surfHold = s;
    }

    public void blitToCanvas(Canvas c) {
	if(c == null) {
	    return; // wtf...
	}

        // if elements are empty, might as well clear all the pixels...
        if(m_viewManager == null) {
            Log.w("RenderElementBlitter", "No view manager!");
            c.drawColor(Color.RED);
            return;
        }

	// just in case view got resized
	initBitmap();

        long startTime = System.nanoTime();

        // start rendering here

        // create tmp bitmap to render to
        Bitmap tbm = null;
        List<Element> data = m_viewManager.getView();

	Log.d("RenderElementBlitter", "Rendering " + data.size() +
	      " elements!");
        for(int i = data.size()-1; i >= 0; i--) {
            // render the bitmap
	    tbm = m_renderElementCache.getUnchecked(data.get(i)).getRenderedElement();

            if(tbm == null) break;

            // blit bitmap to canvas
            m_cbm.drawBitmap(tbm, m_cbm.getWidth()-data.size()+i-1, 0, null);
        }

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
