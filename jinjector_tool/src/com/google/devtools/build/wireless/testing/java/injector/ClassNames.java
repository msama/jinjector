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

/**
 * This class contains classnames and constants.
 * At instrumentation times decorator classnames are used to inject 
 * instructions.
 * 
 * <p>Decorators are compiled separately and, in general, they cannot be 
 * included because their code is targeting a different platform.
 * 
 * <p>For this reason it is necessary to have constant string names. 
 * However if their package is changed this code will still compile but there 
 * will be a ClassNotFoundException at runtime in the client. Which will be 
 * fixable by changing all the constants containing classnames.
 * 
 * <p>By using a constant for the package this will be the only string that 
 * will have to be changed.
 * 
 * @author Michele Sama
 *
 */
public class ClassNames {

  /**
   * Decorators package.
   * */
  public static final String DECORATORS_PKG = "com/google/test/jinjector/";
  
  public static final String STRING_BUFFER = "java/lang/StringBuffer";
  public static final String STRING_BUILDER = "java/lang/StringBuilder";
  public static final String PRINT_STREAM = "java/io/PrintStream";
  public static final String MIDLET = "javax/microedition/midlet/MIDlet";
  public static final String JAVA_LANG_OBJECT = "java/lang/Object";
  public static final String JAVA_LANG_STRING = "java/lang/String";
  public static final String JAVA_LANG_THROWABLE = "java/lang/Throwable";
  
  // Regression test;  
  public static final String PLAYABLE = DECORATORS_PKG + "Playable";

  // com.google.common
  public static final String GOOGLE_GRAPHICS = 
    "com/google/common/graphics/GoogleGraphics";
  public static final String KEY_EVENT =  "com/google/common/ui/KeyEvent";

}
