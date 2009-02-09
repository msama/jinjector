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

import java.io.*;
import java.nio.channels.InterruptibleChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.spi.AbstractInterruptibleChannel;
import java.nio.channels.spi.AbstractSelectableChannel;

import javax.management.remote.rmi.RMIConnection;

/**
 * JUnit test for {@link ClassManager}.
 * 
 * @author Michele Sama
 *
 */
public class ClassManagerTest extends TestCase {

  private ClassManager classManager = null;
  
  /**
   * Creates a new instance of {@link DummyClassManager} to be used instead of
   * the original {@link ClassManager} in order to load classes with the
   * default classloader.
   */
  @Override
  public void setUp() throws Exception {
    classManager = new DummyClassManager();
  }

  /**
   * Test method for 
   * {@link ClassManager#isAssignableFrom(java.lang.String, java.lang.String)}.
   * 
   * <p>This test is verifying that two classes implementing the same 
   * interface but not having a common ancestor would not be assignable.
   */
  public void testIsAssignableFrom_commonInterface() {
    String out = DummyClassManager.getInternalClassname(OutputStream.class);
    String in = DummyClassManager.getInternalClassname(InputStream.class);
    
    assertEquals("Wrong assignemnt with interfaces.",
        OutputStream.class.isAssignableFrom(InputStream.class),
        classManager.isAssignableFrom(out, in));
    assertEquals("Wrong assignemnt with interfaces.", 
        InputStream.class.isAssignableFrom(OutputStream.class),
        classManager.isAssignableFrom(out, in));
  }
  
  /**
   * Test method for 
   * {@link ClassManager#isAssignableFrom(java.lang.String, java.lang.String)}.
   * 
   * <p>This test is verifying that two classes implementing the same 
   * interface but not having a common ancestor would not be assignable.
   */
  public void testIsAssignableFrom_inheritance() {
    String child = DummyClassManager.getInternalClassname(
        ServerSocketChannel.class);
    String parent = DummyClassManager.getInternalClassname(
        AbstractSelectableChannel.class);
    String obj = DummyClassManager.getInternalClassname(Object.class);
    
    assertEquals("Wrong assignment in a hierarchy.", 
        Object.class.isAssignableFrom(AbstractSelectableChannel.class),
        classManager.isAssignableFrom(obj, parent));
    assertEquals("Wrong assignment in a hierarchy.", 
        AbstractSelectableChannel.class.isAssignableFrom(
            ServerSocketChannel.class),
        classManager.isAssignableFrom(parent, child));
    assertEquals("Wrong assignment in a hierarchy.", 
        Object.class.isAssignableFrom(ServerSocketChannel.class),
        classManager.isAssignableFrom(obj, child));
    
    assertEquals("Wrong assignment in a hierarchy.", 
        AbstractSelectableChannel.class.isAssignableFrom(Object.class),
        classManager.isAssignableFrom(parent, obj));
    assertEquals("Wrong assignment in a hierarchy.", 
        ServerSocketChannel.class.isAssignableFrom(Object.class),
        classManager.isAssignableFrom(child, obj));
    assertEquals("Wrong assignment in a hierarchy.", 
        ServerSocketChannel.class.isAssignableFrom(
            AbstractSelectableChannel.class),
        classManager.isAssignableFrom(child, parent));
  }

  /**
   * Test method for 
   * {@link ClassManager#isImplementing(java.lang.String, java.lang.String)}.
   */
  public void testIsImplementing() {
    String closeable = DummyClassManager.getInternalClassname(Closeable.class);
    String interruptable = DummyClassManager.getInternalClassname(
        InterruptibleChannel.class);
    String out = DummyClassManager.getInternalClassname(OutputStream.class);
    
    assertTrue("Classes should be implementing the interface", 
        classManager.isImplementing(out, closeable));
    
    assertFalse("Classes should be implementing the interface", 
        classManager.isImplementing(out, interruptable));
  }

  /**
   * Test method for 
   * {@link ClassManager#isInterface(java.lang.String)}.
   */
  public void testIsInterface() {
    String closeable = DummyClassManager.getInternalClassname(Closeable.class);
    String out = DummyClassManager.getInternalClassname(OutputStream.class);
    
    assertTrue(closeable + " should be an interface", 
        classManager.isInterface(closeable));
    
    assertFalse(out + " is not an interface", 
        classManager.isInterface(out));
  }

