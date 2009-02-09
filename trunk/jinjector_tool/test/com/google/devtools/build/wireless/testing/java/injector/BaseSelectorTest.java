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

import com.google.devtools.build.wireless.testing.java.injector.InclusionSelector;

import junit.framework.TestCase;

/**
 * @author Michele Sama
 *
 */
public class BaseSelectorTest extends TestCase {

  private InclusionSelector selector;
  
  /**
   * @param name
   */
  public BaseSelectorTest(String name) {
    super(name);
  }

  /* (non-Javadoc)
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    selector = new InclusionSelector(false);
  }
  
  /**
   * Test method for 
   * {@link InclusionSelector#getMostSpecificAction(String)}
   * .
   */
  public void testGetMostSpecificAction() {
    selector.put("com.google", Boolean.FALSE);
    selector.put("com.google.foo", Boolean.TRUE);
    selector.put("com.google.foo.Bar", Boolean.FALSE);
    
    assertTrue("com.google.asd", 
        !selector.getMostSpecificAction("com.google.asd"));
    
    assertTrue("com.google.foo.asd", 
        selector.getMostSpecificAction("com.google.foo.asd"));
    
    assertTrue("com.google.foo.Bar", 
        !selector.getMostSpecificAction("com.google.foo.Bar"));
  }

}
