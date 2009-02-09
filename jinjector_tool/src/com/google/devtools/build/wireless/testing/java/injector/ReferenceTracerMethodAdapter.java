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

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Allow tracing of given classes by instrumenting them to behave as singletons.
 * 
 * <p>This method adapter instruments the constructor of a given class by 
 * invoking at the end of it a static setter which store a reference to the 
 * instance. 
 * 
 * <p>The setter is supposed to be a static method of a different class 
 * invokable as setterClassName.setterMethodsName(LsingletonClassName;)
 * 
 * @author Michele Sama
 *
 */
public class ReferenceTracerMethodAdapter extends InjectorMethodAdapter {
  
  private String setterClassName;
  
  private String setterMethodsName;
  
  /**
   * Constructor from superclass.
   * 
   * @param mv The nested {@link MethodVisitor}.
   * @param setterClassName the name of the class containing the setter.
   * @param setterMethodName the name of the setter method.
   * @param access The method's access opcode.
   * @param visitedClass The name of the class containing this method.
   * @param methodName The method's name.
   * @param desc The method's description.
   * @param platform The target {@link Platform}.
   */
  public ReferenceTracerMethodAdapter(MethodVisitor mv,
      String setterClassName, String setterMethodName,
      int access, String visitedClass, String methodName, String desc, 
      Platform platform) {
    super(mv, access, visitedClass, methodName, desc, platform);
    if (!"<init>".equals(methodName)) {
      throw new IllegalStateException("Visited method is not a constructor.");
    }
    this.setterClassName = setterClassName;
    this.setterMethodsName = setterMethodName;
  }

  /**
   * Injects, at the end of the method, which will be a constructor, a set of 
   * instruction to register this instance in the wrapper.
   * 
   * @param opcode The instruction code.
   * @see org.objectweb.asm.MethodAdapter#visitInsn(int)
   */
  @Override
  public void visitInsn(int opcode) {
    if (isReturnInstruction(opcode) || opcode == Opcodes.ATHROW) {
      stackServant.loadThis();
      mv.visitMethodInsn(Opcodes.INVOKESTATIC, setterClassName,
          setterMethodsName, "(L" + ownerClass + ";)V");
    }
    mv.visitInsn(opcode);
  }

}