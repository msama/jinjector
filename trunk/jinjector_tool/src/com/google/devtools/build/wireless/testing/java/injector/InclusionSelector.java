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

package com.google.devtools.build.wireless.testing.java.injector;

import com.google.devtools.build.wireless.testing.java.injector.util.PrefixTrie;

/**
 * Selector specific to handle the inclusion of java classes.
 * 
 * @author Michele Sama
 *
 */
public class InclusionSelector {

  private PrefixTrie<Boolean> prefixes = new PrefixTrie<Boolean>();
  
  /**
   * Creates a selector which with a default behvior.
   * 
   * @param defaultInclusion <code>true</code> if all the class are included 
   *    by default, <code>false</code> otherwise.
   */
  public InclusionSelector(Boolean defaultInclusion) {
    put("", defaultInclusion);
  }

  /**
   * Gets the most specific inclusion for the given query.
   * 
   * @param query the query
   * @return <code>true</code> is a class with the given name has to be 
   *   included, <code>false</code> otherwise.
   */
  public Boolean getMostSpecificAction(String query){
    return prefixes.get(query);
  }

  /**
   * @param prefix the previx to add.
   * @param value The action to perform.
   * @return the action matching the most specific prefix.
   * @see com.google.common.collect.PrefixTrie#put(java.lang.CharSequence, java.lang.Object)
   */
  public Boolean put(CharSequence prefix, Boolean value) {
    return prefixes.put(prefix, value);
  }
  
  /**
   * Adds a collection of strings as prefix. If a string starts with '-' add 
   * it as excluded. If it starts with '+' or if it just starts with the 
   * package name adds it to the inclusion list.
   * 
   * @param inclusion the array to include.
   */
  public void loadInclusionList(String[] inclusion) {
    for (String s : inclusion) {
      if (s.startsWith("-")) {
        put(s.substring(1), Boolean.FALSE);
      } else if(s.startsWith("+")){
        put(s.substring(1), Boolean.TRUE);
      } else {
        put(s, Boolean.TRUE);
      }
    }
  }
  
}
