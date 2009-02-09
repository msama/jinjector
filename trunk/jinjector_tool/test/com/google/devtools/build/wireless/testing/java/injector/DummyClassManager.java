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

import org.objectweb.asm.ClassReader;

import java.io.IOException;

/**
 * Overrides the ClassManager with a dummy implementation which uses the 
 * default class loader.
 * 
 * @author Michele Sama
 *
 */
public class DummyClassManager extends ClassManager {

  /**
   * Creates a ClassManager with no jars o class paths.
   */
  public DummyClassManager() {
    super(null, new String[0]);
  }

  /**
   * Process the class by using the internal class adapter but by loading 
   * it with the default {@link ClassLoader}. 
   * 
   * @see ClassManager#reloadClass(String)
   */
  @Override
  protected void reloadClass(String className) {
    className = className.replace('/', '.');
    try {
      ClassReader cr = new ClassReader(className);
      cr.accept(classAdapter, 0);
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException("An IOException occurred while loading: " 
          + className, e);
    }
  }
  
  /**
   * Converts a class name into the internal representation using '/' as a 
   * package separator instead of '.'.
   * 
   * @param clazz The type.
   * @return The classname.
   */
  public static final String getInternalClassname(Class<?> clazz) {
    return clazz.getName().replace('.', '/');
  }
} 