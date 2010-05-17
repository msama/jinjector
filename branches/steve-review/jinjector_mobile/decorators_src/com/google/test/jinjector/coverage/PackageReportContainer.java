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

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Container class used to store information related to the package breakdown 
 * of the coverage which is going to be used for statistical purposes.
 * It contains a list of covered class, and a list of all the known classes
 * 
 * @author Michele Sama
 *
 */
class PackageReportContainer {
  private Hashtable classes = new Hashtable();
  private String pkg = null;
  private int allClass = 0;
  private int coveredClass = 0;
  private int allMethods = 0;
  private int coveredMethods = 0;
  private int allLines = 0;
  private int coveredLines = 0;  
  
  /**
   * Represent a constant <code>true</code>.
   * 
   * <p>Added to prevent the usage of Boolean.TRUE in order to be complaint 
   * with CLDC 1.0.
   * 
   * TODO: remove this as soon as all the clients will be CLDC 1.1
   */
  private final Boolean TRUE = new Boolean(true);
  
  /**
   * Represent a constant <code>false</code>.
   * 
   * <p>Added to prevent the usage of Boolean.FALSE in order to be complaint 
   * with CLDC 1.0.
   */
  private final Boolean FALSE = new Boolean(false);
  
  /**
   * Creates the report breakdown for a specified package.
   * 
   * @param pkg The package to which this report is related.
   */
  public PackageReportContainer(String pkg) {
    this.pkg = pkg;
  } 
  
  /**
   * Marks classes containing covered methods as covered. 
   * 
   * <p>The produced reports will only contain contain the percentage of 
   * covered methods and covered classes per package. 
   * 
   * @param clazz the class' name of the class containing the method which is 
   *     being explored.
   * @param covered <code>true</code> if the method has been covered.
   */
  public void addMethodFromClass(String clazz, boolean covered) {
    boolean contained = classes.containsKey(clazz);
    if (!contained) {
      allClass++;
      classes.put(clazz, covered ? TRUE : FALSE);
    }
    /* If this method is being flagged as covered, and its containing class was 
     * not flagged as covered before then add the class to the covered class 
     * count. 
     */
    if (covered && 
        (!contained || !((Boolean) classes.get(clazz)).booleanValue())) {
      coveredClass++;
      classes.put(clazz, TRUE);
    }
    
    allMethods++;
    if (covered) {
      coveredMethods++;
    }
  }

  /**
   * Increase the line coverage of the specified amount.
   * 
   * @param covered The covered amount of line.
   * @param total The total amount of lines.
   */
  public void addMethodLinesCoverage(int covered, int total) {
    //preconditions
    if (covered < 0 || total < 0 || covered > total) {
      throw new IllegalArgumentException("Invalid coverage parameters.");
    }
    //computation
    allLines += total;
    coveredLines += covered;
  }
  
  /**
   * @return the pkg
   */
  public String getPkg() {
    return pkg;
  }

  /**
   * @return the allClass
   */
  public int getAllClasses() {
    return allClass;
  }

  /**
   * @return the coveredClass
   */
  public int getCoveredClasses() {
    return coveredClass;
  }

  /**
   * @return the allMethods
   */
  public int getAllMethods() {
    return allMethods;
  }

  /**
   * @return the coveredMethods
   */
  public int getCoveredMethods() {
    return coveredMethods;
  }

  /**
   * @return the allLines
   */
  public int getAllLines() {
    return allLines;
  }

  /**
   * @return the coveredLines
   */
  public int getCoveredLines() {
    return coveredLines;
  }
  
  /**
   * Includes a report into another report.
   * 
   * @param report The report to include.
   */
  public void includeReport(PackageReportContainer report) {
    
    Enumeration en = classes.keys();
    while (en.hasMoreElements()) {
      String key = (String) en.nextElement(); 
      classes.put(key, report.classes.get(key));
    }
    allClass  += report.allClass;
    coveredClass += report.coveredClass;
    
    allMethods += report.allMethods;
    coveredMethods += report.coveredMethods;
    
    allLines += report.allLines;
    coveredLines += report.coveredLines;
  }
  
}
