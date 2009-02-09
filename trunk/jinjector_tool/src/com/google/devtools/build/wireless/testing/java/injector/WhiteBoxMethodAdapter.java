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

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Method Adapter for exposing field and method access.
 * Beware: this adapter strongly increases the stack size.
 * 
 * <p>Field access are exposed by the visitFieldInst.
 * 
 * <p>For exposing methods' invocation it is necessary to instrument the 
 * invocation and the return.
 * 
 * @author Michele Sama
 *
 */
public class WhiteBoxMethodAdapter extends InjectorMethodAdapter {

  protected Pattern classInclusionPattern = null;
  protected Pattern methodInclusionPattern = null;
  protected Pattern fieldInclusionPattern = null;
  
  /**
   * <code>true</code> if the ownerClass class matches the pattern for class
   * inclusion.
   */
  private boolean matchOwnerClass = false;
  
  /**
   * <code>true</code> if the method matches the pattern for method inclusion.
   */
  private boolean matchMethod = false;

  private int timeIndexInStack = -1;
  private int returnIndexInStack = -1;

  /**
   * Public fields accessed externally needs to be injected from external 
   * classes. The following two fields stores those informations.
   * 
   * <p>This procedure will increase the stack by 3 slots.
   */
  private int accessedFieldOwnerIndexInStack = -1;
  private int accessedFieldValueIndexInStack = -1;

  protected Logger logger = Logger.getLogger("WhiteBoxMethodAdapter");

  /**
   * Creates a white box method adapter with the specified inclusion.
   * 
   * <p>Enlarges the frame of 3 slots for public field logging.
   * One slot is used for the remote instance reference.
   * One/two slots are used for the value to be stored.
   * 
   * <p> TODO: remove inclusion patterns as soon as the inclusion 
   *    component will be ready.
   * 
   * @param mv The nested MethodVisitor.
   * @param access The method's access flags.
   * @param currentClass The current class.
   * @param name The method's name.
   * @param desc The method's description.
   * @param classInclusion The regular expression for class inclusion.
   * @param methodInclusion The regular expression for methods inclusion.
   * @param fieldInclusion The regular expression for field inclusion.
   * @param platform The target platform.
   */
  public WhiteBoxMethodAdapter(MethodVisitor mv, int access, 
      String currentClass, String name, String desc, String classInclusion, 
      String methodInclusion, String fieldInclusion, Platform platform) {
    super(mv, access, currentClass, name, desc, platform);

    // patterns
    classInclusionPattern = Pattern.compile(classInclusion);
    methodInclusionPattern = Pattern.compile(methodInclusion);
    fieldInclusionPattern = Pattern.compile(fieldInclusion);

    Matcher classMatcher = classInclusionPattern.matcher(ownerClass);
    matchOwnerClass = classMatcher.find();

    Matcher methodMatcher = methodInclusionPattern.matcher(methodName);
    matchMethod = methodMatcher.find();

    if (matchOwnerClass && matchMethod) {
      // time
      timeIndexInStack = stackServant.getInstrumentedFrameSize();
      stackServant.increaseInstrumentedStack(2);

      // return type
      if (!stackServant.getReturnType().equals("V")) {
        returnIndexInStack = stackServant.getInstrumentedFrameSize();
        stackServant.increaseInstrumentedStack(StackServant
            .getFrameSizeForType(stackServant.getReturnType()));
      } 
    }

    accessedFieldOwnerIndexInStack = stackServant.getInstrumentedFrameSize();
    stackServant.increaseInstrumentedStack(1);
    accessedFieldValueIndexInStack = stackServant.getInstrumentedFrameSize();
    stackServant.increaseInstrumentedStack(2);
  }
  
