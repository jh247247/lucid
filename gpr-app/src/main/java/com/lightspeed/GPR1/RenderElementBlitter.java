package com.lightspeed.GPR1;

import java.util.ArrayList;
import android.graphics.Canvas;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import java.lang.Math;
import android.util.Log;

public class RenderElementBlitter {
    ArrayList<RenderElement> m_elementsToRender = null;
    int m_maxElements;
    Bitmap m_bm;
    Canvas m_cbm;

    public RenderElementBlitter(ArrayList<RenderElement> rearr) {
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
	ArrayList<RenderElement> locElementsToRender = (ArrayList<RenderElement>)m_elementsToRender.clone();

	int maxh = 0;
        for (RenderElement el : locElementsToRender) {
            maxh = Math.max(maxh, el.getElementHeight());
        }

        int maxw = Math.max(m_maxElements,
                            locElementsToRender.size());

        // bitmap doesn't exist or dims change
        if(m_bm == null ||
           maxh != m_bm.getHeight() ||
           m_bm.getWidth() != c.getWidth()) {
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
