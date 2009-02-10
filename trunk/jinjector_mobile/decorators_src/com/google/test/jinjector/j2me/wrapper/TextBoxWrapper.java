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
import javax.microedition.lcdui.TextBox;

/**
 * Wrapper for {@link TextBox}.
 * 
 * @see DisplayableWrapper
 * @author Michele Sama
 */
public class TextBoxWrapper extends TextBox implements DisplayableWrapper {

  private Vector commandListeners = new Vector();
  private Vector commands = new Vector();
  
  /**
   * Constructor from superclass.
   * 
   * @param title the title to set.
   * @param text the text to insert.
   * @param maxSize the maximum size of the text.
   * @param constraints constraints to the accepted input text.
   */
  public TextBoxWrapper(String title, String text, int maxSize, 
      int constraints) {
    super(title, text, maxSize, constraints);
  }

  /**
   * Adds a {@link Command} into the nested {@link TextBox} and into the 
   * local collection.
   * 
   * @param cmd The {@link Command} to add.
   * @see TextBox#addCommand(Command)
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
   * Removes a {@link Command} from the nested {@link TextBox} and from the 
   * local collection. 
   * 
   * @param cmd The {@link Command} to remove.
   * @see TextBox#removeCommand(Command)
   */
  public void removeCommand(Command cmd) {
    synchronized (commands) {
      commands.removeElement(cmd);
    }
    super.removeCommand(cmd);
  }

  /**
   * Sets a {@link CommandListener} into the nested {@link TextBox} and into 
   * the local collection.
   * 
   * @param listener The {@link CommandListener} to set.
   * @see TextBox#setCommandListener(CommandListener)
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
   * Get an array containing all the commands registered in this 
   * {@link TextBox}.
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
   * Get an array with all the listeners registered to this {@link TextBox}.
   * 
   * @return an array with all the listeners
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
   * representation of the nested {@link TextBox}. 
   * 
   * @return a string representation of the nested {@link Textbox}.
   */
  public String toString() {
    return DisplayableVisitor.toString(this);
  }
}
