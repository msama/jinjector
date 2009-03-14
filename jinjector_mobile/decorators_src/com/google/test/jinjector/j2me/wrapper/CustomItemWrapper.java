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
import javax.microedition.lcdui.CustomItem;
import javax.microedition.lcdui.ItemCommandListener;

/**
 * Wrapper class for {@link CustomItem}.
 * 
 * <p>This class contains methods to expose protected methods of the parent 
 * class. However those methods are not overriding the super methods to avoid 
 * verification problems. Specifically this wrapper will be instrumented as a 
 * super class of classes extending from {@link CustomItem}. If those derived 
 * class override one of the protected methods and leave it protected, then 
 * after the instrumentation there will be protected methods overriding public 
 * ones which is not allowed by the JVM.
 * 
 * TODO}. 
 * 
 * @author Michele sama
 *
 */
public abstract class CustomItemWrapper extends CustomItem implements ItemWrapper {

  private Vector itemCommandListeners = new Vector();
  private Vector commands = new Vector();
  
  /**
   * @param label
   */
  public CustomItemWrapper(String label) {
    super(label);
  }

  /**
   * Adds a {@link Command} to the nested {@link CustomItem} and 
   * into the local collection.
   * 
   * @param cmd The {@link Command} to add.
   * @see CustomItem#addCommand(javax.microedition.lcdui.Command)
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
   * Removes a {@link Command} from the nested 
   * {@link CustomItem} and from the local collection.
   * 
   * @param cmd The {@link Command} to be removed.
   * @see CustomItem#removeCommand(javax.microedition.lcdui.Command)
   */
  public void removeCommand(Command cmd) {
    synchronized (commands) {
      commands.removeElement(cmd);
    }
    super.removeCommand(cmd);
  }

  /**
   * Sets an {@link ItemCommandListener} into the nested 
   * {@link CustomItem} and into the local collection.
   * 
   * @param listener The {@link ItemCommandListener} to set.
   * @see CustomItem#setItemCommandListener(ItemCommandListener)
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
   * Delegates {@link ItemVisitor#toString(javax.microedition.lcdui.Item)} 
   * to get a String representation of the nested {@link CustomItem}. 
   * 
   * @return a string representation of the nested {@link CustomItem}.
   */
  public String toString() {
    return ItemVisitor.toString(this);
  }

  /**
   * Sends a key event to the wrapped item.
   * 
   * @see CustomItem#keyPressed(int)
   */
  public void invokeKeyPressed(int keyCode) {
    keyPressed(keyCode);
  }

  /**
   * Sends a key event to the wrapped item.
   * 
   * @see CustomItem#keyReleased(int)
   */
  public void invokeKeyReleased(int keyCode) {
    keyReleased(keyCode);
  }

  /**
   * Sends a key event to the wrapped item.
   * 
   * @see CustomItem#keyRepeated(int)
   */
  public void invokeKeyRepeated(int keyCode) {
    keyRepeated(keyCode);
  }

  /**
   * Sends a pinter event to the wrapped item.
   * 
   * @see CustomItem#pointerDragged(int, int)
   */
  public void invokePointerDragged(int x, int y) {
    pointerDragged(x, y);
  }

  /**
   * Sends a pinter event to the wrapped item.
   * 
   * @see CustomItem#pointerPressed(int, int)
   */
  public void invokePointerPressed(int x, int y) {
    pointerPressed(x, y);
  }

  /**
   * Sends a pinter event to the wrapped item.
   * 
   * @see CustomItem#pointerReleased(int, int)
   */
  public void invokePointerReleased(int x, int y) {
    pointerReleased(x, y);
  }
}
