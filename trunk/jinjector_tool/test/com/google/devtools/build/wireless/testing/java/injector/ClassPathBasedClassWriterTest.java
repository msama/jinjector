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

import org.objectweb.asm.ClassWriter;

import java.io.*;
import java.nio.channels.Channel;

/**
 * JUnit test for {@link ClasspathBasedClassWriter}.
 * 
 * @author Michele Sama
 *
 */
public class ClassPathBasedClassWriterTest extends TestCase {

  private ClasspathBasedClassWriter classWriter = null;
  
  /**
   * Creates the needed instance of {@link ClasspathBasedClassWriter}.
   * 
   * @throws java.lang.Exception
   */
  @Override
  public void setUp() throws Exception {
    classWriter = new ClasspathBasedClassWriter(ClassWriter.COMPUTE_FRAMES, 
        new DummyClassManager());
  }


  /**
   * Test method for 
   * {@link ClasspathBasedClassWriter#getCommonSuperClass(String, String)}
   * .
   * 
   * <p>Tests:
   * <ul>
   * <li> Classes of the same hierarchy.
   * <li> Classes not related.
   * <li> Interfaces in the same hierarchy.
   * <li> Interfaced not related. 
   * </ul>
   */
  public void testGetCommonSuperClass() {
    String object = Object.class.getName().replace('.', '/');
    String child = Writer.class.getName().replace('.', '/');
    String subchild = PrintWriter.class.getName().replace('.', '/');
    String common = null;
    common = classWriter.getCommonSuperClass(child, object);
    assertEquals("Wrong superclass.", object, common);
    
    common = classWriter.getCommonSuperClass(object, child);
    assertEquals("Wrong superclass.", object, common);
    
    common = classWriter.getCommonSuperClass(child, child);
    assertEquals("Wrong superclass.", child, common);
    
    common = classWriter.getCommonSuperClass(child, subchild);
    assertEquals("Wrong superclass.", child, common);
    
    common = classWriter.getCommonSuperClass(subchild, child);
    assertEquals("Wrong superclass.", child, common);
    
    String anotherClass = File.class.getName().replace('.', '/');
    common = classWriter.getCommonSuperClass(child, anotherClass);
    assertEquals("Wrong superclass.", object, common);
    
    common = classWriter.getCommonSuperClass(anotherClass, child);
    assertEquals("Wrong superclass.", object, common);
    
    String baseInerface = Closeable.class.getName().replace('.', '/');
    String subInterface = Channel.class.getName().replace('.', '/');
    
    common = classWriter.getCommonSuperClass(baseInerface, subInterface);
    assertEquals("Wrong super interface.", baseInerface, common);
    
    common = classWriter.getCommonSuperClass(subInterface, baseInerface);
    assertEquals("Wrong super interface.", baseInerface, common);
    
    common = classWriter.getCommonSuperClass(subInterface, subInterface);
    assertEquals("Wrong super interface.", subInterface, common);
    
    String anotherInterface = Serializable.class.getName().replace('.', '/');
    
    common = classWriter.getCommonSuperClass(subInterface, anotherInterface);
    assertEquals("Wrong superclass.", object, common);
  }
  
  /**
   * Test method for 
   * {@link ClasspathBasedClassWriter#getCommonSuperClass(String, String)}
   * .
   * 
   * <p>Tests classes of the same hierarchy.
   */
  public void testGetCommonSuperClass_hierarchy() {
    String object = Object.class.getName().replace('.', '/');
    String child = Writer.class.getName().replace('.', '/');
    String subchild = PrintWriter.class.getName().replace('.', '/');
    String common = null;
    common = classWriter.getCommonSuperClass(child, object);
    assertEquals("Wrong superclass.", object, common);
    
    common = classWriter.getCommonSuperClass(object, child);
    assertEquals("Wrong superclass.", object, common);
    
    common = classWriter.getCommonSuperClass(child, child);
    assertEquals("Wrong superclass.", child, common);
    
    common = classWriter.getCommonSuperClass(child, subchild);
    assertEquals("Wrong superclass.", child, common);
    
    common = classWriter.getCommonSuperClass(subchild, child);
    assertEquals("Wrong superclass.", child, common);
  }
  
  /**
   * Test method for 
   * {@link ClasspathBasedClassWriter#getCommonSuperClass(String, String)}
   * .
   * 
   * <p> Tests that the superclass of two unrelated objects would 
   * be {@link Object}.
   */
  public void testGetCommonSuperClass_classesNotRelated() {
    String object = Object.class.getName().replace('.', '/');
    String child = Writer.class.getName().replace('.', '/');
    String subchild = PrintWriter.class.getName().replace('.', '/');
    String anotherClass = File.class.getName().replace('.', '/');
    
    String common = null;
    
    common = classWriter.getCommonSuperClass(child, anotherClass);
    assertEquals("Wrong superclass.", object, common);
    
    common = classWriter.getCommonSuperClass(anotherClass, child);
    assertEquals("Wrong superclass.", object, common);
  }
  
  /**
   * Test method for 
   * {@link ClasspathBasedClassWriter#getCommonSuperClass(String, String)}
   * .
   * 
   * <p>Tests that ancestor-interfaces would be recognized.
   */
  public void testGetCommonSuperClass_hierarchyOfInterfaces() {
    String baseInerface = Closeable.class.getName().replace('.', '/');
    String subInterface = Channel.class.getName().replace('.', '/');
    
    String common = null;
    
    common = classWriter.getCommonSuperClass(baseInerface, subInterface);
    assertEquals("Wrong super interface.", baseInerface, common);
    
    common = classWriter.getCommonSuperClass(subInterface, baseInerface);
    assertEquals("Wrong super interface.", baseInerface, common);
    
    common = classWriter.getCommonSuperClass(subInterface, subInterface);
    assertEquals("Wrong super interface.", subInterface, common);
  }
  
  /**
   * Test method for 
   * {@link ClasspathBasedClassWriter#getCommonSuperClass(String, String)}
   * .
   * 
   * <p> Tests that if two interfaces are not part of the same hierarchy 
   * the superclass would be object.
   */
  public void testGetCommonSuperClass_interfacesNotRelated() {
    String object = Object.class.getName().replace('.', '/');
    String subInterface = Channel.class.getName().replace('.', '/');
    String anotherInterface = Serializable.class.getName().replace('.', '/');
    
    String common = classWriter.getCommonSuperClass(subInterface, anotherInterface);
    assertEquals("Wrong superclass.", object, common);
  }
}
