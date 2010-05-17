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
import javax.microedition.lcdui.StringItem;

/**
 * Wrapper for {@link StringItem}
 * 
 * @author Michele Sama
 *
 */
public class StringItemWrapper extends StringItem implements ItemWrapper {

  private Vector itemCommandListeners = new Vector();
  private Vector commands = new Vector();
  
  /**
   * Constructor from superclass
   * 
   * @param label The item's label
   * @param text The contained text
   * @param appearanceMode The apparence mode
   */
  public StringItemWrapper(String label, String text, int appearanceMode) {
    super(label, text, appearanceMode);
  }

  /**
   * Constructor from superclass
   * 
   * @param label The item's label
   * @param text The contained text
   */
  public StringItemWrapper(String label, String text) {
    super(label, text);
  }

  /**
   * Adds a {@link Command} into the nested {@link StringItem} and stores it 
   * into the local collection.
   * 
   * @param cmd The {@link Command} to store.
   * @see StringItem#addCommand(Command)
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
   * Removes a {@link Command} from the nested {@link StringItem} and from the 
   * local collection.
   * 
   * @param cmd The {@link Command} to remove.
   * @see StringItem#removeCommand(Command)
   */
  public void removeCommand(Command cmd) {
    synchronized (commands) {
      commands.removeElement(cmd);
    }
    super.removeCommand(cmd);
  }

  /**
   * Sets a {@link ItemCommandListener} into the nested {@link StringItem} and 
   * saves it into the local collection.
   * 
   * @param listener The {@link ItemCommandListener} to set.
   * @see StringItem#setItemCommandListener(ItemCommandListener)
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
   * Get an array containing all the commands registered in this 
   * {@link StringItem}
   * 
   * @return All the Command.
   */
  public Command[] getAllCommands() {
    synchronized (commands) {
      Command[] cmds = new Command[commands.size()];
      commands.copyInto(cmds);
      return cmds;
    }
  }
  
  /**
   * Get an array with all the listeners registered to this 
   * {@link StringItem}
   * 
   * @return an array with all the listeners.
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
   * Delegates {@link ItemVisitor#toString(StringItem)} 
   * to get a String representation of the nested {@link StringItem}. 
   * 
   * @return a string representation of the nested {@link StringItem}.
   */
  public String toString() {
    return ItemVisitor.toString(this);
  }
  
}
