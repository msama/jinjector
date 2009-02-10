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

import j2meunit.framework.Test;
import j2meunit.framework.TestCase;
import j2meunit.framework.TestMethod;
import j2meunit.framework.TestSuite;

/**
 * JUnit tests for @link {@link PackageReportContainer}
 * 
 * @author Michele Sama
 */
public class PackageReportContainerTest extends TestCase {

  private static final int SMALL = 1;
  private static final int BIG = 100;
  private static final int NEGATIVE = -1;
  
  private PackageReportContainer report = null;
  
  /**
   * Constructor from super class.
   */
  public PackageReportContainerTest() {
  }

  /**
   * Constructor from super class.
   */
  public PackageReportContainerTest(String name) {
    super(name);
  }

  /**
   * Constructor from super class.
   */
  public PackageReportContainerTest(String name, TestMethod test) {
    super(name, test);
  }
  
  /**
   * Initializes all the required instances.
   * 
   * @see j2meunit.framework.TestCase#setUp()
   */
  protected void setUp() throws Exception {
    super.setUp();
    report = new PackageReportContainer("org/foo");
  }
  
  /**
   * Try to add a NEGATIVE value for covered lines and checks that it would 
   * be discarded.
   * 
   * @see PackageReportContainer#addMethodLinesCoverage(int, int) 
   */
  public void testLinesCoverageNegativeCoverage() {
       
    try {
      report.addMethodLinesCoverage(NEGATIVE, SMALL);
      fail("Adding coveredLines < 0 should have thrown an exception.");
    } catch (IllegalArgumentException e) {
      // ok!
      assertEquals("Wrong covered lines.", 0, report.getCoveredLines());
      assertEquals("Wrong total lines.", 0, report.getAllLines());
    }
  }
  
  /**
   * Try to add a NEGATIVE value for total lines and checks that it would 
   * be discarded.
   * 
   * @see PackageReportContainer#addMethodLinesCoverage(int, int) 
   */
  public void testLinesCoverageNegativeTotal() {    
    try {
      report.addMethodLinesCoverage(SMALL, NEGATIVE);
      fail("Adding totalLines < 0 should have thrown an exception.");
    } catch (IllegalArgumentException e) {
      // ok!
      assertEquals("Wrong covered lines.", 0, report.getCoveredLines());
      assertEquals("Wrong total lines.", 0, report.getAllLines());
    }
  }

  /**
   * Try to cover more lines than the toal amount and checks that it would 
   * be discarded.
   * 
   *  @see PackageReportContainer#addMethodLinesCoverage(int, int) 
   */
  public void testLinesCoverageSmallerTotal() { 
    try {
      report.addMethodLinesCoverage(BIG, SMALL);
      fail("Adding coveredLines < totalLines should have thrown an exception.");
    } catch (IllegalArgumentException e) {
      // ok!
      assertEquals("Wrong covered lines.", 0, report.getCoveredLines());
      assertEquals("Wrong total lines.", 0, report.getAllLines());
    }
  }
  
  /**
   * Tests if the parameters are checked correctly in the report class.
   * Also verifies that the internal value would be correct.
   * 
   * @see PackageReportContainer#addMethodLinesCoverage(int, int) 
   */
  public void testLinesCoverage() {   
    try {
      report.addMethodLinesCoverage(SMALL, BIG);
      assertEquals("Wrong covered lines.", SMALL, report.getCoveredLines());
      assertEquals("Wrong total lines.", BIG, report.getAllLines());
    } catch (IllegalArgumentException e) {
      fail("Exception while counting lines.");
    }   
  }
  
  /**
   * <p>
   * Tests the method for storing method coverage informations. Verifies:
   * <ul>
   * <li>That uncovered method will be counted correctly.
   * <li>That all the methods will be counted.
   * <li>That covered classes will be counted correctly and only once.
   * <li>That all the classes will be counted but only once.
   * </ul>
   * </p>
   */
  public void testAddMethodFromClass() {
    int max = 50;
    int allClasses = 0;
    int coveredClasses = 0;
    int allMethods = 0;
    int coveredMethods = 0;
    String className = "Foo";
    
    for (int i = 0; i < max; i++) {
      report.addMethodFromClass(className, false);
      assertEquals("Wrong class count.", 1, report.getAllClasses());
      assertEquals("Wrong class coverage.", 0, report.getCoveredClasses());
      assertEquals("Wrong methods count.", i + 1, report.getAllMethods());
      assertEquals("Wrong method coverage.", 0, report.getCoveredMethods());
    }
    
    allClasses = report.getAllClasses();
    coveredClasses = report.getCoveredClasses();
    allMethods = report.getAllMethods();
    coveredMethods = report.getCoveredMethods();
    className = "Foo2";
    
    for (int i = 0; i < max; i++) {
      report.addMethodFromClass(className, true);
      assertEquals("Wrong class count.", allClasses + 1, 
          report.getAllClasses());
      assertEquals("Wrong class coverage.", coveredClasses + 1, 
          report.getCoveredClasses());
      assertEquals("Wrong methods count.", allMethods + i + 1, 
          report.getAllMethods());
      assertEquals("Wrong method coverage.", coveredMethods + i + 1, 
          report.getCoveredMethods());
    }
    
    allClasses = report.getAllClasses();
    coveredClasses = report.getCoveredClasses();
    allMethods = report.getAllMethods();
    coveredMethods = report.getCoveredMethods();
    
    for (int i = 0; i < max; i++) {
      className = "Class" + i;
      report.addMethodFromClass(className, true);
      assertEquals("Wrong class count.", allClasses + i + 1, 
          report.getAllClasses());
      assertEquals("Wrong class coverage.", coveredClasses + i + 1, 
          report.getCoveredClasses());
      assertEquals("Wrong methods count.", allMethods + i + 1, 
          report.getAllMethods());
      assertEquals("Wrong method coverage.", coveredMethods + i + 1, 
          report.getCoveredMethods());
    }
  }
  
  /**
   * @see j2meunit.framework.TestCase#suite()
   */
  public Test suite() {
    TestSuite suite = new TestSuite();

    suite.addTest(new PackageReportContainerTest("testLinesCoverage") {
      public void runTest() {
        testLinesCoverage();
      }
    });
    
    suite.addTest(new PackageReportContainerTest(
        "testLinesCoverageSmallerTotal") {
      public void runTest() {
        testLinesCoverageSmallerTotal();
      }
    });
    
    suite.addTest(new PackageReportContainerTest(
        "testLinesCoverageNegativeTotal") {
      public void runTest() {
        testLinesCoverageNegativeTotal();
      }
    });
    
    suite.addTest(new PackageReportContainerTest(
        "testLinesCoverageNegativeCoverage") {
      public void runTest() {
        testLinesCoverageNegativeCoverage();
      }
    });
    
    suite.addTest(new PackageReportContainerTest("testAddMethodFromClass") {
      public void runTest() {
        testAddMethodFromClass();
      }
    });

    return suite;
  }
  
}
