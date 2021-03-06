package com.lightspeed.gpr.lib;
import java.util.Queue;
import java.util.LinkedList;


public class Average implements Filter{
    /**
     * Set the default amount to average, since filters don't get
     * called with constructor arguments
     */
    final public int DEFAULT_AMOUNT_TO_AVERAGE = 100;

    /**
     * Keep a count of the amount of elements to average
     */
    int m_amountToAverage;

    /**
     * Keep a vector of the previous elements to average and remove
     * from incoming elements
     */
    Queue<Element> m_averageQueue;

    /**
     * This is the running average, as in the average of all current
     * elements.
     * Should be updated whenever the buffer has an element added or removed.
     */
    Element m_runningAverage;

    /**
     * Floating point errors add up, recalculate every
     * m_amountToAverage elements to mitigate this
     * This keeps a count of the amount stored since the last recalculation.
     */
    int m_elementCount;

    public Average() {
        m_amountToAverage = DEFAULT_AMOUNT_TO_AVERAGE;
        m_runningAverage = null;
        m_elementCount = 0;
        m_averageQueue = new LinkedList<Element>();
    }
    /**
     * resizes the incoming element to encompass the ranges of both the incoming data and the current average
     */
    private void resizeRunningAverage(Element data) {
        // running average is not init'd, use the first element to
        // seed it
        if(m_runningAverage == null) {
            m_runningAverage = (Element) data.clone();
            return;
        }

        // new element is longer than the current average!
        // have to resize and move data.
        if(data.getSampleStart() < m_runningAverage.getSampleStart() ||
           data.getSampleStop() > m_runningAverage.getSampleStop()) {
            // make tmp element to copy...
            Element tmp = new Element(Math.min(data.getSampleStart(),
                                               m_runningAverage.getSampleStart()),
                                      Math.max(data.getSampleStop(),
                                               m_runningAverage.getSampleStop()));
            // copy old average to new
            for (int i = m_runningAverage.getSampleStart();
                 i < m_runningAverage.getSampleStop(); i++) {
                tmp.setSample(i,m_runningAverage.getSample(i));
            }
            m_runningAverage = tmp;
        }
    }


    public Element process(Element data) {
        // check if the running average has to be resized to fit the new data first...
        resizeRunningAverage(data.clone());

        // make element to return, should I make element cloneable?
        Element ret = new Element(data.getSampleStart(),
                                  data.getSampleStop());

        // set return element to incoming element minus running average.
        for (int i = ret.getSampleStart(); i < ret.getSampleStop(); i++) {
            ret.setSample(i, data.getSample(i)
                          - m_runningAverage.getSample(i));
        }


        // modify running average using incoming data
        for (int i = data.getSampleStart(); i < data.getSampleStop(); i++) {
            m_runningAverage.setSample(i, m_runningAverage.getSample(i)
                                       + (m_runningAverage.getSample(i)
                                          - data.getSample(i))/m_amountToAverage);
        }

	// add the data to the running average queue
	addToQueue(data);

	// remove floating point errors by recalculating average
	// instead of running average
	if(m_elementCount > m_amountToAverage) {
	    recalculateAverage();
	    m_elementCount = 0;
	} else {
	    m_elementCount++;
	}

        return ret;
    }

    private void addToQueue(Element data) {
        // add incoming data to buffer of average
        m_averageQueue.add(data);

        // amount of elements exceeds set amount to average
        if(m_averageQueue.size() > m_amountToAverage) {
            // take away the running average of the last element
            Element tmp = m_averageQueue.remove();
            for (int i = m_runningAverage.getSampleStart();
                 i < m_runningAverage.getSampleStop(); i++) {
                m_runningAverage.setSample(i,
                                           m_runningAverage.getSample(i)
                                           - tmp.getSample(i)/m_amountToAverage);
            }
        }

    }

    /**
     * Set the amount of data to keep and recalculate the average.
     */

    public void setAmountToAverage(int amountToAverage) {
        m_amountToAverage = amountToAverage;
        while(m_averageQueue.size() > m_amountToAverage)  {
            // remove and don't care about changing the running
            // average, it is going to be recalculated anyway.
            m_averageQueue.remove();
        }
        recalculateAverage();
    }

    /**
     * this recalculates the average by making a new Element and
     * iterating over the entire backlog of data, calculating the average
     * as it goes. Kind of a brute force method, so try to use sparingly.
     */
    private void recalculateAverage() {
        if(m_runningAverage == null) {
            // running average not calculated yet, cannot recalculate!
            return;
        }
        Element tmp = new Element(m_runningAverage.getSampleStart(),
                                  m_runningAverage.getSampleStop());
        // iterate over all the stored elements
        for (Element e : m_averageQueue) {
            for (int i = e.getSampleStart(); i < e.getSampleStop(); i++) {
                tmp.setSample(i, tmp.getSample(i) + e.getSample(i)/m_amountToAverage);
            }
        }
    }
}
