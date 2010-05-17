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

package com.google.test.jinjector;

import j2meunit.framework.Assert;
import j2meunit.framework.TestCase;
import j2meunit.framework.TestMethod;

/**
 * A test case able to flag its execution status which can be used to test 
 * a test runner.
 * 
 * @author Michele Sama
 *
 */
public class FlaggableTestCase extends TestCase {
  private boolean wasRun = false;
  private boolean wasSetUp = false;
  private boolean wasTornDown = false;
  private boolean failed = false;

  /**
   * Creates an instance of this class.
   */
  public FlaggableTestCase() {
  }

  /**
   * Creates an instance of this class which will execute the 
   * specified {@link TestMethod}.
   * 
   * @param name The name to assign to this test.
   * @param method The {@link TestMethod} to execute
   */
  public FlaggableTestCase(String name, TestMethod method) {
    super(name, method);
  }

  /**
   * Creates an instance of this class.
   * 
   * @param name The name to assign to this test.
   */
  public FlaggableTestCase(String name) {
    super(name);
  }

  /**
   * Overrides the fail method in order to flag the failure.
   * 
   * <p>This is possible because in {@link Assert} all assertions and 
   * all failures invoke {@link Assert#fail(String)}.
   * 
   * @see j2meunit.framework.Assert#fail(java.lang.String)
   */
  public void fail(String message) {
    failed = true;
    super.fail(message);
  }

  /**
   * Flags {@link #wasRun} as <code>true</code> if the method 
   * {@link #runTest()} is invoked.
   * 
   * <p> Note that {@link TestCase#runTest()} is not invoked because it fails
   * if a {@link TestMethod} has not been specified.
   * 
   * @see j2meunit.framework.TestCase#runTest()
   */
  protected void runTest() throws Throwable {
    wasRun = true;
  }

  /**
   * Flags {@link #wasSetUp} as <code>true</code> if the method 
   * {@link #setUp()} is invoked.
   * @see j2meunit.framework.TestCase#setUp()
   */
  protected void setUp() throws Exception {
    wasSetUp = true;
    super.setUp();
  }

  /**
   * Flags {@link #wasTornDown} as <code>true</code> if the method 
   * {@link #tearDown()} is invoked.
   * @see j2meunit.framework.TestCase#tearDown()
   */
  protected void tearDown() throws Exception {
    wasTornDown = true;
    super.tearDown();
  }

  /**
   * Tells if the {@link #runTest()} method has been invoked previously.
   * 
   * @return <code>true</code> if this test was run, <code>false</code> 
   *    otherwise.
   */
  public boolean wasRun() {
    return wasRun;
  }

  /**
   * Tells if this test has executed its {@link #setUp()} method.
   * 
   * @return <code>true</code> if the set up was call, <code>false</code> 
   *    otherwise.
   */
  public boolean wasSetUp() {
    return wasSetUp;
  }

  /**
  * Tells if this test has executed its {@link #tearDown()} method.
  * 
  * @return <code>true</code> if the tear down was call, <code>false</code> 
  *    otherwise.
  */
  public boolean wasTornDown() {
    return wasTornDown;
  }

  /**
   * Tells if this test has failed. This becomes <code>true</code> after the 
   * first invocation of {@link #fail(String)}.
   * 
   * @return <code>true</code> if this test failed, <code>false</code> 
   *    otherwise.
   */
  public boolean hasFailed() {
    return failed;
  }
  
  /**
   * Creates an instance of {@link FlaggableTestCase} which always fails when 
   * executed. The new created instance can be used if a predictable 
   * failing test is needed.
   * 
   * @return a failing test case.
   */
  public static FlaggableTestCase createFailingTest() {
    return new FlaggableTestCase("FailingTest") {
      protected void runTest() throws Throwable {
        super.runTest();
        fail("Harcoded failure");
      }
    };
  }
  
  /**
   * Creates an instance of {@link FlaggableTestCase} which always pass. The 
   * new created instance can be used if a predictable passing test is needed.
   * 
   * @return an instance of {@link FlaggableTestCase}.
   */
  public static FlaggableTestCase createPassingTest() {
    return new FlaggableTestCase("PassingTest") {
      protected void runTest() throws Throwable {
        super.runTest();
      }
    };
  }
}
