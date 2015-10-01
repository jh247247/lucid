package com.lightspeed.gpr.test;

import com.lightspeed.gpr.lib.Element;
import com.lightspeed.gpr.lib.DataInputInterface;
import java.util.List;
import java.util.ArrayList;

public class TestDataInput implements DataInputInterface {
    int m_currentIndex = 0;
    boolean m_opened = false;
    ArrayList<Integer> m_accesses = new ArrayList<Integer>();;

    public TestDataInput() {

    }

    /**
     * Gets the current index relative to the start of the file buffer
     */
    public int getCurrentIndex() {
	return m_currentIndex;
    }

    /**
     * This should rewind the data by some given amount, trying a
     * local buffer (hopefully) then resorting to file if it doesn't exist.
     */
    public Element getElement(int index) {
	if(!exists(index) || !m_opened) {
	    System.out.println("Index: " + index +
			       " does not exist yet! " +
			       "Max is currently: " + m_currentIndex);
	    return null;
	}
	Element ret = new Element(1);
	ret.setSample(0, index);

	// new index, add to list
	if(m_accesses.size() == m_currentIndex) {
	    System.out.println("New index: " + index);
	    m_currentIndex++;
	    m_accesses.add(1);
	} else { // older index, increment
	    System.out.println("Old index: " + index +
			       " access no: " + m_accesses.get((int)index));
	    m_accesses.set((int)index, m_accesses.get((int)index)+1);
	}

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
    public boolean exists(int index) {
	return index <= m_currentIndex+1;
    }

    ///////////////////////////
    // HERE BE TESTING STUFF //
    ///////////////////////////

    public boolean opened() {
	return m_opened;
    }

    public List<Integer> getIndexAccesses() {
	return m_accesses;
    }
}
