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

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;

/**
 * A ClassAdapter with a reference to a ClassManager.
 * 
 * <p>All the class adapter which needs to use classpath-related features 
 * needs to extend this class.
 * 
 * @author Michele Sama
 */
public class ManagedClassAdapter extends ClassAdapter {

  protected ClassManager classManager = null;
  
  //An array with all the implemented interfaces.
  protected String[] implementedInterfaces = null;
  
  /**
   * Creates an instance of ClassAdapter with the specified ClassManager.
   * 
   * @param cv The nested ClassVisitor required by the superclass constructor.
   * @param cm The ClassManager to use.
   */
  public ManagedClassAdapter(ClassVisitor cv, ClassManager cm) {
    super(cv);
    classManager = cm;
  }
  
  /**
   * Tells if the current visited class is implementing the given interface.
   * 
   * <p> Please note that the field {@link #implementedInterfaces} needs to 
   * be initialized in the method 
   * {@link #visit(int, int, String, String, String, String[])}. Classes 
   * overriding this method need to reinitialize it as opportune.
   * 
   * <p> this method do not consider Interfaces extending other
   * interfaces. However this adapter is supposed to inject a 
   * <code>class</code> and not an interface (because it injects code and 
   * interfaces do not contain any concrete implementation).
   * 
   * @param interfaceName the query interface.
   * @return <code>true</code> if the given interface is implemented, 
   *    <code>false</code> otherwise.
   */
  protected boolean implementsInterface(String interfaceName) {
    if (implementedInterfaces == null) {
      return false;
    }
    for (String s : implementedInterfaces) {
      if (s.equals(interfaceName)) {
        return true;
      }
    }
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(int version, int access, String name, String signature,
      String superName, String[] interfaces) {
    implementedInterfaces = interfaces;
    super.visit(version, access, name, signature, superName, interfaces);
  }
  
}

