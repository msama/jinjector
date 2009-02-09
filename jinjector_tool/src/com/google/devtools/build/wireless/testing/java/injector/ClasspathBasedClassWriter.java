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
import org.objectweb.asm.ClassWriter;

/**
 * 
 * This class writer uses a different classloader to to load hierarchies.
 * The method getCommonSuperClass of the superclass try to instantiate a class 
 * form the other when needed but in cases in which code is generated at runtime
 * this mechanisms does not work.
 * 
 * <p>Another issue is related to the target platform. The tool will be executed 
 * with J2SE but the code need to be instrumented by using the framework of the
 * target platform.
 * 
 * <p>This class resolve this problem by using a ClassManager.
 * 
 * @author Michele Sama
 * 
 */
public class ClasspathBasedClassWriter extends ClassWriter {

  private ClassManager classManager = null;

  /**
   * Construct a new ClasspathBasedClassWriter with a specified frame handling 
   * and a specified ClassManager
   * 
   * @param frames How frame resizing is handled.
   * @param cm The used {@link ClassManager}.
   */
  public ClasspathBasedClassWriter(int frames, ClassManager cm) {
    super(frames);
    classManager = cm;
  }
  
  /**
   * Construct a new ClasspathBasedClassWriter with a specified 
   * {@link ClassManager}.
   * 
   * @param cr The nested ClassReader
   * @param access The class' access flag.
   * @param cm The ClassManager to use.
   */
  public ClasspathBasedClassWriter(ClassReader cr, int access,
      ClassManager cm) {
    super(cr, access);
    classManager = cm;
  }

  /**
   * Get the common superclass of two given types.
   * In order to retrieve information about classes instead of loading them with
   * a classloader, this implementation delegate a ClassManager to load and 
   * parse the code.
   * 
   * <p>Loading files with a classloader would be impossible because some classes 
   * are build for different environments or platforms.  
   * 
   * @param type1 The first type to compare.
   * @param type2 The second type to compare.
   */
  @Override
  protected String getCommonSuperClass(String type1, String type2){
    if (classManager.isAssignableFrom(type1, type2)) {
      return type1;
    }
    if (classManager.isAssignableFrom(type2, type1)) {
      return type2;
    }
    if (classManager.isInterface(type1) || classManager.isInterface(type2)) {
      return ClassNames.JAVA_LANG_OBJECT;
    } else {
      // They may be part of the same tree.
      while (!type2.equals(ClassNames.JAVA_LANG_OBJECT)
          && !classManager.isAssignableFrom(type2, type1)) {
        type2 = classManager.getSuperclass(type2); 
      }
      return type2; 
    }
  }

}
