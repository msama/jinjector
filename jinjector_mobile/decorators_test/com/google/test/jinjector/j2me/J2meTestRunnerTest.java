/* Copyright 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.test.jinjector.j2me;

import com.google.test.jinjector.FlaggableTestCase;

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import j2meunit.framework.Test;
import j2meunit.framework.TestCase;
import j2meunit.framework.TestMethod;
import j2meunit.framework.TestSuite;

/**
 * Unit test for J2meTestRunner.
 * 
 * @author Michele Sama
 *
 */
public class J2meTestRunnerTest extends TestCase {

  private static final int NUM_TESTS_IN_SUITE = 10;
  
  /**
   * Creates an instance of this class.
   */
  public J2meTestRunnerTest() {
  }

  /**
   * Creates an instance of this class which will execute the 
   * specified {@link TestMethod}.
   * 
   * @param name The name to assign to this test.
   * @param method The {@link TestMethod} to execute
   */
  public J2meTestRunnerTest(String name, TestMethod method) {
    super(name, method);
  }

  /**
   * Creates an instance of this class.
   * 
   * @param name The name to assign to this test.
   */
  public J2meTestRunnerTest(String name) {
    super(name);
  }
  
  /**
   * Tests {@link J2meTestRunner} by creating a {@link J2meTestRunner} with a 
   * <code>null</code> {@link MIDlet}.
   * 
   * <p> A case of a correct instantiation is difficult to be tested because 
   * creating an instance of {@link MIDlet} will produce a 
   * {@link MIDletStateChangeException}. 
   */
  public void testDoRun_nullMIDlet() {
    final TestSuite suite = createTestSuite();
    
    // Creates a test case which will be passed to the runner.
    J2meTestCase allTests = new J2meTestCase() {
      public Test suite() {
        return suite;
      }
    };
    try {
      J2meTestRunner runner = new J2meTestRunner(null, allTests);
      fail("null MIDlet should have thrown an exception.");
    } catch (IllegalArgumentException expected) {
      // OK!!
    } catch (RuntimeException e) {
      fail(e.getMessage());
    }
  }
  
  /**
   * Tests 
   * {@link J2meTestRunner#setMidletToNestedTests(Test)}
   * .
   * 
   * <p> This test adds a {@link TestCase} which is not an instance of 
   * {@link J2meTestCase} to the suite and checks that it will raise an 
   * exception.
   */
  public void testSetMidlet_wrongChildren() {
    final TestSuite suiteUnderTest = new TestSuite();
    suiteUnderTest.addTest(FlaggableTestCase.createPassingTest());
    suiteUnderTest.addTest(FlaggableTestCase.createPassingTest());
    suiteUnderTest.addTest(FlaggableTestCase.createPassingTest());
    suiteUnderTest.addTest(FlaggableTestCase.createPassingTest());
    
    // Creates a test case which will be passed to the runner.
    J2meTestCase allTests = new J2meTestCase() {
      public Test suite() {
        return suiteUnderTest;
      }
    };
    
    try {
      J2meTestRunner runner = new J2meTestRunner(allTests);
      runner.setMidletToNestedTests(suiteUnderTest);
      fail("setMidletToNestedTests should have raise an exception while " +
          "exploring test not for J2me. ");
    } catch (IllegalStateException expected) {
      // OK!!
    } catch (RuntimeException e) {
      fail(e.getMessage());
    } 
  }
  
  /**
   * Creates a {@link TestSuite} containing {@link #NUM_TESTS_IN_SUITE} 
   * test cases.
   * 
   * @return an instance of {@link TestSuite}.
   */
  private static TestSuite createTestSuite() {
    TestSuite suite = new TestSuite();
    for (int i = 0; i < NUM_TESTS_IN_SUITE; i++) {
      suite.addTest(new J2meTestCase());
    }
    return suite;
  }
  
  /**
   * @see j2meunit.framework.TestCase#suite()
   */
  public Test suite() {
    TestSuite suite = new TestSuite();

    suite.addTest(new J2meTestRunnerTest("testDoRun_nullMIDlet") {
      public void runTest() {
        testDoRun_nullMIDlet();
      }
    });
    
    suite.addTest(new J2meTestRunnerTest("testSetMidlet_wrongChildren") {
      public void runTest() {
        testSetMidlet_wrongChildren();
      }
    });
    return suite;
  }
  
}
