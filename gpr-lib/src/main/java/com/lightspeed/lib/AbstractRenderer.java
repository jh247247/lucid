package com.lightspeed.gpr.lib;

import java.util.List;

import com.lightspeed.gpr.lib.Element;
import com.google.common.util.concurrent.ListenableFuture;

public abstract class AbstractRenderer {
    public abstract void setViewManager(AbstractViewManager viewman);
    public abstract void render();
    public abstract void cache(List<ListenableFuture<Element>> l);
    public abstract int getWidth();
    public abstract int getHeight();

    /**
     * These object is sent via the eventbus whenever the surface in
     * the viewport is changed
     */
    static public class SurfaceChangedEvent {
        public final int w;
        public final int h;

        public SurfaceChangedEvent(int wi, int hi) {
            w = wi;
            h = hi;
        }
    }

    static public class SurfaceScrolledEvent {
        public final double dX;
        public final double dY;

        public SurfaceScrolledEvent(double x, double y) {
            dX = x;
            dY = y;
        }
    }

    static public class ResetScrollEvent {
        public ResetScrollEvent() {}
    }

    static public class SurfaceIdleStartEvent {
	public SurfaceIdleStartEvent() {}
    }
}
