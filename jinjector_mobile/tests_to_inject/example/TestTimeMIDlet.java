package example;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.StringItem;

import j2meunit.framework.Test;
import j2meunit.framework.TestMethod;
import j2meunit.framework.TestSuite;

import com.google.test.jinjector.j2me.J2meTestCase;
import com.google.test.jinjector.j2me.J2meUtil;
import com.google.test.jinjector.j2me.wrapper.FormWrapper;
import com.google.test.jinjector.j2me.wrapper.StringItemWrapper;

/**
 * System tests using JInjector for Sun's sample Time MIDlet.
 * 
 * As the application is very simple one test is enough to demonstrate the
 * basic approach of writing a system test using JInjector's LCDUI decorators.
 *
 * @author Julian Harty
 */
public class TestTimeMIDlet extends J2meTestCase {

	public TestTimeMIDlet() {
	}

	public TestTimeMIDlet(String name, TestMethod method) {
		super(name, method);
	}

	public TestTimeMIDlet(String name) {
		super(name);
	}

	/** 
	 * Tests the GetTime feature of the application.
	 * 
	 * This test includes several asserts and debug statements to provide
	 * ideas on how to debug your tests. You can remove them to optimize the
	 * tests.
	 * @throws InterruptedException 
	 */
	public void testGetTime() throws InterruptedException {
		assertNotNull("Midlet should not be null", midlet);
		Displayable mainScreen = J2meUtil.waitAndGetDisplayableOfType(midlet, Form.class); 
		FormWrapper mainForm = (FormWrapper) mainScreen;
		assertNotNull("Form should not be null.", mainForm);
		assertEquals("The form title should be Time Demo.", "Time Demo", mainForm.getTitle());
		
		J2meUtil.fireCommand(mainScreen, mainForm.getAllCommands()[1].getLabel());
		// Allow time for the new form to be displayed
		Thread.sleep(500L);
		
		// A new form should be displayed
		Displayable timeScreen = J2meUtil.waitAndGetDisplayableOfType(midlet, Form.class);
		FormWrapper timeForm = (FormWrapper) timeScreen;
		assertEquals("The form title should be Time Client.", "Time Client", timeForm.getTitle());
		StringItemWrapper timeSiw = null;

		for (int i = 0; i < timeForm.size(); i++) {
			System.out.println(i + ": " + timeForm.get(i).toString());
			if (timeForm.get(i) instanceof StringItem) {
				System.out.println("Found the String that should contain the time");
				timeSiw = (StringItemWrapper) timeForm.get(i);
				break;
			}
		}

		/*
		 *  Here we are assuming the response has been received already. If the
		 *  test is flaky e.g. when servers take longer to respond, the test
		 *  can periodically poll the MIDlet until the string has been modified
		 *  i.e. updated with the time...
		 */ 
		String timeString = timeSiw.getText();
		
		assertTrue("The string should include NIST (the source of the time)",
				timeString.indexOf("NIST") > -1);
		// You can add more asserts here e.g. to compare our time with NIST's.
	}
	
	public Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTest(new TestTimeMIDlet("system tests"){
			public void runTest() throws InterruptedException {
				testGetTime();
			}
		});
		return suite;
	}
}
