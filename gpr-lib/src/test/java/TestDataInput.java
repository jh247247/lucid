package com.lightspeed.gpr.test;
import com.lightspeed.gpr.lib.DataInputInterface;

public class TestDataInput implements DataInputInterface {
    long m_currentIndex = 0;
    boolean m_opened = false;

    public TestDataInput() {

    }

    /**
     * Gets the current index relative to the start of the file buffer
     */
    public long getCurrentIndex() {
	return m_currentIndex;
    }

    /**
     * This should rewind the data by some given amount, trying a
     * local buffer (hopefully) then resorting to file if it doesn't exist.
     */
    public Element getElement(long index) {
	if(!exists(index)) {
	    return null;
	}
	Element ret = new Element(1);
	ret.setSample(0, index);

	return ret;
    }

    /**
     * Attempts to open an interface, hide all the inner workings so
     * it can be a file, serial port or whatevs. Should be able to put
     * up it's own interface to work. ASSUME THIS BLOCKS
     */
    public boolean open() {
	m_opened = true;
	return m_opened;
    }

    /**
     * Closes the opened interface. Should there be anything special
     * about this?
     */
    public void close() {
	m_opened = false;
	return m_opened;
    }

    /**
     * Gets the name of the data input, used for gui interaction like opening/closing.
     * For example, should return "File" or "Serial" so we can go open->File
     */
    public String getName() {
	return "TestDataInput";
    }

    /**
     * Returns true if the given index exists.
     */
    public boolean exists(long index) {
	return index <= m_currentIndex+1;
    }

    ///////////////////////////
    // HERE BE TESTING STUFF //
    ///////////////////////////

    public boolean opened() {
	return m_opened;
    }
}
