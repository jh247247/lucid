package AverageTest;

import static org.junit.Assert.*;
import org.junit.*;
import java.util.Iterator;
import com.lightspeed.gpr.lib.Element;
import com.lightspeed.gpr.lib.Average;
import java.lang.Math;


public class AverageTest {
    @Test
    public void test_instantiation() {
        Average a = new Average();
    }

    @Test
    public void test_process() {
        Average a = new Average();
        a.setAmountToAverage(2);

        // make some random element, don't care
        Element e1 = new Element(0, 5);
        for (int i = 0; i < 5; i++) {
            e1.setSample(i,(double)i);
        }

        // process random element, result for first one should be 0
        Element ret = a.process(e1);
        for (double d : ret) {
            Assert.assertEquals(d,0,0.001);
        }

        // change element
        for (int i = 0; i < 5; i++) {
            e1.setSample(i,(double)(i+1));
        }

        // process changed element
        ret = a.process(e1);
        for (double d : ret) {
            Assert.assertEquals(d,1,0.001);
        }
    }

    @Test
    public void test_resize() {
        Average a = new Average();
        a.setAmountToAverage(2);

        // make some random element, don't care
        Element e1 = new Element(0, 5);
        Element e2 = new Element(10, 20);

        // process random element, result for first one should be 0
        a.process(e1);
        Element ret = a.process(e2);

        Assert.assertEquals(e2.getSampleStart(),
			    ret.getSampleStart());

        Assert.assertEquals(e2.getSampleStop(),
			    ret.getSampleStop());

    }
}
