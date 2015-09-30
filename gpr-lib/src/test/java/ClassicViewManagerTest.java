package ViewManagerTest;

import static org.junit.Assert.*;
import org.junit.*;
import com.lightspeed.gpr.lib.Element;
import com.lightspeed.gpr.lib.Average;
import com.lightspeed.gpr.lib.ClassicViewManager;
import com.lightspeed.gpr.lib.DataInputInterface;

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
}
