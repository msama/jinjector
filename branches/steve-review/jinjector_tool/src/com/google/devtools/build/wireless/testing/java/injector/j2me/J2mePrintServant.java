/*
 * Copyright 2007 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.google.devtools.build.wireless.testing.java.injector.j2me;

import com.google.devtools.build.wireless.testing.java.injector.ClassNames;
import com.google.devtools.build.wireless.testing.java.injector.PrintServant;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * The PrintServant is injected into the J2ME application to log
 * relevant information.
 *
 * <p> The String is built with a {@link StringBuffer} because J2ME does
 * not have a {@link StringBuilder}.
 *
 * @author Michele Sama
 *
 */
public class J2mePrintServant extends PrintServant {

  protected String logIdentifier;

  /**
   * Creates an instance with a default log flag.
   *
   * @param mv The nested MethodVisitor.
   */
  public J2mePrintServant(MethodVisitor mv) {
    // TODO: Either improve or remove the default behavior.
    this(mv, "UI");
  }

  /**
   * Creates an instance which will log with the specified flag.
   *
   * @param mv The nested MethodVisitor.
   * @param logIdentifier The identifier to use while logging. Which can be
   * used to group related log events. e.g. "NETWORK".
   */
  public J2mePrintServant(MethodVisitor mv, String logIdentifier) {
    super(mv);
    this.logIdentifier = logIdentifier;
  }

  /**
   * Logs the new build string with the J2ME Logger.
   *
   * @see PrintServant#finalizePrinting()
   */
  @Override
  protected void finalizePrinting() {
    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, ClassNames.STRING_BUFFER,
        "toString", "()L" + ClassNames.JAVA_LANG_STRING + ";");
    mv.visitLdcInsn(logIdentifier);
    mv.visitInsn(Opcodes.SWAP);
    mv.visitMethodInsn(Opcodes.INVOKESTATIC, J2meClassNames.LOG,
        "log", "(L" + ClassNames.JAVA_LANG_STRING + ";L" +
        ClassNames.JAVA_LANG_STRING + ";)V");
  }

  /**
   * Initialize a string buffer.
   *
   * @see PrintServant#initializePrinting()
   */
  @Override
  protected void initializePrinting() {
    mv.visitTypeInsn(Opcodes.NEW, ClassNames.STRING_BUFFER);
    mv.visitInsn(Opcodes.DUP);
    mv.visitMethodInsn(Opcodes.INVOKESPECIAL, ClassNames.STRING_BUFFER,
        "<init>", "()V");
  }

  /**
   * Prints the element on top of the stack by type.
   *
   * @see PrintServant#print(java.lang.String)
   */
  @Override
  public void print(String typeDesc) {
    String command = null;
    if (typeDesc.equals("")) {
      command = "L" + ClassNames.JAVA_LANG_STRING + ";";
    } else {
      command = typeToString(typeDesc);
    }

    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, ClassNames.STRING_BUFFER,
        "append", "(" + command + ")L" + ClassNames.STRING_BUFFER + ";");
  }
}
