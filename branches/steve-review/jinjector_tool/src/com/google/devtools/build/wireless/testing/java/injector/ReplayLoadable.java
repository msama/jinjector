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

package com.google.devtools.build.wireless.testing.java.injector;


import org.objectweb.asm.ClassVisitor;

import java.util.Properties;
import java.util.logging.Logger;

/**
 * Loadable for {@link ReplayClassAdapter}. 
 * 
 * @author Michele Sama
 *
 */
public class ReplayLoadable extends Loadable {

  public static final String LOGGER_NAME = "ReplayLoadable";
  
  /**
   * Injects a regression test by using the specified class name as a test base.
   * 
   * TODO: rename
   */
  public static final String PROPERTY_TESTSUITE = "regression";
  
  public static final String PROPERTY_TESTSUITE_DESCRIPTION = 
      "Acceptance TestBase class name.";
  
  private String testSuite;
  
  /**
   * Loads the class name of the test suite which will be instrumented.
   * 
   * @see Loadable#load(java.util.Properties)
   */
  @Override
  public void loadInternal(Properties properties) { 
    testSuite = properties.getProperty(PROPERTY_TESTSUITE);
  }

  /** 
   * Adds an instance of {@link ReplayClassAdapter} at the end of the
   * instrumentation chain.
   * 
   * @see Loadable#operation(org.objectweb.asm.ClassVisitor, ClassManager)
   */
  @Override
  public ClassVisitor operation(ClassVisitor cv, ClassManager classManager) {
    cv = new ReplayClassAdapter(
        platform, cv, classManager, testSuite);
    return cv;
  }

  /**
   * Prints the name of this Loadable and a list of required properties.
   * 
   * @see com.google.devtools.build.wireless.testing.java.injector.Loadable#printUsage()
   */
  @Override
  public void printUsage() {
    Logger log = Logger.getLogger(LOGGER_NAME);
    log.fine(getClass().getCanonicalName());
    log.fine("properties:");
    log.fine(PROPERTY_TESTSUITE + ":" + PROPERTY_TESTSUITE_DESCRIPTION);
  }

}
