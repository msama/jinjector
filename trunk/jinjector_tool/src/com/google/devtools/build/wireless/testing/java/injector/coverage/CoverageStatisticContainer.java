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

import com.google.common.base.Join;
import com.google.common.collect.LinkedListMultimap;
import com.google.devtools.build.wireless.testing.java.injector.InstrumentedJarCreator;
import com.google.devtools.build.wireless.testing.java.injector.util.Closeables;
import com.google.devtools.build.wireless.testing.java.injector.util.Files;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class is a container object for coverage statistics at instrumentation 
 * time.
 * 
 * <p> It keeps trace of all the mapped methods, classes and packages and 
 * writes them at the end of the instrumentation. It also contains default 
 * file names.
 * 
 * <p> The mapping is performed by {@link CodeCoverageClassAdapter} using the 
 * code visiting model defined by ASM Objectweb. According to this model 
 * every time a class is visited it must be added, as well as its package 
 * and its file (excluding duplicates). While exploring the class the methods
 * are also visited which provides information about the methods themselves 
 * and about the lines of source code.
 * 
 * @author Michele Sama
 */
public class CoverageStatisticContainer {

  public static final char SEPARATOR = '\t';
  
  public static final char COMMENT = '#';
  
  /**
   * Default file name with which methods coverage information will be stored 
   * if not differently specified. 
   * 
   * <p> This file should be available for the 
   * instrumented application in order to save coverage informations at 
   * runtime.
   */
  public static final String FILENAME_METHOD = "methodCoverage.txt";
  
  /**
   * Default file name with which packages coverage information will be stored 
   * if not differently specified. 
   * 
   * <p> This file should be available for the 
   * instrumented application in order to save coverage informations at 
   * runtime.
   */
  public static final String FILENAME_PACKAGE = "packageCoverage.txt";
  
  /**
   * Default file name with which a summary will be saved if not differently 
   * specified. 
   * 
   * <p> This file should be available for the 
   * instrumented application in order to save coverage informations at 
   * runtime.
   */
  public static final String FILENAME_SUMMARY = "coverageSummary.txt";

  /**
   * File name of file storing line numbers of lines instrumented.
   *
   * <p> This file is needed if the line coverage mode has been selected.
   */
  public static final String FILENAME_INSTRUMENTED_LINES = "coverageInstrumentedLines.txt";
  
  /**
   * A list containing all the methods which have been mapped for coverage and
   * which is going to be stored into a file by 
   * {@link CoverageStatisticContainer#writeMappedMethods(PrintWriter)}.
   * 
   * <p> The position that they have inside the list is used at runtime as 
   * index to flag them as covered and it must not be changed after they have 
   * been added.
   * 
   * @see CoverageStatisticContainer#writeSummary(PrintWriter)
   */
  private final List<String> methods = new ArrayList<String>();
  
  /**
   * A collection containing all the mapped packages which will be saved into a 
   * file by 
   * {@link CoverageStatisticContainer#writeMappedPackages(PrintWriter)}.
   * 
   * <p> At runtime information about all the mapped packages will be used to 
   * generate the emma-styled package breakdown, in which coverage information 
   * are subdivided by package.
   * 
   * @see CoverageStatisticContainer#writeSummary(PrintWriter)
   */
  private final Set<String> packages = new HashSet<String>();
  
  /**
   * A collection containing all the mapped classes which will be used to 
   * trace instrumented classes.
   * 
   * <p> At runtime this will be used to generate statistic about covered 
   * classes.
   * 
   * @see CoverageStatisticContainer#writeSummary(PrintWriter)
   */
  private final List<String> classes = new ArrayList<String>();
  
  /**
   * A collection of all the mapped files. The position in the list will be 
   * used as unique index to identify the file.
   * 
   * @see CoverageStatisticContainer#writeSummary(PrintWriter)
   */
  private final List<String> files = new ArrayList<String>();

  /** 
   * List of instrumented lines for each file. 
   */
  private final LinkedListMultimap<String, Integer> instrumentedLines =
      new LinkedListMultimap<String, Integer>();

  private int totalLineCount = 0;
  
  /**
   * Includes a method in the methods' collection and returns the index which 
   * will be used at runtime by the coverage tool to flag the method as 
   * covered. 
   * 
   * <p> If the method is already in the collection the same class has 
   * been visited twice.
   * In general this underlines a bug in the build system, but this can also
   * can happen when testing the coverage/decoration library.
   * 
   * @param methodName the method to map in the format foo/package/class.method
   * @return the index of the method in the mapping array.
   * @throws IllegalArgumentException If the method is already on the list.
   */
  public int includeMethod(String methodName) {
    // Preconditions.
    if (methods.contains(methodName)) {
      throw new IllegalArgumentException(methodName + " wa already mapped!");
    }

    // Computation.
    int index = methods.size();
    methods.add(methodName);
    return index;
  }

