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

import com.google.test.jinjector.coverage.MethodInfoContainer.MethodInfo;
import com.google.test.jinjector.util.FileConnectionUtil;
import com.google.test.jinjector.util.IoUtil;

import java.io.*;
import java.util.*;

import javax.microedition.io.file.FileConnection;

/**
 * Write a summary of the coverage data gathered by the CoverageManager. This 
 * is a basic implementation of {@link CoverageWriter} which is supposed to be 
 * used with method profilers. To write line coverage informations it is better
 * to use {@link CoverageLcovWriter}. 
 * 
 * <p>The produced report is written in an emma-like plain text report style.
 * 
 * {@link "http://emma.sourceforge.net/samples.html"}
 * @author Michele Sama
 */
public class CoverageSummaryWriter implements CoverageWriter {
  
  public static final String COVERED = "covered";
  public static final String UNCOVERED = "UNCOVERED";

  public static final String FILENAME_SUMMARY = "coverageSummary.txt";

  private final CoverageManager coverageManager;
  private final MethodInfoContainer methods;
  private final String runId;

  /**
   * Emma-style header for coverage.
   */
  private static final String HEADER = 
      "[class, %]\t[method, %]\t[block, %]\t[line, %]\t[name]";

  /**
   * Emma-style separator.
   */
  private static final String LINE_SEPARATOR = 
      "---------------------------------------" +
      "----------------------------------------";
  
  public static final char SEPARATOR = '\t';


  /**
   * Creates an instance of the writer
   * 
   * @param manager
   * @param methods
   * @param runId
   */
  public CoverageSummaryWriter(CoverageManager manager, MethodInfoContainer methods,
      String runId) {
    coverageManager = manager;
    this.methods = methods;
    this.runId = runId;
  }
  
  /**
   * Writes a report on method coverage. This implementation writes 
   * {@link #COVERED} if the method has been covered or {@link #UNCOVERED}
   * otherwise.
   * 
   * 
   * @param ps The {@link PrintStream} in which to write.
   * @param index The index of the method to query.
   */
  private void writeMethodCoverageFlag(PrintStream ps, int index) {
    ps.print(coverageManager.isCovered(index) ? COVERED : UNCOVERED);
  }
  
  /**
   * Writes a report on the method's total number of calls. 
   * 
   * @param ps the stream to write to.
   * @param index the index of the method to query.
   */
  private void writeMethodTotalCalls(PrintStream ps, int index) {
    ps.print(coverageManager.getNumberOfMethodCalls(index));
  }
  
  /**
   * Writes a report on method's execution time coverage. 
   * 
   * @param ps the stream to write to.
   * @param index the index of the method to query.
   */
  private void writeMethodExecutionTime(PrintStream ps, int index) {
    ps.print(coverageManager.getTotalMethodExecutionTime(index));
  }
  
  /**
   * Writes, starting from the array in memory, a list of covered/uncovered
   * methods.
   * 
   * <p>Entry in the base file are composed by lines containing the index used for 
   * the method plus  fullMethodName.
   * 
   * <p>Entries are converted in fullMethodName plus the coverage status.
   * 
   * <p>This list is the base for all the other computations.
   *
   * @throws IOException if an error occurs when writing the report.
   */
  private Hashtable generateAndWriteDetailedCoverage(String path) throws IOException {
    Hashtable packageBreakdown = new Hashtable();

    FileConnection fc = null;
    PrintStream ps = null;

    methods.loadFile(runId);
    Enumeration iter = methods.getMethodNames();

    try {
      final String filename = path + MethodInfoContainer.FILENAME_METHOD;
      fc = FileConnectionUtil.createAndOpenFile(filename);
      ps = new PrintStream(fc.openOutputStream());
      ps.println("Method" + SEPARATOR + "Coverage" + SEPARATOR + 
          "InvocationCount" + SEPARATOR + "TotalExecutionTime");
      // TODO: write average execution time.

      while (iter.hasMoreElements()) {
        String methodName = (String) iter.nextElement();
        MethodInfo info = methods.getMethodInfo(methodName);
        ps.print(info.getFullname());

        // writes method's info
        ps.print(SEPARATOR);
        writeMethodCoverageFlag(ps, info.getMethodIndex());
        
        ps.print(SEPARATOR);
        writeMethodTotalCalls(ps, info.getMethodIndex());

        ps.print(SEPARATOR);
        writeMethodExecutionTime(ps, info.getMethodIndex());
        
        ps.println();
        
        /**
         * Update report information.
         * */
        PackageReportContainer report = null;
        if (!packageBreakdown.containsKey(info.getPackage())) {
          report =  new PackageReportContainer(info.getPackage());
          packageBreakdown.put(info.getPackage(), report);
        } else {
          report = (PackageReportContainer) packageBreakdown.get(info.getPackage());
        }
        report.addMethodFromClass(
            info.getClassname(), coverageManager.isCovered(info.getMethodIndex()));
      }  

    } catch (IOException ioe) {
      throw new IOException(
          "Cannot write coverage report to disk '" + path +
          MethodInfoContainer.FILENAME_METHOD + "'");
    } finally {
      IoUtil.closeCloseable(ps);
      FileConnectionUtil.close(fc);
    }

    return packageBreakdown;
  }

