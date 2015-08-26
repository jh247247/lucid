package com.lightspeed.GPR1;

import com.lightspeed.gpr.lib.Element;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import java.util.concurrent.atomic.AtomicBoolean;
import java.lang.Thread;
import java.lang.Runnable;

import android.graphics.BitmapFactory;


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
    AtomicBoolean m_rendered;

    public RenderElement(Element e) {
        if(e != null) {
            // why...
            m_data = e.clone();
        }
        // render the element when this is created just in case
        m_renderedData = null;
        m_rendered = new AtomicBoolean();
    }

    public boolean isRendered() {
        return m_rendered.get();
    }

    public Bitmap getRenderedElement() {
        // data is still stored, return that.
        if(m_rendered.get()) {
            return m_renderedData;
        }
        renderElement();

        return m_renderedData;
    }

    public int getElementHeight() {
        return m_data.getSampleStop();
    }

    public void recycleBitmap() {
        if(m_renderedData != null){
            m_renderedData.recycle();
            m_rendered.set(false);
        }
    }

    public void renderElement() {
        if(m_data == null ||
           (m_renderedData != null)) {
            return;
        }

        Log.d("RenderElement", "RENDERING");
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
        m_renderedData = tmp;
        m_rendered.set(true);
        Log.d("RenderElement", "DONE RENDERING");
    }



}
