// Copyright 2009 Google Inc. All Rights Reserved.

package com.google.test.jinjector.j2me.wrapper;

import javax.microedition.lcdui.Command;

/**
 * @author danrao@google.com
 *
 */
public class WrapperUtil {

  /**
   * Private constructor.
   */
  private WrapperUtil() {
  }

  /**
   * 
   */
  public static boolean containsAction(String action, Command[] commands) {
    if (action == null) {
      throw new IllegalArgumentException(
          "Expected action string cannot be null.");
    }
    
    for (int i = 0; i < commands.length; i++) {
      if (action.equals(commands[i].getLabel())) {
        return true;
      }
    }
    return false;
  }
  
}
