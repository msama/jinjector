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

package com.google.test.jinjector.coverage;

import com.google.test.jinjector.coverage.CoverageManager.CoverageDataFile;
import com.google.test.jinjector.coverage.MethodInfoContainer.MethodInfo;

import j2meunit.framework.Test;
import j2meunit.framework.TestCase;
import j2meunit.framework.TestSuite;

/**
 * JUnit test for {@link CoverageManager}
 * 
 * @author Michele Sama
 *
 * @size small
 */
public class CoverageManagerTest extends TestCase {

  protected static final int NUMBER_OF_FILES = 10;
  protected static final int NUMBER_OF_METHODS = 40;
  protected static final int NUMBER_OF_LINES = 10;
  
  protected CoverageManager manager = null;
  
  /**
   * Constructor from superclass.
   */
  public CoverageManagerTest() {
  }

  /**
   * Constructor from superclass.
   */
  public CoverageManagerTest(String name) {
    super(name);
  }

  public void testMethodCanBeMarkedAsCovered() {
    for (int i = 0; i < NUMBER_OF_METHODS; i++) {
      assertTrue("Covered from the beginning.", !manager.isCovered(i));
      CoverageManager.setCovered(i);
      assertTrue("Uncovered after flagging.", manager.isCovered(i));
    }
  }
  
  /**
   * Tests that covering a negative index will raise an exception.
   * 
   * @see CoverageManager#setCovered(int)
   * @see CoverageManager#setCoveredImplementation(int)
   */
  public void testMethodCoveringNegativeSize() {
    try {
      CoverageManager.setCovered(-1);
      fail("Covering a negative index should have thrown an exception.");
    } catch (Exception e) {
      // ok!
    }
  }

  /**
   * Tests that querying a negative index will raise an exception.
   * 
   * @see CoverageManager#isCovered(int)
   */
  public void testQueryingNegativeSize() {   
    try {
      manager.isCovered(-1);
      fail("Querying a negative index should have thrown an exception.");
    } catch (Exception e) {
      // ok!
    }
  }

  public void testSetLineCovered() {
    CoverageManager.setLineCovered(2, 0);
    CoverageManager.setLineCovered(3, 1);

    assertTrue("Method 2, Line 0 should have been covered.", 
        manager.isLineCovered(2, 0));
    assertTrue("Method 3, Line 1 should have been covered.", 
        manager.isLineCovered(3, 1));

    assertTrue("Method 2, Line 1 should not have been covered.", 
        !manager.isLineCovered(2, 1));
    assertTrue("Method 3, Line 0 should not have been covered.",
        !manager.isLineCovered(3, 0));
    assertTrue("Method 3, Line 2 should not have been covered.",
        !manager.isLineCovered(3, 2));
  }

  public void testSettingLineCoveredWithOutOfBoundIndex() {
    try {
      manager.isLineCovered(1000, 0);
      fail("Quering a out of bound index should have thrown an exception.");
    } catch (Exception e) {
      // ok!
    }

    try {
      manager.isLineCovered(1, 1000);
      fail("Quering a out of bound index should have thrown an exception.");
    } catch (Exception e) {
      // ok!
    }
  }

  public void testCoveringNegativeSize() {
    try {
      manager.isLineCovered(-1, 0);
      fail("Quering a negative index should have thrown an exception.");
    } catch (Exception e) {
      // ok!
    }

    try {
      manager.isLineCovered(1, -1);
      fail("Quering a negative index should have thrown an exception.");
    } catch (Exception e) {
      // ok!
    }
  }

  public void testCoverageShouldNotBeSetIfCoverageIsDisabled() {
    CoverageManager.disableCoverage();
    CoverageManager.setCovered(2);

    assertTrue("Line has been covered while collection was disabled.", 
        !manager.isCovered(2));
  }
  
  public void testLineCoverageShouldNotBeSetIfCoverageIsDisabled() {
    CoverageManager.disableCoverage();
    CoverageManager.setLineCovered(0, 0);

    assertTrue("Line has been covered while collection was disabled.", 
        !manager.isLineCovered(0, 0));
  }

