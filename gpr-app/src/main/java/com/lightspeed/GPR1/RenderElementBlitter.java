package com.lightspeed.GPR1;

import java.util.ArrayList;
import java.util.List;
import android.graphics.Canvas;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import java.lang.Math;
import android.util.Log;
import java.util.LinkedList;

public class RenderElementBlitter {
    static final String LOGTAG = "RenderElementBlitter";

    List<RenderElement> m_elementsToRender = null;
    int m_maxElements;
    Bitmap m_bm;
    Canvas m_cbm;

    public RenderElementBlitter(List<RenderElement> rearr) {
        m_elementsToRender = rearr;
        // TODO: Throw exception if rearr is null
        m_maxElements = 0; // auto scale...
        m_bm = null;
        m_cbm = null;
    }

    public RenderElementBlitter(ArrayList<RenderElement> rearr,
                                int maxElements) {
        this(rearr);
        m_maxElements = maxElements;
    }

    public void setMaxElements(int max) {
        m_maxElements = max;
    }



    public void blitToCanvas(Canvas c) {
        if(m_elementsToRender == null ||
           m_elementsToRender.size() == 0) {
            // cannot render...
            return;
        }
        // make local copy to elements to render, just in case things
        // change beneath us
        List<RenderElement> locElementsToRender = null;

        synchronized(m_elementsToRender) {
            locElementsToRender = new LinkedList<RenderElement>(m_elementsToRender);
        }


        // get max height of element
        int maxh = 0;
        for (RenderElement el : locElementsToRender) {
            maxh = Math.max(maxh, el.getElementHeight());
        }

        int maxw = Math.max(m_maxElements,
                            locElementsToRender.size());

        // bitmap doesn't exist or dims change
        if(m_bm == null ||
           maxh != m_bm.getHeight() ||
           maxw != m_bm.getWidth()) {
            Log.d(LOGTAG, "Making bitmap of " + maxw + " " + maxh);
            Bitmap.Config conf = Bitmap.Config.ARGB_8888;
            m_bm = Bitmap.createBitmap(maxw,
                                       maxh,conf);
            m_cbm = new Canvas(m_bm);
        }



        long startTime = System.nanoTime();

        // start rendering here

        // create tmp bitmap to render to
        Bitmap tbm = null;
        for(int i = Math.min(m_maxElements,
                             locElementsToRender.size()-1); i >= 0; i--) {
            // render the bitmap
            tbm = locElementsToRender.get(i).getRenderedElement();
            if(tbm == null) break;

            // blit bitmap to canvas
            m_cbm.drawBitmap(tbm, i, 0, null);
        }
        // stop rendering

        Matrix matrix = new Matrix();
        matrix.postScale(((float)c.getWidth()/(float)m_bm.getWidth()),
                         ((float)c.getHeight()/(float)m_bm.getHeight()));

        c.drawBitmap(m_bm,matrix,null);

        long endTime = System.nanoTime();
        long diff = (endTime-startTime)/1000000;
        if(diff > 16) {
            Log.w("RenderElementBlitter","Scene in: " + diff);
        }

    }

}
