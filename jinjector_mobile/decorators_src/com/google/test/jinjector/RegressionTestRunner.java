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

import java.util.Enumeration;

import j2meunit.framework.*;
import j2meunit.textui.TestRunner;
import j2meunit.util.StringUtil;

/**
 * Starting point class for regression tests. 
 * 
 * <p> When instantiated this class starts its own thread 
 * and sequentially executes tests. 
 * 
 * <p>Each tests will be executed inside its own thread (they all 
 * implement {@link Runnable}). 
 * 
 * <p> The playback thread terminates when all tests have been executed and 
 * the results have been printed.
 * 
 * @author Michele Sama
 */
public class RegressionTestRunner extends TestRunner implements Runnable {

  /**
   * If the tests are run automatically, e.g. as part of a continuous build, 
   * the overall result of the tests can be detected by parsing the standard 
   * output for either of the following strings (FAIL, PASS). 
   */
  public static final String FAIL = "===FAIL===";
  
  /**
   * @see #FAIL
   */
  public static final String PASS = "===PASS===";
  
  /**
   * The test suite that this runner will execute. 
   */
  protected Test suite;
  
  /**
   * The test result used to contain test results during the execution. 
   */
  protected TestResult result;
  
  /**
   * Instance of the strategy to be used to display results on screen.
   */
  private final ResultDisplayerStrategy displayStrategy;
  
  /**
   * Specify if this test runner is being executed.
   * 
   * <p>This is necessary to make assertions on the actual execution status of 
   * this test base. 
   */ 
  private boolean executing = false;

  /**
   * Creates a play back thread with the specified {@link TestSuite}.
   * 
   * @param test The {@link TestSuite} that this test runner is
   *    going to reproduce.
   */
  public RegressionTestRunner(AssistedTestCase test) {
    this(test, null);
    // Do NOT invoke start() here!
  }
  
  /**
   * Creates a play back thread with the specified {@link TestSuite}.
   * 
   * @param test The {@link TestSuite} that this test runner is
   *    going to reproduce.
   * @param strategy The {@link ResultDisplayerStrategy} which will be used to 
   *    display results on screen. It can be <code>null</code>.   
   */
  public RegressionTestRunner(AssistedTestCase test, 
      ResultDisplayerStrategy strategy) {
    suite = test.suite();
    displayStrategy = strategy;
    // Do NOT invoke start() here!
  }
  
  /**
   * Starts the test into a separate thread.
   * 
   * <p> Please note that this cannot be invoked inside the constructor because
   * all the derived runner needs to specify additional parameters which have
   * to be set before the Thread starts. If this method was called inside the 
   * constructor the derived class would have invoked <code>super()</code>
   * and it would have generated a synchronization issue. 
   */
  public void start() {
      Thread th = new Thread(this, "RegressionTestRunner");
      th.start();
  }
  
  /**
   * Display tests results on screen.
   * 
   * <p> Derived classes should assign {@link #displayStrategy} properly, and 
   * it will be automatically invoked.
   */
  private final void displayResult() {
    if (displayStrategy != null) {
      displayStrategy.displayResult(result);
    }
  }
  
  /**
   * Runs all the tests sequentially, then write a full log and display 
   * the results on screen.
   * 
   * <p> Please note that when this thread is started the application should be
   * fully loaded. However this is application dependent and each regression 
   * test in its {@link TestCase#setup()} should make it sure that the 
   * application is ready to receive commands.
   * 
   * <p> The partial test result is printed to help in detecting the failure.
   * 
   * @see java.lang.Runnable#run()
   */
  public void run() {
    try {
      executing = true;
      doRun(suite);    
      //output list
      displayResult();
    } catch (Throwable t) {
      fWriter.println("General FAILURE!\n" +
          "This should not be happening and it is a general failure " +
          "not a bug in the application under test.\n" +
          "Possible causes are:\n" +
          "1) the emulator (or device) has run out of memory;\n" +
          "2) there is a bug in the instrumented code\n" +
          "3) there is a problem in the class path\n" +
          "4) The test suite contains tests which require a specific runner.");
      t.printStackTrace();
      print(result);
    } finally {
      executing = false;
    }
  }

  /**
   * Query if the regression tests still executing.
   *  
   * @return <code>true</code> if the play back is still in execution.
   */
  public boolean isExecuting() {
    return executing;
  }

  /**
   * Overrides the super method to prevent {@link System#exit(int)} from 
   * being invoked.
   * 
   * @param test The {@link Test} to run.
   * @see j2meunit.textui.TestRunner#doRun(j2meunit.framework.Test)
   */
  protected void doRun(Test test) {
    result = createTestResult();
    result.addListener(this);

    long startTime = System.currentTimeMillis();
    try {
      suite.run(result);
    } finally {
      long endTime = System.currentTimeMillis();
      long runTime = endTime - startTime;
      fWriter.println();
      fWriter.println("Time: " + StringUtil.elapsedTimeAsString(runTime));
      print(result);
      fWriter.println();
    }
  }

  /**
   * Overrides the super method by printing the stack trace.
   * 
   * @param testResult The {@link TestResult} on which tests have been executed.
   * @see TestRunner#printErrors(TestResult)
   */
  public void printErrors(TestResult testResult) {
    if (testResult.errorCount() != 0) {
        if (testResult.errorCount() == 1) {
            fWriter.println("There was 1 error:");
        } else {
            fWriter.println("There were " + 
                testResult.errorCount() + " errors:");
        }
        printErrorsOrFaults(testResult.errors());
    }
  }

  /**
   * Overrides the super method by printing the stack trace.
   * 
   * @param testResult The {@link TestResult} on which tests have been executed.
   * @see TestRunner#printFailures(TestResult)
   */
  public void printFailures(TestResult testResult) {
    if (testResult.failureCount() != 0) {
        if (testResult.failureCount() == 1) {
            fWriter.println("There was 1 failure:");
        } else {
            fWriter.println("There were " + 
                testResult.failureCount() +" failures:");
        }
        printErrorsOrFaults(testResult.failures());
    }
  }
  
  /**
   * Prints on the print writer contained in the super class (which prints on 
   * the standard output) a full error report for each failed test or for 
   * each error.
   * 
   * @param e An {@link Enumeration} containing a list of errors or faults.
   */
  protected void printErrorsOrFaults(Enumeration e) {
    int i = 1;
    for (; e.hasMoreElements(); i++) {
        TestFailure failure = (TestFailure) e.nextElement();
        fWriter.println(i + ") " + failure.failedTest());
        fWriter.println(failure.thrownException().getMessage());
        failure.thrownException().printStackTrace();
    }
  }

  /**
   * Print the overall header of the test.
   * 
   * <P> The string {@link #FAIL} must be printed in the standard output 
   * in order to allow the continuous build to realize that the test failed.
   * 
   * @see j2meunit.textui.TestRunner#printHeader(j2meunit.framework.TestResult)
   */
  public void printHeader(TestResult testResult) {
    if (testResult.wasSuccessful()) {
        fWriter.println();
        fWriter.print(PASS);
        fWriter.println(" (" + testResult.runCount() + " tests)");
    } else {
        fWriter.println();
        fWriter.println(FAIL);
        fWriter.println("Test Results:");
        fWriter.println("Run: " + testResult.runCount() + " Failures: " +
            testResult.failureCount() + " Errors: " +
            testResult.errorCount());
    }
  }
}
