package com.lightspeed.gpr.lib;
import com.lightspeed.gpr.lib.Element;

public abstract class AbstractDataInput {
    protected NewElementListener m_elementListener = null;

    /**
     * Attempts to open an interface, hide all the inner workings so
     * it can be a file, serial port or whatevs. Should be able to put
     * up it's own interface to work. ASSUME THIS BLOCKS
     */
    public abstract boolean open();

    /**
     * Closes the opened interface. Should there be anything special
     * about this?
     */
    public abstract void close();

    /**
     * Gets the current index relative to the start of the file buffer
     */
    public abstract int getCurrentIndex();

    /**
     * This should rewind the data by some given amount, trying a
     * local buffer (hopefully) then resorting to file if it doesn't exist.
     */
    public abstract Element getElement(int index);

    /**
     * Returns true if the given index exists.
     */
    public abstract boolean exists(int index);

    /**
     * Gets the name of the data input, used for gui interaction like opening/closing.
     * For example, should return "File" or "Serial" so we can go open->File
     */
    public abstract String getName();

    public void setNewElementListener(NewElementListener e) {
	m_elementListener = e;
    }

    public interface NewElementListener {
	public void onNewElement(Element e);
    }
}
