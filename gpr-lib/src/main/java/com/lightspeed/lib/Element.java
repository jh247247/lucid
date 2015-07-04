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

    public Element(int sampleStart,
		   int sampleStop) {

        m_sampleStart = sampleStart;
        m_sampleStop = sampleStop;
        m_amountOfSamples = sampleStop-sampleStart;

        m_samples = new int[m_amountOfSamples];
    }

    public Element(int amountOfSamples) {
        this(0, amountOfSamples);
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

    public int getAmountOfSamples() {
        return m_amountOfSamples;
    }

    public int getSampleStart() {
        return m_sampleStart;
    }

    public int getSampleStop() {
        return m_sampleStop;
    }

    public int getSample(int index) throws NoSuchElementException{
        if(index < 0 || index > m_amountOfSamples) {
            throw new NoSuchElementException();
        }
        return m_samples[index];
    }

    public void setSample(int index, int sample)
        throws NoSuchElementException{
        if(index < 0 || index > m_amountOfSamples) {
            throw new NoSuchElementException();
        }
        m_samples[index] = sample;
    }
}