  /**
   * Injects methods call by logging invocation time and parameters.
   * 
   * @see org.objectweb.asm.MethodAdapter#visitCode()
   */
  @Override
  public void visitCode() {
    mv.visitInsn(Opcodes.ICONST_0);
    stackServant.store("I", accessedFieldOwnerIndexInStack);
    mv.visitInsn(Opcodes.DCONST_0);
    stackServant.store("D", accessedFieldValueIndexInStack);

    if (matchOwnerClass && matchMethod) {
      mv.visitInsn(Opcodes.LCONST_0);
      stackServant.store("J", timeIndexInStack);

      logger.info("\t[WhiteBox][Method call]: " + methodName + " "
          + description);

      printServant.startPrinting();
      printServant.printString("[Call]\t");

      // The init method has some problem with 'this' so it is considered static
      if (stackServant.isStatic() || methodName.equals("<init>")) {
        printServant.printString(ownerClass);
      } else {
        stackServant.loadThis();
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, ownerClass, "toString",
            "()L" + ClassNames.JAVA_LANG_STRING + ";");
        printServant.print("L" + ClassNames.JAVA_LANG_STRING + ";");
      }

      printServant.printString("." + methodName + " Arguments: ");

      for (int i = 0; i < stackServant.getArgumentSize(); i++) {
        stackServant.loadArgumentAt(i);
        printServant.print(stackServant.getArgumentTypeAt(i));
        printServant.printString("; ");
      }

      printServant.printString(" Invoked: ");
      timeServant.loadCurrentTimeMillis();
      printServant.println("J");

      // time
      timeServant.startCountingTime(timeIndexInStack);

      // saving and printing
      printServant.stopPrinting();
    }
    mv.visitCode();
  }

  /**
   * Visits the return instruction by logging the return type (if any), the call
   * duration in msec, and the return time.
   */
  @Override
  public void visitInsn(int opcode) {
    // TODO: exceptions are not handled yet.
    if (matchOwnerClass && matchMethod
        && ((opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN))) {
      logger.info("\t[WhiteBox][Method return]: " + this.methodName
          + " " + description);

      if (!stackServant.getReturnType().equals("V")) {
        stackServant.duplicateAndStore(
            stackServant.getReturnType(), returnIndexInStack);
      }

      printServant.startPrinting();
      printServant.printString("[Return]\t");

      printCurrentClass();

      printServant.printString("." + methodName + " Completed: ");

      timeServant.loadCurrentTimeMillis();
      printServant.print("J");

      printServant.printString(" Execution_time: ");

      // time
      timeServant.stopCountingTime(timeIndexInStack);
      printServant.print("J");

      // add return type to output string
      if (stackServant.returnType.equals("V") == false) {
        printServant.printString(" return ");
        stackServant.load(stackServant.returnType, returnIndexInStack);
        printServant.println(stackServant.getReturnType());
      } else {
        printServant.println();
      }

      // save and print
      this.printServant.stopPrinting();
    }
    mv.visitInsn(opcode);
  }

  /**
   * Prints the type of the current class if static or invoke 
   * the <code>toStrin()</code> method, if virtual.
   */
  private void printCurrentClass() {
    if (stackServant.isStatic()) {
      printServant.printString(ownerClass);
    } else {
      stackServant.loadThis();
      mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, ownerClass, "toString",
          "()L" + ClassNames.JAVA_LANG_STRING + ";");
      printServant.print("L" + ClassNames.JAVA_LANG_OBJECT + ";");
    }
  }

  /**
   * Injects visit putfield and putstatic instructions by logging the new value
   * which is stored into the field.
   */
  @Override
  public void visitFieldInsn(int opcode, String owner, 
      String name, String desc) {
    boolean instrument = false;

    Matcher fieldMatcher = fieldInclusionPattern.matcher(name);
    boolean matchField = fieldMatcher.find();

    if (matchOwnerClass && matchField &&
        (isField(opcode) || isStaticField(opcode))) {
      instrument = true;
      logger.info("\t[SetField]: " + owner + "." + name
          + " desc " + desc);
    }

    if (instrument && isField(opcode)) {
      // duplicate and store the value
      stackServant.store(desc, accessedFieldValueIndexInStack);
      stackServant.duplicateAndStore("L" + owner + ";",
          accessedFieldOwnerIndexInStack);
      stackServant.load(desc, accessedFieldValueIndexInStack);
    }

    mv.visitFieldInsn(opcode, owner, name, desc);

    if (instrument) {
      printServant.startPrinting();
      printServant.printString("[WhiteBox][Set]\t");

      if (isField(opcode)) {
        stackServant.load("L" + owner + ";",
            accessedFieldOwnerIndexInStack);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, owner, "toString",
            "()L" + ClassNames.JAVA_LANG_STRING + ";");
        printServant.print("L" + ClassNames.JAVA_LANG_STRING + ";");
      } else {
        printServant.printString(owner);
      }

      printServant.printString("." + name + " Invoked=");

      timeServant.loadCurrentTimeMillis();
      printServant.print("J");

      // add value type to output string
      printServant.printString(" value=");

      // if the field is not static we must retrieve a pointer to the owner
      int code = -1;
      if (isField(opcode)) {
        stackServant.load("L" + owner + ";",
            accessedFieldOwnerIndexInStack);
        code = Opcodes.GETFIELD;
      } else {
        code = Opcodes.GETSTATIC;
      }
      mv.visitFieldInsn(code, owner, name, desc);
      printServant.println(desc);

      // save and print
      printServant.stopPrinting();
    }
  }
  
  /**
   * Tells if the given instruction is corresponding to 
   * {@link Opcodes#PUTFIELD}.
   * 
   * @param opcode The given instruction;
   * @return <code>true</code> if the given instruction is 
   *    {@link Opcodes#PUTFIELD}, <code>false</code> otherwise.
   */
  private boolean isField(int opcode) {
    return opcode == Opcodes.PUTFIELD;
  }
  
  /**
   * Tells if the given instruction is corresponding to 
   * {@link Opcodes#PUTSTATIC}.
   * 
   * @param opcode The given instruction;
   * @return <code>true</code> if the given instruction is 
   *    {@link Opcodes#PUTSTATIC}, <code>false</code> otherwise.
   */
  private boolean isStaticField(int opcode) {
    return opcode == Opcodes.PUTSTATIC;
  }
  
}
