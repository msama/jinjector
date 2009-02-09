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

package com.google.devtools.build.wireless.testing.java.injector.j2me;

import com.google.devtools.build.wireless.testing.java.injector.ClassManager;
import com.google.devtools.build.wireless.testing.java.injector.Loadable;
import com.google.devtools.build.wireless.testing.java.injector.Platform;

import org.objectweb.asm.ClassVisitor;

import java.util.Properties;
import java.util.logging.Logger;

/**
 * Loads the class adapter for instrumenting LCDUI.
 * 
 * @author Michele Sama
 *
 */
public class LcduiLoadable extends Loadable {

  public static final String LOGGER_NAME = "LcduiLoadable";
  
  @Override
  public void loadInternal(Properties properties) {
    // No additional property is required.
  }

  /**
   * Instruments LCDUI
   * 
   * @see Loadable#operation(org.objectweb.asm.ClassVisitor, com.google.devtools.build.wireless.testing.java.injector.ClassManager)
   */
  @Override
  public ClassVisitor operation(ClassVisitor cv, ClassManager classManager) {
    /*
     * This adapter is only valid if the platform is J2ME and it is required for
     * regression because it requires to wrap items and displayables.
     */  
    if (platform.equals(Platform.J2ME)) {
      cv = new LcduiDecoratorClassAdapter(cv, classManager, platform);
    }
    return cv;
  }

  /**
   * Prints a human-readable of this loadable.
   * 
   * @see com.google.devtools.build.wireless.testing.java.injector.Loadable#printUsage()
   */
  @Override
  public void printUsage() {
    Logger log = Logger.getLogger(LOGGER_NAME);
    log.fine(getClass().getCanonicalName());
  }

}
