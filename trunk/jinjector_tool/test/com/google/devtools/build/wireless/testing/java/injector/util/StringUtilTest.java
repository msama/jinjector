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

package com.google.devtools.build.wireless.testing.java.injector.util;

import junit.framework.TestCase;

import java.util.Arrays;

/**
 * @author Michele Sama
 *
 */
public class StringUtilTest extends TestCase {

  private final static String SEPARATORS = ";:";
  
  private final static String FULL = " A : B ; C ";
  private final static String[] FULL_SPLIT = 
      new String[]{" A ", " B ", " C "};
  private final static String[] FULL_SPLIT_AND_TRIMMED = 
      new String[]{"A", "B", "C"};
  
  private final static String UNSPLITTABLE = " A B C ";
  private final static String[] UNSPLITTABLE_UNSPLIT = 
    new String[]{" A B C "};
  private final static String[] UNSPLITTABLE_UNSPLIT_AND_TRIMMED = 
    new String[]{"A B C"};
  
  private final static String STARTS_WITH_SEPARATOR = ": :A; B :C: ;";
  private final static String[] STARTS_WITH_SEPARATOR_SPLITTED = 
    new String[]{" ", "A", " B ", "C", " "};
  private final static String[] STARTS_WITH_SEPARATOR_SPLITTED_AND_TRIMMED = 
    new String[]{"", "A", "B", "C", ""};
  
  /**
   * @param name
   */
  public StringUtilTest(String name) {
    super(name);
  }
  
  /**
   * Assertion class for this test case.
   *
   */
  private static final class Assert {

    /**
     * Uninstantiable as it is a utility class.
     */
    private Assert() {
      // Do nothing.
    }
    
    /**
     * Compares two different splits and checks if they are equal.
     * 
     * @param expected the expected result.
     * @param actual the current result.
     */
    public static void checkSplit(String[] expected, String[] actual) {
      assertTrue("The split " + Arrays.deepToString(actual) + 
          " does not match the expected split " + 
          Arrays.deepToString(expected), 
          Arrays.equals(expected, actual));
    }
  }

  /**
   * Test method for {@link StringUtil#split(String, String)}.
   */
  public void testSplitString_convenienceMethod() {
    String[] splitted = StringUtil.split(FULL, SEPARATORS);
    String[] splittedNoTrim = StringUtil.split(FULL, SEPARATORS, false);
    StringUtilTest.Assert.checkSplit(splittedNoTrim, splitted);
  }

  /**
   * Test method for {@link StringUtil#split(String, String, boolean)}.
   */
  public void testSplitString_fullString() {
    String[] splitted = StringUtil.split(FULL, SEPARATORS, false);
    StringUtilTest.Assert.checkSplit(FULL_SPLIT, splitted);
  }
  
  /**
   * Test method for {@link StringUtil#split(String, String, boolean)}.
   */
  public void testSplitString_fullStringTrimmed() {
    String[] splitted = StringUtil.split(FULL, SEPARATORS, true);
    StringUtilTest.Assert.checkSplit(
        FULL_SPLIT_AND_TRIMMED, splitted);
  }

  /**
   * Test method for {@link StringUtil#split(String, String, boolean)}.
   */
  public void testSplitString_noDelimiters() {
    String[] splitted = StringUtil.split(UNSPLITTABLE, SEPARATORS, false);
    StringUtilTest.Assert.checkSplit(UNSPLITTABLE_UNSPLIT, splitted);
  }
  
  /**
   * Test method for {@link StringUtil#split(String, String, boolean)}.
   */
  public void testSplitString_noDelimitersTrimmed() {
    String[] splitted = StringUtil.split(UNSPLITTABLE, SEPARATORS, true);
    StringUtilTest.Assert.checkSplit(
        UNSPLITTABLE_UNSPLIT_AND_TRIMMED, splitted);
  }
  
  /**
   * Test method for {@link StringUtil#split(String, String, boolean)}.
   */
  public void testSplitString_startWithDelimiters() {
    String[] splitted = StringUtil.split(STARTS_WITH_SEPARATOR, SEPARATORS, false);
    StringUtilTest.Assert.checkSplit(STARTS_WITH_SEPARATOR_SPLITTED, splitted);
  }
  
  /**
   * Test method for {@link StringUtil#split(String, String, boolean)}.
   */
  public void testSplitString_startWithDelimitersTrimmed() {
    String[] splitted = StringUtil.split(
        STARTS_WITH_SEPARATOR, SEPARATORS, true);
    StringUtilTest.Assert.checkSplit(
        STARTS_WITH_SEPARATOR_SPLITTED_AND_TRIMMED, splitted);
  }
}