  /**
   * Load the statistic file from inside the jar and print it on a given stream.
   * 
   * @param ps The print stream on which to write (the stream will be closed by
   *     this method).
   * @throws IOException if an error occurs while reading/writing.
   */
  private void writeStatistics(PrintStream ps) throws IOException {
    InputStreamReader isr = null; 
    try {
      isr = new InputStreamReader( 
          CoverageManager.class.getResourceAsStream("/" + FILENAME_SUMMARY));
      int k = 0;
      while ( (k = isr.read()) != -1) {
        ps.write(k);
      }

      isr.close();
      isr = null;

    } finally {
      IoUtil.closeCloseable(isr);
    }
  }
  
 /**
  * Writes a full report in emma style.
  */
  public void writeFullReport(String path) throws IOException {
    Hashtable packageBreakdown = generateAndWriteDetailedCoverage(path);
    FileConnection fc = null;
    PrintStream ps = null;
    try {
      fc = FileConnectionUtil.createAndOpenFile(path + CoverageManager.FILENAME_COVERAGE);
      ps = new PrintStream(fc.openOutputStream());
     
      ps.println("[JINJECTOR report, generated " + new Date() + "]");
      ps.println(LINE_SEPARATOR);
      ps.println("OVERALL COVERAGE SUMMARY:\n");
      ps.println(HEADER);
     
      writeOverall(ps, packageBreakdown);
     
      writeStatistics(ps);
      ps.println();
      ps.println("COVERAGE BREAKDOWN BY PACKAGE:\n");
      ps.println(HEADER);
      writeBreakdown(ps, packageBreakdown);
      ps.println(LINE_SEPARATOR);

    } finally {
      IoUtil.closeCloseable(ps);
      FileConnectionUtil.close(fc);
    } 
  }
 
   /**
    * Parses the detailed method coverage and writes the overall
    * coverage summary to the specified PrintStream.
    * 
    * @param ps The PrintStream in which to write.
    * @param packageBreakdown The Hashtable from which to collect data.
    */
  private void writeOverall(PrintStream ps, Hashtable packageBreakdown) {
    Enumeration keys = packageBreakdown.keys();
    PackageReportContainer fullReport = new PackageReportContainer("all classes");
    while (keys.hasMoreElements()) {
      fullReport.includeReport(
          (PackageReportContainer) packageBreakdown.get(keys.nextElement())); 
    }
    writeReport(fullReport, ps);
    ps.print("\n");
  }
 
  /**
   * Writes the coverage breakdown of monitored classes divided by package.
   * 
   * @param ps The PrintStream in which to write.
   * @param packageBreakdown The Hashtable from which to collect data.
   */
  private void writeBreakdown(PrintStream ps, Hashtable packageBreakdown) {
    Enumeration keys = packageBreakdown.keys();
    String pkg = null;
    int totalClasses = 0;
    while (keys.hasMoreElements()) {
      pkg = (String) keys.nextElement();
      PackageReportContainer report = (PackageReportContainer) packageBreakdown.get(pkg);
      writeReport(report, ps);
      totalClasses += report.getAllClasses();
    }
  }
 
 /**
  * Writes emma-like statistic at package level. This is used to write line by
  * line in the output file.
  * 
  * <p>The output will look like:
  * <pre>
  *   [class, %]  [method, %] [block, %]      [line, %]       [name]
  *   100% (3/3)  100% (7/7)  95% (120/126)   90% (27/30)     all classes
  * </pre>
  *
  * @param report The Report to print.
  * @param ps the PrintStream on which to write
  */
 private void writeReport(PackageReportContainer report, PrintStream ps) {
   // print class
   int percent = (100 * report.getCoveredClasses()) / report.getAllClasses();
   ps.print(percent + "% (" + report.getCoveredClasses() + "/" + 
       report.getAllClasses() + ")\t");
   
   // print method
   percent = (100 * report.getCoveredMethods()) / report.getAllMethods();
   ps.print(percent + "% (" + report.getCoveredMethods() + "/" + 
       report.getAllMethods() + ")\t");
   
   // block
   ps.print("NA% (NA/NA)\t");
   
   // line
   percent = (100 * report.getCoveredLines()) / report.getAllLines();
   ps.print(percent + "% (" + report.getCoveredLines() + "/" + 
       report.getAllLines() + ")\t");
   
   // names
   if (report.getPkg().equals("")) {
     ps.println("default package");
   } else {
     ps.println(report.getPkg());
   } 
 }
}
