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

import com.google.devtools.build.wireless.testing.java.injector.util.StringUtil;

import junit.framework.TestCase;

import java.io.*;
import java.util.*;

/**
 * JUnit test file for {@link CoverageStatisticContainer}.
 * 
 * @author Michele Sama
 */
public class CoverageStatisticContainerTest extends TestCase {

  /**
   * Defines the number of entry to try when adding objects.
   */
  private static final int MAX = 50;
  
  /**
   * The container under test.
   */
  protected CoverageStatisticContainer statisticContainer = null;
  
  /**
   * Creates a brand new instance of statistic container.
   * 
   * @throws java.lang.Exception
   */
  @Override
  public void setUp() throws Exception {
    statisticContainer = new CoverageStatisticContainer();
  }

  /**
   * Resets the instance.
   * 
   * @throws java.lang.Exception
   */
  @Override
  public void tearDown() throws Exception {
    statisticContainer = null;
  }

  /**
   * Tests 
   * {@link CoverageStatisticContainer#includeMethod(String)}.
   * 
   * <p>This tests verify that all the inserted methods are mapped and that no 
   * duplicated entry would be stored.
   */
  public void testIncludeMethod() { 
    // Adds different methods.
    for (int i = 0; i < MAX; i++) {
      statisticContainer.includeMethod("dummyMethod" + i);
      assertEquals("Insert method", i + 1, 
          statisticContainer.getMethodSize());
    }
    /**
     * Verifies that multiple methods cannot be added.
     * */
    String name = "duplicatedMethod";
    statisticContainer.includeMethod(name);
    try {
      statisticContainer.includeMethod(name);
      fail("Duplicated method added.");
    } catch (IllegalArgumentException e) {
      // This is the expected behavior
    }
  }

  /**
   * Tests 
   * {@link CoverageStatisticContainer#addInstrumentedLineAndGetLineIndex(String, int)}
   * with a <code>null</code> classname.
   */
  public void tincludeClassLines_nullClassName() {
    try {
      //statisticContainer.includeFileLines(null, 14);
      statisticContainer.addInstrumentedLineAndGetLineIndex(null, 14);
      fail("Adding a null class should throw an exception.");
    } catch (IllegalArgumentException e) {
      // ok
    }
  }
   
  /**
   * Tests 
   * {@link CoverageStatisticContainer#addInstrumentedLineAndGetLineIndex(String, int)}
   * with a negative line number.
   */
  public void includeClassLines_negativeLine() {
    String name = "foo/Bar";
    statisticContainer.includeFile(name);
    try {
      statisticContainer.addInstrumentedLineAndGetLineIndex(name, -14);
      fail("Adding a negative line number should throw an exception.");
    } catch (IllegalArgumentException e) {
      // ok
    }
  }
    
  /**
   * Tests the integrity of
   * {@link CoverageStatisticContainer#addInstrumentedLineAndGetLineIndex(String, int)}
   * by adding fixed number of different lines and by verifying that exactly 
   * the same number has been included in the instrumentation.
   */
  public void includeClassLines_integrity() {  
    String name = "foo/Bar";
    statisticContainer.includeFile(name);
    int lines = 12;
    
    for (int i = 0; i < lines; i++) {
      statisticContainer.addInstrumentedLineAndGetLineIndex(name, i);
    }
    assertEquals("The line number is not correct.",
        lines, statisticContainer.getLineSize());
    assertEquals("The line number does not match with the number of lines " +
    	"for each file.", statisticContainer.getLineSize(), 
        statisticContainer.getNumberOfLinesForFile(name));
  }
 
  /**
   * Tests the integrity of
   * {@link CoverageStatisticContainer#addInstrumentedLineAndGetLineIndex(String, int)}
   * by adding twice the same line.
   */
  public void includeClassLines_doubleInclusion() {  
    String name = "foo/Bar";
    statisticContainer.includeFile(name);
    int line = 12345;
    
    statisticContainer.addInstrumentedLineAndGetLineIndex(name, line);
    try {
      statisticContainer.addInstrumentedLineAndGetLineIndex(name, line);
      fail("A double inclusion of the same line for the same file " +
          "should have failed!");
    } catch(IllegalStateException ex) {
      // Ok
    }
  }
  
  /**
   * Tests
   * {@link CoverageStatisticContainer#addInstrumentedLineAndGetLineIndex(String, int)}
   * by adding lines to an unknown class.
   */
  public void includeClassLines_unknownClass() {  
    try {
      statisticContainer.addInstrumentedLineAndGetLineIndex("foo1234", 1234);
      fail("Adding lines for a file which is not known should throw an exception");
    } catch (RuntimeException e) {
      // ok!
    }
  }
   
