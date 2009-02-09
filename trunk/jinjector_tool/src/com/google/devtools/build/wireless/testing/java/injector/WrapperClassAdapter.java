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
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Map;

/**
 * General purpose instrumentation wrapper.
 * 
 * <p> This {@link ClassAdapter} wraps specified instances with an opportune 
 * wrapper. Wrapped classes and relative wrappers are contained in 
 * {@link #wrappersMap} and accessed through {@link #wrapperize(String)}.
 * 
 * <p> The implementation assumes that the wrapper is extending the original 
 * class. This strongly reduce the impact on the code because it allows to leave
 * all the invocation on the wrapper assuming that the wrapper is extending 
 * the wrapped.
 * 
 * <p> In order to wrap a class it is necessary to:
 * <ul>
 * <li> Instruments the operator NEW by replacing the wrapped with the wrapper.
 * <li> Instrumenting call to the wrappee's constructor and replacing them 
 * with calls to the wrapper's constructor.
 * <li> Replacing the super class of any class extending the wrapped with the 
 * wrapper.
 * </ul>
 * 
 * <p>If the wrapper class is not preverifying with an error like 
 * "java/lang/ClassCircularityError" it means that inside the constructor of 
 * the wrapper has been instrumented as well and that invocations to the 
 * super constructor habe been instrumented in calls to the the constructor 
 * itself. To fix this it is necessary to exclude the wrapper from the 
 * instrumentation. Eventually it would be possible to add a condition in the 
 * code of this class, but at the moment there is no reason to wrap the wrapper.
 * 
 * @author Michele Sama
 */
public class WrapperClassAdapter extends ManagedClassAdapter {

  /**
   * Maps a class name with the name of is wrapper, if any.
   * 
   * <p> Each "client" is creating an instance with a specific maps.
   */
  private Map<String, String> wrappersMap = null;
  
  /**
   * The name of the current class.
   */
  protected String className = null;
  
  protected Platform targetPlatform;
  
  /**
   * Creates an instance of {@link WrapperClassAdapter} by specifying a 
   * {@link Map} of target and wrappers.
   * 
   * @param cv The nested {@link ClassVisitor}.
   * @param cm The {@link ClassManager} to be used.
   * @param platform The target {@link Platform}.
   * @param wrappers A {@link Map} of wrappers.
   */
  public WrapperClassAdapter(ClassVisitor cv, ClassManager cm, 
      Platform platform, Map<String, String> wrappers) {
    super(cv, cm);
    wrappersMap = wrappers;
    targetPlatform = platform;
  }

  /**
   * Changes any wrappable class names with the right wrapper.
   * 
   * @param name The original class name.
   * @return The name of the wrapper class if any or the original name.
   */
  protected String wrapperize(String name) {
    String wrapperName = wrappersMap.get(name);
    if (wrapperName != null) {
      return wrapperName;
    }
    return name;
  }
  
  /**
   * Wraps the superclass if extending a class which is going to be wrapped.
   */
  @Override
  public void visit(int version, int access, String name, String signature,
      String superName, String[] interfaces) {
    className  = name;
    superName = wrapperize(superName);
    cv.visit(version, access, name, signature, superName, interfaces);
  }

  /**
   * Visits all the method of this class with a {@link WrapperMethodAdapter}.
   */
  @Override
  public MethodVisitor visitMethod(int access, String name, String desc,
      String signature, String[] exceptions) {
    MethodVisitor mv =
      cv.visitMethod(access, name, desc, signature, exceptions);

    // wrap objects
    mv = new WrapperMethodAdapter(mv, access, className, name,
      desc, targetPlatform);
    return mv;
  }
  
 
  /**
   * Method adapter which redirect any constructor invocation to the 
   * opportune wrapper class in order to create instances of the wrapper. 
   * This is working because each wrapper is extending the wrapped class.
   * 
   * <p> Instruction such as:
   * <pre>variable = new Wrappable();</pre>
   * are instrumented with:
   * <pre>variable = new Wrapper();</pre>.
   * 
   * <p> To create the right instance it is also required to patch NEW 
   * instructions.
   * 
   * @author Michele Sama
   *
   */
  class WrapperMethodAdapter extends InjectorMethodAdapter {
    
    /**
     * Creates an instance of this method adapter.
     * 
     * @param mv The nested {@link MethodVisitor}.
     * @param access The method access.
     * @param owner The name of the class containing this method.
     * @param name The method's name.
     * @param desc The method description.
     * @param platform The targetPlatform.
     */
    public WrapperMethodAdapter(MethodVisitor mv, 
        int access, String owner, String name, String desc, Platform platform) {
      super(mv, access, owner, name, desc, platform);
    }

    /**
     * Changes the type (if required) before any constructor invocation.
     */
    @Override
    public void visitMethodInsn(int opcode, String owner, 
        String name, String desc) {
      if (name.equals("<init>")) {
        owner = wrapperize(owner);
      }
      mv.visitMethodInsn(opcode, owner, name, desc);
    }

    /**
     * Changes the type (if required) before any NEW instruction.
     */
    @Override
    public void visitTypeInsn(int opcode, String type) {
      if (Opcodes.NEW == opcode) {
        type = wrapperize(type);
      }
      mv.visitTypeInsn(opcode, type);
    }
  }
}