  /**
   * Adds a line to the list of instrumented lines.
   * 
   * <p> The file <b>MUST</b> have been already mapped by calling 
   * {@link #includeFile(String)}.
   * 
   * <p> Injected files or files without line information, if 
   * included in the coverage will be added with size 0, because they must 
   * have a total line information, but they should not modify the total size.
   * 
   * @param filename the name of the file of which we are instrumenting 
   *     the lines.
   * @param line the line to add.
   * @return the current index for the instrumented line which is going to be 
   *     used as index for the bit array.
   */ 
  public int addInstrumentedLineAndGetLineIndex(String filename, int line) {
    if (filename == null) {
      throw new IllegalArgumentException("Filename cannot be null!");
    }
    
    List<Integer> lines = instrumentedLines.get(filename);

    int index = 0;
    if ( lines != null) {
      if (lines.contains(line)) {
        throw new IllegalStateException("Line " + line + 
            " was already mapped for file " + filename);
        /* 
         * A for loop compiled with the OpenJDK will have the line information
         * twice, one at the beginning and one at the end of the loop.
         * In that case this method throws an exception and does not add the same
         * line twice. The caller method should handle the exception and
         * continue its computation if this exception was raised by a for, or it 
         * could prevent the double invocation.
         */ 
      }
      index = lines.size();
    }
    instrumentedLines.put(filename, line);
    
    // if the list was empty index is 0 because this line would be the first one.
    return index;
  }
  
  /**
   * Gets the number of methods which will be monitored.
   * 
   * @return An integer with the number of monitored methods.
   */
  public int getMethodSize() {
    return methods.size();
  }
  
  /**
   * Gets the number of classes which will be monitored.
   * 
   * @return An integer with the number of monitored classes.
   */
  public int getClassSize() {
    return classes.size();
  }
  
  /**
   * Gets the number of packages which will be monitored.
   * 
   * @return An integer with the number of monitored packages.
   */
  public int getPackageSize() {
    return packages.size();
  }
  
  /**
   * Gets the number of lines which will be monitored.
   * 
   * @return An integer with the number of monitored lines.
   */
  public int getLineSize() {
    return totalLineCount;
  }
  
  /**
   * Gets the number of lines which will be monitored for a specific method.
   * 
   * <p>This method is for testing only
   * 
   * @return An integer with the number of monitored lines .
   */
  int getNumberOfLinesForFile(String fileName) {
    return instrumentedLines.get(fileName).size();
  }

  /**
   * Gets the number of source files which will be monitored.
   * 
   * @return An integer with the number of monitored source files.
   */
  public int getSourceFileCount() {
    return files.size();
  }
  
  /**
   * Returns the line numbers of lines instrumented in the given file.
   * 
   * @return a collection of instrumented line for the given file.
   */
  public List<Integer> getInstrumentedLines(String filename) {
    return instrumentedLines.get(filename);
  }
  
  /**
   * Includes the specified file on the coverage collection.
   * 
   * @param filename The file to include.
   * @throws NullPointerException If the file name is <code>null</code>.
   */
  public int includeFile(String filename) {
    // Precondition. 
    if (filename == null) {
      throw new IllegalArgumentException("File name cannot be null.");
    }
    
    // Computation.
    if (!files.contains(filename)) {
      int index = files.size();
      files.add(filename);
      return index;
    } else {
      return files.indexOf(filename);
    }
    
  }
  
  /**
   * Includes the specified package on the coverage collection.
   * 
   * @param pkgname The package to include.
   * @throws NullPointerException If the package name is <code>null</code>.
   */
  public void includePackage(String pkgname) {
    // Precondition. 
    if (pkgname == null) {
      throw new IllegalArgumentException("Method name cannot be null.");
    }
    
    // Computation.
    if (!packages.contains(pkgname)) {
      packages.add(pkgname);
    }
  }
  
  /**
   * Includes the specified class on the coverage collection.
   * 
   * @param className The class to include. The class name must be a full name 
   *    including package using '/' as a separator.
   * @throws NullPointerException If the class name is <code>null</code>.
   */
  public void includeClass(String className) {
    // Precondition. 
    if (className == null) {
      throw new IllegalArgumentException("Class name cannot be null.");
    }
    
    // Computation
    int slash = className.lastIndexOf('/');
    if (slash < 0) {
      // Default package
      includePackage("");
    } else {
      String pkg = className.substring(0, className.lastIndexOf('/'));
      includePackage(pkg);
    }
    
    // Internal check.
    if (!classes.contains(className)) {
      classes.add(className);
    } else {
      throw new IllegalStateException("Class " + className + " was already " +
          "mapped for coverage. Maybe the same class has been processed twice.");
    }
  }
  
