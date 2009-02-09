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

import java.util.logging.Logger;

/**
 * Logs exceptions as the are propagated at runtime in the stack.
 * 
 * <p>In order to prevent end users to receive exceptions developers 
 * encapsulate and catch all of them. This is a good habit in terms of usability 
 * but also very bad for testing/debugging. This class adapter enables
 * exceptions to be exposed in an instrumented version of the target
 * application. 
 * 
 * @author Michele Sama
 * 
 */
public class ExceptionClassAdapter extends ClassAdapter {

  private String ownerClass;
  private Platform targetPlatform;
  
  private static Logger logger = 
    Logger.getLogger(ExceptionClassAdapter.class.getName());

  /**
   * Creates an instance of this {@link ClassAdapter} specific for the given 
   * platform.
   * 
   * @param cv The nested ClassVisitor.
   * @param platform The target {@link Platform}.
   */
  public ExceptionClassAdapter(ClassVisitor cv, Platform platform) {
    super(cv);
    targetPlatform = platform;
  }

  /** 
   * Delegates an Exception method adapter for the chain of visiting.
   * 
   * @see org.objectweb.asm.ClassAdapter#visitMethod(int, java.lang.String,
   *      java.lang.String, java.lang.String, java.lang.String[])
   */
  @Override
  public MethodVisitor visitMethod(int access, String name, String desc,
      String signature, String[] exceptions) {
    MethodVisitor mv =
        cv.visitMethod(access, name, desc, signature, exceptions);
    return new ExceptionMethodAdapter(mv, access, ownerClass, name, desc, 
        targetPlatform);
  }

  /**
   * Stores the actual class name in order to pass it to the method adapter.
   * 
   * @see org.objectweb.asm.ClassAdapter# visit(int, int, java.lang.String,
   *      java.lang.String, java.lang.String, java.lang.String[])
   */
  @Override
  public void visit(int version, int access, String name, String signature,
      String superName, String[] interfaces) {
    super.visit(version, access, name, signature, superName, interfaces);
    ownerClass = name;
  }

  /**
   * Method adapter which effectively expose the exception.
   * 
   * @author Michele Sama
   *
   */
  class ExceptionMethodAdapter extends InjectorMethodAdapter {
    
    /**
     * Creates an instance of ExceptionMethodAdapter.
     * @see InjectorMethodAdapter#InjectorMethodAdapter(MethodVisitor, int, 
     *  String, String, String, Platform)
     *  
     * @param mv The nested MethodVisitor.
     * @param access The access flag of the visited method.
     * @param ownerClass The currenClass class.
     * @param name The method's name.
     * @param desc The method's description.
     * @param platform The target platform.
     */
    public ExceptionMethodAdapter(MethodVisitor mv, int access, String ownerClass,
        String name, String desc, Platform platform) {
      super(mv, access, ownerClass, name, desc, platform);
    }

    /**
     * Injects the required code to expose an exception.
     * Depending from the print servant in use, which depends from the current 
     * plaform, injected code will look like:
     * <code>
     * [log begin message]
     * trowable.printStackTrace();
     * [log end message]
     * </code>
     * 
     * @see org.objectweb.asm.MethodAdapter#visitInsn(int)
     */
    @Override
    public void visitInsn(int opcode) {

      if (Opcodes.ATHROW == opcode) {
        mv.visitInsn(Opcodes.DUP);
        printServant.startPrinting();
        printServant.printString(
            "************************\n" +
            "*  Exception Exposer   *\n" + 
            "************************\n");
        printServant.stopPrinting();
        /* 
         * TODO: as soon as the compilation process allows it, put the 
         * trace in the log by using Trowables.
         * */
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, 
            ClassNames.JAVA_LANG_THROWABLE, "printStackTrace", "()V");
        printServant.startPrinting();
        printServant.printString("************************\n");
        printServant.stopPrinting();
        logger.info("Class " + ownerClass
            + " method " + methodName);
      }
      mv.visitInsn(opcode);
    }

  }
  
}
