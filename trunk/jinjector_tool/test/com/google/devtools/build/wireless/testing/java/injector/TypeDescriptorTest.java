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

import junit.framework.TestCase;

import org.objectweb.asm.Opcodes;

/**
 * JUnit testing for {@link TypeDescriptor}.
 * 
 * @author Michele Sama
 *
 */
public class TypeDescriptorTest extends TestCase {

  /**
   * Test method for {@link TypeDescriptor#getType(java.lang.String)}.
   */
  public void testGetTypeString() {
    assertEquals("Wrong type conversion", 
        TypeDescriptor.VOID, TypeDescriptor.getType("V"));
    assertEquals("Wrong type conversion", 
        TypeDescriptor.BOOLEAN, TypeDescriptor.getType("Z"));
    assertEquals("Wrong type conversion", 
        TypeDescriptor.BYTE, TypeDescriptor.getType("B"));
    assertEquals("Wrong type conversion", 
        TypeDescriptor.CHAR, TypeDescriptor.getType("C"));
    assertEquals("Wrong type conversion", 
        TypeDescriptor.SHORT, TypeDescriptor.getType("S"));
    assertEquals("Wrong type conversion", 
        TypeDescriptor.INTEGER, TypeDescriptor.getType("I"));
    assertEquals("Wrong type conversion", 
        TypeDescriptor.LONG, TypeDescriptor.getType("J"));
    assertEquals("Wrong type conversion", 
        TypeDescriptor.FLOAT, TypeDescriptor.getType("F"));
    assertEquals("Wrong type conversion", 
        TypeDescriptor.DOUBLE, TypeDescriptor.getType("D"));
    assertEquals("Wrong type conversion", 
        TypeDescriptor.CLASS, TypeDescriptor.getType("Lorg/foo/Foo;"));
    assertEquals("Wrong type conversion", 
        TypeDescriptor.ARRAY, TypeDescriptor.getType("[[[I"));
    
    try {
      TypeDescriptor.getType(null);
      fail("Null string should have thrown an exception");
    } catch (IllegalArgumentException e) {
      // OK!!
    }
    
    try {
      TypeDescriptor.getType("ZED");
      fail("Wrong string should have thrown an exception");
    } catch (IllegalArgumentException e) {
      // OK!!
    }
  }

  /**
   * Test method for {@link TypeDescriptor#getType(char)}, 
   * {@link TypeDescriptor#getValue()}.
   */
  public void testGetTypeChar() {
    assertEquals("Wrong type conversion", TypeDescriptor.VOID, 
        TypeDescriptor.getType(TypeDescriptor.VOID.getValue()));
    assertEquals("Wrong type conversion", TypeDescriptor.BOOLEAN, 
        TypeDescriptor.getType(TypeDescriptor.BOOLEAN.getValue()));
    assertEquals("Wrong type conversion", TypeDescriptor.BYTE, 
        TypeDescriptor.getType(TypeDescriptor.BYTE.getValue()));
    assertEquals("Wrong type conversion", TypeDescriptor.CHAR, 
        TypeDescriptor.getType(TypeDescriptor.CHAR.getValue()));
    assertEquals("Wrong type conversion", TypeDescriptor.SHORT, 
        TypeDescriptor.getType(TypeDescriptor.SHORT.getValue()));
    assertEquals("Wrong type conversion", TypeDescriptor.INTEGER, 
        TypeDescriptor.getType(TypeDescriptor.INTEGER.getValue()));
    assertEquals("Wrong type conversion", TypeDescriptor.LONG, 
        TypeDescriptor.getType(TypeDescriptor.LONG.getValue()));
    assertEquals("Wrong type conversion", TypeDescriptor.FLOAT, 
        TypeDescriptor.getType(TypeDescriptor.FLOAT.getValue()));
    assertEquals("Wrong type conversion", TypeDescriptor.DOUBLE, 
        TypeDescriptor.getType(TypeDescriptor.DOUBLE.getValue()));
    assertEquals("Wrong type conversion", TypeDescriptor.CLASS, 
        TypeDescriptor.getType(TypeDescriptor.CLASS.getValue()));
    assertEquals("Wrong type conversion", TypeDescriptor.ARRAY, 
        TypeDescriptor.getType(TypeDescriptor.ARRAY.getValue()));
  }

  /**
   * Test method for {@link TypeDescriptor#getFrameSize()}.
   */
  public void testGetFrameSizeForType() {
    assertEquals("Wrong Frame size", 0, 
        TypeDescriptor.VOID.getFrameSize());
    assertEquals("Wrong Frame size", 1, 
        TypeDescriptor.BOOLEAN.getFrameSize());
    assertEquals("Wrong Frame size", 1, 
        TypeDescriptor.BYTE.getFrameSize());
    assertEquals("Wrong Frame size", 1, 
        TypeDescriptor.CHAR.getFrameSize());
    assertEquals("Wrong Frame size", 1, 
        TypeDescriptor.SHORT.getFrameSize());
    assertEquals("Wrong Frame size", 1, 
        TypeDescriptor.INTEGER.getFrameSize());
    assertEquals("Wrong Frame size", 2, 
        TypeDescriptor.LONG.getFrameSize());
    assertEquals("Wrong Frame size", 1, 
        TypeDescriptor.FLOAT.getFrameSize());
    assertEquals("Wrong Frame size", 2, 
        TypeDescriptor.DOUBLE.getFrameSize());
    assertEquals("Wrong Frame size", 1, 
        TypeDescriptor.CLASS.getFrameSize());
    assertEquals("Wrong Frame size", 1, 
        TypeDescriptor.ARRAY.getFrameSize());
  }

