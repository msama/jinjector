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

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * JUnit test for StackServant.
 * 
 * @author Michele Sama
 * 
 */
public class StackServantTest extends TestCase {
  
  String[] symbols =
    new String[] {"Z", "B", "S", "C", "I", "F", "Lasd;", "J", "D"};
  
  /**
   * Tests {@link StackServant#processParams(int, java.lang.String)}.
   */
  public void testProcessParams() {
    String testName = "testProcessParams";

    MethodVisitor mv = null;
    int access = 0;
    String description = null;
    StackServant servant = null;
    boolean exception = false;

    // null
    exception = false;
    try {
      servant = new StackServant(mv, access, null);
    } catch (RuntimeException e) {
      exception = true;
    }
    assertTrue(testName, exception);

    // X
    description = "()D";
    servant = new StackServant(mv, access, description);
    assertEquals(testName, 0, servant.getArgumentSize());

    // "Z", "B", "S", "C", "I", "F", "L", "[", "J", "D"
    String[] symbols1 =
        new String[] {"Z", "B", "S", "C", "I", "F", "Lasd;", "J", "D"};
    for (String s : symbols1) {
      for (int i = 0; i < 5; i++) {
        StringBuffer array = new StringBuffer();
        for (int j = 0; j < i; j++) {
          array.append("[");
        }
        array.append(s);
        s = array.toString();
        servant = new StackServant(mv, access, "(" + s + ")V");
        assertEquals(testName, 1, servant.getArgumentSize());
        assertEquals(testName + " desc=(" + s + ")V", s, servant
            .getArgumentTypeAt(0));
      }
    }
  }

  /**
   * Tests {@link StackServant#processReturnType(String)}.
   */
  public void testProcessReturnType() {
    // V
    assertReturnType("V");
    
    //Wrong types
    try {
      assertReturnType("");
      fail("Missing type should have raised an exception.");
    } catch (IllegalArgumentException e) {
      // Ok!
    }
    try {
      assertReturnType("X");
      fail("Wrong type should have raised an Exception!");
    } catch (IllegalArgumentException e) {
      // Ok!
    }
    try {
      assertReturnType("ASDASDsss");
      fail("Illegal type should have raised an exception.");
    } catch (IllegalArgumentException e) {
      // Ok!
    }

    // "Z", "B", "S", "C", "I", "F", "L", "[", "J", "D"
    for (String s : symbols) {
      for (int i = 0; i < 5; i++) {
        StringBuilder builder = new StringBuilder();
        for (int j = 0; j < i; j++) {
          builder.append('[');
        }
        builder.append(s);
        assertReturnType(builder.toString());
      }
    }

    // Multiple returns.
    for (String s0 : symbols) {
      for (String s1 : symbols) {
        try {
          assertReturnType(s0 + s1);
          fail("Wrong type should have raised an exception: " + s0 + s1);
        } catch (IllegalArgumentException e) {
          // Ok!
        } 
      }
    }
  }


  /**
   * Tests {@link StackServant#increaseInstrumentedStack(int)}.
   */
  public void testIncreaseInstrumentedStack() {
    String testName = "testIncreaseInstrumentedStack";

    MethodVisitor mv = null;
    int access = Opcodes.ACC_STATIC;
    String description = null;
    StackServant servant = null;
    int size = -1;

    description = "()V";
    servant = new StackServant(mv, access, description);
    size = servant.getInstrumentedFrameSize();
    assertEquals(testName, 0, size);
    for (int i = 1; i < 20; i++) {
      servant.increaseInstrumentedStack(1);
      size = servant.getInstrumentedFrameSize();
      assertEquals(testName, i, size);
    }

    description = "(IZI)V";
    servant = new StackServant(mv, access, description);
    size = servant.getInstrumentedFrameSize();
    assertEquals(testName, 3, size);
    for (int i = 1; i < 20; i++) {
      servant.increaseInstrumentedStack(1);
      size = servant.getInstrumentedFrameSize();
      assertEquals(testName, 3 + i, size);
    }
  }

