/* Copyright 2009 Google Inc.
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

/**
 * Utility class for arrays.
 * 
 * @author Dan Rao
 *
 */
public class ArrayUtil {

  /**
   * Private constructor to prevent from instantiation.
   */
  private ArrayUtil() {
  }

  /**
   * Converts the given array in a readable string in which all the elements
   * are separated by the given separator.
   * 
   * @param objects the array of which the string representation will be
   *     generated.
   * 
   * @param separator the separator which will be placed between elements.
   * 
   * @return all {@link String}s separated by " "; if the array is
   *     <code>null</code>, return empty {@link String}.
   */
  static public String toString(Object[] objects, String separator) {
    if (objects == null) {
      return "";
    }
    StringBuffer buffer = new StringBuffer(); 
    for (int i = 0; i < objects.length; i++){
     buffer.append(objects[i].toString());
     if (i != objects.length - 1) {
       buffer.append(separator);
     }
    }
    return buffer.toString();
  }

}
