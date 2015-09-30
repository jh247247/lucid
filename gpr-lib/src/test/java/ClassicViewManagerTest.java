package ViewManagerTest;

import static org.junit.Assert.*;
import org.junit.*;
import com.lightspeed.gpr.lib.Element;
import com.lightspeed.gpr.lib.Average;
import com.lightspeed.gpr.lib.ClassicViewManager;
import com.lightspeed.gpr.lib.DataInputInterface;
import com.lightspeed.gpr.test.TestDataInput;

import java.util.List;

public class ClassicViewManagerTest {
    @Test
    public void test_instantiation() {
	ClassicViewManager c = new ClassicViewManager();
    }

    @Test
    public void test_getView() {
	ClassicViewManager c = new ClassicViewManager();

	// no input, cannot actually get view
	List<Element> l = c.getView();
	Assert.assertEquals(l.size(), 0);
    }

    @Test
    public void test_caching() {
	ClassicViewManager c = new ClassicViewManager();
	TestDataInput t = new TestDataInput();
	t.open();

	c.setInput(t);
	c.goToIndex(0);
	c.setViewWidth(10);

	// get the view from the manager
	List<Element> l = c.getView();
	Assert.assertEquals(10, l.size());

	// test to see if the indexes have been accessed
	List<Integer> a = t.getIndexAccesses();
	Assert.assertEquals(10, a.size());
	for(int i : a) {
	    Assert.assertEquals(1, i);
	}

	// get the view again, it should be cached
	l = c.getView();

	// make sure the indexes haven't been accessed again...
	Assert.assertEquals(l.size(), 10);
	for(int i : a) {
	    Assert.assertEquals(i, 1);
	}
    }
}