  /**
   * Tests {@link StackServant#getInitialFrameSize()}.
   */
  public void testGetInitialFrameSize() {
    String testName = "testGetInitialFrameSize";

    MethodVisitor mv = null;
    String description = null;
    StackServant servant = null;
    int size = -1;

    description = "()V";
    servant = new StackServant(mv, Opcodes.ACC_STATIC, description);
    size = servant.getInitialFrameSize();
    assertEquals(testName, 0, size);
    servant = new StackServant(mv, 0, description);
    size = servant.getInitialFrameSize();
    assertEquals(testName, 1, size);

    description = "()D";
    servant = new StackServant(mv, Opcodes.ACC_STATIC, description);
    size = servant.getInitialFrameSize();
    assertEquals(testName, 0, size);
    servant = new StackServant(mv, 0, description);
    size = servant.getInitialFrameSize();
    assertEquals(testName, 1, size);

    // "Z", "B", "S", "C", "I", "F", "L", "["
    String[] symbols1 =
        new String[] {"Z", "B", "S", "C", "I", "F", "Lasd;", "[I"};
    for (String s : symbols1) {
      servant = new StackServant(mv, Opcodes.ACC_STATIC, "(" + s + ")V");
      size = servant.getInitialFrameSize();
      assertEquals(testName, 1, size);
    }

    // "J", "D"
    String[] symbols2 = new String[] {"J", "D"};
    for (String s : symbols2) {
      servant = new StackServant(mv, Opcodes.ACC_STATIC, "(" + s + ")V");
      size = servant.getInitialFrameSize();
      assertEquals(testName, 2, size);
    }


    StringBuffer sb = new StringBuffer("(");
    int finalSize = 0;
    for (int i = 1; i < 5; i++) {
      sb.append(symbols1[(int) (symbols1.length * Math.random())]);
      sb.append(symbols2[(int) (symbols2.length * Math.random())]);
      finalSize += 3;
    }
    sb.append(")J");
    servant = new StackServant(mv, Opcodes.ACC_STATIC, sb.toString());
    size = servant.getInitialFrameSize();
    assertEquals(testName, finalSize, size);
  }



  /**
   * Tests {@link StackServant#getReturnType()}.
   * 
   * <p>This test is only verifying the correctness of the getter and not the 
   * computation of the return type which is verified by 
   * {@link #testProcessReturnType()}.
   */
  public void testGetReturnType() {
    for (String s : symbols) {
      assertReturnType(s);
    }
  }
  
  /**
   * Initializes a new StackServant with a given description and checks if 
   * the return type is correct.
   * 
   * @param type The given return type.
   */
  private void assertReturnType(String type) {
    StackServant servant = new StackServant(null, Opcodes.ACC_PUBLIC, 
        "()" + type);
    assertEquals("Wrong return type", type, servant.getReturnType());
  }

  /**
   * Tests {@link StackServant#getArgumentSize()} and 
   * {@link StackServant#getArgumentTypeAt(int)}.
   */
  public void testGetArguments() {
    StackServant servant = null;
    StringBuilder builder = new StringBuilder();
    
    for (int i = 0; i < symbols.length; i++) {
      for (int j = 0; j < i; j++) {
        builder.append(symbols[j]);
      }
      servant = new StackServant(null, Opcodes.ACC_PRIVATE, 
          "(" + builder.toString() + ")V");
      assertEquals("Wrong arguments size.", i, servant.getArgumentSize());
      for (int j = 0; j < i; j++) {
        assertEquals("Wrong arguments at index.", symbols[j], 
            servant.getArgumentTypeAt(j));
      }
      builder.setLength(0);
    }
  }

  /**
   * Tests {@link StackServant#getFrameSizeForType(java.lang.String)}.
   */
  public void testGetFrameSizeForType() {
    String testName = "testGetFrameSizeForType";

    String type = null;
    int size = -1;
    boolean exception = false;

    // null
    exception = false;
    try {
      size = StackServant.getFrameSizeForType(type);
    } catch (RuntimeException e) {
      exception = true;
    }
    assertTrue(testName + " type=null.", exception);

    // type=""
    type = "";
    exception = false;
    try {
      size = StackServant.getFrameSizeForType(type);
    } catch (RuntimeException e) {
      exception = true;
    }
    assertTrue(testName + " type=\"\".", exception);

    // type="asd"
    type = "asd";
    exception = false;
    try {
      size = StackServant.getFrameSizeForType(type);
    } catch (RuntimeException e) {
      exception = true;
    }
    assertTrue(testName + " type=\"asd\".", exception);

    // type="V"
    type = "V";
    size = StackServant.getFrameSizeForType(type);
    assertEquals(testName + " type=\"V\".", 0, size);

    // "Z", "B", "S", "C", "I", "F", "L", "["
    String[] symbols1 = new String[] {"Z", "B", "S", "C", "I", "F", "L", "["};
    for (String s : symbols1) {
      size = StackServant.getFrameSizeForType(s);
      assertEquals(testName + " type=\"" + s + "\".", 1, size);
      size = StackServant.getFrameSizeForType("[" + s);
      assertEquals(testName + " type=\"[" + s + "\".", 1, size);
    }

    // "Z", "B", "S", "C", "I", "F", "L", "["
    String[] symbols2 = new String[] {"J", "D"};
    for (String s : symbols2) {
      size = StackServant.getFrameSizeForType(s);
      assertEquals(testName + " type=\"V\".", 2, size);
      size = StackServant.getFrameSizeForType("[" + s);
      assertEquals(testName + " type=\"[" + s + "\".", 1, size);
    }
  }
  
}
