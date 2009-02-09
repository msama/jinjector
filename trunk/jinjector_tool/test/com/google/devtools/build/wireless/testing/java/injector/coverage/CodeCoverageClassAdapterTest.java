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

import junit.framework.TestCase;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.EmptyVisitor;


/**
 * Verifies the correct integration between CodeCoverageClassAdapter and 
 * CoverageStatisticContainer by visiting classes and verifying that they have 
 * been mapped for coverage correctly.
 * 
 * <p>Each ClassAdapter is supposed to be part of a chain of ClassVisitors. 
 * The real usage would be a sequence of several ClassAdapters closed by a 
 * ClassWriter which will generate the bytecode. This is mockked by creating a 
 * dummy ClassVisitors which close the chain. 
 * 
 * @author Michele Sama
 *
 */
public class CodeCoverageClassAdapterTest extends TestCase {

  /**
   * Defines the number of entries to try when adding objects.
   */
  private static final int MAX = 50;
  
  protected CoverageStatisticContainer statisticContainer = null;
  protected CodeCoverageClassAdapter classAdapter = null;
  
  /**
   * Creates a new Class adapter with an empty statistic container.
   * 
   * @throws java.lang.Exception
   */
  @Override
  public void setUp() throws Exception {
    statisticContainer = new CoverageStatisticContainer();
    classAdapter = new CodeCoverageClassAdapter(new EmptyVisitor(), 
        statisticContainer, null, CoverageMode.SUMMARY);
  }

  /**
   * Resets the class adapter and the statistic container.
   * 
   * @throws java.lang.Exception
   */
  @Override
  public void tearDown() throws Exception {
    statisticContainer = null;
    classAdapter = null;
  }
  
  public void testCoverageIncludeExpressionHandledCorrectly() {
    String[] inclusion = new String[]{"+com/google/foo"};
    classAdapter = new CodeCoverageClassAdapter(new EmptyVisitor(), 
        statisticContainer, inclusion, CoverageMode.SUMMARY);
    assertTrue(classAdapter.shouldInstrumentClass("com/google/foo/AnyClass"));
    assertFalse(classAdapter.shouldInstrumentClass("com/google/bar/AnyClass"));
  }  
  
  public void testCoverageExcludeExpressionHandledCorrectly() {
    String[] inclusion = new String[]{"-com/google/foo", "+com"};
    classAdapter = new CodeCoverageClassAdapter(new EmptyVisitor(), 
        statisticContainer, inclusion, CoverageMode.SUMMARY);
    assertFalse(classAdapter.shouldInstrumentClass("com/google/foo/AnyClass"));
    assertTrue(classAdapter.shouldInstrumentClass("com/google/util/AnyClass"));    
  }
  
  public void testCoverageIncludeAndExcludeExpressionHandledCorrectly() {
    String[] inclusion = new String[]{"+com/google/foo", "-com/google/foo/TimerTest$"};
    classAdapter = new CodeCoverageClassAdapter(new EmptyVisitor(), 
        statisticContainer, inclusion, CoverageMode.SUMMARY);
    assertTrue(classAdapter.shouldInstrumentClass("com/google/foo/AnyClass"));
    assertFalse(classAdapter.shouldInstrumentClass("com/google/foo/TimerTest$1"));    
  }

  /**
   * Tests 
   * {@link CodeCoverageClassAdapter#visitMethod(int, String, String, String, String[])}
   * .
   * 
   * <p>Tests that the right method visitor would be returned.
   * The correct chain would be 
   * <br>|DummyMethodAdapter|<br>
   * if the method does not have 
   * to be mapped or 
   * <br>|CodeCoverageClassAdapter.MethodCoverage|DummyMethodAdapter|<br>
   * if the method has to be covered. This allows to make 
   * assertion on the correct usage by checking the type of the first element in 
   * the chain.
   */
  public void testVisitMethod() {
    String name = null;
    classAdapter.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, "Foo", null, 
        ClassNames.JAVA_LANG_OBJECT, null);
    classAdapter.visitSource("Foo.java", null);
    
