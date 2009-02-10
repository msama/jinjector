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

package com.google.test.jinjector.j2me.wrapper;

import com.google.test.jinjector.j2me.ItemVisitor;

import java.util.TimeZone;
import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.DateField;
import javax.microedition.lcdui.ItemCommandListener;

/**
 * Wrapper for {@link DateField}.
 * 
 * <p>It exposes getter methods for contained {@link Command}s and 
 * {@link ItemCommandListener}s which allows to fire events.
 * 
 * @author Michele Sama
 */
public class DateFieldWrapper extends DateField implements ItemWrapper {

  private Vector itemCommandListeners = new Vector();
  private Vector commands = new Vector();
  
  /**
   * Constructor from superclass. 
   *  
   * @param label The title to set.
   * @param mode The mode in which to display the date.
   * @param timeZone The time zone to use.
   */
  public DateFieldWrapper(String label, int mode, TimeZone timeZone) {
    super(label, mode, timeZone);
  }

  /**
   * Constructor from superclass. 
   *  
   * @param label The title to set.
   * @param mode The mode in which to display the date.
   */
  public DateFieldWrapper(String label, int mode) {
    super(label, mode);
  }

  /**
   * Adds a {@link Command} to the nested {@link DateField} and stores it into 
   * the local collection.
   * 
   * @param cmd The {@link Command} to add.
   * @see DateField#addCommand(Command)
   */
  public void addCommand(Command cmd) {
    synchronized (commands) {
      if (!commands.contains(cmd)) {
        commands.addElement(cmd);
      }
    }
    super.addCommand(cmd);
  }

  /**
   * Removes a {@link Command} to the nested {@link DateField} and from
   * the local collection.
   * 
   * @param cmd The {@link Command} to remove.
   * @see DateField#removeCommand(Command)
   */
  public void removeCommand(Command cmd) {
    synchronized (commands) {
      commands.removeElement(cmd);
    }
    super.removeCommand(cmd);
  }

  /**
   * Sets an {@link ItemCommandListener} to the nested {@link DateField} and
   * stores it into the local collection.
   * 
   * @param listener The {@link ItemCommandListener} to set.
   * @see DateField#setItemCommandListener(ItemCommandListener)
   */
  public void setItemCommandListener(ItemCommandListener listener) {
    synchronized (itemCommandListeners) {
      if (!itemCommandListeners.contains(listener)) {
        itemCommandListeners.addElement(listener);
      }
    }
    super.setItemCommandListener(listener);
  }
 
  /**
   * Gets an array containing all the {@link Command}s registered in the nested
   * {@link DateField}.
   * 
   * @return An array containing all the registered {@link Command}s.
   */
  public Command[] getAllCommands() {
    synchronized (commands) {
      Command[] cmds = new Command[commands.size()];
      commands.copyInto(cmds);
      return cmds;
    }
  }
  
  /**
   * Gets an array with all the {@link ItemCommandListener}s registered to this
   * {@link DateField}.
   * 
   * @return An array with all the {@link ItemCommandListener}s.
   */
  public ItemCommandListener[] getAllItemCommandListeners() {
    synchronized (itemCommandListeners) {
      ItemCommandListener[] cmds =
          new ItemCommandListener[itemCommandListeners.size()];
      itemCommandListeners.copyInto(cmds);
      return cmds;
    }
  }
  
  /**
   * Delegates {@link ItemVisitor#toString(DateField)} 
   * to get a String representation of the nested {@link DateField}. 
   * 
   * @return a string representation of the nested {@link DateField}.
   */
  public String toString() {
    return ItemVisitor.toString(this);
  }
}
