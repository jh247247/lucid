package com.lightspeed.GPR1;

import com.lightspeed.gpr.lib.Element;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;


public class RenderElement {
    /**
     * Stores the actual data to display
     */
    Element m_data;
    /**
     * Stores the rendered data. Weak reference so that it can be
     * garbage collected on devices with small amounts of memory.
     *
     * Re-rendered upon demand.
     */
    Bitmap m_renderedData;

    public RenderElement(Element e) {
        m_data = e.clone();
        // render the element when this is created just in case
        m_renderedData = null;

    }


    public Bitmap getRenderedElement() {
        Bitmap bm = m_renderedData;
        // data is still stored, return that.
        if(bm != null) {
            return m_renderedData;
        }
        m_renderedData = renderElement();
        return m_renderedData;
    }

    public int getElementHeight() {
	return m_data.getSampleStop();
    }

    private Bitmap renderElement() {
        if(m_data == null) {
            // User must be stupid.
            return null;
        }

        // make the paint, bitmap and canvas so we can actually render
        // the element

        Paint p = new Paint();
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap tmp = Bitmap.createBitmap(1,
                                         m_data.getAmountOfSamples(),
                                         conf);
        Canvas c = new Canvas(tmp);
        for(int i = m_data.getSampleStart();
            i < m_data.getSampleStop(); i++) {
            p.setARGB(255,
                      (int)m_data.getSample(i),
                      (int)m_data.getSample(i),
                      (int)m_data.getSample(i));
	    c.drawPoint(0,i,p);
	}

        return tmp;
    }
}
