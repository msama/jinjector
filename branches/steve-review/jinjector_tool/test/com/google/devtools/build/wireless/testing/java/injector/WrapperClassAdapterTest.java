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

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.V1_3;

import com.google.devtools.build.wireless.testing.java.injector.WrapperClassAdapter.WrapperMethodAdapter;

import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * JUnit test class for {@link WrapperClassAdapter}.
 * 
 * @author Michele Sama
 *
 */
public class WrapperClassAdapterTest extends TestCase {

  private Map<String, String> classMap;
  private SpyClassVisitor spyVisitor;
  private WrapperClassAdapter wrapper;
  private String fakeKey = "Foo";

  /**
   * Creates a Map of class names to wrap during the test. 
   * 
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    classMap = new HashMap<String, String>();
    classMap.put("OriginalFoo0", "InstrumentedFoo0");
    classMap.put("OriginalFoo1", "InstrumentedFoo1");
    classMap.put("OriginalFoo2", "InstrumentedFoo2");
    classMap.put("OriginalFoo3", "InstrumentedFoo3");
    spyVisitor = new SpyClassVisitor();
    wrapper = new WrapperClassAdapter(spyVisitor, 
        new DummyClassManager(), Platform.J2ME, classMap);
  }

  /**
   * Tests that {@link WrapperClassAdapter#wrapperize(String)} wraps all and 
   * only the given classnames.
   */
  public void testWrapperize() {
    WrapperClassAdapter wrapper = new WrapperClassAdapter(null, 
        new DummyClassManager(), Platform.J2ME, classMap);
    
    for (String key : classMap.keySet()) {
      assertEquals("The given classname should have been wrapped", 
          classMap.get(key), wrapper.wrapperize(key));
    }
    
    String fakeKey = "Foo";
    assertEquals("The given classname should NOT have been wrapped", 
        fakeKey, wrapper.wrapperize(fakeKey));
  }

  /**
   * Tests 
   * {@link WrapperClassAdapter#visit(int, int, String, String, String, String[])}
   * .
   * 
   * <p>Tests that wrapped superclasses have been replaced with the opportune 
   * wrapper.
   */
  public void testWrappingSuperclass() {
    for (String key : classMap.keySet()) {
      wrapper.visit(V1_3, ACC_PUBLIC, ClassNames.JAVA_LANG_OBJECT, 
          null, key, null);
      assertEquals("The given classname should have been wrapped", 
          classMap.get(key), spyVisitor.superClassName);
    }
    
    wrapper.visit(V1_3, ACC_PUBLIC, ClassNames.JAVA_LANG_OBJECT, 
        null, fakeKey, null);
    assertEquals("The given classname should NOT have been wrapped", 
        fakeKey, spyVisitor.superClassName);
  }

  /**
   * Tests 
   * {@link WrapperClassAdapter#visitMethod(int, String, String, String, String[])}
   * .
   * 
   * <p>This tests verifies that constructors of wrapped types are wrapped 
   * with invocations to the related constructor of the wrapper.
   */
  public void testWrappingConstructor() {
    MethodVisitor mv = wrapper.visitMethod(Opcodes.ACC_PUBLIC, "Method", "()V", 
        null, null);
    assertTrue("Wrong MethodVisitor.", mv instanceof WrapperMethodAdapter);
    
    for (String key : classMap.keySet()) {
      mv.visitMethodInsn(Opcodes.ACC_PUBLIC, key, "<init>", "()V");
      assertEquals("The given classname should have been wrapped", 
          classMap.get(key), spyVisitor.ownerClassName);
    }
    
    mv.visitMethodInsn(Opcodes.ACC_PUBLIC, fakeKey, "<init>", "()V");
    assertEquals("The given classname should NOT have been wrapped", 
        fakeKey, spyVisitor.ownerClassName);
  }
  
  /**
   * Tests 
   * {@link WrapperClassAdapter#visitMethod(int, String, String, String, String[])}
   * .
   * 
   * <p>This test checks that the operator new has been invoked on the wrapped 
   * type when necessary.
   */
  public void testWrappingOperatorNew() {
    MethodVisitor mv = wrapper.visitMethod(Opcodes.ACC_PUBLIC, "Method", "()V", 
        null, null);
    assertTrue("Wrong MethodVisitor.", mv instanceof WrapperMethodAdapter);
    
    for (String key : classMap.keySet()) {
      mv.visitTypeInsn(Opcodes.NEW, key);
      assertEquals("The given classname should have been wrapped", 
          classMap.get(key), spyVisitor.ownerClassName);
    }
    
    mv.visitTypeInsn(Opcodes.NEW, fakeKey);
    assertEquals("The given classname should NOT have been wrapped", 
        fakeKey, spyVisitor.ownerClassName);
  }
}
