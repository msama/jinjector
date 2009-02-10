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

import com.google.test.jinjector.j2me.wrapper.DateFieldWrapper;
import com.google.test.jinjector.j2me.wrapper.FormWrapper;
import com.google.test.jinjector.j2me.wrapper.ListWrapper;
import com.google.test.jinjector.j2me.wrapper.StringItemWrapper;
import com.google.test.jinjector.j2me.wrapper.TextBoxWrapper;
import com.google.test.jinjector.j2me.wrapper.TextFieldWrapper;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.DateField;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextField;

import j2meunit.framework.Test;
import j2meunit.framework.TestCase;
import j2meunit.framework.TestMethod;
import j2meunit.framework.TestSuite;

/**
 * JMEUnit test for testing J2mePlaybackRunnable.
 * 
 * <p>This test try to generate events and verify that they are 
 * handled correctly.
 * 
 * @author Michele Sama
 * 
 */
public class J2meTestCaseTest extends TestCase 
implements CommandListener, ItemCommandListener {

  String commandName = "A";
  String wrongCommandName = "B";
  Command cmd = new Command(commandName, Command.ITEM, 1);
  
  protected Command lastFiredCommand = null;
  protected Item lastItem = null;
  protected Displayable lastDisplayable = null;
  
  /**
   * Creates an instance of this class.
   */
  public J2meTestCaseTest() {
  }

  /**
   * Creates an instance of this class which will execute the 
   * specified {@link TestMethod}.
   * 
   * @param name The name to assign to this test.
   * @param method The {@link TestMethod} to execute
   */
  public J2meTestCaseTest(String name, TestMethod method) {
    super(name, method);
  }

  /**
   * Creates an instance of this class.
   * 
   * @param name The name to assign to this test.
   */
  public J2meTestCaseTest(String name) {
    super(name);
  }

  /**
   * Initialize variables.
   * 
   * @see j2meunit.framework.TestCase#setUp()
   */
  protected void setUp() throws Exception {
    super.setUp();
    commandName = "A";
    wrongCommandName = "B";
    cmd = new Command(commandName, Command.ITEM, 1);
  }
  
  /**
   * <p>This test sequentially creates different ItemWrapper and tries to fire 
   * events, catches them and verifies that they are correct.
   * 
   * <p>The whole procedure is executed with different items to make the test 
   * more reliable.
   */
  public void testFireItemCommand() {
    convenienceFireItemCommand(new StringItemWrapper("item", "text"));
    
    convenienceFireItemCommand(
        new DateFieldWrapper("label", DateField.DATE_TIME));
    
    convenienceFireItemCommand(
        new TextFieldWrapper("label", "", 12, TextField.PASSWORD));
  }
  
  /**
   * Convenience method to test item events.
   * 
   * @param item The instance to use during the test.
   */
  protected void convenienceFireItemCommand(Item item) {
    item.addCommand(cmd);
    item.setItemCommandListener(this);
    J2meUtil.fireCommand(item, commandName);
    assertEquals("Command", commandName, lastFiredCommand.getLabel());
    assertEquals("Item", item, lastItem);
    clear();
    J2meUtil.fireCommand(item, wrongCommandName);
    assertEquals("Command", null, lastFiredCommand);
    assertEquals("Item", null, lastItem);
    clear();
  }

  /**
   * This test sequentially creates different DisplayableWrapper and tries to
   * fire events, catches them and verifies that they are correct.
   * 
   * The whole procedure is executed with different Displayables to make the
   * test more reliable.
   */
  public void testFireDisplayableCommand() {
    convenienceFireDisplayableCommand(new FormWrapper("item"));
    
    convenienceFireDisplayableCommand(
        new ListWrapper("label", List.IMPLICIT));
    
    convenienceFireDisplayableCommand(
        new TextBoxWrapper("label", "", 12, TextField.PASSWORD));
  }
  
  /**
   * Convenience method to test item events
   * @param dsp The instance to use during the test
   */
  protected void convenienceFireDisplayableCommand(Displayable dsp) {
    dsp.addCommand(cmd);
    dsp.setCommandListener(this);
    J2meUtil.fireCommand(dsp, commandName);
    assertEquals("Command", commandName, lastFiredCommand.getLabel());
    assertEquals("Displayable", dsp, lastDisplayable);
    clear();
    J2meUtil.fireCommand(dsp, wrongCommandName);
    assertEquals("Command", null, lastFiredCommand);
    assertEquals("Displayable", null, lastDisplayable);
    clear();
  }
  
  /**
   * Traces the last fired event in order to check if the wrapper is working
   * properly.
   * 
   * @see CommandListener#commandAction(Command, Displayable)
   */
  public void commandAction(Command command, Displayable dsp) {
    lastFiredCommand = command;
    lastDisplayable = dsp;
  }

  /**
   * Traces the last fired event in order to check if the wrapper is working
   * properly.
   * 
   * @see ItemCommandListener#commandAction(Command, Item)
   */
  public void commandAction(Command command, Item item) {
    lastFiredCommand = command;
    lastItem = item;
  }
  
  /**
   * Clear the last fired event
   */
  protected void clear() {
    lastDisplayable = null;
    lastFiredCommand = null;
    lastItem = null;
  }

  /**
   * @see j2meunit.framework.TestCase#suite()
   */
  public Test suite() {
    TestSuite suite = new TestSuite();

    suite.addTest(new J2meTestCaseTest("testFireDisplayableCommand") {
      public void runTest() {
        testFireDisplayableCommand();
      }
    });
    
    suite.addTest(new J2meTestCaseTest("testFireItemCommand") {
      public void runTest() {
        testFireItemCommand();
      }
    });
    
    return suite;
  }
  
}
