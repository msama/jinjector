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

package com.google.test.jinjector.j2me;

import com.google.test.jinjector.j2me.wrapper.DisplayableWrapper;
import com.google.test.jinjector.j2me.wrapper.ItemWrapper;
import com.google.test.jinjector.util.Log;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.midlet.MIDlet;

/**
 * Utility class providing convenience methods for scripting regression 
 * tests for J2ME applications.
 * 
 * @author Michele Sama
 *
 */
public class J2meUtil {

  /**
   *  Specify the delay in milliseconds between each attempt to retrieve a
   *  {@link Displayable}.
   *  
   *  <p>A small delay means faster tests but more computation and a bigger 
   *  impact in the behavior of the original application. A long delay means 
   *  slow tests.
   */
  private static final int DELAY_FOR_GUI_UPDATE = 25;
  
  /**
   * The number of times that a query is performed. 
   * 
   * <p>If a test reaches this limit the test will fail. Queries are repeated 
   * sequentially until they are verified or until the timeout is reached.
   * This has to be big number in order to wait for the standard GUI update 
   * delay. In slow devices this value may need to be increased.
   * 
   * <p>The timeout will be this value multiplied for 
   * {@link #DELAY_FOR_GUI_UPDATE}.
   */
  private static final int NUM_RETRY_FOR_GUI_UPDATE = 1000;

  /**
   * Utility class
   */
  private J2meUtil() {
    // Unininstantiable
  }

  /**
   * Casts the Displayable passed as a parameter in a wrapper 
   * then looks for the command wrapping the wanted action and 
   * fires the action on all the listeners.
   * 
   * @param sourceDisp the source Displayable.
   * @param action the action to fire.
   * @throws ClassCastException if Displayables have not been wrapped 
   *    properly.
   */
  public static void fireCommand(Displayable sourceDisp, String action) {
    DisplayableWrapper wrapper = (DisplayableWrapper) sourceDisp;
   
    CommandListener[] listeners = wrapper.getAllCommandListeners();
    
    Command targetCommand = getCommandFromAction(action, wrapper.getAllCommands());
    // No Command implementing the specific action has been found, return;
    if (targetCommand == null) {
      return;
    }
    
    for (int i = 0; i < listeners.length; i++) {
      CommandListener list = listeners[i];
      list.commandAction(targetCommand, sourceDisp);
    }
  }

  /**
   * Casts the Item passed as parameter in a wrapper 
   * then looks for the command wrapping the wanted action and 
   * fires it on all the listeners.
   * 
   * @param sourceItem the source Item.
   * @param action The action to fire.
   * @throws ClassCastException if Items have not been wrapped 
   *    properly.
   */
  public static void fireCommand(Item sourceItem, String action) { 
    ItemWrapper wrapper = (ItemWrapper) sourceItem;
    
    ItemCommandListener[] listeners = wrapper.getAllItemCommandListeners();
    
    Command targetCommand = getCommandFromAction(action, wrapper.getAllCommands());
    // No Command implementing the specific action has been found, return;
    if (targetCommand == null) {
      return;
    }
    
    // Fires the event to all the listeners
    for (int i = 0; i < listeners.length; i++) {
      ItemCommandListener list = listeners[i];
      list.commandAction(targetCommand, sourceItem);
    }
  }
  
  /**
   * Gets a command that wraps a given action from a given array of commands.
   * 
   * @param action the String used as action name for the event.
   * @param commands an array of commands.
   * @return the {@link Command} wrapping the given action.
   */
  private static Command getCommandFromAction(String action, Command[] commands) {
    for (int i = 0; i < commands.length; i++) {
      Command cmd = commands[i];
      if (cmd.getLabel().equals(action)) {
        return cmd;
      }
    }
    return null;
  }

  /**
   * Get the {@link Displayable} currently on screen for the 
   * given {@link MIDlet}.
   * 
   * @param midlet The {@link MIDlet} to query.
   * @return The {@link Displayable} on screen.
   */
  public static Displayable getCurrentDisplayable(MIDlet midlet) {
    return Display.getDisplay(midlet).getCurrent();
  }

