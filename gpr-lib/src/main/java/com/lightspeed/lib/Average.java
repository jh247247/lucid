package com.lightspeed.gpr.lib;
import java.util.Vector;


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
    Vector<Element> m_averageBuffer;

    /**
     * This is the running average, as in the average of all current
     * elements.
     * Should be updated whenever the buffer has an element added or removed.
     */
    Element m_runningAverage;

    public Average() {
        m_amountToAverage = DEFAULT_AMOUNT_TO_AVERAGE;
        m_runningAverage = null;
    }


    public Element process(Element data) {
        // running average is not init'd, set it up with the length of
        // the incoming data.
        if(m_runningAverage == null) {
            m_runningAverage = new Element(data.getAmountOfSamples());
        }

        // new element is longer than the current average!
        // have to resize and move data.
        if(data.getAmountOfSamples() > m_runningAverage.getAmountOfSamples()) {
            Element tmp = new Element(data.getAmountOfSamples());
            for (int i : m_runningAverage) {

            }
        }
        m_averageBuffer.add(data);
	return data; // FIXME: return processed data, placeholder

			 }
}
