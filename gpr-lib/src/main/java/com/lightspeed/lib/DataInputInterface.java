package com.lightspeed.gpr.lib;
import com.lightspeed.gpr.lib.Element;

public interface DataInputInterface {

    /**
     * Attempts to open an interface, hide all the inner workings so
     * it can be a file, serial port or whatevs. Should be able to put
     * up it's own interface to work. ASSUME THIS BLOCKS
     */
    public boolean open();

    /**
     * Closes the opened interface. Should there be anything special
     * about this?
     */
    public void close();

    /**
     * Gets the current index relative to the start of the file buffer
     */
    public long getCurrentIndex();

    /**
     * This should rewind the data by some given amount, trying a
     * local buffer (hopefully) then resorting to file if it doesn't exist.
     */
    public Element getElement(long index);

    /**
     * Returns true if the given index exists.
     */
    public boolean exists(long index);

    /**
     * Gets the name of the data input, used for gui interaction like opening/closing.
     * For example, should return "File" or "Serial" so we can go open->File
     */
    public String getName();
}
