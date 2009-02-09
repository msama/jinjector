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

package com.google.devtools.build.wireless.testing.java.injector.rim;

import com.google.devtools.build.wireless.testing.java.injector.ClassNames;

/**
 * Constant pool for RIM instrumentation.
 * 
 * @author Michele Sama
 */
public class RimClassNames {

  /**
   * Base package for RIM decoration.
   */
  public static final String RIM_PKG = 
    ClassNames.DECORATORS_PKG + ("rim.").replace('.', '/');
  
  /**
   * Application class name used in RIM clients.
   */
  public static final String UIAPPLICATION = 
    "net/rim/device/api/ui/UiApplication";

  /**
   * Class name used by the Unit-test test-runner for BlackBerry for 
   * handling the end of testing code.
   */
  public static final String BB_TEST_RUNNER_END_TEST =
    "com/google/sync/utils/blackberry/BBTestRunner$EndOfTestRunnable";
}
