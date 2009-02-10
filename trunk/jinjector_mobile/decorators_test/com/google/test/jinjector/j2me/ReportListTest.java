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

package com.google.test.jinjector.j2me;

import j2meunit.framework.Test;
import j2meunit.framework.TestCase;
import j2meunit.framework.TestMethod;
import j2meunit.framework.TestSuite;

/**
 * Tests for {@link ReportList}.
 * 
 * @author Michele Sama
 */
public class ReportListTest extends TestCase {

  ReportList reportList;
  
  /**
   * Constructor from superclass.
   */
  public ReportListTest() {
  }

  /**
   * Constructor from superclass.
   * 
   * @param name the test's name.
   */
  public ReportListTest(String name) {
    super(name);
  }

  /**
   * Constructor from superclass.
   * 
   * @param name the test's name.
   * @param method the test's method.
   */
  public ReportListTest(String name, TestMethod method) {
    super(name, method);
  }

  /**
   * Creates a new empty report list.
   * 
   * @see j2meunit.framework.TestCase#setUp()
   */
  protected void setUp() throws Exception {
    super.setUp();
    reportList = new ReportList(null);
  }

  /**
   * Tests {@link ReportList#createNewReportContainer(String, String)}. 
   */
  public void testCreateNewReportContainer() {
    String message = "Error";
    String description = "description";
    reportList.addError(message, description);
    assertEquals("The report list does not contains the added error.",
        1, reportList.getReports().size());
    
    ReportList.ReportContainer container = (ReportList.ReportContainer) 
        reportList.getReports().get(new Integer(0));
    assertEquals("Wrong error message", container.message, message);
    assertEquals("Wrong error description",
        container.detailedDescription, description);
  }
  
  /**
   * Tests {@link ReportList#addError(String, String)}. 
   */
  public void testAddError() {
    String message = "Error";
    String description = "description";
    reportList.addError(message, description);
    assertEquals("The report list does not contains the added error.",
        1, reportList.getReports().size());
  }
  
  /**
   * Tests {@link ReportList#addFault(String, String)}. 
   */
  public void testAddFailure() {
    String message = "Error";
    String description = "description";
    reportList.addFault(message, description);
    assertEquals("The report list does not contains the added error.",
        1, reportList.getReports().size());
  }
  
  /**
   * Tests {@link ReportList#addSuccess(String, String)}. 
   */
  public void testAddSuccess() {
    String message = "Error";
    String description = "description";
    reportList.addSuccess(message, description);
    assertEquals("The report list does not contains the added error.",
        1, reportList.getReports().size());
  }
  
  /**
   * @see j2meunit.framework.TestCase#suite()
   */
  public Test suite() {
    TestSuite suite = new TestSuite();

    suite.addTest(new J2meTestCaseTest("testAddError") {
      public void runTest() {
        testAddError();
      }
    });
    
    suite.addTest(new J2meTestCaseTest("testAddFailure") {
      public void runTest() {
        testAddFailure();
      }
    });
    
    suite.addTest(new J2meTestCaseTest("testAddSuccess") {
      public void runTest() {
        testAddSuccess();
      }
    });
    
    suite.addTest(new J2meTestCaseTest("testCreateNewReportContainer") {
      public void runTest() {
        testCreateNewReportContainer();
      }
    });
    

    return suite;
  }
  
}