  /**
   * Test method for 
   * {@link ClassManager#getSuperclass(java.lang.String)}.
   * 
   * <p>Verifies that inheritance would be loaded correctly.
   */
  public void testGetSuperclass_derivedClass() {
    String child = DummyClassManager.getInternalClassname(
        ServerSocketChannel.class);
    String parent = DummyClassManager.getInternalClassname(
        AbstractSelectableChannel.class);
    String parent2 = DummyClassManager.getInternalClassname(
        SelectableChannel.class);
    String parent3 = DummyClassManager.getInternalClassname(
        AbstractInterruptibleChannel.class);
    String obj = DummyClassManager.getInternalClassname(Object.class);
    
    assertEquals("Wrong parent", parent, classManager.getSuperclass(child));
    assertEquals("Wrong parent", parent2, classManager.getSuperclass(parent));
    assertEquals("Wrong parent", parent3, classManager.getSuperclass(parent2));
    assertEquals("Wrong parent", obj, classManager.getSuperclass(parent3));
  }
  
  /**
   * Test method for 
   * {@link ClassManager#getSuperclass(java.lang.String)}.
   * 
   * <p> Verifies that an interface will have <code>Object</code> as superclass.
   */
  public void testGetSuperclass_interface() {
    String obj = DummyClassManager.getInternalClassname(Object.class);
    String closeable = DummyClassManager.getInternalClassname(
        Closeable.class);
    assertEquals("Interface must have Object as superclass", obj,
        classManager.getSuperclass(closeable));
  }
  
  /**
   * Test method for 
   * {@link ClassManager#getSuperclass(java.lang.String)}.
   * 
   * <p>Verifies that <code>Object</code> will have null as superclass.
   */
  public void testGetSuperclass_object() {
    String obj = DummyClassManager.getInternalClassname(Object.class);
    assertNull("Object must have null superclass", 
        classManager.getSuperclass(obj));
  }
  
  /**
   * Test method for 
   * {@link ClassManager#getSuperclass(java.lang.String)} and 
   * {@link ClassManager#isAssignableFrom(java.lang.String, java.lang.String)}.
   * 
   * <p>This specific test case check the behavior when an interface extends
   * another interface.
   */
  public void testInterfaceExtension() {
    String closeable = DummyClassManager.getInternalClassname(Closeable.class);
    String interruptibleChannel = DummyClassManager.getInternalClassname(
        InterruptibleChannel.class);
    String connection = DummyClassManager.getInternalClassname(
        RMIConnection.class);
    String obj = DummyClassManager.getInternalClassname(Object.class);
    
    assertEquals("Interface must have Object as superclass", obj,
        classManager.getSuperclass(closeable));
    assertEquals("Interface must have Object as superclass", obj,
        classManager.getSuperclass(interruptibleChannel));
    assertEquals("Interface must have Object as superclass", obj,
        classManager.getSuperclass(connection));
    
    assertEquals("Interface assignement", 
        RMIConnection.class.isAssignableFrom(Closeable.class), 
        classManager.isAssignableFrom(connection, closeable));
    
    assertEquals("Interface assignement", 
        InterruptibleChannel.class.isAssignableFrom(Closeable.class), 
        classManager.isAssignableFrom(interruptibleChannel, closeable));
    
    assertEquals("Interface assignement", 
        Closeable.class.isAssignableFrom(InterruptibleChannel.class), 
        classManager.isAssignableFrom(closeable, interruptibleChannel));
    
    assertEquals("Interface assignement", 
        Closeable.class.isAssignableFrom(RMIConnection.class), 
        classManager.isAssignableFrom(closeable, connection));
    
    assertEquals("Interface assignement", 
        InterruptibleChannel.class.isAssignableFrom(RMIConnection.class), 
        classManager.isAssignableFrom(interruptibleChannel, connection));
    
    assertEquals("Interface assignement", 
        RMIConnection.class.isAssignableFrom(InterruptibleChannel.class), 
        classManager.isAssignableFrom(connection, interruptibleChannel));
  }
}
