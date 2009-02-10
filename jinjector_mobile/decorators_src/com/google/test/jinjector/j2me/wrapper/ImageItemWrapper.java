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
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.ImageItem;
import javax.microedition.lcdui.ItemCommandListener;

/**
 * Wrapper for {@link ImageItem}.
 * 
 * @see ItemWrapper
 * @author Michele Sama
 *
 */
public class ImageItemWrapper extends ImageItem implements ItemWrapper {

  private Vector itemCommandListeners = new Vector();
  private Vector commands = new Vector();
  
  /**
   * Constructor from superclass.
   * 
   * @param label The label to set.
   * @param img The image to set.
   * @param layout The wanted layout.
   * @param altText The alternative text.
   */
  public ImageItemWrapper(String label, Image img, int layout, String altText) {
    super(label, img, layout, altText);

  }

  /**
   * Constructor from superclass.
   * 
   * @param label The label to set.
   * @param img The image to set.
   * @param layout The wanted layout.
   * @param altText The alternative text.
   * @param appearanceMode The appearence mode.
   */
  public ImageItemWrapper(String label, Image img, int layout,
      String altText, int appearanceMode) {
    super(label, img, layout, altText, appearanceMode);

  }
  
  /**
   * Adds a {@link Command} into the nested {@link ImageItem} and into the 
   * local collection.
   * 
   * @param cmd The {@link Command} to add.
   * @see ImageItem#addCommand(Command)
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
   * Removes a {@link Command} from the nested {@link ImageItem} and from the 
   * local collection.
   * 
   * @param cmd The {@link Command} to remove.
   * @see ImageItem#removeCommand(Command)
   */
  public void removeCommand(Command cmd) {
    synchronized (commands) {
      commands.removeElement(cmd);
    }
    super.removeCommand(cmd);
  }

  /**
   * Sets an {@link ItemCommandListener} into the nested {@link ImageItem} and 
   * into the local collection.
   * 
   * @param listener The {@link ItemCommandListener} to set.
   * @see ImageItem#setItemCommandListener(ItemCommandListener)
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
   * {@link ImageItem}.
   * 
   * @return An array containing all the registered {@link Command}.
   */
  public Command[] getAllCommands() {
    synchronized (commands) {
      Command[] cmds = new Command[commands.size()];
      commands.copyInto(cmds);
      return cmds;
    }
  }
  
  /**
   * Get an array with all the {@link ItemCommandListener}s registered in the 
   * nested  {@link ImageItem}.
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
   * Delegates {@link ItemVisitor#toString(javax.microedition.lcdui.Item)} 
   * to get a String representation of the nested {@link ImageItem}. 
   * 
   * @return a string representation of the nested {@link ImageItem}.
   */
  public String toString() {
    return ItemVisitor.toString(this);
  }
}