  public void testMethodProfilingStoresCorrectNumberOfMethodCalls() {
    CoverageManager.incrementMethodCallCount(2);
    CoverageManager.incrementMethodCallCount(2);
    CoverageManager.incrementMethodCallCount(3);

    assertEquals(2, manager.getNumberOfMethodCalls(2));
    assertEquals(1, manager.getNumberOfMethodCalls(3));
    assertEquals(0, manager.getNumberOfMethodCalls(4));
  }

  /**
   * Creates a new instance of the manager by using a getter method for 
   * testing purposes.
   * 
   * @see j2meunit.framework.TestCase#setUp()
   */
  protected void setUp() throws Exception {
    super.setUp();
    manager = new CoverageManager();
    MethodInfoContainer methods = new MethodInfoContainer();
    for (int i = 0; i < NUMBER_OF_METHODS; i++) {
      MethodInfo dummyMethod = new MethodInfo();
      dummyMethod.fullName = "dummyMethod" + i;
      dummyMethod.methodIndex = i;
      methods.addMethodInfo(dummyMethod);
    }
    
    Bitfield[] fields = new Bitfield[NUMBER_OF_FILES];
    for (int i = 0; i < NUMBER_OF_FILES; i++) {
      fields[i] = new Bitfield(NUMBER_OF_LINES);
    }
    
    
    manager.setMethodCoverageFields(methods, "");
    manager.setMethodProfilingFields(methods, "");
    manager.setLineCoverageFields(fields, new CoverageDataFile("", ""));
    CoverageManager.setInstance(manager);
    CoverageManager.enableCoverage();
  }

  /**
   * Resets the default instance of Coverage manager.
   * 
   * @see j2meunit.framework.TestCase#tearDown()
   */
  protected void tearDown() throws Exception {
    CoverageManager.setInstance(null);
    super.tearDown();
  }

  /**
   * @see j2meunit.framework.TestCase#suite()
   */
  public Test suite() {
    TestSuite suite = new TestSuite();
    
    suite.addTest(new CoverageManagerTest(
        "testCoverageShouldNotBeSetIfCoverageIsDisabled") {
      public void runTest() {
        testCoverageShouldNotBeSetIfCoverageIsDisabled();
      }
    });

    suite.addTest(new CoverageManagerTest("testCoveringNegativeSize") {
      public void runTest() {
        testCoveringNegativeSize();
      }
    });
    
    suite.addTest(new CoverageManagerTest(
        "testLineCoverageShouldNotBeSetIfCoverageIsDisabled") {
      public void runTest() {
        testLineCoverageShouldNotBeSetIfCoverageIsDisabled();
      }
    });
    
    suite.addTest(new CoverageManagerTest("testMethodCanBeMarkedAsCovered") {
      public void runTest() {
        testMethodCanBeMarkedAsCovered();
      }
    });
    
    suite.addTest(new CoverageManagerTest("testMethodCoveringNegativeSize") {
      public void runTest() {
        testMethodCoveringNegativeSize();
      }
    });
    
    suite.addTest(new CoverageManagerTest(
        "testMethodProfilingStoresCorrectNumberOfMethodCalls") {
      public void runTest() {
        testMethodProfilingStoresCorrectNumberOfMethodCalls();
      }
    });
    
    suite.addTest(new CoverageManagerTest("testQueryingNegativeSize") {
      public void runTest() {
        testQueryingNegativeSize();
      }
    });
    
    suite.addTest(new CoverageManagerTest("testSetLineCovered") {
      public void runTest() {
        testSetLineCovered();
      }
    });
    
    suite.addTest(new CoverageManagerTest(
        "testSettingLineCoveredWithOutOfBoundIndex") {
      public void runTest() {
        testSettingLineCoveredWithOutOfBoundIndex();
      }
    });
    
    return suite;
  }
  
}