  /**
   * Wait for a instance of {@link Displayable} to appear on screen that 
   * matches the specified type. 
   * 
   * @param midlet The MIDlet on which to query.
   * @param type The type of Displayable to query.
   * @return The instance of the wanted {@link Displayable} or 
   *    <code>null</code> if it could not be retrieved in the specified number 
   *    of attempts.
   */
  public static Displayable waitAndGetDisplayableOfType(MIDlet midlet, 
      Class type) {
    return waitAndGetDisplayableOfType(midlet, 
        type, NUM_RETRY_FOR_GUI_UPDATE, DELAY_FOR_GUI_UPDATE);
  }

  /**
   * Wait for a instance of {@link Displayable} to appear on screen that 
   * matches the specified type. 
   * 
   * @param midlet The MIDlet on which to query.
   * @param type The type of Displayable to query.
   * @param retry The number of attempts.
   * @param delay The delay between each attempt.
   * @return The instance of the wanted {@link Displayable} or 
   *    <code>null</code> if it could not be retrieved in the specified number 
   *    of attempts.
   */
  public static Displayable waitAndGetDisplayableOfType(MIDlet midlet, 
      Class type, int retry, long delay) {
    Displayable disp = null;
    while ((disp = getCurrentDisplayable(midlet)) == null ||
        !type.isAssignableFrom(disp.getClass())) {
      retry--;
      if (retry <= 0) {
        return null;
      }
      try {
        Thread.sleep(delay);
      } catch (InterruptedException e) {
        throw new RuntimeException(e.getMessage());
      }
    }
    return disp;
  }

  /**
   * Logs a list of {@link Command}s registered to the given 
   * {@link DisplayableWrapper}.
   * 
   * @param wrapper the displayable.
   */
  public static void logAllRegisteredCommands(DisplayableWrapper wrapper) {
    StringBuffer buffer = new StringBuffer();
    buffer.append("Commands in displayable: " + wrapper + "\n");
    Command[] commands = wrapper.getAllCommands();
    for(int i = 0; i < commands.length; i++) {
      buffer.append(i +") " + commands[i].getLabel() + "\n");
    }
    Log.log(J2meUtil.class, buffer.toString());
  }
  
  /**
   * Logs a list of {@link Command}s registered to the given 
   * {@link ItemWrapper}.
   * 
   * @param wrapper the item.
   */
  public static void logAllRegisteredCommands(ItemWrapper wrapper) {
    StringBuffer buffer = new StringBuffer();
    buffer.append("Commands in displayable: " + wrapper + "\n");
    Command[] commands = wrapper.getAllCommands();
    for(int i = 0; i < commands.length; i++) {
      buffer.append(i +") " + commands[i].getLabel() + "\n");
    }
    Log.log(J2meUtil.class, buffer.toString());
  }

  /**
   * Logs a list of {@link CommandListener}s registered to the given 
   * {@link DisplayableWrapper}.
   * 
   * @param wrapper the displayable.
   */
  public static void logAllRegisteredListeners(DisplayableWrapper wrapper) {
    StringBuffer buffer = new StringBuffer();
    buffer.append("CommandListeners in displayable: " + wrapper + "\n");
    CommandListener[] listeners = wrapper.getAllCommandListeners();
    for(int i = 0; i < listeners.length; i++) {
      buffer.append(i +") " + listeners[i].toString() + "\n");
    }
    Log.log(J2meUtil.class, buffer.toString());
  }
  
  /**
   * Logs a list of {@link ItemCommandListener}s registered to the given 
   * {@link ItemWrapper}.
   * 
   * @param wrapper the item.
   */
  public static void logAllRegisteredListeners(ItemWrapper wrapper) {
    StringBuffer buffer = new StringBuffer();
    buffer.append("ItemCommandListeners in displayable: " + wrapper + "\n");
    ItemCommandListener[] listeners = wrapper.getAllItemCommandListeners();
    for(int i = 0; i < listeners.length; i++) {
      buffer.append(i +") " + listeners[i].toString() + "\n");
    }
    Log.log(J2meUtil.class, buffer.toString());
  }
}
