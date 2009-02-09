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


import java.util.StringTokenizer;

/**
 * Utility class providing support for handling {@link String}s.
 * 
 * @author Michele Sama
 *
 */
public class StringUtil {

  /**
   * Uninstantiable because it is a utility class
   */
  private StringUtil() {
    // Do nothing
  }

  /**
   * Splits a string into tokens at the specified delimiters.
   * @param source The string to split.  Must not be null.
   * @param delimiters The delimiter characters. Each character in the
   *        string is individually treated as a delimiter.
   * @return An array of tokens. Will not return null. Individual tokens
   *        do not have leading/trailing whitespace removed.
   */
  public static String[] split(String source, String delimiters) {
    return split(source, delimiters, false);
  }

  /**
   * Splits a string into tokens at the specified delimiters.
   * @param str The string to split.  Must not be null.
   * @param delims The delimiter characters. Each character in the string
   *        is individually treated as a delimiter.
   * @param trimTokens If true, leading/trailing whitespace is removed
   *        from the tokens.
   * @return An array of tokens. Will not return null.
   */
  public static String[] split(String str, String delims, boolean trimTokens) {
    StringTokenizer tokenizer = new StringTokenizer(str, delims);
    int n = tokenizer.countTokens();
    String[] list = new String[n];
    for (int i = 0; i < n; i++) {
      if (trimTokens) {
        list[i] = tokenizer.nextToken().trim();
      } else {
        list[i] = tokenizer.nextToken();
      } 
    }
    return list;
  }
}