    for (int i = 0; i < MAX; i++) {
      name = "dummeyMethod" + i;
      MethodVisitor mv = classAdapter.visitMethod(Opcodes.ACC_PUBLIC, name, 
          "()V", null, null);
      if (classAdapter.isIncluded()) {
        assertTrue("Mapped as covered.", !(mv instanceof EmptyVisitor));
      } else {
        assertTrue("Mapped as covered.", (mv instanceof EmptyVisitor));
      }
    }   
  }
  
  /**
   * Tests 
   * {@link CoverageStatisticContainer#addInstrumentedLineAndGetLineIndex(String, int)}
   * .
   * 
   * <p>Simulates the adding of a full method starting from line 0 until a 
   * value.
   */
  public void testIncludeLines_sequence() {
    String className = "com/Foo";
    String sourceName = "Foo.java";
    String producedFilename = "com/Foo";
    String methodName = "method";
    String methodDesc = "()V";
    int lines = 12345;
    
    String[] inclusion = new String[]{"+com"};
    classAdapter = new CodeCoverageClassAdapter(new EmptyVisitor(), 
        statisticContainer, inclusion, CoverageMode.LINE);
    classAdapter.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, className, null, 
        ClassNames.JAVA_LANG_OBJECT, null);
    classAdapter.visitSource(sourceName, null);
    
    MethodVisitor mv = classAdapter.visitMethod(Opcodes.ACC_PUBLIC, methodName, 
        methodDesc, null, null);
    mv.visitCode();
    for (int i = 0; i < lines; i++) {
      mv.visitLineNumber(i, null);
    }
    mv.visitEnd();
    assertEquals("Wrong line size.", lines, 
        statisticContainer.getNumberOfLinesForFile(producedFilename));
  }
  
  /**
   * Tests 
   * {@link CodeCoverageClassAdapter#visitMethod(int, String, String, String, String[])}
   * .
   * 
   * <p> Tests that an abstract method would be skipped.
   */
  public void testVisitMethod_skipAbstract() {
    String className = "Foo";
    String methodName = "method";
    String methodDesc = "()V";
    int lines = 12345;
    
    classAdapter.visit(1, Opcodes.ACC_ABSTRACT, className, null, 
        ClassNames.JAVA_LANG_OBJECT, null);    
    MethodVisitor mv = classAdapter.visitMethod(Opcodes.ACC_ABSTRACT, 
        methodName, methodDesc, null, null);
    
    assertFalse("Abstract method should not be visited by MethodCoverage", 
        mv instanceof CodeCoverageClassAdapter.MethodCoverage);
  }
  
  public void testVisitMethod_skipClassesNotIncluded() {
    String[] inclusion = new String[]{"+com/google/io"};   
    classAdapter = new CodeCoverageClassAdapter(new EmptyVisitor(), 
        statisticContainer, inclusion, CoverageMode.LINE);
        
    classAdapter.visit(1, Opcodes.ACC_PUBLIC, "com/google/common/Timer", null, 
        ClassNames.JAVA_LANG_OBJECT, null);
    MethodVisitor mv = classAdapter.visitMethod(Opcodes.ACC_PUBLIC,
       "method", "()V", null, null);
   
   assertFalse("Classes that do not match the include pattern should not be instrumented", 
       mv instanceof CodeCoverageClassAdapter.MethodCoverage);
  }
  
  public void testVisitMethod_skipAutoGeneratedClasses() {
    classAdapter = new CodeCoverageClassAdapter(new EmptyVisitor(), 
        statisticContainer, null, CoverageMode.LINE);
        
    classAdapter.visit(1, Opcodes.ACC_PUBLIC, "com/google/common/Timer$1", null, 
        ClassNames.JAVA_LANG_OBJECT, null);
    MethodVisitor mv = classAdapter.visitMethod(Opcodes.ACC_PUBLIC,
       "method", "()V", null, null);
   
   assertFalse("Auto-generated classes should not be instrumented", 
       mv instanceof CodeCoverageClassAdapter.MethodCoverage);
  }
  
  public void testVisitMethod_lineCoverageInstrumentationAdded() {
    final String methodName = "method";
    final String className = "com/google/common/Timer";
    final String sourceFile = "Timer.java";
    final String methodDesc = "()V";
    final String method = className + "." + methodName + methodDesc;
    
    String[] inclusion = new String[]{"+com"};
    classAdapter = new CodeCoverageClassAdapter(new EmptyVisitor(), 
        statisticContainer, inclusion, CoverageMode.LINE);
    
    classAdapter.visit(1, Opcodes.ACC_PUBLIC, className, null, 
        ClassNames.JAVA_LANG_OBJECT, null);
    classAdapter.visitSource(sourceFile, null);
    
    MethodVisitor mv = classAdapter.visitMethod(
        Opcodes.ACC_PUBLIC, methodName, "()V", null, null);
    final int lineNumber = 20;
    mv.visitLineNumber(20, new Label());
    
    assertEquals(1, statisticContainer.getInstrumentedLines(className).size());
    assertTrue(statisticContainer.getInstrumentedLines(className).get(0).equals(lineNumber));
  }
  
  public void testVisitMethod_lineCoverageInstrumentationNotAdded() {
    final String methodName = "method";
    final String className = "com/google/common/Timer";
    final String sourceFile = "Timer.java";
    final String methodDesc = "()V";
    final String method = className + "." + methodName + methodDesc;
    
    String[] inclusion = new String[]{"+com"};
    classAdapter = new CodeCoverageClassAdapter(new EmptyVisitor(), 
        statisticContainer, inclusion, CoverageMode.SUMMARY);
    
    classAdapter.visit(1, Opcodes.ACC_PUBLIC, className, null, 
        ClassNames.JAVA_LANG_OBJECT, null);
    classAdapter.visitSource(sourceFile, null);
    
    MethodVisitor mv = classAdapter.visitMethod(
        Opcodes.ACC_PUBLIC, methodName, "()V", null, null);
    final int lineNumber = 20;
    mv.visitLineNumber(20, new Label());
    
    assertEquals(0, statisticContainer.getInstrumentedLines(method).size());
  }

  /**
   * Tests 
   * {@link CodeCoverageClassAdapter#visit(int, int, String, String, String, String[])}
   * .
   * 
   * <p>
   * Verifies:
   * <ul> 
   * <li>that source file would be collected correctly.
   * <li>that duplicated source file would be collected only once.
   * <li>that classes in the default package would be considered.
   * <li>that classes with the same name but in different packages would be 
   *    collected correctly.
   * </ul> 
   * In case of failure the problem is probably located in the class adapter or 
   * in the statistic container.
   */
  public void testVisit() {
    int currentClassCovered = 0;
    String name = null;
    int currentCoveredPkg = 0;
    
    /*
     * Visit several different classes in the default package and verify that 
     * they have all been mapped.
     */
    for (int i = 0; i < MAX; i++) {
      name = "Dummy" + i;
      classAdapter.visit(1, Opcodes.ACC_PUBLIC, name, null, 
          ClassNames.JAVA_LANG_OBJECT, null);
      if (classAdapter.isIncluded()) {
        currentClassCovered++;
        // All the classes are in the default package
        currentCoveredPkg = 1;
      }
      assertEquals("Number of classes.", currentClassCovered, 
          statisticContainer.getClassSize());
      assertEquals("Number of packages.", currentCoveredPkg, 
          statisticContainer.getPackageSize());
    }
    
    /*
     * Visits the same class several times and check that it will be added only
     * once.
     */
    currentClassCovered = statisticContainer.getClassSize();
    currentCoveredPkg = statisticContainer.getPackageSize();
    name = "duplicated/DuplicatedDummy";
    if (classAdapter.isIncluded()) {
      currentClassCovered++;
      currentCoveredPkg++;
    }
    for (int i = 0; i < MAX; i++) {
      classAdapter.visit(1, Opcodes.ACC_PUBLIC, name, null, 
          ClassNames.JAVA_LANG_OBJECT, null);
      assertEquals("Duplicated classes.", currentClassCovered, 
          statisticContainer.getClassSize());
      assertEquals("Number of packages.", currentCoveredPkg, 
          statisticContainer.getPackageSize());
    }
    
    /*
     * Visits classes with the same name but in different packages and verify 
     * that all of them have been covered.
     */
    currentClassCovered = statisticContainer.getClassSize();
    currentCoveredPkg = statisticContainer.getPackageSize();
    for (int i = 0; i < MAX; i++) {
      name = "org/foo" + i + "/Dummy";
      classAdapter.visit(1, Opcodes.ACC_PUBLIC, name, null, 
          ClassNames.JAVA_LANG_OBJECT, null);
      if (classAdapter.isIncluded()) {
        currentClassCovered++;
        // All the classes are in different packages
        currentCoveredPkg++;
      }
      assertEquals("Package handling in class counting.", currentClassCovered, 
          statisticContainer.getClassSize());
      assertEquals("Number of packages.", currentCoveredPkg, 
          statisticContainer.getPackageSize());
    }
  }

  /**
   * Tests
   * {@link CodeCoverageClassAdapter#visitSource(String, String)}
   * .
   * 
   * <p>Verifies:
   * <ul> 
   * <li>that source file would be collected correctly.
   * <li>that duplicated source file would be collected only once.
   * <li>that file with the same name but in different packages would be 
   *    considered as different files.
   * </ul>
   * In case of failure the problems is probably located in the class adapter or
   * in the statistic container.
   */
  public void testVisitSource() {
    String[] inclusion = new String[]{"+com"};
    classAdapter = new CodeCoverageClassAdapter(new EmptyVisitor(), 
        statisticContainer, inclusion, CoverageMode.SUMMARY);
    
    int currentSize = 0;
    for (int i = 0; i < MAX; i++) {
      classAdapter.visit(1, Opcodes.ACC_PUBLIC, "com/"+i, null, 
          ClassNames.JAVA_LANG_OBJECT, null);
      classAdapter.visitSource(i + ".java", null);
      assertEquals("Number of source files.", i + 1, 
          statisticContainer.getSourceFileCount());
    }
    
    currentSize = statisticContainer.getSourceFileCount();
    for (int i = 0; i < MAX; i++) {
      classAdapter.visit(1, Opcodes.ACC_PUBLIC, "com/Dummy" + i, null, 
          ClassNames.JAVA_LANG_OBJECT, null);
      classAdapter.visitSource("Dummy.java", null);
      assertEquals("Duplicated source files.", currentSize + 1, 
          statisticContainer.getSourceFileCount());
    }
    
    currentSize = statisticContainer.getSourceFileCount();
    for (int i = 0; i < MAX; i++) {
      classAdapter.visit(1, Opcodes.ACC_PUBLIC, "com/foo" + i + "/Dummy", null, 
          ClassNames.JAVA_LANG_OBJECT, null);
      // TODO: this will never happen. The filename is relative to the path
      classAdapter.visitSource("com/foo" + i + "/Dummy.java", null);
      assertEquals("Package handling in source files counting.", 
          currentSize + i + 1, statisticContainer.getSourceFileCount());
    }
  }
  
}
