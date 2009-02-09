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

package com.google.devtools.build.wireless.testing.java.injector.coverage;

import com.google.devtools.build.wireless.testing.java.injector.ClassNames;

/**
 * Constant pool for injecting client side code class names.
 * Extended class names are used by ClassAdapters and ClassVisitors to inject 
 * instructions.
 * 
 * <p>Class names represented by these constants are client side classes, 
 * which cannot be imported. 
 * 
 * <p>Class names are expressed using the internal binary form of the Java 
 * Virtual Machine as defined in section 4 of the The Java Virtual Machine 
 * Specification, Second Edition
 * {@link "http://java.sun.com/docs/books/jvms/second_edition/html/ClassFile.doc.html#1169"}
 * .
 * 
 * <p>This static class contains client side class names used for coverage.
 * 
 * @author Michele Sama
 */
public class CoverageClassNames {

    /**
     * Client side package name for coverage classes.
     * <br>Subpackage names must be separated with '/' in order to be used 
     * with ASM.
     */
    public static final String COVERAGE_PKG = 
      ClassNames.DECORATORS_PKG + "coverage/";
    
    /**
     * Name of the coverage manager class which will collect coverage 
     * information at runtime.
     */
    public static final String COVERAGE_MANAGER = 
      COVERAGE_PKG + "CoverageManager";

    /**
     * Name of the reporter interface which will be injected into the class 
     * responsible for writing coverage information.
     */
    public static final String COVERAGE_REPORTER = 
      COVERAGE_PKG + "CoverageReporter";

    /**
     * Uninstantiable because it is a utility class. 
     */
    private CoverageClassNames() {
    }
}