  /**
   * Tests
   * {@link CoverageStatisticContainer#addInstrumentedLineAndGetLineIndex(String, int)}
   * by adding lines to multiple classes.
   */
  public void includeClassLines_multipleClasses() {  
    int lines0 = 234;
    int lines1 = 567;
    String file0 = "Bar";
    String file1 = "Foo";
    statisticContainer.includeFile(file0);
    statisticContainer.includeFile(file1);
    
    for (int i = 0; i < lines0; i++) {
      statisticContainer.addInstrumentedLineAndGetLineIndex(file0, i);
    }
    for (int i = 0; i < lines1; i++) {
      statisticContainer.addInstrumentedLineAndGetLineIndex(file1, i);
    }


    assertEquals("Wrong stored line number for class.", lines0, 
        statisticContainer.getNumberOfLinesForFile(file0));
    assertEquals("Wrong stored line number for class.", lines1, 
        statisticContainer.getNumberOfLinesForFile(file1));
    
    assertEquals("The total line number should be the sum of the lines " +
        "instrumented in each file.", lines0 + lines1, 
        statisticContainer.getLineSize());
  }
  
  /**
   * Tests 
   * {@link CoverageStatisticContainer#includeFile(String)}.
   * 
   * <p> Verifies:
   * <ul>
   * <li>that no null entry will be added.
   * <li>that duplicated entry will be skipped.
   * <li>that files with the same name but in different packages will be added.
   * </ul>
   */
  public void testIncludeFile() {
    try {
      statisticContainer.includeFile(null);
      fail("Null file name should have raised an exception!");
    } catch (IllegalArgumentException e) {
      // Ok!
    }
    
    String name = "Foo.class";
    statisticContainer.includeFile(name);
    assertEquals("A valid source file has not been added.", 1, 
        statisticContainer.getSourceFileCount());
    statisticContainer.includeFile(name);
    assertEquals("The same file has been added twice.", 1, 
        statisticContainer.getSourceFileCount());
    
    int count = statisticContainer.getSourceFileCount();
    for (int i = 0; i < MAX; i++) {
      name = "org/" + i + "Foo.class";
      statisticContainer.includeFile(name);
      count++;
      assertEquals("Package issues in adding file.", count, 
          statisticContainer.getSourceFileCount());
    }
  }

  /**
   * Tests 
   * {@link CoverageStatisticContainer#includePackage(String)}.
   * 
   * <p> This method verifies:
   * <ul>
   * <li>that a null package cannot be added.
   * <li>that valid packaged will be added.
   * <li>that nested packages with the same nema will be added.
   * </ul>
   */
  public void testIncludePackage() {
    try {
      statisticContainer.includePackage(null);
      fail("Null package name should have raised an exception!");
    } catch (IllegalArgumentException e) {
      // Ok!
    }

    
    String name = "org/foo";
    statisticContainer.includePackage(name);
    assertEquals("Valid package has not been added.", 1, 
        statisticContainer.getPackageSize());
    statisticContainer.includePackage(name);
    assertEquals("Package has been added twice.", 1, 
        statisticContainer.getPackageSize());
    
    int count = statisticContainer.getPackageSize();
    for (int i = 0; i < MAX; i++) {
      name = "org/" + i + "/Foo";
      statisticContainer.includePackage(name);
      count++;
      assertEquals("A set of nested packages has not be counted correctly.",
          count, statisticContainer.getPackageSize());
    }
  }

  /**
   * Tests  
   * {@link CoverageStatisticContainer#includeClass(String)}.
   */
  public void testIncludeClass() {
    try {
      statisticContainer.includeClass(null);
      fail("Null class name should have raised an exception!");
    } catch (IllegalArgumentException e) {
      // Ok!
    }
    
    String name = "Class";
    statisticContainer.includeClass(name);
    assertEquals("A valid class has not been added.", 1, 
        statisticContainer.getClassSize());
    assertEquals("A valid package has not been added.", 1, 
        statisticContainer.getPackageSize());
    try {
      statisticContainer.includeClass(name);
      fail("A class visited twice should have thrown an exception!");
    } catch (IllegalStateException ex) {
      //Ok
    }
    assertEquals("Only one instance of a package should be added when we try" +
    	"to add it more than once.", 1, 
        statisticContainer.getPackageSize());
    
    name = "Class1";
    statisticContainer.includeClass(name);
    assertEquals("Two classes should exist in the statistic container.", 2, 
        statisticContainer.getClassSize());
    assertEquals("The same package added twice.", 1, 
        statisticContainer.getPackageSize());
    
    name = "foo/Class";
    statisticContainer.includeClass(name);
    assertEquals("Three classes should exist in the statistic container.", 3, 
        statisticContainer.getClassSize());
    assertEquals("Two packages should exist in the statistic container.", 2, 
        statisticContainer.getPackageSize());
  }

