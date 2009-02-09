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
// TODO: change class name to CoverageGeneratingClassAdapter (same
// for GenerateCoverageInitializationData)

package com.google.devtools.build.wireless.testing.java.injector.coverage;

import com.google.devtools.build.wireless.testing.java.injector.ClassManager;
import com.google.devtools.build.wireless.testing.java.injector.ClassNames;
import com.google.devtools.build.wireless.testing.java.injector.InjectorMethodAdapter;
import com.google.devtools.build.wireless.testing.java.injector.ManagedClassAdapter;
import com.google.devtools.build.wireless.testing.java.injector.Platform;
import com.google.devtools.build.wireless.testing.java.injector.j2me.J2meClassNames;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.logging.Logger;

/**
 * A class adapter which adds the print method at the end of the program.
 * 
 * <p> It is not clear what the end of the program is for an application with 
 * a GUI (especially for a mobile applications). 
 * This adapter injects the write-code-coverage-results method in the correct
 * method for the target platform.
 * 
 * <p> For a MIDlet (according to the midlet lifesycle) it is assumed that the 
 * method destroyApp() will be invoked before the end of the program.
 * However this is not always true and depends from developers.
 * 
 * <p> For BB, a specific method (testsCompletedMarkerMethod) needs to be
 * included in the application at a point shortly before expected application
 * termination. This is to account for the troubles with identifying a clean
 * shutdown location in RIM applications. For a further discussion, see
 * {@link "http://wiki/Main/JInjectorCoverageForBlackBerryTestingStrategy"}
 * 
 * <p> When injecting the code it is also necessary to avoid duplicated 
 * injections into subclasses, so an empty interface is also added to flag 
 * the injection.
 * 
 * @author Michele Sama
 */
public class GenerateCoverageClassAdapter extends ManagedClassAdapter {
  
  // The target platform.
  private final Platform targetPlatform;

  // Specify if the current explored class needs injections,
  private boolean shouldInjectReportWriting = false;

  // The logger to use during the computation.
  static Logger logger = Logger.getLogger(
      GenerateCoverageClassAdapter.class.getSimpleName());
  
  /**
   * Create an instence which will instrument the code according to the 
   * specified platform.
   * 
   * @param targetPlatform The target platform.
   * @param cv The nested ClassVisitor
   * @param classManager The ClassManager which has to be used
   */
  public GenerateCoverageClassAdapter(Platform targetPlatform, ClassVisitor cv,
      ClassManager classManager) {
    super(cv, classManager);
    this.targetPlatform = targetPlatform;
  }

  /**
   * Visits classes looking for the proper class in which to inject a collection
   * point for coverage statistics. If the class is one of the right ones a flag
   * is marked, and an the interface reporter is injected.
   * 
   * <p>Injecting an empty interface is necessary to avoid duplicate 
   * instrumentation. The current implementation does NOT suffer from duplicate
   * injection because only search for matching with the super class' name. 
   * However future implementation may suffer for this problem and the 
   * prevention code is already working.
   * 
   * <p>If the superclass is a MIDlet and the target platform is J2ME
   * or if the superclass is a UiApplication and the target platform is 
   * rim/BB.
   * 
   * <p>If the current class is a coverage reporter, then it has already been 
   * injected. So we just skip it
   * 
   * @param version The class version.
   * @param access The class's access flags. 
   *    This parameter also indicates if the class is deprecated.
   * @param name The class' name.
   * @param signature The signature of this class. May be null if the class is 
   *    not a generic one, and does not extend or implement generic classes 
   *    or interfaces.
   * @param superName The name of the super class.
   * @param interfaces The internal names of the class's interfaces @Nullable.
   */
  @Override
  public void visit(int version, int access, String name, String signature,
      String superName, String[] interfaces) {

    implementedInterfaces  = interfaces;
    
    shouldInjectReportWriting = false;
    if (targetPlatform.getPlatformSpecificCoverageCollectionClass(name, superName)
        && !implementsInterface(CoverageClassNames.COVERAGE_REPORTER)) {
      shouldInjectReportWriting = true;
      String[] injectedInterfaces = new String[interfaces.length + 1];
      for (int i = 0; i < interfaces.length; i++) {
        injectedInterfaces[i] = interfaces[i];
      }
      injectedInterfaces[interfaces.length] = 
          CoverageClassNames.COVERAGE_REPORTER;
      interfaces = injectedInterfaces;
      implementedInterfaces = interfaces;
    }
    cv.visit(version, access, name, signature, superName, interfaces);
  }

