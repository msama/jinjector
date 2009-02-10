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

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.TextField;

/**
 * Wrapper for {@link TextField}.
 * 
 * @see ItemWrapper
 * @author Michele Sama
 */
public class TextFieldWrapper extends TextField implements ItemWrapper {

  private Vector itemCommandListeners = new Vector();
  private Vector commands = new Vector();
  
  /**
   * Constructor from superclass
   * 
   * @param label the item's label
   * @param text the contained text
   * @param maxSize the maximum length of contained text
   * @param constraints constraints to the accepted input text.
   */
  public TextFieldWrapper(String label, String text, int maxSize,
      int constraints) {
    super(label, text, maxSize, constraints);
  }

  /**
   * Adds a {@link Command} to the nested {@link TextField} and into the local 
   * collection.
   * 
   * @param cmd the {@link Command} to add.
   * @see TextField#addCommand(javax.microedition.lcdui.Command)
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
   * Removes a {@link Command} from the nested {@link TextField} and from the 
   * local collection.
   * 
   * @param cmd The {@link Command} to be removed.
   * @see TextField#removeCommand(javax.microedition.lcdui.Command)
   */
  public void removeCommand(Command cmd) {
    synchronized (commands) {
      commands.removeElement(cmd);
    }
    super.removeCommand(cmd);
  }

  /**
   * Sets an {@link ItemCommandListener} into the nested {@link TextField} and 
   * into the local collection.
   * 
   * @param listener The {@link ItemCommandListener} to set.
   * @see TextField#setItemCommandListener(ItemCommandListener)
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
   * Gets all the registered {@link Command}s.
   * 
   * @return an array containing all the registered {@link Command}s.
   * @see ItemWrapper#getAllCommands()
   */
  public Command[] getAllCommands() {
    synchronized (commands) {
      Command[] cmds = new Command[commands.size()];
      commands.copyInto(cmds);
      return cmds;
    }
  }
  
  /**
   * Gets an array with all the registered {@link ItemCommandListener}s.
   * 
   * @return an array with all the registered {@link ItemCommandListener}s.
   * @see ItemWrapper#getAllItemCommandListeners()
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
   * Delegates {@link ItemVisitor#toString(TextField)} 
   * to get a String representation of the nested {@link TextField}. 
   * 
   * @return a string representation of the nested {@link TextField}.
   */
  public String toString() {
    return ItemVisitor.toString(this);
  }
}
