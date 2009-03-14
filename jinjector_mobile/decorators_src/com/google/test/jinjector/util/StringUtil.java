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

import java.util.Vector;

/**
 * Utility class to manipulate strings.
 * 
 * @author Michele Sama
 *
 */
public class StringUtil {

  /**
   * Uninstantiable because it is a utility class.
   */
  private StringUtil() {
  }

  /**
   * Splits the target {@link java.lang.String} with the same semantics as
   * J2SE's {@code String#split(String)} except {@code separator} is a literal
   * and not a regular expression.
   * 
   * @param source the {@link java.lang.String} to be split.
   * @param separator the delimiting literal character.
   * @return the array of strings computed by splitting this string.
   */
  public static String[] split(String source, String separator) {
    if (source == null) {
      throw new IllegalArgumentException("Cannot split a null string.");
    }
    if (separator == null) {
      throw new IllegalArgumentException("Cannot split using null separator.");
    }

    final int sourceLength = source.length();
    final int separatorLength = separator.length();
    
    Vector splittedStrings = new Vector();
    int currentCursor = 0;
    int nextCursor = 0;
    
    while (currentCursor < sourceLength) {
      nextCursor = source.indexOf(separator, currentCursor);
      if (currentCursor == nextCursor) {
        // Two sequential separators, skip increment and continue.
        currentCursor += separatorLength;
        continue;
      } else if (nextCursor == -1) {
        // No more separators, add the rest of the string and break.
        splittedStrings.addElement(
            source.substring(currentCursor));
        break;
      } else {
        // Add the string between the separators and increment the initial one.
        splittedStrings.addElement(
            source.substring(currentCursor, nextCursor));
        currentCursor = nextCursor + separatorLength;
      }
    }

    String[] result = new String[splittedStrings.size()];
    splittedStrings.copyInto(result);
    return result;
  }
  
  /**
   * @see #split(String, String)
   */
  public static String[] split(String target, char separator) {
    return split(target, String.valueOf(separator));
  }
  
  /**
   * Counts the number of occurrence of a given string in a source string.
   * 
   * @param source the string in which to count for matches.
   * @param toCount the string to match.
   */
  public static int countOccurrences(String source, String toCount) {
    if (source == null) {
      throw new IllegalArgumentException("Cannot count on a null string.");
    }
    if (toCount == null) {
      throw new IllegalArgumentException("The count's target cannot be null.");
    }

    final int sourceLength = source.length();
    final int targetLength = toCount.length();
    
    int occurrences = 0;
    int currentCursor = 0;
    while (currentCursor < sourceLength) {
      currentCursor = source.indexOf(toCount, currentCursor);
      if (currentCursor == -1) {
        // target not found
        break;
      } else {
        // target matching
        occurrences++;
        currentCursor += targetLength;
      }
    }
    
    return occurrences;
  }
  
}
