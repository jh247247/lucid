package com.lightspeed.GPR1;

import com.lightspeed.gpr.lib.DataInputInterface;
import com.lightspeed.gpr.lib.Element;
import java.util.ArrayList;
import java.lang.ref.WeakReference;
import java.lang.Math;

public class RandomDataInput implements DataInputInterface{
    ArrayList<WeakReference<Element>> m_previous; // cbf writing to
    // file

    final int START_ELEMENT = 0;
    final int END_ELEMENT = 255;
    final int MAX_VAL = 255;

    private void trimPrevious() {
        while(m_previous.size() > 0 && // still have elements
	      m_previous.get(m_previous.size()-1).get() == null) { // start element is expired
	    m_previous.remove(m_previous.size()-1);
	}
    }

    public RandomDataInput() {
        m_previous = new ArrayList<WeakReference<Element>>();
    }

    public boolean hasNext() {
	return true;
    }

    public Element getNext(){
	trimPrevious();
	Element ret = new Element(START_ELEMENT,END_ELEMENT);
        for(int i = START_ELEMENT; i < END_ELEMENT; i++) {
            ret.setSample(i,Math.random()*MAX_VAL);
        }
        m_previous.add(0,new WeakReference<Element>(ret));
        return ret;
    }

    public Element getPrevious(int offset) {
        Element ret = null;
	trimPrevious();
	if(offset > m_previous.size()-1) {
	    // would load it from file, but cbf
	    ret = new Element(START_ELEMENT,END_ELEMENT);
	    for(int i = START_ELEMENT; i < END_ELEMENT; i++) {
		ret.setSample(i,Math.random()*MAX_VAL);
	    }
	    return ret;
	}

	if(m_previous.get(offset).get() == null) {
            // would load it from file, but cbf
            ret = new Element(START_ELEMENT,END_ELEMENT);
            for(int i = START_ELEMENT; i < END_ELEMENT; i++) {
                ret.setSample(i,Math.random()*MAX_VAL);
            }
	    return ret;
	}
	return m_previous.get(offset).get();
    }

    public boolean open() {
        return true;
    }

    public void close() {
        return;
    }

    public String getName() {
        return "Random Data";
    }
}
