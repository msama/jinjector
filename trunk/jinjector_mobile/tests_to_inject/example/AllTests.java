package example;
import j2meunit.framework.Test;
import j2meunit.framework.TestMethod;
import j2meunit.framework.TestSuite;

import com.google.test.jinjector.j2me.J2meTestCase;



public class AllTests extends J2meTestCase {

	public AllTests() {
	}

	public AllTests(String name, TestMethod method) {
		super(name, method);
	}

	public AllTests(String name) {
		super(name);
	}

	public Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTest(new TestTimeMIDlet().suite());
		return suite;
		
	}
}
