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

package com.google.test.jinjector.rim;


import com.google.test.jinjector.ResultDisplayerStrategy;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ObjectListField;
import net.rim.device.api.ui.container.MainScreen;

import java.util.Enumeration;

import j2meunit.framework.TestFailure;
import j2meunit.framework.TestResult;

/**
 * Implements {@link ResultDisplayerStrategy} for BlackBerry / RIM.
 * 
 * @author Michele Sama
 *
 */
public class RimResultDisplayer implements ResultDisplayerStrategy {

  /**
   * Displays result using an ObjectListField.
   * 
   * @see ResultDisplayerStrategy#displayResult(TestResult)
   */
  public void displayResult(TestResult result) {
    ObjectListField resultList = new ObjectListField();    
    Enumeration en = null;
    en = result.errors();
    int i = 0;
    while (en.hasMoreElements()) {
      TestFailure tf = (TestFailure) en.nextElement();
      resultList.insert(i++, tf.failedTest() + " " +
          tf.thrownException().getMessage());
    }
    en = result.failures();
    while (en.hasMoreElements()) {
      TestFailure tf = (TestFailure) en.nextElement();
      resultList.insert(i++, tf.failedTest() + " " +
          tf.thrownException().getMessage());
    }
    
    MainScreen resultScreen = new MainScreen();
    resultScreen.add(resultList);
    resultScreen.setTitle("Failures and Errors");
    
    synchronized (UiApplication.getEventLock()) {
      UiApplication.getUiApplication().pushScreen(resultScreen);
    }
  }

}
