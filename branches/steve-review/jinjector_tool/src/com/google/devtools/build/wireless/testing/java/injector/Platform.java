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

import com.google.devtools.build.wireless.testing.java.injector.j2me.J2meClassNames;
import com.google.devtools.build.wireless.testing.java.injector.j2me.J2mePrintServant;
import com.google.devtools.build.wireless.testing.java.injector.rim.RimClassNames;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


/*
 * TODO: The more I look at the interface for this enumerative the more I don't
 * like it. It would be better to have a set of methods accepting classname,
 * supername, method name and all the other necessary parameters returning a
 * boolean.
 * 
 * For instance:
 * 
 * boolean instrumentCoverageInitialization(class, superclass, method) {
 *   return class.isEquals(MIDLET) && method.isEquals("startApp");
 * } 
 */

/**
 * Enumerative to specify the current platform and to inject platform-specific 
 * instructions.
 * 
 * <p> This class helps supporting code injection into different platforms.
 * 
 * <p> The first issue is the compilation level. J2ME and RIM applications 
 * support Java 1.3. Android and J2SE support 1.5 at least. This has a strong 
 * impact in injected code. This is constraining injected instructions and 
 * decorators. 
 * 
 * <p> Note that Java 1.3 do not supports {@link StringBuilder}, generics and 
 * enumeratives. 
 * 
 * <p> Moreover specific platforms (e.g. RIM, J2ME) have their own lifecycles
 * which give information about where the application starts and stops. 
 * 
 * <p> Specific architectures also have custom classes, like loggers, profilers 
 * and util which should be used.
 * 
 * @author Michele Sama
 *
 */
public enum Platform {
  
  /**
   * Describes a J2ME application following the MIDlet model. 
   */
  J2ME(){
    
    /**
     * Injects a playback thread specific for J2ME.
     * 
     * <p> Injected code would be:
     * <code>
     * TestRunner tr = new TestRunner(this, new TestSuite());
     * tr.start();
     * </code>
     * 
     * @param mv The {@link InjectorMethodAdapter} to be used.
     * @param allTests The test base to inject.
     */
    @Override
    public void injectPlayback(InjectorMethodAdapter mv,
        String allTests) {    
      mv.visitTypeInsn(Opcodes.NEW, getTestRunnerClassName());
      mv.visitInsn(Opcodes.DUP);
      mv.stackServant.loadThis();
      mv.visitTypeInsn(Opcodes.NEW, allTests);
      mv.visitInsn(Opcodes.DUP);
      mv.visitMethodInsn(Opcodes.INVOKESPECIAL, allTests, "<init>",
          "()V");
      mv.visitMethodInsn(Opcodes.INVOKESPECIAL, getTestRunnerClassName(),
          "<init>", "(L" + ClassNames.MIDLET + ";L"
              + getTestCaseClassName() + ";)V");
      mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, getTestRunnerClassName(),
          "start", "()V");
    }
    
    /**
     * Returns the name of the end point method for a <code>MIDlet</code>.
     * 
     * @return the end method.
     */
    @Override
    public String getPlatformSpecificCoverageCollectionMethod() {
      return "destroyApp";
    }
    
    /**
     * Creates an instance of logger specific to J2ME. 
     * 
     * @param mv The {@link MethodVisitor} which will control the logger.
     * @return An instance of {@link J2mePrintServant}.
     */
    @Override
    public PrintServant getPlatformSpecificPrintServant(
        MethodVisitor mv) {
      return new J2mePrintServant(mv);
    }
    
    /**
     * Returns the name of the starting point method for a <code>MIDlet</code>.
     * 
     * @return the starting method.
     */
    @Override
    public String getPlatformSpecificStartupMethod() {
      return "startApp";
    }
    
    /**
     * According to the J2ME model all the application must extend 
     * <code>MIDlet</code>. 
     * 
     * @param superName the superclass' name.
     * @return <code>true</code> if the superclass is <code>MIDlet</code>.
     */
    @Override
    public boolean getPlatformSpecificMainSuperClass(String superName) {
      return ClassNames.MIDLET.equals(superName);
    }

    /**
     * According to the J2ME model all applications must extend 
     * <code>MIDlet</code>.  By happy chance, this class will be
     * the same as the one returned by
     * {@link #getPlatformSpecificMainSuperClass(String)} due to a J2ME
     * midlet having both the startApp and dispose methods in the same class.
     * 
     * @param superName the superclass' name.
     * @param className the class name to check.
     * @return <code>true</code> if the superclass is <code>MIDlet</code>.
     */
    @Override
    public boolean getPlatformSpecificCoverageCollectionClass
        (String className, String superName) {
      
      return getPlatformSpecificMainSuperClass(superName);
    }
    
