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
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.ItemCommandListener;

/**
 * Wrapper for {@link Gauge}.
 * 
 * @see ItemWrapper
 * @author Michele Sama
 */
public class GaugeWrapper extends Gauge implements ItemWrapper {

  private Vector itemCommandListeners = new Vector();
  private Vector commands = new Vector();
  
  /**
   * Constructor from superclass.
   * 
   * @param label The label to set.
   * @param interactive <code>true</code> if is interective.
   * @param maxValue The max value.
   * @param initialValue The initial value.
   */
  public GaugeWrapper(String label, boolean interactive, int maxValue,
      int initialValue) {
    super(label, interactive, maxValue, initialValue);
  }
  
  /**
   * Adds a {@link Command} to the nested {@link Gauge} and stores it into the
   * local collection.
   * 
   * @param cmd The {@link Command} to add.
   * @see Gauge#addCommand(Command)
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
   * Removes a {@link Command} from the nested {@link Gauge} and from the local
   * collection.
   * 
   * @param cmd The {@link Command} to remove.
   * @see Gauge#removeCommand(Command)
   */
  public void removeCommand(Command cmd) {
    synchronized (commands) {
      commands.removeElement(cmd);
    }
    super.removeCommand(cmd);
  }

  /**
   * Sets an {@link ItemCommandListener} into the nested {@link Gauge} and 
   * stores it into the local collection.
   * 
   * @param listener The {@link ItemCommandListener} to set. 
   * @see Gauge#setItemCommandListener(ItemCommandListener)
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
   * Gets an array containing all the {@link Command}s registered in this 
   * {@link Gauge}.
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
   * Gets an array with all the {@link ItemCommandListener} registered to the 
   * nested {@link Gauge}.
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
   * Delegates {@link ItemVisitor#toString(javax.microedition.lcdui.Item)} 
   * to get a String representation of the nested {@link Gauge}. 
   * 
   * @return a string representation of the nested {@link Gauge}.
   */
  public String toString() {
    return ItemVisitor.toString(this);
  }

}