  /**
   * Tests  
   * {@link CoverageStatisticContainer#writeMappedMethods(PrintWriter)}.
   * 
   * <p> Adds a set of methods to the container, and uses the name's length as a 
   * line number. The writes all the methods and verifies that line number and 
   * indexes will be correct.
   * 
   * @throws IOException If an exception occurs while writing the methods.
   */
  @SuppressWarnings("unchecked")
  public void testWriteMappedMethods() throws IOException {
    List<String> methods = new ArrayList<String>();
    
    methods.add("A.a()V");
    methods.add("org/A.a(I)V");
    methods.add("org/foo/A.b()Z");
    methods.add("org/foo/foo/A.c(LA;)V");
    methods.add("gor/A.b(ZIII)L");
    methods.add("gor/oof/A.a()V");
    methods.add("gor/oof/oof/A.b()D");
    for (String s : methods) {
      statisticContainer.includeMethod(s);
    }
    List<String> methodsClone = new ArrayList<String>(methods);
    
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    
    statisticContainer.writeMappedMethods(printWriter);
    System.out.println(stringWriter.toString());
    BufferedReader br = new BufferedReader(
        new StringReader(stringWriter.toString()));
    String line = null;
    
    while ((line = br.readLine()) != null) {
      StringTokenizer tokenizer = new StringTokenizer(line);
      assertEquals("Line composition", 2, tokenizer.countTokens());
      String name = tokenizer.nextToken();
      int index = Integer.parseInt(tokenizer.nextToken());
      
      assertTrue("Written method is in list", methods.contains(name));
      assertEquals("Method index", methods.indexOf(name), index);
      methodsClone.remove(name);
    }
    assertTrue("All the methods must be logged", methodsClone.isEmpty());
    br.close();
    printWriter.close();
  }

  /**
   * Tests 
   * {@link CoverageStatisticContainer#writeInstrumentedLines(PrintWriter)}.
   * 
   * <p>Adds a known set of classes to the container then writes the classes
   * and checks that only and all the added classes have been written.
   * 
   * @throws IOException If an error occurs in writing the class list.
   */
  public void testWriteMappedClasses() throws IOException {
    List<String> files = new ArrayList<String>();
    files.add("A");
    files.add("org/A");
    files.add("org/foo/A");
    files.add("org/foo/foo/A");
    files.add("gor/A");
    files.add("gor/oof/A");
    files.add("gor/oof/oof/A");
    for (String s : files) {
      statisticContainer.includeFile(s);
      // We will only add one line.
      statisticContainer.addInstrumentedLineAndGetLineIndex(s, 0);
    }
    List<String> filesClone = new ArrayList<String>(files);
    
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    
    statisticContainer.writeInstrumentedLines(printWriter);
    BufferedReader br = new BufferedReader(
        new StringReader(stringWriter.toString()));
    String line = null;
    
    while ((line = br.readLine()) != null) {
      // skips comments
      if (line.startsWith("#")) {
        System.out.println(line);
        continue;
      }
      String[] tokens = StringUtil.split(line, "\t");
      
      assertEquals("Wrong format in read line: " + line + 
          ". Expected: index\tinstrumentedLinesCount\tfilename\t[list of lines]", 4, tokens.length);
      
      int index = Integer.parseInt(tokens[0]);
      int lines = Integer.parseInt(tokens[1]);
      String fileName = tokens[2];
      
      // Index
      assertEquals("Wrong index.", files.indexOf(fileName), index);
     
      // We only added one line
      assertEquals("Only one line should have been added.", 1, lines);
       
      assertTrue("Written class should be in list: " + fileName,
          files.contains(fileName));
      
      filesClone.remove(fileName);
    }
    assertTrue("All the package must be logged", filesClone.isEmpty());
    br.close();
    printWriter.close();
  }

  /**
   * Tests 
   * {@link CoverageStatisticContainer#writeMappedPackages(PrintWriter)}.
   * 
   * <p> Adds a list of packages to the container and write them. The test 
   * collects the produced output and verify that only and all the added 
   * test are on the list.
   * 
   * @throws IOException If an exception occurs in the container under test 
   *    while writing mapped packages.
   */
  public void testWriteMappedPackages() throws IOException {
    List<String> packages = new ArrayList<String>();
    packages.add("");
    packages.add("org");
    packages.add("org/foo");
    packages.add("org/foo/foo");
    packages.add("gor");
    packages.add("gor/oof");
    packages.add("gor/oof/oof");
    for (String s : packages) {
      statisticContainer.includePackage(s);
    }
    
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    
    statisticContainer.writeMappedPackages(printWriter);
    BufferedReader br = new BufferedReader(
        new StringReader(stringWriter.toString()));
    String line = null;
    
    while ((line = br.readLine()) != null) {
      assertTrue("Written package is in list", packages.contains(line));
      packages.remove(line);
    }
    assertTrue("All the package must be logged", packages.isEmpty());
    br.close();
    printWriter.close();
  }  
}
