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

package com.google.devtools.build.wireless.testing.java.injector.j2me;

import com.google.common.collect.ImmutableMap;
import com.google.devtools.build.wireless.testing.java.injector.ClassManager;
import com.google.devtools.build.wireless.testing.java.injector.InjectorMethodAdapter;
import com.google.devtools.build.wireless.testing.java.injector.Platform;
import com.google.devtools.build.wireless.testing.java.injector.WrapperClassAdapter;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Map;
import java.util.logging.Logger;


/**
 * ClassAdapter to log and inject Command events.
 * 
 * @author Michele Sama
 * 
 */
public class LcduiDecoratorClassAdapter extends WrapperClassAdapter {

  /**
   * Contains the method's name for command listeners.
   * 
   * <p>This field is protected for testing purposes. 
   */
  protected static final String COMMAND_ACTION = "commandAction";

  private static final Map<String, String> WRAPPER_MAP = 
      new ImmutableMap.Builder<String, String>()
      // Displayables
      .put(J2meClassNames.CANVAS, J2meClassNames.CANVAS_WRAPPER)
      .put(J2meClassNames.GAMECANVAS, J2meClassNames.GAMECANVAS_WRAPPER)
      .put(J2meClassNames.ALERT, J2meClassNames.ALERT_WRAPPER)
      .put(J2meClassNames.FORM, J2meClassNames.FORM_WRAPPER)
      .put(J2meClassNames.LIST, J2meClassNames.LIST_WRAPPER)
      .put(J2meClassNames.TEXTBOX, J2meClassNames.TEXTBOX_WRAPPER)
      // Items
      .put(J2meClassNames.CHOICEGROUP, J2meClassNames.CHOICEGROUP_WRAPPER)
      .put(J2meClassNames.CUSTOMITEM, J2meClassNames.CUSTOMITEM_WRAPPER)
      .put(J2meClassNames.DATEFIELD, J2meClassNames.DATEFIELD_WRAPPER)
      .put(J2meClassNames.GAUGE, J2meClassNames.GAUGE_WRAPPER)
      .put(J2meClassNames.IMAGEITEM, J2meClassNames.IMAGEITEM_WRAPPER)
      .put(J2meClassNames.SPACER, J2meClassNames.SPACER_WRAPPER)
      .put(J2meClassNames.STRINGITEM, J2meClassNames.STRINGITEM_WRAPPER)
      .put(J2meClassNames.TEXTFIELD, J2meClassNames.TEXTFIELD_WRAPPER)
      .build();
  
  private Logger logger = Logger.getLogger("LcduiDecoratorClassAdapter");

  /**
   * Creates an instance of this {@link ClassAdapter} which will use the 
   * specified {@link ClassManager} and the specified {@link Platform}.
   * 
   * @param cv The nested {@link ClassVisitor}.
   * @param cm The specified {@link ClassManager}.
   * @param platform The target {@link Platform}.
   */
  public LcduiDecoratorClassAdapter(ClassVisitor cv, ClassManager cm, 
      Platform platform) {
    super(cv, cm, platform, WRAPPER_MAP);
  }

  /**
   * Visits a class and replaces it with its wrapper if needed.
   */
  @Override
  public void visit(int version, int access, String name, String signature,
      String superName, String[] interfaces) {

    implementedInterfaces = interfaces;

    // Invokes super in order to instrument the superclass
    super.visit(version, access, name, signature, superName, interfaces);
  }

  /**
   * If the method visited is implementing ItemCommandListener.commandAction or
   * CommandListener.commandAction then they have to be injected with a 
   * command logger. A class can contain one (or both) those methods if it
   * extends ItemCommandListener or CommandListener or both.
   * 
   * <p>{@link CommandLoggerMethodAdapter} is logging command invocations.
   * 
   * <p>BEWARE that a class can implement both CommandListener and 
   * ItemCommandListener, so both the delegated visitor must be added to 
   * the instrumentation chain.
   */
  @Override
  public MethodVisitor visitMethod(int access, String name, String desc,
      String signature, String[] exceptions) {
    // Invokes super in order to instrument NEW and <init>
    MethodVisitor mv =
        super.visitMethod(access, name, desc, signature, exceptions);
    
    /* Instruments ItemCommandListener.commandAction.
     * BEWARE classes can also implement CommandListener.
     */
    mv = applyLogger(mv, access, name, desc, 
        J2meClassNames.ITEM_COMMAND_LISTENER, J2meClassNames.ITEM);

    /* Instruments CommandListener.commandAction.
     * BEWARE classes can also implement ItemCommandListener.
     */
    mv = applyLogger(mv, access, name, desc, 
        J2meClassNames.COMMAND_LISTENER, J2meClassNames.DISPLAYABLE);
    
    return mv;
  }

  /**
   * Adds an instance of {@link CommandLoggerMethodAdapter} to the 
   * {@link MethodVisitor} chain if it is applicable.
   * 
   * @param mv The nested {@link MethodVisitor}.
   * @param access the access code.
   * @param name the method's name.
   * @param desc the method's description.
   * @param targetInterface The interface that the current class must 
   *    implement in order to be applicable.
   * @param targetClass The target class that the logger is going to log.
   * @return the chain of {@link MethodVisitor} to which 
   *    {@link CommandLoggerMethodAdapter} has been added if applicable.
   */
  private MethodVisitor applyLogger(MethodVisitor mv, 
      int access, String name, String desc, 
      String targetInterface, String targetClass) {
    if (implementsInterface(targetInterface)
        && name.equals(COMMAND_ACTION)
        && desc.equals("(L" + J2meClassNames.COMMAND + ";" + "L"
            + targetClass + ";)V")) {
      mv = new CommandLoggerMethodAdapter(mv, access, className, 
          name, desc, targetPlatform, targetClass);
      logger.info("Command Loging injected into Class: " + className + 
          " method: " + name + " desc: " + desc);
    }
    return mv;
  }

}


/**
 * MethodAdapter to log any invocation of: ItemCommandListener.commandAction() 
 * and CommandListener.commandAction().
 * 
 * @author Michele Sama
 * 
 */
class CommandLoggerMethodAdapter extends InjectorMethodAdapter {
 
  private String type = null;
  
  public CommandLoggerMethodAdapter(MethodVisitor mv,
      int access, String owner, String name, String desc, Platform platform, 
      String type) {
    super(mv, access, owner, name, desc, platform);
    this.type = type;
  }

  /**
   * Logs any call to commandAction by exposing the action fired.
   * 
   * @see org.objectweb.asm.MethodAdapter#visitCode()
   */
  @Override
  public void visitCode() {

    printServant.startPrinting();
    printServant.printString("[Listener] ");

    stackServant.loadThis();
    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, ownerClass, "toString",
        "()Ljava/lang/String;");
    printServant.print("Ljava/lang/String;");

    printServant.printString(" Command = ");
    stackServant.loadArgumentAt(0);
    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, J2meClassNames.COMMAND,
        "getLabel", "()Ljava/lang/String;");
    printServant.print("Ljava/lang/String;");

    printServant.printString(" Target = ");
    // method = "getLabel";
    stackServant.loadArgumentAt(1);
    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, type,
        "toString", "()Ljava/lang/String;");
    printServant.println("Ljava/lang/String;");

    printServant.stopPrinting();
    mv.visitCode();
  }
}