    /**
     * Indicates that the coverage reporting should occur at the end of the
     * targeted method.
     * 
     * @return true
     */
    @Override
    public boolean injectCoverageReportingAtEnd() {
      return true;
    }
    
    /**
     * Returns the base test class for this platform.
     * @see Platform#getTestCaseClassName()
     */
    @Override
    public String getTestCaseClassName() {
      return J2meClassNames.J2ME_PKG + "J2meTestCase";
    }
    
    /**
     * Returns the test runner for this platform.
     * @see Platform#getTestRunnerClassName()
     */
    @Override
    public String getTestRunnerClassName() {
      return J2meClassNames.J2ME_PKG + "J2meTestRunner";
    }

    /**
     * Return the filesystem's root for J2ME applications. 
     */
    @Override
    public String getFileConnectionPrefix() {
      return J2meClassNames.FILESYSTEM_ROOT;
    }
  }, 
  
  /**
   * Describes a RIM-BlackBerry application.
   */
  RIM(){
    
    /**
     * Injects a playback thread specific for RIM.
     * 
     * @param mv The {@link InjectorMethodAdapter} to be used.
     * @param playbackTestBase The test base to inject.
     */
    @Override
    public void injectPlayback(InjectorMethodAdapter mv,
        String playbackTestBase) {
      mv.visitTypeInsn(Opcodes.NEW, getTestRunnerClassName());
      mv.visitInsn(Opcodes.DUP);
      mv.visitTypeInsn(Opcodes.NEW, playbackTestBase);
      mv.visitInsn(Opcodes.DUP);
      mv.visitMethodInsn(Opcodes.INVOKESPECIAL, playbackTestBase, "<init>",
      "()V");
      mv.visitMethodInsn(Opcodes.INVOKESPECIAL, getTestRunnerClassName(),
          "<init>", "(L" + getTestCaseClassName() + ";)V");
      mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, getTestRunnerClassName(),
          "start", "()V");
    }
    
    /**
     * Returns the name after which the application is supposed to terminate; 
     * For RIM, you need to nominate a specific method other than the main,
     * because the main method usually never terminates, and when it does,
     * it is a false indicator of the end of application.
     * */
    @Override
    public String getPlatformSpecificCoverageCollectionMethod() {
      return "run";
    }
    
    /**
     * Return an instance of a RIM specific logger, which is the same as J2ME.
     * 
     * @param mv The {@link MethodVisitor} which needs to be logged.
     * @return An instance of {@link J2mePrintServant}.
     * */
    @Override
    public PrintServant getPlatformSpecificPrintServant(
        MethodVisitor mv) {
      return new J2mePrintServant(mv);
    }
    
    /**
     * Return the starting point for RIM application which is a main method.
     * 
     * @return The <code>main</code> method.
     */
    @Override
    public String getPlatformSpecificStartupMethod() {
      return "main";
    }
    
    /**
     * Always returns <code>true</code> because any class can contain a main 
     * method.
     * 
     * @param superName The superclass name.
     * @return <code>true</code>.
     */
    @Override
    public boolean getPlatformSpecificMainSuperClass(String superName) {
      return true;
    }
    
    /**
     * Returns true if the correct class which should contain the coverage
     * collection method is specified by <code>className</code>
     * 
     * @param superName The superclass name to check
     * @param className The class name to check
     * @return true if the class name is the expected one.
     */
    @Override
    public boolean getPlatformSpecificCoverageCollectionClass
        (String className, String superName) {
      // TODO: This is a temporary implementation until the 
      // enumerations are refactored as per the class-level comments
      return RimClassNames.BB_TEST_RUNNER_END_TEST.equals(className);
    }
    
    /**
     * Indicates that the coverage reporting should occur at the start of the
     * targeted method.
     * 
     * @return false
     */
    @Override
    public boolean injectCoverageReportingAtEnd() {
      return false;
    }
    
    /**
     * Returns the base test class for this platform.
     * @see Platform#getTestCaseClassName()
     */
    @Override
    public String getTestCaseClassName() {
      return "j2meunit/framework/TestCase"; 
    }
    
    /**
     * Returns the test runner for this platform.
     * @see Platform#getTestRunnerClassName()
     */
    @Override
    public String getTestRunnerClassName() {
      return RimClassNames.RIM_PKG + "RimTestRunner"; 
    }

    /**
     * Returns the filesystem's root for RIM which is the same as for J2ME.
     */
    @Override
    public String getFileConnectionPrefix() {
      return J2meClassNames.FILESYSTEM_ROOT;
    }
  };

  /**
   * Creates an instance of PrintServant with regard to the specified platform.
   * <p>Factory method.
   * 
   * @param mv The nested {@link MethodVisitor}.
   * @return An instance of {@link PrintServant}.
   * @throws IllegalStateException if no specific servant is suitable for the 
   *    given platform.
   */
  public abstract PrintServant getPlatformSpecificPrintServant(
      MethodVisitor mv);
  
  
  /**
   * Returns the name of the method in which the replay thread is supposed 
   * to be started.
   * 
   * @return The method's name.
   */
  public abstract String getPlatformSpecificStartupMethod();
  
  /**
   * Tells if a class must be injected with a play back thread for 
   * regression tests. 
   * 
   * <p>The target class for this instrumentation depends from the platform. 
   * In some platform the application starts from a <code>main</code> method 
   * which can be contained in any other class. In other platform-specific 
   * architecture the main class is extending a specific class. 
   * 
   * @param superName The super class name.
   * @return <code>true</code> if the current super class can be the starting 
   *    point of the application.
   */
  public abstract boolean getPlatformSpecificMainSuperClass(String superName);
  
  /**
   * Tells if a class is the one which might contain the method specified by
   * {@link #getPlatformSpecificCoverageCollectionMethod}.
   * 
   * <p>The target class specified by <code>className</code> is intended
   * to be the class which contains some sort of finalizer-style method.
   * 
   * <p>The contract on this method is that it must not return false
   * if the class identified by <code>className</code> and
   * <code>superName</code> contains the method that is intended to receive the
   * start of code coverage injection.  That is to say, if this method returns
   * false, then the class being examined does not contain the start of code
   * coverage injection method.  This is not a bi-conditional, thus a return 
   * value of true comes with no guarantees.
   * 
   * <p>The above seems unusual, but basically, this method is a means of
   * filtering potential classes, leaving only certain options to be tested
   * by further checks, e.g. {@link #getPlatformSpecificStartupMethod()}
   * 
   * <p>For J2ME applications, the super class is the key identifier, not the
   * class itself, but for RIM CLDC Applications, the class itself is of
   * interest.  Thus both values are provided, and the method is expected to
   * check the appropriate value and return true or false accordingly.
   * 
   * <p>For J2ME applications, the super class is expected to match the one
   * expected by {@link #getPlatformSpecificMainSuperClass}.
   * 
   * @param className the class name.
   * @param superName the super class's name.
   * @return <code>true</code> if the current class might contain the
   *    expected near-terminal method.
   */
  public abstract boolean
      getPlatformSpecificCoverageCollectionClass(String className,
          String superName);

  /**
   * Invokes on the specified {@link MethodVisitor} the platform specific 
   * instructions to create an instance of playback thread.
   * 
   * @param mv The {@link InjectorMethodAdapter} on which to invoke visit 
   *    commands.
   * @param playbackTestBase The test base to inject.
   */
  public abstract void injectPlayback(InjectorMethodAdapter mv, 
      String playbackTestBase);
   
  /**
   * Gets the method's name at the end of which coverage information have to 
   * be collected.
   * 
   * @return A String representing the method's name.
   */
  public abstract String getPlatformSpecificCoverageCollectionMethod();
 
  /**
   * Returns true if the coverage reporting should be injected at the end of
   * the targeted method, or false if at the start.
   * 
   * @return true or false depending on where in the targeted method the report
   * method should be injected. 
   */
  public abstract boolean injectCoverageReportingAtEnd();
  
  /**
   * Gets the name of the base test class which can be injected to this 
   * platform using '/' as a package separator.
   * 
   * <p> Please not that this is going to be the base class. Certain library 
   * may require a specific test case which has to implement the class 
   * returned by this method.
   * 
   * <p> An example of derived Test case is the test case specific for the 
   * Echoes Widget Library which extends from a J2ME test case.
   * 
   * @return The name of the platform specific test case.
   */
  public abstract String getTestCaseClassName();
  
  /**
   * Gets the name of the test runner which is supposed to be used with 
   * this plaftorm.
   * 
   * @return The name of the test runner class.
   */
  public abstract String getTestRunnerClassName();
  
  /**
   * Gets the root of the filesystem of the given platform.
   * 
   * @return the filesystem's root.
   */
  public abstract String getFileConnectionPrefix();
}
