package com.gecko.gpr.gfx;

import com.annimon.stream.Stream;
import com.lightspeed.gpr.lib.Element;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import com.lightspeed.gpr.lib.cache.LoadingCache;
import android.graphics.BitmapFactory;
import com.lightspeed.gpr.lib.cache.LoadingCache.CacheLoader;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.lang.Thread;
import java.lang.Runnable;
import java.util.concurrent.Callable;



public class RenderElement implements Callable<Bitmap> {
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

    // static lookup table for color->paint
    static LoadingCache<Integer,Paint> m_paintCache =
        new LoadingCache<Integer,Paint>(300,
                                        new CacheLoader<Integer,Paint> (){
                                            @Override
                                            public Paint load(Integer i) {
                                                Paint p = new Paint();
                                                p.setARGB(255,i,i,i); // TODO: custom colors?
                                                return p;
                                            }
                                        });

    public RenderElement(Element e) {
        if(e != null) {
            // why... Maybe throw exception here?
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

    private void renderElement() {
        if(m_data == null ||
           (m_renderedData != null)) {
            return;
        }

        Log.d("RENDERELEMENT","Rendering " +
              m_data.getAmountOfSamples() + " points...");

        // make the paint, bitmap and canvas so we can actually render
        // the element
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap tmp = Bitmap.createBitmap(1,
                                         m_data.getAmountOfSamples(),
                                         conf);

        Canvas c = new Canvas(tmp);
        Stream.ofRange(m_data.getSampleStart(), m_data.getSampleStop())
            .forEach((i) -> {
                    int col = m_data.getSample(i);
                    c.drawPoint(0,i,m_paintCache.get(col));
                });

        m_renderedData = tmp;
        m_rendered.set(true);
        Log.d("RENDERELEMENT","Done!");
    }

    @Override
    public Bitmap call() {
        return getRenderedElement();
    }

}
