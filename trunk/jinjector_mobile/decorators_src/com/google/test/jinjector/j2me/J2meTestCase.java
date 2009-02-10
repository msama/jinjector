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


import com.google.test.jinjector.AssistedTestCase;

import j2meunit.framework.AssertionFailedError;
import j2meunit.framework.TestMethod;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;

/**
 * Specific test case for j2me/CLDC/MIDP applications.
 * 
 * <p>This runnable provides convenience methods for scripting regression 
 * tests for J2ME applications.
 * 
 * @author Michele Sama
 * @noautotest
 */
public class J2meTestCase extends AssistedTestCase {
  
  protected MIDlet midlet = null;
  
  /**
   * Specifies whether the test runner running this test should exit from the 
   * application the application after all the suite has been executed.
   */
  protected boolean exitAfterExecution = false;
  
  /**
   * Creates an instance of J2ME specific runnable.
   */
  public J2meTestCase() {
  }

  /**
   * Creates an instance of J2ME specific runnable.
   * 
   * @param name The name of the test method.
   * @param method The TestMethod wrapper for the method to execute.
   */
  public J2meTestCase(String name, TestMethod method) {
    super(name, method);
  }

  /**
   * Creates an instance of J2ME specific runnable.
   * 
   * @param name The name of the test method.
   */
  public J2meTestCase(String name) {
    super(name);
  }

  /**
   * Get the MIDlet on which this test is going to be executed.
   * 
   * @return the midlet.
   */
  public MIDlet getMidlet() {
    return midlet;
  }

  /**
   * Set the MIDlet on which this test is going to be executed.
   * 
   * @param midlet the midlet to set.
   */
  public void setMidlet(MIDlet midlet) {
    this.midlet = midlet;
  }

  /**
   * Asserts that {@link #midlet} is not <code>null</code> before 
   * setting up the test.
   * 
   * @throws AssertionFailedError if {@link #midlet} is null.
   * @see j2meunit.framework.TestCase#run(j2meunit.framework.TestResult)
   * @see #setMidlet(MIDlet)
   */
  public void runBare() throws Throwable {
    if (midlet == null) {
      fail("No MIDlet has been specified. " +
          "Please check that the test runner is an instance of " +
          "J2meTestRunner, or invoke setMidlet() on the instance of " +
          "TestRunner before running the tests.");
    }
    super.runBare();
  }
  
  
  /**
   * Get the {@link Displayable} currently on screen for the 
   * given {@link MIDlet}.
   * 
   * @return The {@link Displayable} on screen.
   */
  public Displayable getCurrentDisplayableInMIDlet() {
    return getCurrentDisplayable(midlet);
  }
  
  /**
   * Get the {@link Displayable} currently on screen for the 
   * given {@link MIDlet}.
   * 
   * @param midlet The {@link MIDlet} to query.
   * @return The {@link Displayable} on screen.
   */
  public static Displayable getCurrentDisplayable(MIDlet midlet) {
    return Display.getDisplay(midlet).getCurrent();
  }



  /**
   * @return the exitAfterExecution
   */
  protected boolean isExitAfterExecution() {
    return exitAfterExecution;
  }

  /**
   * @param exitAfterExecution the exitAfterExecution to set
   */
  protected void setExitAfterExecution(boolean exitAfterExecution) {
    this.exitAfterExecution = exitAfterExecution;
  }
}
