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

package com.google.test.jinjector.util;


import j2meunit.framework.Test;
import j2meunit.framework.TestCase;
import j2meunit.framework.TestMethod;
import j2meunit.framework.TestSuite;

/**
 * Test for {@link StringUtil}.
 * 
 * @author Michele Sama
 *
 */
public class StringUtilTest extends TestCase {

  /**
   * Array of strings to be used as an expected result.
   */
  protected final static String[] SOURCE = new String[] {
    "1", "2", "3", "12", "123", "1234" }; 
  
  /**
   * Single-char string used as target or as separator.
   */
  protected final static String SPLIT_A = "a";
  
  /**
   * Multi-char string used as target or as separator. This string is tricky 
   * because internally it repeats itself. 
   */
  protected final static String SPLIT_BBB = "bbb";
  
  /**
   * Default error message used to report a wrong split size.
   */
  private final static String WRONG_SPLIT_SIZE = 
      "The length of the split array is incorrect.";
  
  /**
   * Default error message used to report a wrong number of counted 
   * occurrences.
   */
  private static final String WRONG_NUMBER_OF_COUNTED_OCCURRENCES = 
      "Wrong number of counted occurrences.";
  
  /**
   * Constructor from superclass.
   */
  public StringUtilTest() {
  }

  /**
   * Constructor from superclass.
   * 
   * @param name the test's name.
   */
  public StringUtilTest(String name) {
    super(name);
  }

  /**
   * Constructor from superclass.
   * 
   * @param name the test's name.
   * @param method the test's method.
   */
  public StringUtilTest(String name, TestMethod method) {
    super(name, method);
  }
  
  protected String build(String[] source, String split) {
    StringBuffer buffer = new StringBuffer();
    buffer.append(source[0]);
    for (int i = 1; i < source.length; i++) {
      buffer.append(split);
      buffer.append(source[i]);
    }
    return buffer.toString();
  }
  
  /**
   * Tests the method {@link StringUtil#split(String, String)} with a separator 
   * composed of a single character.
   */
  public void testSplit_simpleSeparator(){
    String source = build(SOURCE, SPLIT_A);
    String[] split = StringUtil.split(source, SPLIT_A.charAt(0));
    assertEquals(WRONG_SPLIT_SIZE, 
        SOURCE.length, split.length);
    for (int i = 0; i < SOURCE.length; i++) {
      assertEquals("Splitted elements do not match", SOURCE[i], split[i]);
    }
  }
  
  /**
   * Tests the method {@link StringUtil#split(String, String)} with a String 
   * separator.
   */
  public void testSplit_stringSeparator(){
    String source = build(SOURCE, SPLIT_BBB);
    String[] split = StringUtil.split(source, SPLIT_BBB);
    assertEquals(WRONG_SPLIT_SIZE, 
        SOURCE.length, split.length);
    for (int i = 0; i < SOURCE.length; i++) {
      assertEquals("Splitted elements do not match", SOURCE[i], split[i]);
    }
  }
  
  /**
   * Tests the method {@link StringUtil#split(String, String)} with sequential 
   * separators in the source string.
   */
  public void testSplit_sequentialSeparators(){
    String source = build(SOURCE, SPLIT_A + SPLIT_A);
    String[] split = StringUtil.split(source, SPLIT_A);
    assertEquals(WRONG_SPLIT_SIZE, 
        SOURCE.length, split.length);
    for (int i = 0; i < SOURCE.length; i++) {
      assertEquals("Splitted elements do not match", SOURCE[i], split[i]);
    }
  }
  
  /**
   * Tests the method {@link StringUtil#split(String, String)} with separators 
   * at the beginning of the string.
   */
  public void testSplit_beginsWithSeparator(){
    String source = SPLIT_A + build(SOURCE, SPLIT_A);
    String[] split = StringUtil.split(source, SPLIT_A);
    assertEquals(WRONG_SPLIT_SIZE, 
        SOURCE.length, split.length);
    for (int i = 0; i < SOURCE.length; i++) {
      assertEquals("Splitted elements do not match", SOURCE[i], split[i]);
    }
  }
  
  /**
   * Tests the method {@link StringUtil#split(String, String)} with a 
   * separator at the end of the string.
   */
  public void testSplit_endsWithSeparator(){
    String source = build(SOURCE, SPLIT_A) + SPLIT_A;
    String[] split = StringUtil.split(source, SPLIT_A);
    assertEquals(WRONG_SPLIT_SIZE, 
        SOURCE.length, split.length);
    for (int i = 0; i < SOURCE.length; i++) {
      /*
       * TODO: This assert would fail the test with the first 
       * discrepancy. It might be worth creating a custom assert that compares 
       * the entire array and reports which elements are incorrect.
       */
      assertEquals("Splitted elements do not match", SOURCE[i], split[i]);
    }
  }

  /**
   * Tests the method {@link StringUtil#split(String, String)} with a 
   * separator which has no match in the source string.
   */
  public void testSplit_noMatch(){
    String source = SPLIT_A + SPLIT_A + SPLIT_A + SPLIT_A;
    String[] split = StringUtil.split(source, SPLIT_BBB);
    assertEquals(WRONG_SPLIT_SIZE, 
        1, split.length);
    assertEquals("Splitted elements do not match", source, split[0]);
  }
  
  /**
   * Tests the method {@link StringUtil#split(String, String)} with a 
   * source string which is a subset of the separator.
   */
  public void testSplit_sourceIsSubset(){
    String source = SPLIT_A + SPLIT_A + SPLIT_A + SPLIT_A;
    String[] split = StringUtil.split(source, source + SPLIT_BBB);
    assertEquals(WRONG_SPLIT_SIZE, 
        1, split.length);
    assertEquals("Splitted elements do not match", source, split[0]);
  }
  
