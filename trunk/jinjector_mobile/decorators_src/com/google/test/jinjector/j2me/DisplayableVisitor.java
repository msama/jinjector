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

package com.google.test.jinjector.j2me;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.Screen;
import javax.microedition.lcdui.TextBox;

/**
 * Utility class which prints a description of a {@link Displayable} and all of 
 * its subclasses: {@link Canvas}, {@link Screen}, {@link Alert}, {@link Form}, 
 * {@link List}, {@link TextBox}.
 * 
 * <p>Instead of using this class it would have been possible to override the 
 * toString method in all the Wrappers. But then the implementation would have 
 * been subdivided over multiple classes.
 * 
 * @see com.google.test.jinjector.j2me.ItemVisitor
 * 
 * @author Michele Sama
 */
public class DisplayableVisitor {

  /**
   * Uninstantiable.
   */
  private DisplayableVisitor() {
  }

  /**
   * Creates a String representation of a {@link Displayable}.
   * 
   * @param disp The instance to visit.
   * @return the String representation.
   */
  public static String toString(Displayable disp) {
    return "Displayable: title = " + disp.getTitle();
  }
  
  /**
   * Creates a String representation of a {@link Alert}.
   * 
   * @param alert The instance to visit.
   * @return the String representation.
   */
  public static String toString(Alert alert) {
    return toString((Displayable) alert) + " message " + alert.getString();
  }
  
  /**
   * Creates a String representation of a {@link List}.
   * 
   * @param list The instance to visit.
   * @return the String representation.
   */
  public static String toString(List list) { 
    int selectedIndex = list.getSelectedIndex();
    return toString((Displayable) list) + " selectedItem " +
        ((selectedIndex < 0) ? "null" : list.getString(selectedIndex));
  }
  
  /**
   * Creates a String representation of a {@link TextBox}.
   * 
   * @param textBox The instance to visit.
   * @return the String representation.
   */
  public static String toString(TextBox textBox) {
    return toString((Displayable) textBox) + " message " + textBox.getString();
  }

}
