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

import j2meunit.framework.TestCase;
import j2meunit.framework.TestMethod;

/**
 * A derived test case which reports error from {@link #runTest()} instead of 
 * errors in {@link #tearDown()} when both are thrown.
 * 
 * <p>This class changes the normal error-reporting behavior of Unit tests to
 * improve the readability of the error messages.
 * 
 * @author Michele Sama
 *
 */
public class AssistedTestCase extends TestCase {

  /**
   * Constructor from superclass.
   */
  public AssistedTestCase() {
  }

  /**
   * Constructor from superclass.
   * 
   * @param name The name of the test method.
   */
  public AssistedTestCase(String name) {
    super(name);
  }

  /**
   * Constructor from superclass.
   * 
   * @param name The name of the test method.
   * @param testMethod The TestMethod wrapper for the method to execute.
   */
  public AssistedTestCase(String name, TestMethod testMethod) {
    super(name, testMethod);
  }

  /**
   * Runs the test by invoking {@link #setUp()} first, then {@link #runTest()}
   * inside a try-catch block and finally {@link #tearDown()}. 
   * However if an error occurs BOTH in {@link #runTest()} and in 
   * {@link #tearDown()}, only the error from {@link #runTest()} will be thrown.
   * 
   * <p>To improve the readability of error messages, the normal behavior of 
   * unit tests is changed so exceptions during the {@link #tearDown()} 
   * override other exceptions. 
   * 
   * @see j2meunit.framework.TestCase#runBare()
   */
  public void runBare() throws Throwable {
    Throwable catchedThrowable = null;
   
    setUp();

    try {
      runTest();
    } catch (Throwable thrownDuringRun){
      catchedThrowable = thrownDuringRun;
    } finally {
      try {
        tearDown();
      } catch (Throwable thrownDuringTearDown){
        catchedThrowable = (catchedThrowable != null) ? 
            catchedThrowable : thrownDuringTearDown;  
      } finally {
        if (catchedThrowable != null) {
          throw catchedThrowable;
        }
      }
    }
  }

}
