package com.lightspeed.gpr.lib;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.lang.UnsupportedOperationException;



public class Element implements Iterable<Integer> {
    /**
     * Keep track of the amount of samples in this element
     */
    private int m_amountOfSamples;
    /**
     * This is where the samples start
     * Should obey: 0 < m_sampleStart < m_sampleStop < m_amountOfSamples
     */
    private int m_sampleStart;
    /**
     * This is where the samples stop
     * Should obey: 0 < m_sampleStart < m_sampleStop < m_amountOfSamples
     */
    private int m_sampleStop;
    /**
     * This actually holds the data for the element.
     * Should have a length that is the minimum of:
     * m_sampleStart - m_sampleStop
     * m_amountOfSamples
     */
    private int[] m_samples;

    public Element(int amountOfSamples,
                   int sampleStart,
                   int sampleStop) {

        m_sampleStart = sampleStart;
        m_sampleStop = sampleStop;
        m_amountOfSamples = amountOfSamples;
    }

    public Element(int amountOfSamples) {
        this(amountOfSamples, 0, amountOfSamples);
    }

    public Iterator<Integer> iterator() {
        return new ElementIterator();
    }


    public class ElementIterator implements Iterator<Integer> {
        /**
         * Current sample in element
         */
        int m_current;

        public ElementIterator() {
            if (m_sampleStart < 0) {
                // m_sampleStart was not set, a full sample, starts
                // from 0
                m_current = 0;
            } else {
                m_current = m_sampleStart;
            }
        }

        @Override
        public boolean hasNext() {
            return (m_current < m_sampleStop);
        }

        @Override
        public Integer next() throws NoSuchElementException{
            if(!hasNext()) {
                throw new NoSuchElementException();
            }
            return m_samples[m_current++];
        }

        @Override
        public void remove() throws UnsupportedOperationException{
            throw new UnsupportedOperationException();
        }
    }
}
