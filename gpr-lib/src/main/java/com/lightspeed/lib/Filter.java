package com.lightspeed.gpr.lib;



public abstract class Filter {
    /**
     * Parent class gets typecast to this, so when data gets processed
     * it can be passed into the next filter that is enabled.
     */
    public interface FilterCallBack {

    }

};
