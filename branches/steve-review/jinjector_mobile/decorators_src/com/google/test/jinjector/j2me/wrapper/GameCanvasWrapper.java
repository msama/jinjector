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
import javax.microedition.lcdui.game.GameCanvas;

/**
 * Wrapper for {@link GameCanvas}.
 * 
 * <p>It exposes getter methods for contained {@link Command}s and 
 * {@link CommandListener}s which allows to fire commands.
 * 
 * @see DisplayableWrapper
 * @author Michele Sama
 */
public class GameCanvasWrapper extends GameCanvas 
implements DisplayableWrapper {
  
  private Vector commandListeners = new Vector();
  private Vector commands = new Vector();
  
  /**
   * Constructor from superclass.
   * 
   * @param suppressKeyEvents <code>true</code> if KeyEvents are going to be 
   *    suppressed.
   */
  protected GameCanvasWrapper(boolean suppressKeyEvents) {
    super(suppressKeyEvents);
  }

  /**
   * Adds a {@link Command} to the nested {@link GameCanvas} and to the local 
   * collection.
   * 
   * @param cmd The {@link Command} to add.
   * @see GameCanvas#addCommand(Command)
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
   * Removes a {@link Command} from the nested {@link GameCanvas} and from the 
   * local collection.
   * 
   * @param cmd The {@link Command} to remove.
   * @see GameCanvas#removeCommand(Command)
   */
  public void removeCommand(Command cmd) {
    synchronized (commands) {
      commands.removeElement(cmd);
    }
    super.removeCommand(cmd);
  }

  /**
   * Sets a {@link CommandListener} into the nested {@link GameCanvas} and 
   * stores it into the local collection.
   * 
   * @param listener The {@link CommandListener} to be set.
   * @see GameCanvas#setCommandListener(CommandListener)
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
   * Gets an array containing all the {@link Command} registered in the nested 
   * {@link GameCanvas}.
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
   * Gets an array with all the {@link CommandListener}s registered to the 
   * nested {@link GameCanvas}.
   * 
   * @return An array with all the {@link CommandListener}s
   */
  public CommandListener[] getAllCommandListeners() {
    synchronized (commandListeners) {
      CommandListener[] cmds = new CommandListener[commandListeners.size()];
      commandListeners.copyInto(cmds);
      return cmds;
    }
  }
  
  /**
   * Delegates {@link DisplayableVisitor#toString(Displayable)} to create
   *  a String representation of the nested {@link GameCanvas}. 
   * 
   * @return a string representation of the nested {@link GameCanvas}.
   */
  public String toString() {
    return DisplayableVisitor.toString(this);
  }

  /**
   * Sends a key event directly to the wrapped canvas.
   * 
   * @see javax.microedition.lcdui.Canvas#keyPressed(int)
   */
  public void invokeKeyPressed(int keyCode) {
    keyPressed(keyCode);
  }

  /**
   * Sends a key event directly to the wrapped canvas.
   * 
   * @see javax.microedition.lcdui.Canvas#keyReleased(int)
   */
  public void invokeKeyReleased(int keyCode) {
    keyReleased(keyCode);
  }

  /**
   * Sends a key event directly to the wrapped canvas.
   * 
   * @see javax.microedition.lcdui.Canvas#keyRepeated(int)
   */
  public void invokeKeyRepeated(int keyCode) {
    keyRepeated(keyCode);
  }

  /**
   * Sends a pointer event to the wrapped canvas.
   * 
   * @see javax.microedition.lcdui.Canvas#pointerDragged(int, int)
   */
  protected void invokePointerDragged(int x, int y) {
    pointerDragged(x, y);
  }

  /**
   * Sends a pointer event to the wrapped canvas.
   * 
   * @see javax.microedition.lcdui.Canvas#pointerPressed(int, int)
   */
  protected void invokePointerPressed(int x, int y) {
    pointerPressed(x, y);
  }

  /**
   * Sends a pointer event to the wrapped canvas.
   * 
   * @see javax.microedition.lcdui.Canvas#pointerReleased(int, int)
   */
  protected void invokePointerReleased(int x, int y) {
    pointerReleased(x, y);
  }
  
}
