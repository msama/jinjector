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


import com.google.test.jinjector.RegressionTestRunner;
import com.google.test.jinjector.ResultDisplayerStrategy;
import com.google.test.jinjector.util.Log;

import j2meunit.framework.Test;
import j2meunit.framework.TestSuite;

import java.util.Enumeration;

import javax.microedition.midlet.MIDlet;

/**
 * Playback thread for J2ME applications.
 * This subclass will display a list box with results after the execution.
 * 
 * @author Michele Sama
 *
 */
public class J2meTestRunner extends RegressionTestRunner {

  /**
   * Default path
   * 
   *  TODO: this should be assigned automatically.
   */
  private static final String FILE_LOCALHOST = "file://localhost/";
  protected final MIDlet midlet;
  
  /**
   * Creates a test playback without depending from a midlet.
   * 
   * <p> This constructor must be only used for testing purposes!
   * 
   * @param test The test suite to be executed.
   */
  protected  J2meTestRunner(J2meTestCase test) {
    super(test, null);
    midlet = null;
  } 
  
  /**
   * Creates a test playback which will execute a specific test base on a 
   * specific MIDlet
   * 
   * @param midlet The terget MIDlet.
   * @param test The test suite to be executed.
   * @throws IllegalArgumentException if the MIDlet is null.
   */
  public J2meTestRunner(MIDlet midlet, J2meTestCase test) {
    this(midlet, test, new ReportList(midlet));
  } 
  
  /**
   * Creates a test playback which will execute a specific test base on a 
   * specific MIDlet
   * 
   * @param midlet The target MIDlet.
   * @param test The test suite to be executed.
   * @param strategy The {@link ResultDisplayerStrategy} which will be used to 
   *    display results on screen. It can be <code>null</code>.  
   * @throws IllegalArgumentException if the MIDlet is <code>null</code>.
   */
  public J2meTestRunner(MIDlet midlet, J2meTestCase test, 
      ResultDisplayerStrategy strategy) {
    super(test, strategy);
    if (midlet == null) {
      Log.log(J2meTestRunner.class, "MIDlet is null.");
      throw new IllegalArgumentException("MIDlet is null.");
    }
    Log.log(J2meTestRunner.class, "MIDlet is " + midlet);
    this.midlet = midlet;
  } 

  /**
   * Set a reference to the current MIDlet to all the tests.
   * 
   * TODO: put the root as a parameter!!!!!
   * 
   * @see com.google.test.jinjector.RegressionTestRunner#doRun(j2meunit.framework.Test)
   */
  protected void doRun(Test test) {
    setMidletToNestedTests(test);
    super.doRun(test);
    requestCoverageReport(midlet, FILE_LOCALHOST);
  }
  
  /**
   * Set the MIDlet to the new added tests.
   * 
   * <p> If the specified test is a {@link TestSuite} recursively invoke 
   * this method on all the contained tests.
   * 
   * <p> This is protected for testing purposes.
   * 
   * @param test The test to which to set the MIDlet.
   * @throws IllegalStateException if nested tests are not instances of 
   *    {@link J2meTestCase}.
   */
  protected void setMidletToNestedTests(Test test) {
    if (test instanceof TestSuite) {
      Enumeration tests = ((TestSuite) test).tests();
      while (tests.hasMoreElements()) {
        setMidletToNestedTests((Test) tests.nextElement());
      }
    } else if (test instanceof J2meTestCase) {
      ((J2meTestCase) test).setMidlet(midlet);
    } else {
      throw new IllegalStateException("J2meTestRunner can only run other " +
          "TestSuites or J2meTestCases. Found: " + test.getClass());
    }
  }

  /**
   * Force the MIDlet to exit after the execution of all the tests.
   * 
   * @see com.google.test.jinjector.RegressionTestRunner#run()
   */
  public void run() {
    super.run();
    if (needToExit((TestSuite)this.suite)) {
      Log.log(J2meTestRunner.class, "Exit requested");
      midlet.notifyDestroyed();
    }
  }
  
  /**
   * Tells if at least one test in the given suite is requiring to terminate
   * the application.
   * 
   * @return <code>true</code> if at least one of the test wants to terminate
   *   the execution.
   */
  private boolean needToExit(TestSuite suite){
    for (int i = 0; i < suite.testCount(); i++) {
      Test test = suite.testAt(i);
      if (test instanceof J2meTestCase) {
        if (((J2meTestCase)test).isExitAfterExecution()) {
          return true;
        }  
      } else if (test instanceof TestSuite) {
        if (needToExit((TestSuite)test)) {
          return true;
        }
      }
    }
    return false;
  }
}