  /**
   * Test method for {@link TypeDescriptor#getLoadOpcode()}.
   */
  public void testGetLoadOpcode() {
    try {
      TypeDescriptor.VOID.getLoadOpcode();
      fail("Void should have thrown an exception!");
    } catch (IllegalStateException e) {
      // OK!
    }
    assertEquals("Wrong LOAD instruction", Opcodes.ILOAD, 
        TypeDescriptor.BOOLEAN.getLoadOpcode());
    assertEquals("Wrong STORE instruction", Opcodes.ILOAD, 
        TypeDescriptor.BYTE.getLoadOpcode());
    assertEquals("Wrong STORE instruction", Opcodes.ILOAD, 
        TypeDescriptor.CHAR.getLoadOpcode());
    assertEquals("Wrong STORE instruction", Opcodes.ILOAD, 
        TypeDescriptor.SHORT.getLoadOpcode());
    assertEquals("Wrong STORE instruction", Opcodes.ILOAD, 
        TypeDescriptor.INTEGER.getLoadOpcode());
    assertEquals("Wrong STORE instruction", Opcodes.LLOAD, 
        TypeDescriptor.LONG.getLoadOpcode());
    assertEquals("Wrong STORE instruction", Opcodes.FLOAD, 
        TypeDescriptor.FLOAT.getLoadOpcode());
    assertEquals("Wrong STORE instruction", Opcodes.DLOAD, 
        TypeDescriptor.DOUBLE.getLoadOpcode());
    assertEquals("Wrong STORE instruction", Opcodes.ALOAD, 
        TypeDescriptor.CLASS.getLoadOpcode());
    assertEquals("Wrong STORE instruction", Opcodes.ALOAD, 
        TypeDescriptor.ARRAY.getLoadOpcode());
  }

  /**
   * Test method for {@link TypeDescriptor#getStoreOpcode()}.
   */
  public void testGetStoreOpcode() {
    try {
      TypeDescriptor.VOID.getStoreOpcode();
      fail("Void should have thrown an exception!");
    } catch (IllegalStateException e) {
      // OK!
    }
    assertEquals("Wrong STORE instruction", Opcodes.ISTORE, 
        TypeDescriptor.BOOLEAN.getStoreOpcode());
    assertEquals("Wrong STORE instruction", Opcodes.ISTORE, 
        TypeDescriptor.BYTE.getStoreOpcode());
    assertEquals("Wrong STORE instruction", Opcodes.ISTORE, 
        TypeDescriptor.CHAR.getStoreOpcode());
    assertEquals("Wrong STORE instruction", Opcodes.ISTORE, 
        TypeDescriptor.SHORT.getStoreOpcode());
    assertEquals("Wrong STORE instruction", Opcodes.ISTORE, 
        TypeDescriptor.INTEGER.getStoreOpcode());
    assertEquals("Wrong STORE instruction", Opcodes.LSTORE, 
        TypeDescriptor.LONG.getStoreOpcode());
    assertEquals("Wrong STORE instruction", Opcodes.FSTORE, 
        TypeDescriptor.FLOAT.getStoreOpcode());
    assertEquals("Wrong STORE instruction", Opcodes.DSTORE, 
        TypeDescriptor.DOUBLE.getStoreOpcode());
    assertEquals("Wrong STORE instruction", Opcodes.ASTORE, 
        TypeDescriptor.CLASS.getStoreOpcode());
    assertEquals("Wrong STORE instruction", Opcodes.ASTORE, 
        TypeDescriptor.ARRAY.getStoreOpcode());
  }

  /**
   * Test method for {@link TypeDescriptor#getDupOpcode()}.
   */
  public void testGetDupOpcode() {
    try {
      TypeDescriptor.VOID.getDupOpcode();
      fail("Void should have thrown an exception!");
    } catch (IllegalStateException e) {
      // OK!
    }
    assertEquals("Wrong DUP instruction", Opcodes.DUP, 
        TypeDescriptor.BOOLEAN.getDupOpcode());
    assertEquals("Wrong DUP instruction", Opcodes.DUP, 
        TypeDescriptor.BYTE.getDupOpcode());
    assertEquals("Wrong DUP instruction", Opcodes.DUP, 
        TypeDescriptor.CHAR.getDupOpcode());
    assertEquals("Wrong DUP instruction", Opcodes.DUP, 
        TypeDescriptor.INTEGER.getDupOpcode());
    assertEquals("Wrong DUP instruction", Opcodes.DUP, 
        TypeDescriptor.SHORT.getDupOpcode());
    assertEquals("Wrong DUP instruction", Opcodes.DUP2, 
        TypeDescriptor.LONG.getDupOpcode());
    assertEquals("Wrong DUP instruction", Opcodes.DUP, 
        TypeDescriptor.FLOAT.getDupOpcode());
    assertEquals("Wrong DUP instruction", Opcodes.DUP2, 
        TypeDescriptor.DOUBLE.getDupOpcode());
    assertEquals("Wrong DUP instruction", Opcodes.DUP, 
        TypeDescriptor.CLASS.getDupOpcode());
    assertEquals("Wrong DUP instruction", Opcodes.DUP, 
        TypeDescriptor.ARRAY.getDupOpcode());
  }
}
