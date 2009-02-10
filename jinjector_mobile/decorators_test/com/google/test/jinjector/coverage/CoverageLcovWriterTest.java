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

package com.google.test.jinjector.coverage;


import com.google.test.jinjector.coverage.CoverageLcovWriter.InstrumentedLineParser;
import com.google.test.jinjector.coverage.CoverageManager.CoverageDataFile;
import com.google.test.jinjector.util.FileConnectionUtil;
import com.google.test.jinjector.util.IoUtil;
import com.google.test.jinjector.util.StringUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import j2meunit.framework.Test;
import j2meunit.framework.TestCase;
import j2meunit.framework.TestMethod;
import j2meunit.framework.TestSuite;

/**
 * Tests for {@link CoverageLcovWriter}
 * 
 * @author Olivier Gaillard
 * @author Michele Sama
 *
 * @resource jinjector_example/coverageInstrumentedLines.txt
 * @resource jinjector_example/expectedCoverage.txt
 */
public class CoverageLcovWriterTest extends TestCase {

  /**
   * Represent the text contained in a line coverage index files.
   * 
   * <p>The tests represent two files with 2 and 1 lines.
   */
  private static final String INSTRUMENTED_LINES =
    "0" + CoverageLcovWriter.SEPARATOR + "2" + CoverageLcovWriter.SEPARATOR + 
    "testfile" + CoverageLcovWriter.SEPARATOR + "20" + 
    CoverageLcovWriter.SEPARATOR + "25\n" +
    "1" + CoverageLcovWriter.SEPARATOR + "1" + CoverageLcovWriter.SEPARATOR + 
    "testfile2" + CoverageLcovWriter.SEPARATOR + "1\n";
  
  /**
   * Constructor from superclass.
   */
  public CoverageLcovWriterTest() {
  }

  /**
   * Constructor from superclass.
   * 
   * @param name the test's name.
   * @param method the test's method.
   */
  public CoverageLcovWriterTest(String name, TestMethod method) {
    super(name, method);
  }

  /**
   * Constructor from superclass.
   * 
   * @param name the test's name.
   */
  public CoverageLcovWriterTest(String name) {
    super(name);
  }

  /**
   * Generates the lcov coverage report for the given bitfields initialized 
   * from the given text.
   * 
   * @param fields the bitfield.
   * @param text the line coverage index report.
   * @return the lcov coverage report.
   */
  private String writeInfoUsingLcovFormat(Bitfield[] fields, String text) {
    CoverageManager cm = createCoverageManagerAndEnableCoverage(fields);
    
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(os);
    
    InstrumentedLineParser parser = new InstrumentedLineParser(cm, ps);
    
    try {
      String lines[] = StringUtil.split(text, "\n");
      for (int i = 0; i < lines.length; i++) {
        parser.parseLineAndWriteLineCoverage(lines[i]);
      }
    } catch (IOException ex) {
      fail("It was imppossible to write info: " + ex.getMessage());
    }
    return os.toString();
  }

  /**
   * Tests that the produced lcov coverage report will be correct.
   * 
   * @size small
   */
  public void testCoveredLinesCorrectlyTransformedToLcovFormat() {

    Bitfield[] fields = new Bitfield[2];
    fields[0] = new Bitfield(2);
    fields[1] = new Bitfield(1);
    fields[0].set(0);
    fields[0].set(1);
    fields[1].set(0);
    
    String output = writeInfoUsingLcovFormat(fields, INSTRUMENTED_LINES);

    assertEquals("Wrong number of files detected",
        2, StringUtil.countOccurrences(output, "SF:"));
    assertEquals("Wrong number of covered lines written",
        3, StringUtil.countOccurrences(output, "DA:"));
    assertTrue("Line covered not correctly written", 
        output.indexOf("DA:1,1") != -1);
  }

  /**
   * @size small
   */
  public void testUncoveredLinesCorrectlyTransformedToLcovFormat() {
    Bitfield[] fields = new Bitfield[2];
    fields[0] = new Bitfield(2);
    fields[1] = new Bitfield(1);
    fields[0].set(1);
    
    String output = writeInfoUsingLcovFormat(fields, INSTRUMENTED_LINES);
    
    assertTrue("Line not covered not correctly written", 
        output.indexOf("DA:20,0") != -1);
    assertTrue("Line covered not correctly written", 
        output.indexOf("DA:25,1") != -1);
  }

