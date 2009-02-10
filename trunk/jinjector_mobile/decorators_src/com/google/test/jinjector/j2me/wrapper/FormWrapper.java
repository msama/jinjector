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

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;

/**
 * Wrapper for {@link Form}.
 * 
 * @see DisplayableWrapper
 * @author Michele Sama
 */
public class FormWrapper extends Form implements DisplayableWrapper {

  private Vector commandListeners = new Vector();
  private Vector commands = new Vector();
  
  /**
   * Constructor from superclass. 
   * 
   * @param title The title to set.
   * @param items An array of nested {@link Item}s.
   */
  public FormWrapper(String title, Item[] items) {
    super(title, items);
  }

  /**
   * Constructor from superclass.
   * 
   * @param title The title to set.
   */
  public FormWrapper(String title) {
    super(title);
  }

  /**
   * Adds a {@link Command} to the nested {@link Form} and stores it into the 
   * local collection. 
   * 
   * @param cmd The {@link Command} to add.
   * @see Form#addCommand(Command)
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
   * Removes a {@link Command} from the nested {@link Form} and from the local 
   * collection.
   * 
   * @param cmd The {@link Command} to remove.
   * @see Form#removeCommand(Command)
   */
  public void removeCommand(Command cmd) {
    synchronized (commands) {
      commands.removeElement(cmd);
    }
    super.removeCommand(cmd);
  }

  /**
   * Sets a {@link CommandListener} into the nested {@link Form} and adds it 
   * to the local collection.
   * 
   * @param listener The {@link CommandListener} to set.
   * @see Form#setCommandListener(CommandListener)
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
   * Gets an array containing all the {@link Command}s registered in the nested 
   * {@link Form}.
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
   * Gets an array with all the {@link CommandListener}s registered to
   * the nested {@link Form}.
   * 
   * @return An array with all the {@link CommandListener}s.
   */
  public CommandListener[] getAllCommandListeners() {
    synchronized (commandListeners) {
      CommandListener[] cmds = new CommandListener[commandListeners.size()];
      commandListeners.copyInto(cmds);
      return cmds;
    }
  }

  /**
   * Delegates {@link DisplayableVisitor#toString(Displayable)} to get a String 
   * representation of the nested {@link Form}. 
   * 
   * @return a string representation of the nested {@link Form}.
   */
  public String toString() {
    return DisplayableVisitor.toString(this);
  }
}
