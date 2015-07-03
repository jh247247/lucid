package com.lightspeed.gpr.lib;

/**
 * This is just an interface for generic processing of incoming
 * data. Doesn't need to be super complicated.
 */

public interface Filter {
    /**
     * Take in one element of data, process it and return a
     * (hopefully) modified one. Elements shouldn't grow to take too
     * long to process, although this interface may need to be
     * modified in the future to handle async returns. (multithreading)
     */
	Element process(Element data);
};