  private CoverageManager createCoverageManagerAndEnableCoverage(
      Bitfield[] bitfields) {
    CoverageManager manager = new CoverageManager();
    manager.setLineCoverageFields(bitfields, new CoverageDataFile("", ""));
    CoverageManager.setInstance(manager);
    CoverageManager.enableCoverage();
    return manager;
  }

  /**
   * @size small
   */
  public void testLinesInstrumentedDataFileCorrectlyRead() {
    ByteArrayInputStream is = new ByteArrayInputStream(INSTRUMENTED_LINES.getBytes());
    Bitfield[] bitfileds = Bitfield.getBitfieldsForLineCoverage(is);

    assertEquals(2, bitfileds.length);
  }
  
  /**
   * This test uses two pre build files to set up the coverage and verify 
   * that a known sequences of call will produce an expected result.
   * 
   * @size large
   */
  public void testLineCoverage() {
    String root = "file:///root1/";
    String outputFile = "testOut.txt";
    String runid = "";
    
    CoverageManager.setInstance(new CoverageManager());
    CoverageManager.initLineCoverage(runid, outputFile);
    CoverageManager.enableCoverage();
    CoverageManager.setLineCovered(0, 0);
    CoverageManager.setLineCovered(1, 0);
    CoverageManager.setLineCovered(2, 1);
    CoverageManager.setLineCovered(0, 0);
    CoverageManager.setLineCovered(1, 0);
    CoverageManager.setLineCovered(2, 1);
    CoverageManager.setLineCovered(0, 9);
    CoverageManager.setLineCovered(1, 2);
    CoverageManager.setLineCovered(2, 3);
    
    try {
      CoverageManager.setLineCovered(4, 0);
      fail("Covering a class not mapped should have thrown an exception.");
    } catch(Exception ex) {
      // ok.
    }
    
    try {
      CoverageManager.setLineCovered(3, 40);
      fail("Covering a line not mapped should have thrown an exception.");
    } catch(Exception ex) {
      // ok.
    }
    
    CoverageManager.writeReport(root);
    
    String obtainedResult = null;
    String expectedResult = null;
    
    FileConnection fc = null;
    InputStream is = null;
    
    // Read the produced output
    try {
      fc = (FileConnection) 
          Connector.open(root + outputFile + runid);
    
      try {
        is = fc.openInputStream();
        obtainedResult = new String(IoUtil.readAllBytesAndClose(is));
      } catch (IOException e) {
        IoUtil.closeCloseable(is);
        fail("An error occurred while reading generated output file: " 
            + e.getMessage()); 
      } 
      
    } catch (IOException ex) {
      fail("An error occurred while opening output file: " + ex.getMessage());
    } finally {
      FileConnectionUtil.close(fc);
    }
    
    // Read the pre-generated output file to use as a comparison
    try {
      is = getClass().getResourceAsStream("/expectedCoverage.txt");
      expectedResult = new String(IoUtil.readAllBytesAndClose(is));
    } catch (IOException e) {
      IoUtil.closeCloseable(is);
      fail("An error occurred while reading expected result file: " +
          e.getMessage());
    }
    
    assertEquals("The produced lcov file is different than the expected one!",
        expectedResult, obtainedResult);
  }
  
  /**
   * @see j2meunit.framework.TestCase#suite()
   */
  public Test suite() {
    TestSuite suite = new TestSuite();
    
    suite.addTest(new CoverageLcovWriterTest(
        "testCoveredLinesCorrectlyTransformedToLcovFormat") {
      public void runTest() {
        testCoveredLinesCorrectlyTransformedToLcovFormat();
      }
    });
    
    suite.addTest(new CoverageLcovWriterTest(
        "testLineCoverage") {
      public void runTest() {
        testLineCoverage();
      }
    });
    
    suite.addTest(new CoverageLcovWriterTest(
        "testLinesInstrumentedDataFileCorrectlyRead") {
      public void runTest() {
        testLinesInstrumentedDataFileCorrectlyRead();
      }
    });
    
    suite.addTest(new CoverageLcovWriterTest(
        "testUncoveredLinesCorrectlyTransformedToLcovFormat") {
      public void runTest() {
        testUncoveredLinesCorrectlyTransformedToLcovFormat();
      }
    });
    
    return suite;
  }
  
}
