package com.lightspeed.GPR1;

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

import de.greenrobot.event.EventBus;

public class RenderElementBlitter {
    static final String LOGTAG = "RenderElementBlitter";

    static final int MAX_PIXEL_SIZE = 3;

    List<RenderElement> m_elementsToRender = null;
    int m_maxElements;
    Bitmap m_bm;
    Canvas m_cbm;
    Paint m_paint;

    SurfaceHolder m_surfHold;

    public RenderElementBlitter(List<RenderElement> rearr) {
        m_elementsToRender = rearr;
        // TODO: Throw exception if rearr is null
        m_maxElements = 0; // auto scale...
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

    public RenderElementBlitter(List<RenderElement> rearr,
                                int maxElements) {
        this(rearr);
        m_maxElements = maxElements;
    }

    public void setMaxElements(int max) {
        m_maxElements = max;
    }

    public int getMaxElements() {
        return m_maxElements;
    }

    public void setSurfaceHolder(SurfaceHolder s) {
        m_surfHold = s;
    }

    public int getMaxElementLength() {
        int len = -1;
        for (RenderElement el : m_elementsToRender) {
            len = Math.max(len, el.getElementHeight());
        }
        return len;
    }

    public void onEvent(RenderView.SurfaceChangedEvent e) {
        resizeBitmapToData(e.w,e.h);
    }

    private void resizeBitmapToData(int w, int h) {
        // get max height of element
        int maxh = getMaxElementLength();
        int maxw = Math.max(m_maxElements,
                            m_elementsToRender.size());

        // bitmap doesn't exist or dims change
        if(m_bm == null ||
           maxh != m_bm.getHeight() ||
           maxw != m_bm.getWidth()) {
            Log.d(LOGTAG, "Making bitmap of " + maxw + " " + maxh);
            Bitmap.Config conf = Bitmap.Config.ARGB_8888;
            m_bm = Bitmap.createBitmap(maxw,
                                       maxh,conf);
            m_cbm = new Canvas(m_bm);

            // not required, but if we have a holder, get the GPU to
            // do some of the resizing..
            if(m_surfHold != null) {
                m_surfHold.setFixedSize(maxw,
                                        maxh*Math.max(m_maxElements,
                                                      m_elementsToRender.size()));
            }
        }
    }

    public void blitToCanvas(Canvas c) {
        Log.d("RenderElementBlitter", "Trying to render!");
        if(m_elementsToRender == null) {
            // cannot render...
            Log.e("RenderElementBlitter","Cannot render!");
            return;
        }
        // make local copy to elements to render, just in case things
        // change beneath us
        ArrayList<RenderElement> locElementsToRender = null;

        synchronized(m_elementsToRender) {
            locElementsToRender = new ArrayList<RenderElement>(m_elementsToRender);
        }

        // if elements are empty, might as well clear all the pixels...
        if(locElementsToRender.isEmpty()) {
            c.drawColor(Color.BLACK);
            //Log.w("RenderElementBlitter","No elements!");
            return;
        }

        // create bitmap just in case we havent yet
        if(m_bm == null) {
            resizeBitmapToData(c.getWidth(),c.getHeight());
        }


        long startTime = System.nanoTime();

        // start rendering here

        // create tmp bitmap to render to
        Bitmap tbm = null;
        int amountToRender = Math.min(m_maxElements,
                                      locElementsToRender.size()-1);

        //Log.d("RenderElementBlitter","Rendered " + amountToRender + " elements");

        for(int i = amountToRender; i >= 0; i--) {
            // render the bitmap
            tbm = locElementsToRender.get(i).getRenderedElement();
            if(tbm == null) break;

            // blit bitmap to canvas
            m_cbm.drawBitmap(tbm, m_cbm.getWidth()-amountToRender+i-1, 0, null);
        }
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
