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

import com.google.devtools.build.wireless.testing.java.injector.j2me.J2meClassNames;
import com.google.devtools.build.wireless.testing.java.injector.j2me.LcduiDecoratorClassAdapter;

import junit.framework.TestCase;

import org.objectweb.asm.commons.EmptyVisitor;

import java.io.Serializable;

/**
 * @author Michele Sama
 *
 */
public class ManagedClassAdapterTest extends TestCase {

  private ManagedClassAdapter adapter;
  private DummyClassManager classManager;
  private String[] interfaces;
  
  public ManagedClassAdapterTest(String name) {
    super(name);
  }

  /**
   * Creates an instance of the class adapter.
   * 
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    classManager = new DummyClassManager();
    adapter = new ManagedClassAdapter(new EmptyVisitor(), classManager);
    interfaces = new String[]{
        J2meClassNames.COMMAND_LISTENER, 
        J2meClassNames.ITEM_COMMAND_LISTENER};
  }

  /**
   * Test the constructor by verifying that the class manager has been 
   * assigned correctly.
   */
  public void testConstructor() {
    assertEquals("Wrong or unassigned class manager.", 
        classManager, adapter.classManager);
  }
  
  /**
   * Tests {@link LcduiDecoratorClassAdapter#implementsInterface(String)}.
   */
  public void testImplementsInterface() {
    adapter.visit(V1_3, ACC_PUBLIC, ClassNames.JAVA_LANG_OBJECT, 
          null, null, interfaces);
    
    for (String s : interfaces) {
      assertTrue("Missing implemented interface: " + s, 
          adapter.implementsInterface(s));
    }
    
    assertFalse("Uniplemented interface seams to be implemented.", 
        adapter.implementsInterface(
            Serializable.class.getName().replace(".", "/")));
  }

}
