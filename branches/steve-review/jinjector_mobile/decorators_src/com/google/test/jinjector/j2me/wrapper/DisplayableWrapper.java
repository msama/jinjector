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

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

/**
 * Defines the base method which a Wrapper wrapping a {@link Displayable} 
 * should have in order to force it to expose fields.
 * 
 * <p>The purpose of this class and its subclasses is to inject a wanted 
 * behavior in all the wrapped instances. At instrumentation time, all the 
 * wrapped objects will be changed into instances of the relevant wrapper 
 * e.g. a LCDUI {@link Alert} will be changed into an {@link AlertWrapper} 
 * instance, so the tests can interact with the underlying object.
 * 
 * <p>A smarter way to do this would be to inject a superclass in the common 
 * superclass. Unfortunately this is not possible because the superclass is 
 * part of the Framework and it cannot be modified. And also java does not 
 * support multiple inheritance. However injecting a superlass is the 
 * correct way to wrap instances for classes which are not part of the J2ME SDK.
 * 
 * <p>Another possible implementation would be using a delegate. A delegate 
 * will probably reduce this code but will also require a bigger effort in 
 * instrumenting the original code, so the solution with the lowest impact in 
 * the original byte code has been chosen.
 * 
 * <p>This implementation wraps all the instances with a subclass that 
 * implements this interface. Unfortunately all the wrapper classes have to 
 * re-implement all the methods of this interface. 
 * 
 * @author Michele Sama
 */
public interface DisplayableWrapper {

  /**
   * Wraps {@link Displayable#addCommand(Command)}.
   * 
   * <p> A correct implementation saves {@link Command}s internally.
   */
  public abstract void addCommand(Command cmd);

  /**
   * Wraps {@link Displayable#removeCommand(Command)}.
   */
  public abstract void removeCommand(Command cmd);

  /**
   * Wraps {@link Displayable#setCommandListener(CommandListener)}.
   * 
   * <p>A correct implementation of this method saves added listeners.
   */
  public abstract void setCommandListener(CommandListener l);

  /**
   * Gets an array containing all the commands registered in the wrapped 
   * {@link Displayable}.
   * 
   * @return An array containing all the {@link Command}s added to the wrapped 
   *    {@link Displayable}.
   */
  public abstract Command[] getAllCommands();

  /**
   * Gets an array with all the {@link CommandListener} registered to the 
   * wrapped {@link Displayable}.
   * 
   * @return an array with all the registered {@link CommandListener}s.
   */
  public abstract CommandListener[] getAllCommandListeners();

}
