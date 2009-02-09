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
 * This servant provides methods to obtain the current time and to record the 
 * duration of events. This is useful when recording the execution time of a 
 * method
 * 
 * TODO: add a profiler mechanism to save the total amount of time spent 
 *   in a method. An example of implementation is in the ASM manual.
 * 
 * @author Michele Sama
 * 
 */
public class TimeServant extends Servant {

  /**
   * Creates a new TimeServant for the specified MethodVisitor.
   * 
   * @param mv The ownerClass MethodVisitor 
   */
  public TimeServant(MethodVisitor mv) {
    super(mv);
  }

  /**
   * Puts a Long on top of the stack containing the current time msec.
   */
  public void loadCurrentTimeMillis() {
    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", 
        "currentTimeMillis", "()J");
  }

  /**
   * Stores a negative timestamp of the current time into a specific position of
   * the stack.
   * 
   * <p>The time at the end of the computation is going to be added to this 
   * value.
   * 
   * @param indexInStack The index in the method's stack where to save the time.
   * @throws IllegalArgumentException If the index is negative.
   */
  public void startCountingTime(int indexInStack) {
    if (indexInStack < 0) {
      throw new IllegalArgumentException("index in stack must be >= 0");
    }
    loadCurrentTimeMillis();
    mv.visitInsn(Opcodes.LNEG);
    mv.visitVarInsn(Opcodes.LSTORE, indexInStack);
  }

  /**
   * Reloads from a previously saved stack a negative timestamp to which the new
   * time is added. The result is left on top of the stack.
   * 
   * @param indexInStack The index in stack from where to load the 
   *    previously saved time;
   * @throws IllegalArgumentException If the index is negative.
   */
  public void stopCountingTime(int indexInStack) {
    if (indexInStack < 0) {
      throw new IllegalArgumentException("Index in stack must be >= 0");
    }
    mv.visitVarInsn(Opcodes.LLOAD, indexInStack);
    loadCurrentTimeMillis();
    mv.visitInsn(Opcodes.LADD);
  }

}