  /**
   * Tests the method {@link StringUtil#split(String, String)} with a 
   * source string equals to the separator.
   */
  public void testSplit_sourceEqualsSeparator(){
    String source = SPLIT_BBB;
    String[] split = StringUtil.split(source, source);
    assertEquals(WRONG_SPLIT_SIZE, 
        0, split.length);
  }
  
  /**
   * Tests the method {@link StringUtil#split(String, String)} with a 
   * source string containing only separators.
   */
  public void testSplit_sourceContainsOnlySeparators(){
    String source = "SPLIT_BBB";
    String[] split = StringUtil.split(source + source, source);
    assertEquals(WRONG_SPLIT_SIZE, 
        0, split.length);
  }

  /**
   * Tests the method {@link StringUtil#countOccurrences(String, String)} with 
   * an empty source string.
   */
  public void testCountOccurrences_emptySource() {
    int num = StringUtil.countOccurrences("", SPLIT_A);
    assertEquals("Empty source string should contain no occurrences.", 0, num);
  }
  
  /**
   * Tests the method {@link StringUtil#countOccurrences(String, String)} with 
   * empty source string and target.
   */
  public void testCountOccurrences_emptySourceAndTarget() {
    int num = StringUtil.countOccurrences("", "");
    assertEquals("Empty source string should contain no occurrences.", 0, num);
  }
  
  /**
   * Tests the method {@link StringUtil#countOccurrences(String, String)} with 
   * a target which repeats itself.
   * 
   * <p>Given a composite target "bbb" and a string containing a sequence of 
   * them, e.g. "bbbbbb", this test checks that only 2 occurrences are found.
   * When the first instance of the target has been detected only the substring
   * starting from the target's position plus target's length should be 
   * considered. If this test fails because 4 occurrences are found the code 
   * is not considering target's length. 
   */
  public void testCountOccurrences_targetRepeating() {
    int num = StringUtil.countOccurrences(SPLIT_BBB + SPLIT_BBB, SPLIT_BBB);
    assertEquals(WRONG_NUMBER_OF_COUNTED_OCCURRENCES, 2, num);
  }
  
  /**
   * Tests the method {@link StringUtil#countOccurrences(String, String)} with 
   * a source string containing the target in the middle.
   */
  public void testCountOccurrences_targetInTheMiddle() {
    int num = StringUtil.countOccurrences(
        SPLIT_BBB + SPLIT_A + SPLIT_BBB + SPLIT_A + SPLIT_BBB, 
        SPLIT_A);
    assertEquals(WRONG_NUMBER_OF_COUNTED_OCCURRENCES, 2, num);
  }
  
  /**
   * Tests the method {@link StringUtil#countOccurrences(String, String)} with 
   * a source string containing the target at the beginning and at the end.
   */
  public void testCountOccurrences_targetExternal() {
    int num = StringUtil.countOccurrences(
        SPLIT_A + SPLIT_BBB + SPLIT_A, 
        SPLIT_A);
    assertEquals(WRONG_NUMBER_OF_COUNTED_OCCURRENCES, 2, num);
  }

  /* (non-Javadoc)
   * @see j2meunit.framework.TestCase#suite()
   */
  public Test suite() {
    TestSuite suite = new TestSuite();
    
    // Count occurrences
    suite.addTest(new StringUtilTest("testCountOccurrences_emptySource") {
      public void runTest() {
        testCountOccurrences_emptySource();
      }
    });
    
    suite.addTest(new StringUtilTest("testCountOccurrences_emptySourceAndTarget") {
      public void runTest() {
        testCountOccurrences_emptySourceAndTarget();
      }
    });
    
    suite.addTest(new StringUtilTest("testCountOccurrences_targetExternal") {
      public void runTest() {
        testCountOccurrences_targetExternal();
      }
    });
    
    suite.addTest(new StringUtilTest("testCountOccurrences_targetInTheMiddle") {
      public void runTest() {
        testCountOccurrences_targetInTheMiddle();
      }
    });
    
    suite.addTest(new StringUtilTest("testCountOccurrences_targetRepeating") {
      public void runTest() {
        testCountOccurrences_targetRepeating();
      }
    });
    
    // Split
    suite.addTest(new StringUtilTest("testSplit_beginsWithSeparator") {
      public void runTest() {
        testSplit_beginsWithSeparator();
      }
    });
    
    suite.addTest(new StringUtilTest("testSplit_endsWithSeparator") {
      public void runTest() {
        testSplit_endsWithSeparator();
      }
    });
    
    suite.addTest(new StringUtilTest("testSplit_noMatch") {
      public void runTest() {
        testSplit_noMatch();
      }
    });
    
    suite.addTest(new StringUtilTest("testSplit_sequentialSeparators") {
      public void runTest() {
        testSplit_sequentialSeparators();
      }
    });
    
    suite.addTest(new StringUtilTest("testSplit_simpleSeparator") {
      public void runTest() {
        testSplit_simpleSeparator();
      }
    });
    
    suite.addTest(new StringUtilTest("testSplit_sourceContainsOnlySeparators") {
      public void runTest() {
        testSplit_sourceContainsOnlySeparators();
      }
    });
    
    suite.addTest(new StringUtilTest("testSplit_sourceEqualsSeparator") {
      public void runTest() {
        testSplit_sourceEqualsSeparator();
      }
    });
    
    suite.addTest(new StringUtilTest("testSplit_sourceIsSubset") {
      public void runTest() {
        testSplit_sourceIsSubset();
      }
    });
    
    suite.addTest(new StringUtilTest("testSplit_stringSeparator") {
      public void runTest() {
        testSplit_stringSeparator();
      }
    });
    
    return suite;
  }
  
}