  /**   
   *  Returns the opportune method visitor. If the class needs to be injected 
   *  the returned visitor is injecting a method call to store coverage 
   *  information. If not the nested visitor of the chain is returned.
   *  
   * @param access The type associated with the method.
   * @param desc the method's descriptor.
   * @param signature the method's signature. May be null if the method 
   *    parameters, return type and exceptions do not use generic types.
   * @param exceptions The internal names of the method's exception classes.
   *    May be null. 
   * @return The method visitor which must be used to process the method.
   */
  @Override
  public MethodVisitor visitMethod(int access, String name, String desc,
      String signature, String[] exceptions) {
    MethodVisitor mv =
        cv.visitMethod(access, name, desc, signature, exceptions);

    if (shouldInjectReportWriting && 
        targetPlatform.getPlatformSpecificCoverageCollectionMethod()
        .equals(name)) {
      if (targetPlatform.injectCoverageReportingAtEnd()) {
        mv = new CoverageReportMethodAdapter(mv);
      } else {
        mv = new StartOfMethodCoverageReportMethodAdapter(mv);
      }
    }
    return mv;
  }

  /**
   * Injects the code coverage report when the current visited method returns.
   * 
   * <p>Method visited with this adapter suffer from a transformation 
   * that turns: 
   * <pre>public void foo() { 
   *   bar();
   * }</pre>
   * into: 
   * <pre>public void foo() { 
   *   bar();
   *   CoverageManager.writeReport(path); 
   * }</pre>
   * where <code>path</code> is {@link J2meClassNames#FILESYSTEM_ROOT}, the root
   * in which to store coverage results.
   * 
   * @author Michele Sama
   */
  class CoverageReportMethodAdapter extends MethodAdapter {
    
    /**
     * Constructor from superclass.
     * @param mv The nested MethodVisitor
     */
    public CoverageReportMethodAdapter(MethodVisitor mv) {
      super(mv);
    }
  
    /**
     * Checks the current operation and if it is a RETURN then it injects a 
     * method call to write the coverage report on file.
     * 
     * <p>Please note that coverage is not collected if the application 
     * terminates with a Throwable. 
     * 
     * @param opcode The instruction to be processed.
     * @see org.objectweb.asm.MethodAdapter#visitInsn(int)
     */
    @Override
    public void visitInsn(int opcode) {
      if (InjectorMethodAdapter.isReturnInstruction(opcode)) {
        mv.visitLdcInsn(J2meClassNames.FILESYSTEM_ROOT);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, 
            CoverageClassNames.COVERAGE_MANAGER, "writeReport", "(L" + 
            ClassNames.JAVA_LANG_STRING + ";)V");
      }
      mv.visitInsn(opcode);
    }
  }
  
  /**
   * Injects the code coverage report method at the start of the targeted
   * method.
   * 
   * <p>Methods visited by this adapter will be transformed from:
   * 
   * <pre>public void foo() {
   *   bar();
   * }</pre>
   * into
   * <pre>public void foo() {
   *   CoverageManager.writeReport(path);
   *   bar();
   * }</pre>
   * where <code>path</code> is {@link J2meClassNames#FILESYSTEM_ROOT},
   * the root in which to store coverage results.
   * 
   * <p>This is an alternative to the approach of injecting code at the end
   * of a method, to allow for the method in question to be able to safely
   * throw an Exception without defeating the coverage injection.
   * 
   * @author Stephen Woodward
   */
  class StartOfMethodCoverageReportMethodAdapter extends MethodAdapter {

    /**
     * Constructor from the superclass.
     * @param mv the nested MethodVisitor
     */
    public StartOfMethodCoverageReportMethodAdapter(MethodVisitor mv) {
      super(mv);
    }
    
    /**
     * Injects the coverage report writer at the start of the targeted method
     *  
     */
    @Override
    public void visitCode() {
      mv.visitLdcInsn(J2meClassNames.FILESYSTEM_ROOT);
      mv.visitMethodInsn(Opcodes.INVOKESTATIC, 
          CoverageClassNames.COVERAGE_MANAGER, "writeReport", "(L" + 
          ClassNames.JAVA_LANG_STRING + ";)V");
      mv.visitCode();
    }
  }
}
