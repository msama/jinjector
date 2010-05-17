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

package com.google.devtools.build.wireless.testing.java.injector.coverage;

import com.google.devtools.build.wireless.testing.java.injector.ClassNames;
import com.google.devtools.build.wireless.testing.java.injector.Platform;
import com.google.devtools.build.wireless.testing.java.injector.j2me.J2meClassNames;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


/**
 * Injects the initialization code needed by the coverage mechanism. This is
 * used to set up the parameters needed by coverage system such as the source
 * path of the java files or the coverage mode.
 *
 * @see GenerateCoverageInitializationClassAdapter.CoverageInitializationData
 * for more details about the parameters available.
 *
 * @author Olivier Gaillard
 */
public class GenerateCoverageInitializationClassAdapter extends ClassAdapter {
  private final Platform targetPlatform;
  private final CoverageInitializationData initializationData;

  private boolean shouldInjectCoverageParameters = false;

  /**
   *
   * @param targetPlatform the target platform.
   * @param coverageInitData the data that has to be injected to initialize the
   * coverage mechanism.
   */
  public GenerateCoverageInitializationClassAdapter(
      Platform targetPlatform, ClassVisitor cv, CoverageInitializationData coverageInitData) {

    super(cv);
    this.initializationData = coverageInitData;
    this.targetPlatform = targetPlatform;
  }

  /**
   * Decides if the current class has to be injected to initialize the coverage.
   */
  @Override
  public void visit(int version, int access, String name, String signature,
      String superName, String[] interfaces) {
    shouldInjectCoverageParameters = false;

    // TODO: use a better name: getStartAppClassName?
    if (targetPlatform.getPlatformSpecificMainSuperClass(superName) ||
        // TODO: remove this condition, 
        // Special case to handle AutoTest, the TestMidlet class used by AutoTest
        // overrides "startApp" without calling the "super.startApp".
        J2meClassNames.TEST_MIDLET.equals(name)) {
      shouldInjectCoverageParameters = true;
    }
    cv.visit(version, access, name, signature, superName, interfaces);
  }

  /**
   * Injects an initialization method at the beginning of the right method.
   * 
   * @see org.objectweb.asm.ClassAdapter#visitMethod(int, java.lang.String, 
   *    java.lang.String, java.lang.String, java.lang.String[])
   */
  @Override
  public MethodVisitor visitMethod(int access, String name, String desc,
      String signature, String[] exceptions) {
    MethodVisitor mv =
        cv.visitMethod(access, name, desc, signature, exceptions);

    final String targetMethod = targetPlatform.getPlatformSpecificStartupMethod();
    if (shouldInjectCoverageParameters &&
        name.equals(targetMethod) &&
        initializationData.getCoverageMode() != CoverageMode.DISABLED) {
      mv = new CoverageInitializationCodeMethodVisitor(mv, initializationData);
    }

    return mv;
  }

  /**
   * Injects the code necessary to initialize the coverage mechanism.
   * 
   * TODO: this should depend on the type of coverage. We should 
   *    have multiple classes behaving differently. The same client side.
   */
  static class CoverageInitializationCodeMethodVisitor extends MethodAdapter {
    private final CoverageInitializationData initData;

    public CoverageInitializationCodeMethodVisitor(
        MethodVisitor mv, CoverageInitializationData initData) {
      super(mv);
      this.initData = initData;
      if (initData.coverageMode == CoverageMode.DISABLED) {
        throw new IllegalStateException("This MethodVisitor should not be " +
            "created if coverage is disabled. Please report this error to " +
            "developers.");
      }
    }

    /**
     * Initializes a coverage manager once the application has been started.
     * The instrumented code looks like:
     * <pre>
     * CoverageManager.initMethodCoverage(runid);
     * CoverageManager.initLineCoverage(runid, sourcepath, outputFile);
     * CoverageManager.enableCoverage();
     * </pre>
     * 
     * @see org.objectweb.asm.MethodAdapter#visitCode()
     */
    @Override
    public void visitCode() {
      if (initData.getCoverageMode() == CoverageMode.SUMMARY) {
        mv.visitLdcInsn(initData.getRunId());
        mv.visitMethodInsn(
            Opcodes.INVOKESTATIC,
            CoverageClassNames.COVERAGE_MANAGER, "initMethodCoverage",
            "(L" + ClassNames.JAVA_LANG_STRING + ";)V");
      }

      if (initData.getCoverageMode() == CoverageMode.LINE) {
        String descriptor = String.format("(L%s;L%s;)V",
            ClassNames.JAVA_LANG_STRING, ClassNames.JAVA_LANG_STRING);

        mv.visitLdcInsn(initData.getRunId());
        mv.visitLdcInsn(initData.getLineCoverageOutputFilename());
        mv.visitMethodInsn(
            Opcodes.INVOKESTATIC,
            CoverageClassNames.COVERAGE_MANAGER, "initLineCoverage",
            descriptor);
      }

      mv.visitMethodInsn(Opcodes.INVOKESTATIC,
          CoverageClassNames.COVERAGE_MANAGER, "enableCoverage", "()V");
      
      mv.visitCode();
    }
  }

  /**
   * Stores parameters needed to initialize the CoverageManager. All of these
   * parameters will be injected to the CoverageManager using methods such as
   * CoverageManager#initMethodCoverage(String) and
   * CoverageManager#initLineCoverage(String,String,String).
   *
   * New parameters needed by the CoverageManager can be added to this class in
   * order to be injected by the CoverageInitializationCodeMethodVisitor class.
   */
  public static final class CoverageInitializationData {
    private final CoverageMode coverageMode;
    private final String runId;
    private final String lineCoverageOutputFilename;

    public CoverageInitializationData(CoverageMode coverageMode, String runId,
        String lineCoverageOutputFilename) {
      this.coverageMode = coverageMode;
      this.runId = runId;
      this.lineCoverageOutputFilename = lineCoverageOutputFilename;
    }

    public CoverageMode getCoverageMode() {
      return coverageMode;
    }

    public String getRunId() {
      return runId;
    }

    public String getLineCoverageOutputFilename() {
      return lineCoverageOutputFilename;
    }

  }
}
