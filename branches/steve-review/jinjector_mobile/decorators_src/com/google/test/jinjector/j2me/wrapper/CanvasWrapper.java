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

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

/**
 * Wrapper class for {@link Canvas}.
 * 
 * <p>This class contains methods to expose protected methods of the parent 
 * class. However those methods are not overriding the super methods to avoid 
 * verification problems. Specifically this wrapper will be instrumented as a 
 * super class of classes extending from {@link Canvas}. If those derived class
 * override one of the protected methods and leave it protected, then after the
 * instrumentation there will be protected methods overriding public ones which
 * is not allowed by the JVM.
 * 
 * @see DisplayableWrapper
 * @author Michele Sama
 */
public abstract class CanvasWrapper extends Canvas 
    implements DisplayableWrapper {  
  
  private Vector commandListeners = new Vector();
  private Vector commands = new Vector();
  
  /**
   * Constructor from superclass
   */
  protected CanvasWrapper() {
    super();
  }

  /**
   * Register a {@link Command} in the nested {@link Canvas} and stores it in 
   * the local collection.
   * 
   * @param cmd The {@link Command} to be added.
   * @see Canvas#addCommand(Command)
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
   * Removes a {@link Command} from the nested {@link Canvas} and from the local
   * collection. 
   * 
   * @param cmd The {@link Command} to be removed.
   * @see Canvas#removeCommand(Command)
   */
  public void removeCommand(Command cmd) {
    synchronized (commands) {
      commands.removeElement(cmd);
    }
    super.removeCommand(cmd);
  }

  /**
   * Adds a {@link CommandListener} to the nested {@link Canvas} and to the 
   * local collection.
   * 
   * @param listener The {@link CommandListener} to be added.
   * @see Canvas#setCommandListener(CommandListener)
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
   * Gets an array containing all the {@link Command} registered in this 
   * {@link Canvas}.
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
   * Gets an array with all the {@link CommandListener}s registered to this 
   * {@link Canvas}.
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
   * representation of the nested {@link Canvas}. 
   * 
   * @return a string representation of the nested {@link Canvas}.
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
  public void invokePointerDragged(int x, int y) {
    pointerDragged(x, y);
  }

  /**
   * Sends a pointer event to the wrapped canvas.
   * 
   * @see javax.microedition.lcdui.Canvas#pointerPressed(int, int)
   */
  public void invokePointerPressed(int x, int y) {
    pointerPressed(x, y);
  }

  /**
   * Sends a pointer event to the wrapped canvas.
   * 
   * @see javax.microedition.lcdui.Canvas#pointerReleased(int, int)
   */
  public void invokePointerReleased(int x, int y) {
    pointerReleased(x, y);
  }

}
