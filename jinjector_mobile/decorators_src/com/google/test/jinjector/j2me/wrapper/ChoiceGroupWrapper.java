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

import java.util.Vector;

import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.ItemCommandListener;

/**
 * Wrapper for {@link ChoiceGroup}.
 * 
 * @author Michele Sama
 */
public class ChoiceGroupWrapper extends ChoiceGroup implements ItemWrapper {

  private Vector itemCommandListeners = new Vector();
  private Vector commands = new Vector();

  /**
   * Constructor from superclass.
   * 
   * @param label The title to set.
   * @param choiceType The selection type to use for this {@link ChoiceGroup}.
   * @param stringElements A set of initial elements.
   * @param imageElements A set of initial images.
   */
  public ChoiceGroupWrapper(String label, int choiceType,
      String[] stringElements, Image[] imageElements) {
    super(label, choiceType, stringElements, imageElements);
  }

  /**
   * Constructor from superclass.
   * 
   * @param label The title for this {@link ChoiceGroup}.
   * @param choiceType The type for this {@link ChoiceGroup}.
   */
  public ChoiceGroupWrapper(String label, int choiceType) {
    super(label, choiceType);
  }

  /**
   * Adds a {@link Command} to the nested {@link ChoiceGroup} and to the 
   * local collection.
   * 
   * @see ChoiceGroup#addCommand(Command)
   */
  public void addCommand(Command cmd) {
    synchronized (commands) {
      if (!commands.contains(cmd)) {
        if (!commands.contains(cmd)) {
          commands.addElement(cmd);
        }
      }
    }
    super.addCommand(cmd);
  }

  /**
   * Removes a {@link Command} from the nested {@link ChoiceGroup} and from the
   * local collection.
   * 
   * @param cmd The {@link Command} to be removed.
   * @see ChoiceGroup#removeCommand(Command)
   */
  public void removeCommand(Command cmd) {
    synchronized (commands) {
      commands.removeElement(cmd);
    }
    super.removeCommand(cmd);
  }

  /**
   * Sets an {@link ItemCommandListener} to this {@link ChoiceGroup} and adds 
   * it to the local collection.
   * 
   * @param listener The {@link ItemCommandListener} to be set.
   * @see ChoiceGroup#setItemCommandListener(ItemCommandListener)
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
   * Gets an array containing all the {@link ItemCommandListener}s registered 
   * to this {@link ChoiceGroup}.
   * 
   * @return An array containing all the {@link ItemCommandListener}s.
   */
  public Command[] getAllCommands() {
    synchronized (commands) {
      Command[] cmds = new Command[commands.size()];
      commands.copyInto(cmds);
      return cmds;
    }
  }
  
  /**
   * Gets an array with all the listeners registered to this 
   * {@link ChoiceGroup}.
   * 
   * @return an array with all the {@link ItemCommandListener}s.
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
   * Delegates {@link ItemVisitor#toString(ChoiceGroup)} to get a String 
   * representation of the nested {@link ChoiceGroup}. 
   * 
   * @return a string representation of the nested {@link ChoiceGroup}.
   */
  public String toString() {
    return ItemVisitor.toString(this);
  }

}
