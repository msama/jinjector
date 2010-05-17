// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.test.jinjector.j2me;

import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.CustomItem;
import javax.microedition.lcdui.DateField;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.ImageItem;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.Spacer;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;

/**
 * Utility class for Item and all of his subclasses: {@link ChoiceGroup}, 
 * {@link CustomItem}, {@link DateField}, {@link Gauge}, {@link ImageItem}, 
 * {@link Spacer}, {@link StringItem}, {@link TextField}. 
 * 
 * <p> Those classes are part of MIDP and they cannot be instrumented properly 
 * (e.g adding the accept method) so this visitor uses the instanceof command 
 * to invoke the right method/perform the right action.
 * 
 * <p> Another possible implementation would have been to override the toString 
 * method in all the wrapper, but that would have also duplicated part of the 
 * code.
 * 
 * @see com.google.test.jinjector.j2me.DisplayableVisitor
 * 
 * @author Michele Sama
 * 
 */
public class ItemVisitor {

  /**
   * Uninstantiable.
   */
  private ItemVisitor() {
  }
 
  /**
   * Creates a String representation of a {@link Item}.
   * 
   * @param item The instance to visit.
   * @return the String representation.
   */
  public static String toString(Item item) {
    return "Displayable: lable = " + item.getLabel();
  }
  
  /**
   * Creates a String representation of a {@link ChoiceGroup}.
   * 
   * @param choiceGroup The instance to visit.
   * @return the String representation.
   */
  public static String toString(ChoiceGroup choiceGroup) {
    int selectedIndex = choiceGroup.getSelectedIndex();
    return  toString((Item) choiceGroup) + " choice = " +
        ((selectedIndex < 0) ? "null" : choiceGroup.getString(selectedIndex));
  }
  
  /**
   * Creates a String representation of a {@link DateField}.
   * 
   * @param dateField The instance to visit.
   * @return the String representation.
   */
  public static String toString(DateField dateField) {
    return toString((Item) dateField) + " date = " + 
        ((dateField.getDate() == null) ? "null" : 
              dateField.getDate().toString());
  }
  
  /**
   * Creates a String representation of a {@link StringItem}.
   * 
   * @param stringItem The instance to visit.
   * @return the String representation.
   */
  public static String toString(StringItem stringItem) {
    return toString((Item) stringItem) + " text = " + stringItem.getText();
  }
  
  /**
   * Creates a String representation of a {@link TextField}.
   * 
   * @param textField The instance to visit.
   * @return the String representation.
   */
  public static String toString(TextField textField) {
    return toString((Item) textField) + " text = " + textField.getString();
  }
  
}
