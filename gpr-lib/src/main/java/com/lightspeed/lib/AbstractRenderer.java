package com.lightspeed.gpr.lib;

public abstract class AbstractRenderer {
    public abstract void setViewManager(AbstractViewManager viewman);
    public abstract void render();

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
}
