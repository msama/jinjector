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

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;

/**
 * Defines the base method which a wrapper for {@link Item}s should have.
 * It force subclasses to expose fields.
 * 
 * <p>The purpose of ItemWrapper and its subclasses is to inject a wanted 
 * behavior in all the wrapped instances. At instrumentation time all the new 
 * instruction creating an instance of the wrapped object will be changed into 
 * instances of the wrapper.  
 * 
 * <p>A smarter way to do this would be to inject a 
 * superclass in the common superclass. Unfortunately this is not possible 
 * because the superclass is part of the Framework and it cannot be modified.
 * And also java does not support multiple inheritance. 
 * 
 * <p>This implementation wraps all the instances with a subclass that 
 * implements this interface. Unfortunately all the wrapper will have to 
 * re-implement all the method of this interface.
 * 
 * @see DisplayableWrapper
 * @author Michele Sama
 */
public interface ItemWrapper {

  /**
   * Wrapper method for {@link Item#addCommand(Command)}.
   * 
   * <p>A correct implementation will save a local copy of the argument.
   * 
   * @param cmd The {@link Command} to add.
   * @see Item#addCommand(Command)
   */
  public abstract void addCommand(Command cmd);

  /**
   * Wrapper method for {@link Item#removeCommand(Command)}
   * 
   * @param cmd The {@link Command} to remove.
   * @see Item#removeCommand(Command)
   */
  public abstract void removeCommand(Command cmd);

  /** 
   * Wrapper method for 
   * {@link Item#setItemCommandListener(ItemCommandListener)}.
   * 
   * <p>A correct implementation will save a local copy of the argument.
   * 
   * @param listener The {@link ItemCommandListener} to add.
   * @see Item#setItemCommandListener(ItemCommandListener)
   */
  public abstract void setItemCommandListener(ItemCommandListener listener);

  /**
   * Gets an array containing all the {@link Command} registered into the 
   * wrapped {@link Item}.
   * 
   * @return An array containing all the registered {@link Command}s.
   */
  public abstract Command[] getAllCommands();

  /**
   * Gets an array with all the {@link ItemCommandListener} registered to the 
   * wrapped {@link Item}.
   * 
   * @return an array with all the {@link ItemCommandListener}s
   */
  public abstract ItemCommandListener[] getAllItemCommandListeners();

}
