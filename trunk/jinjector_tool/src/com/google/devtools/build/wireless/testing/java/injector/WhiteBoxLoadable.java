/*
 * Copyright 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
 * Class adapter for white box testing.
 *
 * @author Michele Sama
 */
public class WhiteBoxLoadable extends Loadable {

  public static final String LOGGER_NAME = "WhiteBoxLoadable";

  protected static final String PROPERTY_WHITEBOX_CLASS_INCLUSION = "whiteboxClassInclusion";
  protected static String whiteboxClassInclusion;

  protected static final String PROPERTY_WHITEBOX_METHOD_INCLUSION = "whiteboxMethodInclusion";
  protected static String whiteboxMethodInclusion;

  protected static final String PROPERTY_WHITEBOX_FIELD_INCLUSION = "whiteboxFieldInclusion";
  protected static String whiteboxFieldInclusion;

  /* (non-Javadoc)
   * @see com.google.devtools.build.wireless.testing.java.injector.Loadable#loadInternal(java.util.Properties)
   */
  @Override
  protected void loadInternal(Properties properties) {
    whiteboxClassInclusion = properties.getProperty(PROPERTY_WHITEBOX_CLASS_INCLUSION, "");
    whiteboxMethodInclusion = properties.getProperty(PROPERTY_WHITEBOX_METHOD_INCLUSION, "");
    whiteboxFieldInclusion = properties.getProperty(PROPERTY_WHITEBOX_FIELD_INCLUSION, "");
  }

  /* (non-Javadoc)
   * @see com.google.devtools.build.wireless.testing.java.injector.Loadable#operation(org.objectweb.asm.ClassVisitor, com.google.devtools.build.wireless.testing.java.injector.ClassManager)
   */
  @Override
  public ClassVisitor operation(ClassVisitor cv, ClassManager classManager) {
    cv = new WhiteBoxClassAdapter(cv, whiteboxClassInclusion, 
        whiteboxMethodInclusion, whiteboxFieldInclusion, platform);
    return cv;
  }

  /* (non-Javadoc)
   * @see com.google.devtools.build.wireless.testing.java.injector.Loadable#printUsage()
   */
  @Override
  public void printUsage() {
    Logger log = Logger.getLogger(LOGGER_NAME);
    log.fine(getClass().getCanonicalName());
    log.fine("properties:");
    // TODO: write properties.
  }

}
