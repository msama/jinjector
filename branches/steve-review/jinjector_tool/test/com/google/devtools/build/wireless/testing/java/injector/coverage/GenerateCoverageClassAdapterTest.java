/* Copyright 2009 Google Inc.
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
import com.google.devtools.build.wireless.testing.java.injector.Platform;
import com.google.devtools.build.wireless.testing.java.injector.coverage.GenerateCoverageClassAdapter.ReportBeforeDestroyingMethodAdapter;

import junit.framework.TestCase;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static org.objectweb.asm.Opcodes.*;

import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.*;

/**
 * @author Michele Sama
 *
 */
public class GenerateCoverageClassAdapterTest extends TestCase {

  private static final String[] NULL_ARRAY = null;
  
  GenerateCoverageClassAdapter classAdapterJ2ME;
  MethodVisitor methodVisitorMock;
  ClassVisitor classVisitorMock;
  
  /**
   * @param name
   */
  public GenerateCoverageClassAdapterTest(String name) {
    super(name);
  }

  /**
   * Nullifies all the fields.
   * 
   * @see junit.framework.TestCase#tearDown()
   */
  @Override
  protected void tearDown() throws Exception {
    classVisitorMock = null;
    methodVisitorMock = null;
    super.tearDown();
  }
  
  /**
   * Creates a dummy string to allow easy mock to compare the passed argument.
   * 
   * TODO among tests
   *     for different class visitors. Define a super class for all these tests
   *     or a utility class to avoid code duplication.
   *  
   * @return an empty instance of String;
   */
  private String anyString() {
    return isA(String.class);
  }
  
  /**
   * Creates a {@link ClassVisitor} mock which expects a call to the method
   * {@link ClassVisitor#visitMethod(int, String, String, String, String[])}.
   * 
   * @param mv the {@link MethodVisitor} which will be passed as argument to the
   *     {@link ClassVisitor#visitMethod(int, String, String, String, String[])}
   *     call.
   * @return a new instance of the mock object.
   */
  protected ClassVisitor createClassVisitorMock(MethodVisitor mv) {
    ClassVisitor cv = createMock(ClassVisitor.class);
    cv.visit(anyInt(), anyInt(), anyString(), anyString(), anyString(),
        eq(NULL_ARRAY));
    expect(cv.visitMethod(anyInt(), anyString(), anyString(), anyString(),
        eq(NULL_ARRAY)))
        .andStubReturn(mv);
    replay(cv);
    return cv;
  }
  
  /**
   * Verify that the instruction to collect coverage would be invoked before any
   * invocation of <code>MIDlet.notifyDestroyed()</code> by 
   * {@link GenerateCoverageClassAdapter#visitMethod(int, String, String, String, String[])}
   * . 
   */
  public void testVisitMethod_notifyDestroyed() {
    methodVisitorMock = createMock(MethodVisitor.class);
    methodVisitorMock.visitLdcInsn(eq(Platform.J2ME.getFileConnectionPrefix()));
    methodVisitorMock.visitMethodInsn(eq(Opcodes.INVOKESTATIC), 
        eq(CoverageClassNames.COVERAGE_MANAGER), eq("writeReport"),
        eq("(L" + ClassNames.JAVA_LANG_STRING + ";)V"));
    methodVisitorMock.visitMethodInsn(eq(ACC_PUBLIC), eq(ClassNames.MIDLET),
        eq("notifyDestroyed"), eq("()v"));
    replay(methodVisitorMock);
    
    classVisitorMock = createClassVisitorMock(methodVisitorMock);
    classAdapterJ2ME =
      new GenerateCoverageClassAdapter(Platform.J2ME, classVisitorMock, null);
    
    
    classAdapterJ2ME.visit(0, ACC_PUBLIC, "Foo", "",
        ClassNames.JAVA_LANG_OBJECT, null);
    MethodVisitor mv = classAdapterJ2ME.visitMethod(ACC_PUBLIC, "bar",
        "()v", "", null);
    verify(classVisitorMock);
    
    mv.visitMethodInsn(ACC_PUBLIC, ClassNames.MIDLET, "notifyDestroyed", "()v");
    verify(methodVisitorMock);
    
    assertTrue("The returned instance should be an instance of " +
        ReportBeforeDestroyingMethodAdapter.class.getName(), 
        mv instanceof ReportBeforeDestroyingMethodAdapter);
  }
  
  /**
   * Tests the normal execution of
   * {@link GenerateCoverageClassAdapter#visitMethod(int, String, String, String, String[])}
   * with common methods. 
   */
  public void testVisitMethod_unknownMethod() {
    methodVisitorMock = createMock(MethodVisitor.class);
    methodVisitorMock.visitMethodInsn(anyInt(), anyString(),
        anyString(), anyString());
    replay(methodVisitorMock);
    
    classVisitorMock = createClassVisitorMock(methodVisitorMock);
    classAdapterJ2ME =
      new GenerateCoverageClassAdapter(Platform.J2ME, classVisitorMock, null);
    
    
    classAdapterJ2ME.visit(0, ACC_PUBLIC, "Foo", "",
        ClassNames.JAVA_LANG_OBJECT, null);
    MethodVisitor mv = classAdapterJ2ME.visitMethod(ACC_PUBLIC, "bar",
        "()v", "", null);
    verify(classVisitorMock);
    
    mv.visitMethodInsn(ACC_PUBLIC, ClassNames.MIDLET, "asd", "()v");
    verify(methodVisitorMock);
    
    assertTrue("The returned instance should be an instance of " +
        ReportBeforeDestroyingMethodAdapter.class.getName(), 
        mv instanceof ReportBeforeDestroyingMethodAdapter);
  }
  
  /**
   * Verify that the instruction to collect coverage would be invoked before any
   * invocation of <code>MIDlet.notifyDestroyed()</code> by 
   * {@link GenerateCoverageClassAdapter#visitMethod(int, String, String, String, String[])}
   * . 
   */
  public void testVisitMethod_notJ2ME() {
    methodVisitorMock = createMock(MethodVisitor.class);
    methodVisitorMock.visitMethodInsn(anyInt(), anyString(),
        anyString(), anyString());
    replay(methodVisitorMock);
    
    classVisitorMock = createClassVisitorMock(methodVisitorMock);
    classAdapterJ2ME =
      new GenerateCoverageClassAdapter(Platform.RIM, classVisitorMock, null);
    
    
    classAdapterJ2ME.visit(0, ACC_PUBLIC, "Foo", "",
        ClassNames.JAVA_LANG_OBJECT, null);
    MethodVisitor mv = classAdapterJ2ME.visitMethod(ACC_PUBLIC, "bar",
        "()v", "", null);
    verify(classVisitorMock);
    
    mv.visitMethodInsn(ACC_PUBLIC, ClassNames.MIDLET, "notifyDestroyed", "()v");
    verify(methodVisitorMock);
    
    assertFalse("The returned instance should NOT be an instance of " +
        ReportBeforeDestroyingMethodAdapter.class.getName(), 
        mv instanceof ReportBeforeDestroyingMethodAdapter);
  }

}
