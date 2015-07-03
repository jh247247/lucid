package DataHandler;

import static org.junit.Assert.*;
import org.junit.*;
import java.util.Iterator;
import com.lightspeed.gpr.lib.Element;

public class ElementTest {
    @Test
    public void test_instantiation() {
        Element e = new Element(255);
        Iterator<Integer> i = e.iterator();
    }

    @Test
    public void test_set() {
	Element e = new Element(1);
	e.setSample(0,1);
	Assert.assertEquals(e.getSample(0),1);
    }

    @Test
    public void test_iterator() {
        Element e = new Element(10);
        for(int i = 0; i < e.getAmountOfSamples(); i++) {
            e.setSample(i,i);
        }
        int curr = 0;
        for (int i : e) {
            Assert.assertEquals(curr++, i);
        }
    }
}
