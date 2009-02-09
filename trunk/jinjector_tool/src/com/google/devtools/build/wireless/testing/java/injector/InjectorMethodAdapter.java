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

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Base class for all the Method injectors.
 * 
 * <p>It initialize a stack servant, a print servant and a time servant.
 * It also manage frame resizing according to used functions. 
 * Subclasses may need to modify this behavior.
 * 
 * @author Michele Sama
 *
 */
public class InjectorMethodAdapter extends MethodAdapter {

  protected Platform targetPlatform = null;
  protected StackServant stackServant = null;
  protected PrintServant printServant = null;
  protected TimeServant timeServant = null;
  protected String methodName;
  protected String ownerClass;
  protected String description;
  protected int access;

  /**
   * Creates a method adapter which has informations about the method which it 
   * is going to instrument.
   * 
   * @param mv The nested MethodVisitor.
   * @param access The bitmask representing the method type.
   * @param owner The currenClass class.
   * @param name The method name.
   * @param desc The method description containing parameters and return type.
   * @param platform The target {@link Platform}.
   */
  public InjectorMethodAdapter(MethodVisitor mv, int access, String owner,
      String name, String desc, Platform platform) {
    super(mv);
    targetPlatform = platform;
    methodName = name;
    ownerClass = owner;
    description = desc;
    this.access = access;

    stackServant = new StackServant(mv, access, desc);
    printServant = targetPlatform.getPlatformSpecificPrintServant(mv);
    timeServant = new TimeServant(mv);
  }

  /**
   * Moves variable index in the frame according to the number of injected 
   * values.
   * 
   * @see org.objectweb.asm.MethodAdapter#visitVarInsn(int, int)
   */
  @Override
  public void visitVarInsn(int opcode, int var) {
    var = localVariableIncrement(var);
    mv.visitVarInsn(opcode, var);
  }

  /**
   * Moves variable index in the frame according to the number of injected 
   * values.
   * 
   * @see org.objectweb.asm.MethodAdapter#visitIincInsn(int, int)
   */
  @Override
  public void visitIincInsn(int var, int increment) {
    var = localVariableIncrement(var);
    mv.visitIincInsn(var, increment);
  }

  /**
   * Increments the index of a local variable according to the injected ones.
   * 
   * @see MethodAdapter#visitLocalVariable(String, String, String, 
   *    Label, Label, int)
   */
  @Override
  public void visitLocalVariable(String name, String desc, String signature,
      Label start, Label end, int index) {
    index = localVariableIncrement(index);
    mv.visitLocalVariable(name, desc, signature, start, end, index);
  }

  /**
   * Instruments load and store instructions in order to point to the 
   * right position in the frame according to the new size.
   * Parameters are still at the beginning of the frame. Local variables are 
   * shifted after the injected ones.
   * 
   * @param varIndex The wanted position in memory.
   * @return The new position of the wanted position in memory.
   */
  protected int localVariableIncrement(int varIndex) {
    if (varIndex >= this.stackServant.getInitialFrameSize()) {
      varIndex += stackServant.getInstrumentedFrameSize() - 
          stackServant.getInitialFrameSize();
    }
    return varIndex;
  }
  
  /**
   * Tells if an opcode is a return instruction.
   * <br>
   * Helper function.
   * 
   * @param opcode The opcode to check.
   * @return <code>true</code> if the parameter indicates a return instruction.
   */
  public static boolean isReturnInstruction(int opcode) {
    return opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN;
  }
}