  /**
   * Writes on the selected {@link PrintWriter} on a different line, the name 
   * of the method and the index comma-separated.
   * 
   * <p> The writer it is NOT closed at the end.
   *
   * <p> a line has the format
   *
   * <pre>
   * methodName numberOfLinesForMethod  methodIndex     methodStartLine
   * </pre>
   * 
   * @param w The {@link PrintWriter} on which to write
   */
  
  public void writeMappedMethods(PrintWriter w) {
    String id;
    for (int i = 0; i < methods.size(); i++) {
      id = methods.get(i);
      w.println(id + SEPARATOR + i);
    } 
  }
  
  /**
   * Writes on the selected {@link PrintWriter} on a different line using '/' 
   * as a separator.
   * 
   * <p> The writer it is NOT closed at the end.
   * 
   * @param w The {@link PrintWriter} on which to write.
   */
  public void writeMappedPackages(PrintWriter w) {
    for (String s : packages) {
      w.println(s);
    } 
  }

  /**
   * Writes all the line numbers of lines instrumented in the following format:
   * <pre>
   * index instrumentedLinesCount filename listOfLines
   * </pre>
   * one file per line, in which all the entries are separated by 
   * {@link #SEPARATOR}.
   *
   * <p> The writer it is NOT closed at the end.
   * 
   * @param pw The {@link PrintWriter} on which to write.
   */
  public void writeInstrumentedLines(PrintWriter pw) {
    pw.println(COMMENT + "---------------------------------------------------");
    pw.println(COMMENT + "Line coverage instrumentation report.");
    pw.println(COMMENT + "");
    pw.println(COMMENT + "The file is written in the following format:");
    pw.println(COMMENT + "index" + SEPARATOR + "instrumentedLinesCount" + 
        SEPARATOR + "filename" + SEPARATOR + "listOfLines");
    pw.println(COMMENT + "---------------------------------------------------");
    for (String filename : instrumentedLines.keySet()) {
      int fileIndex = files.indexOf(filename);
      List<Integer> lines = instrumentedLines.get(filename);
      pw.println(Integer.toString(fileIndex) + SEPARATOR + 
          Integer.toString(lines.size()) + SEPARATOR + filename + 
          SEPARATOR + Join.join(Character.toString(SEPARATOR), lines));
    }
  }
  
  /**
   * Writes summary information in emma-style on the selected 
   * {@link PrintWriter}. This file will be used at runtime to generate the 
   * emma-styled coverage report. It contains statistical information on the 
   * number of packages, classes, methods, files and lines of code.
   * 
   * <p> The writer it is NOT closed at the end.
   * 
   * @param pw The writer with which to write.
   */
  public void writeSummary(PrintWriter pw){
    pw.write("OVERALL STATS SUMMARY:\n\n" +
        "total packages: " + packages.size() + "\n" +
        "total classes:  " + classes.size() + "\n" +
        "total methods:  " + methods.size() + "\n" +
        "total executable files: " + files.size() + "\n" +
        "total executable lines: " + totalLineCount + "\n");
  }
  
  /**
   * Writes a full report.
   * 
   * <p> This is a convenience method to invoke all the other write methods 
   * on a specified directory using the default filenames.
   *
   * @param folder The directory on which to save the output.
   * @param runId The id used to make the filenames unique for each run
   * @param jar The output jar to write the files to
   * 
   * @throws IOException If an error occurs while writing.
   */
  public void generateOutput(String folder, String runId, 
      InstrumentedJarCreator jar) throws IOException {
    PrintWriter printWriter = null;
    
    // TODO: prepend with runId instead of putting it after the extension.
    final File summaryFile = new File(folder, FILENAME_SUMMARY + runId);
    
    final File methodDataFile = new File(folder, FILENAME_METHOD + runId);
    
    
    final File linesInstrumentedDataFile =
        new File(folder, FILENAME_INSTRUMENTED_LINES + runId);
    
    final File packagesDataFile = new File(folder, FILENAME_PACKAGE + runId);
    
    // Writing summary
    try {
      printWriter = new PrintWriter(summaryFile);
      writeSummary(printWriter);
    } finally {
      Closeables.closeQuietly(printWriter);
    }
    
    // Writing method coverage
    try {
      printWriter = new PrintWriter(methodDataFile);
      writeMappedMethods(printWriter);
    } finally {
      Closeables.closeQuietly(printWriter);
    }
    
    // Writing package coverage
    try {
      printWriter = new PrintWriter(packagesDataFile);
      writeMappedPackages(printWriter);
    } finally {
      Closeables.closeQuietly(printWriter);
    }

    // Writing instrumented line information
    try {
      printWriter = new PrintWriter(linesInstrumentedDataFile);
      writeInstrumentedLines(printWriter);
    } finally {
      Closeables.closeQuietly(printWriter);
    }
    
    jar.addFile(methodDataFile, Files.toByteArray(methodDataFile));
    jar.addFile(linesInstrumentedDataFile, 
        Files.toByteArray(linesInstrumentedDataFile));
  }
}
