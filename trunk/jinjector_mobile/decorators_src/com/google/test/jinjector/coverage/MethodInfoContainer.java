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

import com.google.test.jinjector.util.LineReader;

import java.io.*;
import java.util.*;

/**
 * Contains a collection of {@link MethodInfo}s read from a given file.
 * 
 * <p>Internally an instance of {@link LineReader} reads the file line by 
 * line, an instance of {@link LineInfoParser} parses each line and stores 
 * the information into a new instance of {@link MethodInfo}.
 * 
 * TODO: this class should read libe by line and print the result directly 
 *     storing only statistic information. 
 *
 * @author Michele Sama
 * @author Olivier Gaillard
 */
public class MethodInfoContainer {
 
  public static final String FILENAME_METHOD = "methodCoverage.txt";

  // Map of method's name to MethodInfo.
  private static Hashtable methodInfo = new Hashtable();

  /**
   * Gets the information for the given method.
   */
  public MethodInfo getMethodInfo(String method) {
    return (MethodInfo) methodInfo.get(method);
  }

  /**
   * Returns all the names of the instrumented methods.
   */
  public Enumeration getMethodNames() {
    return methodInfo.keys();
  }

  public int size() {
    return methodInfo.size();
  }

  /**
   * Reads the file containing method information if necessary.
   * 
   * <p>If the file has previously been loaded the method returns without 
   * doing anything.
   *
   * @param runId id of the file to read
   */
  public void loadFile(String runId) {
    if (methodInfo.size() != 0) {
      return;
    }

    String filename = "/" + FILENAME_METHOD + runId;
    LineReader lr = null;
    try {
      lr = new LineReader( 
          CoverageManager.class.getResourceAsStream(filename));

      String line;
      /**
       * For each line in the file write a line in the output file and update 
       * the internal table.
       * */
      while ((line = lr.readline()) != null) {
        /**
         * Parse the full method name
         * */
        LineInfoParser parser = new LineInfoParser(line);
        addMethodInfo(parser.info);
      }

      lr.close();
      lr = null;

    } catch (IOException e) {
      // Not safe to use Log here. Look at CoverageManager#getInstance() for more details.
      throw new RuntimeException(
          "Cannot open method info containing data needed to gather coverage information '" +
          filename + "'");

    } finally {
      // Not safe to use IoUtil here. Look at CoverageManager#getInstance() for
      // more details.
      if (lr != null) {
        try {
          lr.close();
        } catch (IOException e) {
          throw new RuntimeException(
              "Caught IOException while closing reader " + e.getMessage());
        }
      }
    }
  }

  /**
   * Adds a method info to the internal collection.
   * 
   * <p>This method is visible for testing purposes only to manually specify 
   * which methods are contained. A client should never invoke this method 
   * directly because it will be automatically invoked by 
   * {@link #loadFile(String)}.
   */
  void addMethodInfo(MethodInfo info) {
    methodInfo.put(info.getFullname(), info);
  }

  /**
   * Stores information about a method.
   */
  public static class MethodInfo {
    String className;
    String packageName;
    // Full class name and method description.
    String fullName;

    // Index of the method used to look for the coverage data.
    int methodIndex = 0;


    public String getFullname() {
      return fullName;
    }

    public int getMethodIndex() {
      return methodIndex;
    }

    public String getPackage() {
      return packageName;
    }

    public String getClassname() {
      return className;
    }
  }

  /**
   * Parses method's name and index from a line read in the method's list file.
   * 
   * <p>Parsed strings contains the method full name containing accepted 
   * parameters and the index which has been assigned to the method, separated 
   * by {@link MethodInfoContainer.LineInfoParser#SEPARATOR} (e.g. 
   * <code>org/Bar.Foo$1.doFoo(II)V 123</code>) 
   * 
   * @author Michele Sama
   */
  static class LineInfoParser {
    public static final char SEPARATOR = '\t';
    
    private MethodInfo info = new MethodInfo();

    public LineInfoParser(String line) {
      info.fullName = parseNameFromMethodMap(line);
      info.methodIndex = parseIndexFromMethodMap(line);

      // Look for package and class names.
      int dot = info.fullName.indexOf('.');
      int slash = info.fullName.lastIndexOf('/', dot + 1);
      info.packageName = info.fullName.substring(0, slash);        
      info.className = info.fullName.substring(slash + 1, dot);
    }

    /**
     * Gets the full method name starting from the line containing method 
     * coverage.
     * 
     * @param line The line to parse.
     * @return The method's name.
     */
    private String parseNameFromMethodMap(String line) {
      return line.substring(0, line.indexOf(SEPARATOR));
    }

    /**
     * Gets the method index starting from the line containing method 
     * coverage.
     * 
     * @param line The line to parse.
     * @return The method's index.
     */
    private int parseIndexFromMethodMap(String line) {
      return Integer.parseInt(
          line.substring(line.lastIndexOf(SEPARATOR) + 1));
    }
  }
}
