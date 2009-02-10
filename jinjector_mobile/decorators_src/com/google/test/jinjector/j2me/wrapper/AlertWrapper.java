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

import com.google.test.jinjector.j2me.DisplayableVisitor;

import java.util.Vector;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Image;

/**
 * Wrapper for {@link Alert}.
 * 
 * @see DisplayableWrapper
 * @author Michele Sama
 */
public class AlertWrapper extends Alert implements DisplayableWrapper {

  private Vector commandListeners = new Vector();
  private Vector commands = new Vector();
  
  /**
   * Constructor from superclass.
   * 
   * @param title The title of the {@link Alert}.
   * @param alertText The contained text.
   * @param alertImage The displayed image.
   * @param alertType The type of {@link Alert}.
   */
  public AlertWrapper(String title, String alertText, Image alertImage,
      AlertType alertType) {
    super(title, alertText, alertImage, alertType);
  }

  /**
   * Constructor from superclass.
   * 
   * @param title The title of the {@link Alert}.
   */
  public AlertWrapper(String title) {
    super(title);
  }

  /**
   * Adds a {@link Command} to the internal collection and stores it locally.
   * 
   * @param cmd The {@link Command} to be added to the nested {@link Alert} and
   *    stored locally.
   * @see Alert#addCommand(Command)
   */
  public void addCommand(Command cmd) {
    synchronized (commands) {
      commands.addElement(cmd);
    }
    super.addCommand(cmd);
  }

  /**
   * Removes a {@link Command} from the internal collection and from the 
   * nested {@link Alert}.
   * 
   * @param cmd The {@link Command} to be removed.
   * @see Alert#removeCommand(Command)
   */
  public void removeCommand(Command cmd) {
    synchronized (commands) {
      commands.removeElement(cmd);
    }
    super.removeCommand(cmd);
  }

  /**
   * Adds the {@link CommandListener} to the internal collection and to the 
   * nested {@link Alert}.
   * 
   * @param listener The {@link CommandListener} to be added.
   * @see Alert#setCommandListener(CommandListener)
   */
  public void setCommandListener(CommandListener listener) {
    synchronized (commandListeners) {
      if (!commandListeners.contains(listener)) {
        commandListeners.addElement(listener);
      }
    }
    super.setCommandListener(listener);
  }

  /**
   * Get all registered {@link Command}s from the internal collection.
   * 
   * @return An array containing all the registered {@link Command}s.
   * @see DisplayableWrapper#getAllCommands()
   */
  public Command[] getAllCommands() {
    synchronized (commands) {
      Command[] cmds = new Command[commands.size()];
      commands.copyInto(cmds);
      return cmds;
    }
  }
  
  /**
   * Get all the registered {@link CommandListener}s from the internal 
   * collection.
   * 
   * @return An array containing all the registered {@link CommandListener}s.
   * @see DisplayableWrapper#getAllCommandListeners()
   */
  public CommandListener[] getAllCommandListeners() {
    synchronized (commandListeners) {
      CommandListener[] cmds = new CommandListener[commandListeners.size()];
      commandListeners.copyInto(cmds);
      return cmds;
    }
  }

  /**
   * Delegates {@link DisplayableVisitor#toString(Alert)} to get a String 
   * representation of the nested {@link Alert}. 
   * 
   * @return a string representation of the nested {@link Alert}.
   */
  public String toString() {
    return DisplayableVisitor.toString(this);
  }
}
