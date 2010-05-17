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
import javax.microedition.lcdui.Spacer;

/**
 * Wrapper for {@link Spacer}.
 * 
 * @see ItemWrapper
 * @author Michele Sama
 */
public class SpacerWrapper extends Spacer implements ItemWrapper {

  private Vector itemCommandListeners = new Vector();
  private Vector commands = new Vector();
  
  /**
   * Constructor from superclass.
   * 
   * @param minWidth the minimum width.
   * @param minHeight the minimum heigh.
   */
  public SpacerWrapper(int minWidth, int minHeight) {
    super(minWidth, minHeight);
  }

  /**
   * Adds a {@link Command} to the nested {@link Spacer} and into the local 
   * collection.
   * 
   * @param cmd The {@link Command} to add.
   * @see Spacer#addCommand(Command)
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
   * Removes a {@link Command} from the nested {@link Spacer} and from the 
   * local collection.
   * 
   * @param cmd The {@link Command} to be removed.
   * @see Spacer#removeCommand(Command)
   */
  public void removeCommand(Command cmd) {
    synchronized (commands) {
      commands.removeElement(cmd);
    }
    super.removeCommand(cmd);
  }

  /**
   * Sets a {@link ItemCommandListener} into the nested {@link Spacer} and 
   * saves it into the local collection.
   * 
   * @param listener the {@link ItemCommandListener} to set.
   * @see Spacer#setItemCommandListener(ItemCommandListener)
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
   * Get an array containing all the commands registered in this {@link Spacer}.
   * 
   * @return All the Command
   */
  public Command[] getAllCommands() {
    synchronized (commands) {
      Command[] cmds = new Command[commands.size()];
      commands.copyInto(cmds);
      return cmds;
    }
  }
  
  /**
   * Get an array with all the listeners registered to this {@link Spacer}.
   * 
   * @return an array with all the listeners
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
   * Delegates {@link ItemVisitor#toString(javax.microedition.lcdui.Item)} 
   * to get a String representation of the nested {@link Spacer}. 
   * 
   * @return a string representation of the nested {@link Spacer}.
   */
  public String toString() {
    return ItemVisitor.toString(this);
  }
}
