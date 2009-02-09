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

package com.google.devtools.build.wireless.testing.java.injector.j2me;

import static org.objectweb.asm.Opcodes.*;

import com.google.devtools.build.wireless.testing.java.injector.DummyClassManager;
import com.google.devtools.build.wireless.testing.java.injector.Platform;

import junit.framework.TestCase;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.EmptyVisitor;


/**
 * JUnit tests for {@link LcduiDecoratorClassAdapter}.
 * 
 * @author Michele Sama
 *
 */
public class LcduiDecoratorClassAdapterTest extends TestCase {

  private LcduiDecoratorClassAdapter adapter;
  private String[] interfaces;

  /**
   * Initialize the adapter and an array of interfaces.
   * 
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    adapter = new LcduiDecoratorClassAdapter(new EmptyVisitor(), 
        new DummyClassManager(), Platform.RIM);
    interfaces = new String[]{
        J2meClassNames.COMMAND_LISTENER, 
        J2meClassNames.ITEM_COMMAND_LISTENER};
  }
  
  /**
   * Tests 
   * {@link LcduiDecoratorClassAdapter#visitMethod(int, String, String, String, String[])}
   * by invoking it and checking that the correct {@link MethodVisitor} has
   * been returned.
   */
  public void testMethodVisitorAllocation_visitingDisplayable() {
    MethodVisitor mv = invokeVisitMethodOnAdapter(J2meClassNames.DISPLAYABLE);
    assertTrue("The returned method visitor should be an instance of " +
        "CommandLoggerMethodAdapter.", 
        mv instanceof CommandLoggerMethodAdapter);
  }

  /**
   * Tests 
   * {@link LcduiDecoratorClassAdapter#visitMethod(int, String, String, String, String[])}
   * by invoking it and checking that the correct {@link MethodVisitor} has
   * been returned.
   */
  public void testMethodVisitorAllocation_visitingItem() {
    MethodVisitor mv = invokeVisitMethodOnAdapter(J2meClassNames.ITEM);
    assertTrue("The returned method visitor should be an instance of " +
        "CommandLoggerMethodAdapter.", 
        mv instanceof CommandLoggerMethodAdapter);
  }
  
  /**
   * Starts the visiting process and returns the opportune 
   * {@link MethodVisitor}.
   * 
   * @param targetParameter the target class name. It can be 
   *    <code>Displayable</code> or <code>Item</code>.
   * @return the generated {@link MethodVisitor}.
   */
  private MethodVisitor invokeVisitMethodOnAdapter(String targetParameter) {
    adapter.visit(V1_3, ACC_PUBLIC, J2meClassNames.ALERT, null, 
        targetParameter, interfaces);
    return adapter.visitMethod(ACC_PUBLIC, 
        LcduiDecoratorClassAdapter.COMMAND_ACTION, 
        "(L" + J2meClassNames.COMMAND + ";" + 
        "L"+ targetParameter + ";)V", 
        null, null);
  }
  
}
