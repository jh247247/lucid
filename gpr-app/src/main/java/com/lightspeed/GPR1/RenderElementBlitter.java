package com.lightspeed.GPR1;

import com.lightspeed.gpr.lib.Element;
import com.lightspeed.gpr.lib.AbstractViewManager;
import com.lightspeed.gpr.lib.AbstractRenderer;
import com.lightspeed.gpr.lib.ThreadPoolHandler;


import android.graphics.Canvas;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.Paint;
import android.graphics.Matrix;
import android.graphics.Color;
import android.view.SurfaceHolder;
import android.util.Log;
import android.view.View;

import java.lang.Runtime;
import java.util.ArrayList;
import java.util.List;
import java.lang.Math;
import java.util.LinkedList;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ListeningExecutorService;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;


public class RenderElementBlitter extends AbstractRenderer {
    static final String LOGTAG = "RenderElementBlitter";

    static final int CACHE_SIZE = 3000;

    Bitmap m_bm;
    Canvas m_cbm;
    Paint m_paint;

    WeakReference<View> m_renderView;
    AbstractViewManager m_viewManager;

    LoadingCache<ListenableFuture<Element>,
        ListenableFuture<RenderElement>> m_renderElementCache =
        CacheBuilder.newBuilder()
        .maximumSize(CACHE_SIZE)
        .build(new CacheLoader<ListenableFuture<Element>, ListenableFuture<RenderElement>>() {
                @Override public ListenableFuture<RenderElement> load(ListenableFuture<Element> e) {
                    final SettableFuture<RenderElement> re = SettableFuture.create();

                    Futures.addCallback(e, new FutureCallback<Element>() {
                            @Override
                            public void onSuccess(Element e) {
                                re.set(new RenderElement(e));
                                try {
                                    ListenableFuture<Bitmap> fbm = ThreadPoolHandler.submit(re.get());
				    Futures.addCallback(fbm, new FutureCallback<Bitmap>(){
					    @Override
					    public void onSuccess(Bitmap b) {
						render();
					    }
					    @Override
					    public void onFailure(Throwable t) {
						// doubt we'll get here
					    }
					});
                                }
                                catch(Exception ex) {
                                    // todo I guess...
                                    Log.e("RenderElementBlitter",
                                          "Failed to render element: " + ex);
                                }
                            }
                            @Override
                            public void onFailure(Throwable thrown) {
                                // todo I guess...
                                Log.e("RenderElementBlitter",
                                      "Failed to load element!" + thrown);
                            }
                        });
                    return re;
                }
            }
            );

    public RenderElementBlitter() {
        m_bm = null;
        m_cbm = null;
        m_viewManager = null;
        m_renderView = new WeakReference(null);

        // disable filtering...
        m_paint = new Paint();
        m_paint.setAntiAlias(false);
        m_paint.setFilterBitmap(false);
        m_paint.setColor(Color.BLACK);
    }

    @Override
    public void setViewManager(AbstractViewManager viewman) {
        m_viewManager = viewman;
        m_viewManager.setRenderer(this);
        initBitmap();

        // todo: caching...
    }

    @Override
    public void render() {
        if(m_renderView.get() != null) {
            m_renderView.get().postInvalidate();
        }
    }


    public void setRenderView(View v) {
        m_renderView = new WeakReference<View>(v);
    }


    public void initBitmap() {
        // create bitmap just in case we havent yet
        if(m_viewManager == null) return;

        if(m_bm == null ||
           m_bm.getWidth() != m_viewManager.getViewWidth() ||
           m_bm.getHeight() != m_viewManager.getViewHeight()) {
            Bitmap.Config conf = Bitmap.Config.ARGB_8888;
            m_bm = Bitmap.createBitmap(m_viewManager.getViewWidth(),
                                       m_viewManager.getViewHeight(),
                                       conf);
            m_cbm = new Canvas(m_bm);
        }
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
        Future<Bitmap> fbm = null;
        ListenableFuture<RenderElement> re = null;
        List<ListenableFuture<Element>> data = m_viewManager.getView();

        Log.d("RenderElementBlitter", "Rendering " + data.size() +
              " elements!");

        for(int i = data.size()-1; i >= 0; i--) {
            // check if element is actually loaded, if not, skip.

            re = m_renderElementCache.getUnchecked(data.get(i));
            if(re == null || !re.isDone()) continue;
            try {
                if(re.get().isRendered()) {
                    m_cbm.drawBitmap(re.get().getRenderedElement(),
                                     m_cbm.getWidth()-data.size()+i-1, 0, null);
                } else {
                    m_cbm.drawRect(m_cbm.getWidth()-data.size()+i-1,
                                   0,
                                   m_cbm.getWidth()-data.size()+i,
                                   m_cbm.getHeight(),
                                   m_paint);
                }
            }
	    catch (Exception ex){
		Log.e("RenderElementBlitter","Error while rendering: " + ex);
	    }

        }

        Log.d("RenderElementBlitter",m_bm.getWidth() + " x " + m_bm.getHeight());


        Matrix matrix = new Matrix();
        matrix.postScale(((float)c.getWidth()/(float)m_bm.getWidth()),
                         ((float)c.getHeight()/(float)m_bm.getHeight()));

        c.drawBitmap(m_bm,matrix,m_paint);

        long endTime = System.nanoTime();
        long diff = (endTime-startTime)/1000000;
        //if(diff > 16) {
            Log.w("RenderElementBlitter","Scene in: " + diff);
	    //}
    }

    @Override
    public void cache(List<ListenableFuture<Element>> l) {
	for(ListenableFuture<Element> fe : l) {
	    try {
		m_renderElementCache.get(fe);
	    }
	    catch(Exception ex) {
		// todo...
	    }
	}
    }

    /////////////////////
    // PRIVATE METHODS //
    /////////////////////
}
