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
import org.objectweb.asm.Type;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This Servant provides convenience methods for accessing parameters, return 
 * type and local variables according to the description of the ownerClass 
 * method.
 * 
 * <p>This class also contains convenience methods for retrieving reference to 
 * <code>this</code> or for loading and storing in memory.
 * 
 * <p>It also provide features for increasing the stack.
 * 
 * @author Michele Sama
 * 
 */
public class StackServant extends Servant {
 
  /**
   * A regular expression indicating elements in methods description in java.
   * 
   * <p> This is used on strings like the following to retrieve each single 
   * element: 
   * <ul>
   * <li>I, for an integer;
   * <li>II, for two integers;
   * <li>ZI,for a boolean and an integer;
   * <li>[I, for a single dimensioned array of integers;
   * <li>[[I, for a double dimensioned array of integers;
   * <li>Lorg/foo/Foo;, for the class Foo;
   * <li>IILorg/foo/Foo;Lorg/foo/Foo;, two integers and two instances of Foo;
   * <li>[Lorg/foo/Foo;, for an array of foo.
   * </ul>
   */
  public static final String PARAM_REGEX = 
    "V" + // Void
    "|" + // or
    "\\[*" + // a multidimensional array
    "(" + 
    "[ZCBIFJDS]" + // of base types
    "|" + // or
    "L[\\w/$]+;" + // of classes
    ")";

  /**
   * A pattern compiling the typing in java.
   */
  private static final Pattern TYPE_PATTERN = Pattern.compile(PARAM_REGEX);

  // TODO: replace this with org.objectweb.asm.Type[]
  protected List<String> params = new ArrayList<String>();
  
  // TODO: replace this with org.objectweb.asm.Type
  protected String returnType = null;

  protected boolean staticFlag = false;
  protected int initialLocalFrameSize = 0;
  protected int instrumentedLocalFrameSize = 0;

  /**
   * Creates a new StackServant.
   * 
   * @param mv The ownerClass MethodVisitor.
   * @param access The access type.
   * @param description The method description.
   */
  public StackServant(MethodVisitor mv, int access, String description) {
    super(mv);
    processParams(access, description);
    returnType = processReturnType(description);
  }

  /**
   * Process method description to retrieve informations about arguments and
   * initial stack size.
   * 
   * <p>Double and Long takes two slots in the initial frame size. Any other 
   * type including arrays and classes takes one single slot.
   * 
   * <p>If the method is not static a pointer to the current class is passed as
   * an implicit argument which is placed at the beginning of the frame at 
   * index 0 and which takes one slot (because it is a reference to an 
   * instance).
   * 
   * @param access Method's flags.
   * @param description A string containing the description of the method.
   * 
   * TODO} 
   */
  protected void processParams(int access, String description) {

    if (description == null) {
      throw new RuntimeException("Description cannot be null.");
    }

    String argument = description.substring(1, description.indexOf(')'));
    Matcher matcher = StackServant.TYPE_PATTERN.matcher(argument);
    int groupCount = matcher.groupCount();

    if ((access & Opcodes.ACC_STATIC) > 0) {
      staticFlag = true;
    }
    initialLocalFrameSize = getParamInitialIndex();

    /*
     * Process all the parameters and set the initial frame's size.
     * This servant is in charge for handling any increase to the size of the 
     * frame.
     */
    while (matcher.find()) {
      String s = matcher.group();
      params.add(s);
      initialLocalFrameSize += getFrameSizeForType(s);      
    }
    instrumentedLocalFrameSize = initialLocalFrameSize;
  }


  /**
   * Evaluates return type and store it in the opportune field.
   * 
   * <p> {@link Type#getReturnType(String)} does not control the input string, 
   * so this method 
   * 
   * @param description The method description.
   * @throws IllegalArgumentException If the given String is not a valid 
   *    return type.
   *    
   * TODO}   
   */
  protected String processReturnType(String description) {
    String argument = description.substring(description.indexOf(')') + 1);
    Matcher matcher = StackServant.TYPE_PATTERN.matcher(argument);
    String retType = null;
    int groupCount = matcher.groupCount();
    
    if (matcher.find()) {
      retType = matcher.group();
      if (matcher.find()) {
        throw new IllegalArgumentException("Multiple types found: " 
            + description);
      }
      return retType;
    } else {
      throw new IllegalArgumentException("Wrong return type: " + description);
    }
  }

  /**
   * Duplicate the last stack entry and store it in a 
   * specific position of the stack. Since generally when instrumenting is 
   * necessary to use values which are going to be used after the 
   * instrumentation. 
   * 
   * <p> The required base actions are "dup" and "store" according to 
   * the type of variable.
   * 
   * @param typeDesc type of the variable according to JVM specifications.
   * @param stackIndex the position in which to store.
   */
  public void duplicateAndStore(String typeDesc, int stackIndex) {
    TypeDescriptor type = TypeDescriptor.getType(typeDesc);
    mv.visitInsn(type.getDupOpcode());
    mv.visitVarInsn(type.getStoreOpcode(), stackIndex);
  }

