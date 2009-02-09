// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.devtools.build.wireless.testing.java.injector.coverage;

import com.google.devtools.build.wireless.testing.java.injector.ClassNames;
import com.google.devtools.build.wireless.testing.java.injector.Platform;
import com.google.devtools.build.wireless.testing.java.injector.coverage.GenerateCoverageInitializationClassAdapter.CoverageInitializationCodeMethodVisitor;
import com.google.devtools.build.wireless.testing.java.injector.coverage.GenerateCoverageInitializationClassAdapter.CoverageInitializationData;

import static org.easymock.classextension.EasyMock.*;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;


import junit.framework.TestCase;

/**
 * Tests for GenerateCoverageInitializationClassAdapter.
 *
 * @author Olivier Gaillard
 */
public class GenerateCoverageInitializationClassAdapterTest extends TestCase {
  private final static CoverageInitializationData LINE_COVERAGE =
      new CoverageInitializationData(CoverageMode.LINE, "", "");

  private final static CoverageInitializationData DISABLED_COVERAGE =
      new CoverageInitializationData(CoverageMode.DISABLED, "", "");

  private static final String[] DUMMY_ARRAY = new String[0];

  private String anyString() {
    return isA(String.class);
  }

  public ClassVisitor createClassVisitorMock(MethodVisitor mv) {
    ClassVisitor cv = createMock(ClassVisitor.class);
    cv.visit(anyInt(), anyInt(), anyString(), anyString(), anyString(), eq(DUMMY_ARRAY));
    expect(cv.visitMethod(anyInt(), anyString(), anyString(), anyString(), eq(DUMMY_ARRAY)))
        .andStubReturn(mv);
    replay(cv);
    return cv;
  }

  public void testNoInstrumentationIfWrongClass() throws Exception {
    MethodVisitor mv = createMock(MethodVisitor.class);
    ClassVisitor cv = createClassVisitorMock(mv);
    GenerateCoverageInitializationClassAdapter classAdapter =
        new GenerateCoverageInitializationClassAdapter(Platform.J2ME, cv, DISABLED_COVERAGE);
    classAdapter.visit(0, 0, "", "", "foo/bar", DUMMY_ARRAY);
    MethodVisitor result = classAdapter.visitMethod(0, "startApp", "", "", DUMMY_ARRAY);

    assertFalse(result instanceof CoverageInitializationCodeMethodVisitor);
  }

  public void testNoInstrumentationIfWrongMethod() throws Exception {
    MethodVisitor mv = createMock(MethodVisitor.class);
    ClassVisitor cv = createClassVisitorMock(mv);
    GenerateCoverageInitializationClassAdapter classAdapter =
        new GenerateCoverageInitializationClassAdapter(Platform.J2ME, cv, DISABLED_COVERAGE);
    classAdapter.visit(0, 0, "", "", ClassNames.MIDLET, DUMMY_ARRAY);
    MethodVisitor result = classAdapter.visitMethod(0, "fooBar", "", "", DUMMY_ARRAY);

    assertFalse(result instanceof CoverageInitializationCodeMethodVisitor);
  }

  public void testNoInstrumentationIfCoverageDisabled() throws Exception {
    MethodVisitor mv = createMock(MethodVisitor.class);
    ClassVisitor cv = createClassVisitorMock(mv);
    GenerateCoverageInitializationClassAdapter classAdapter =
        new GenerateCoverageInitializationClassAdapter(Platform.J2ME, cv, DISABLED_COVERAGE);
    classAdapter.visit(0, 0, "", "", ClassNames.MIDLET, DUMMY_ARRAY);
    MethodVisitor result = classAdapter.visitMethod(0, "startApp", "", "", DUMMY_ARRAY);

    assertFalse(result instanceof CoverageInitializationCodeMethodVisitor);
  }

  public void testInitiliazationCodeMethodVisitorAddedToVisitorChain() {
    MethodVisitor mv = createMock(MethodVisitor.class);
    ClassVisitor cv = createClassVisitorMock(mv);
    GenerateCoverageInitializationClassAdapter classAdapter =
        new GenerateCoverageInitializationClassAdapter(Platform.J2ME, cv, LINE_COVERAGE);
    classAdapter.visit(0, 0, "", "", ClassNames.MIDLET, DUMMY_ARRAY);
    MethodVisitor result = classAdapter.visitMethod(0, "startApp", "", "", DUMMY_ARRAY);

    assertTrue(result instanceof CoverageInitializationCodeMethodVisitor);
  }
  
  public void testLineCoverageInstrumentationApplied() throws Exception {
    MethodVisitor mv = createMock(MethodVisitor.class);
    // Put runId on stack
    mv.visitLdcInsn(anyString()); 
    // Put the output file name on stack
    mv.visitLdcInsn(anyString()); 
    // Call initLineCoverage
    mv.visitMethodInsn(anyInt(), eq(CoverageClassNames.COVERAGE_MANAGER), 
        eq("initLineCoverage"), anyString());
    // Call enableCoverage
    mv.visitMethodInsn(anyInt(), eq(CoverageClassNames.COVERAGE_MANAGER), 
        eq("enableCoverage"), anyString());
    // Invoke to the nested visitor 
    mv.visitCode();
    replay(mv);

    CoverageInitializationCodeMethodVisitor coverageInitMethodVisitor =
        new CoverageInitializationCodeMethodVisitor(mv, LINE_COVERAGE);
    coverageInitMethodVisitor.visitCode();
    verify(mv);
  }
}
