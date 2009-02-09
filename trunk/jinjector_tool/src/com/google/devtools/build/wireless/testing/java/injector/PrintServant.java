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

/**
 * This servant helps in printing by providing an set of method which 
 * if invoked in sequence will effectively print.
 * 
 * <p>This is useful when instrumenting the code in order to have in the stack 
 * variables in the right position. 
 * <ul>
 * <li><b>startPrinting</b> invokes constructors and put on top of the stack the
 *    required instances. A StringBuilder or a StringBuffer are created (or any 
 *    other platform specific String factory class) an ready to be used.
 * <li><b>print, println</b> append strings at the end of the buffer.
 * <li><b>stopPrinting</b> Prints or logs or saves the buffer according to the 
 *    concrete class.
 * </ul>
 * 
 * <p>The correct usage is:
 * startPrinting & ( print | println )* & stopPrinting
 * 
 * @author Michele Sama
 */
public abstract class PrintServant extends Servant {

  private boolean printing = false;

  /**
   * @param mv the MethodVisitor to use
   */
  public PrintServant(MethodVisitor mv) {
    super(mv);
  }

  /**
   * External method to start printing. Internally invokes initializePrinting()
   * 
   * @throws java.lang.RuntimeException if the PrintServant has already been 
   *    started.
   */
  public void startPrinting() {
    if (this.printing == true) {
      throw new RuntimeException("PrinterServant already started.");
    }
    initializePrinting();
    printing = true;
  }

  /**
   * Initialize the printing mechanism Any class implementing PrintServant must
   * re-implement this method.
   */
  protected abstract void initializePrinting();

  /**
   * Stops the PrintServant. It invokes finalizePrinting() on
   * the implementing class.
   * 
   * @throws java.lang.RuntimeException is the PrintServant was not started 
   *    previously.
   */
  public void stopPrinting() {
    if (printing == false) {
      throw new RuntimeException("PrinterServant is not started.");
    }
    finalizePrinting();
    printing = false;
  }

  /**
   * Stops the printing mechanism Any class implementing PrintServant should
   * implement this method.
   */
  protected abstract void finalizePrinting();

  /**
   * Prints with the PrintServant the last element on the stack.
   * 
   * @param typeDesc type of the last element on the stack according to the JVM.
   */
  public abstract void print(String typeDesc);

  /**
   * Loads a constant in the stack and print it.
   * 
   * @param text the constant string to print
   */
  public void printString(String text) {
    mv.visitLdcInsn(text);
    print("L" + ClassNames.JAVA_LANG_OBJECT + ";");
  }

  /**
   * Prints a new line.
   */
  public void println() {
    mv.visitLdcInsn("\n");
    print("L" + ClassNames.JAVA_LANG_STRING + ";");
  }

  /**
   * Prints a value and starts a new line.
   * 
   * @param typeDesc typeDesc type of the last element on the stack according to 
   *    the JVM.
   */
  public void println(String typeDesc) {
    print(typeDesc);
    println();
  }
  
  /**
   * Returns a string description of the given parameter.
   * 
   * <p> This is a convenience method to invoke the right method in 
   * {@link StringBuilder} and {@link StringBuffer}. Given a string 
   * representation of a type, this is going to be converted in the right 
   * parameter for the most suitable append(...) method which is returned in a string format. 
   * 
   * @param typeDesc The string representation of the given parameter.
   * @return The string representation of the most suitable parameter.
   */
  protected String typeToString(String typeDesc) {
    char type = typeDesc.charAt(0);
    switch (type) {
      case 'S':
      case 'C':
      case 'B':
      case 'I': {
        return "I";
      }
      case 'J':
      case 'Z':
      case 'F':
      case 'D': {
        return "" + type;
      }
      case 'L': {
        return "Ljava/lang/Object;";
      }
      case '[': {
        // TODO: print the content of the array instead of the reference 
        return "Ljava/lang/Object;";
      }
    }
    throw new RuntimeException("Wrong/Unrecognized type.");
  }
  
}
