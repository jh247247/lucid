package com.lightspeed.gpr.lib;

import java.util.List;

import com.lightspeed.gpr.lib.Element;

public abstract class AbstractRenderer {
    public abstract void setViewManager(AbstractViewManager viewman);
    public abstract void render();
    public abstract void cache(List<Element> l);

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
        public ResetScrollEvent() {

        }
    }

}