  /**
   * Saves a variable in the stack.
   *  
   * <p>The variable type is processed and the opportune instruction is 
   * injected.
   * 
   * @param typeDesc The type of variable.
   * @param stackIndex The position in stack.
   * @throws IllegalArgumentException if typeDesc is unrecognized.
   */
  public void store(String typeDesc, int stackIndex) {
    TypeDescriptor type = TypeDescriptor.getType(typeDesc);
    mv.visitVarInsn(type.getStoreOpcode(), stackIndex);
  }

  /**
   * Loads a specific value from a specific index to the top of the stack.
   * 
   * <p> No preconditions are expressed because all the check are performed 
   * in {@link TypeDescriptor#getType(String)}.
   * 
   * @param typeDesc the type of value according to JVM specification.
   * @param stackIndex the position from where to load.
   */
  public void load(String typeDesc, int stackIndex) {
    TypeDescriptor type = TypeDescriptor.getType(typeDesc);
    mv.visitVarInsn(type.getLoadOpcode(), stackIndex);
  }

  /**
   * Loads this (alias arg0) on top of the stack.
   * 
   * @throws IllegalArgumentException if the method is static.
   */
  public void loadThis() {
    if (staticFlag) {
      throw new IllegalArgumentException(
          "Cannot invoke this in a Method marked as static.");
    }
    mv.visitVarInsn(Opcodes.ALOAD, 0);
  }

  /**
   * Increases the stack size as result of another action.
   * 
   * @param size the number of slot to add at the stack.
   * @throws IllegalArgumentException if size is < 0.
   */
  public void increaseInstrumentedStack(int size) {
    // TODO: stack has a limit!!!!!!!!!!!!!!!!!!
    if (size < 0) {
      throw new IllegalArgumentException("Size must be >= 0.");
    }
    instrumentedLocalFrameSize += size;
  }

  /**
   * Gets the instrumented size of the stack on top of which the program will
   * create new variables.
   * 
   * @return the actual size of the instrumented stack.
   */
  public int getInstrumentedFrameSize() {
    return instrumentedLocalFrameSize;
  }

  /**
   * Get the initial size of the stack.
   * 
   * @return the actual size of the original stack.
   */
  public int getInitialFrameSize() {
    return initialLocalFrameSize;
  }

  /**
   * Checks is the method is static.
   * 
   * @return true if the method is static, false otherwise.
   */
  public boolean isStatic() {
    return staticFlag;
  }

  /**
   * Returns the type of returned value, according to JVM specification.
   * 
   * @return a string containing the type of the returned value
   */
  public String getReturnType() {
    return returnType;
  }

  /**
   * Retrieves the number of argument.
   * 
   * @return the number of arguments of the method served by this servant
   */
  public int getArgumentSize() {
    return params.size();
  }

  /**
   * Gets the type of a specific argument.
   * 
   * @param i the index of the argument
   * @return a String containing the type of the argument according to JVM 
   *    specifications.
   */
  public String getArgumentTypeAt(int i) {
    return params.get(i);
  }

  /**
   * Loads on top of the stack the argument with that specific index.
   * 
   * @param i The index of the argument to load.
   */
  public void loadArgumentAt(int i) {
    int index = getParamInitialIndex();

    for (int k = 0; k < i; k++) {
      String argType = getArgumentTypeAt(k);
      index += getFrameSizeForType(argType);
    }
    load(getArgumentTypeAt(i), index);
  }

  /**
   * Returns the frame size for a specified type.
   * 
   * <p> The size of Void is 0, the size of Long and double is 2, the size of any 
   * other base type plus arrays and references is 1.
   * 
   * <p> No precondition is specified because they are already performed in 
   * {@link TypeDescriptor#getType(String)}.
   * 
   * @param typeDesc the string description of the type according to JVM
   *        specifications
   * @return 0 if void, 2 if long or double, 1 otherwise.
   */
  public static int getFrameSizeForType(String typeDesc) {
    TypeDescriptor type = TypeDescriptor.getType(typeDesc);
    return type.getFrameSize();
  }
  
  /**
   * Return the initial index of parameters in the frame. This is 0 if the 
   * method is static, 1 otherwise.
   * 
   * <p>This is happening because non-static methods contains a pointer to the
   * local instance (this).
   * 
   * @return 0 if the method is static, 1 otherwise.
   */
  private int getParamInitialIndex() {
    // non static have this
    if (staticFlag) {
      return 0;
    } else {
      return 1;
    }
  }
}
