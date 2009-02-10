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

import j2meunit.framework.Test;
import j2meunit.framework.TestCase;
import j2meunit.framework.TestMethod;
import j2meunit.framework.TestSuite;

import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;

/**
 * Defines a set of common methods for testing all the subclasses of 
 * {@link DisplayableWrapper} because they implement similar behaviors.
 * 
 * <p>
 * This abstract class provide a set of methods to test those common behaviors.
 * This method must be invoked with the right instance.  
 * 
 * @author Michele Sama
 * @author Rakesh Shah
 */
public class DisplayableWrapperTest extends TestCase {

  private static final String UNOVERRIDED_TOSTRING_ERROR = 
      "Either toString() has not been overridden, or the implementation " +
      "is not consistent.";
  
  final String title = "title";
  final String text = "text";
  
  /**
   * Constructor from superclass.
   */
  public DisplayableWrapperTest() {
  }

  /**
   * Constructor from superclass.
   */
  public DisplayableWrapperTest(String name, TestMethod testMethod) {
    super(name, testMethod);
  }

  /**
   * Constructor from superclass.
   */
  public DisplayableWrapperTest(String name) {
    super(name);
  }

  /**
   * This method will be invoked by test methods for each subclass of 
   * {@link DisplayableWrapper}.
   * 
   * @param wrapper The instance under test
   */
  protected void tryCommandHandling(DisplayableWrapper wrapper) {
    int size = 10;
    for (int i = 0; i < size; i++) {
      addCommandToWrapper(wrapper, "Command" + i);
      assertEquals("Number of commands", i + 1, 
          wrapper.getAllCommands().length);
    }
    
    for (int i = size - 1; i >= 0; i--) {
      wrapper.removeCommand(wrapper.getAllCommands()[i]);
      assertEquals("Number of commands", i, wrapper.getAllCommands().length);
    }
  }

  /**
   * Create a new command and adds it to the specified wrapper.
   * 
   * @param wrapper
   * @param action
   * @return Newly created command
   */
  private Command addCommandToWrapper(DisplayableWrapper wrapper, 
      String action) {
    Command cmd = new Command(action, Command.ITEM, 1);
    wrapper.addCommand(cmd);
    return cmd;
  }
  
  /**
   * This method will be invoked by test methods for each single 
   * subclass of {@link DisplayableWrapper}.
   * 
   * @param wrapper The instance under test
   */
  protected void tryListenerHandling(DisplayableWrapper wrapper) {
    int size = 10;
    for (int i = 0; i < size; i++) {
      wrapper.setCommandListener(new CommandListener() {
        public void commandAction(Command cmd, Displayable dsp) {
        }});
      assertEquals("Number of listeners", i + 1, 
          wrapper.getAllCommandListeners().length);
    }
  }

  /**
   * Verifies that the 
   * {@link AlertWrapper#AlertWrapper(String, String, Image, AlertType)} 
   * properly invokes the wrapped ones.
   */
  public void testAlertWrapper_constructors() {
    Image image = null;
    AlertType type = AlertType.ALARM;
    
    AlertWrapper aw = new AlertWrapper(title);
    assertEquals("Alert Title", title, aw.getTitle());
    
    aw = new AlertWrapper(title, text, image, type);
    assertEquals("Alert Title", title, aw.getTitle());
    assertEquals("Alert Text", text, aw.getString());
    assertEquals("Alert Image", image, aw.getImage());
    assertEquals("Alert Type", type, aw.getType());
  }

  /**
   * Invoke the super method for testing commands with an instance of 
   * AlertWrapper.
   */
  public void testAlertWrapper_commandHandling() {
    AlertWrapper aw = new AlertWrapper(title);
    tryCommandHandling(aw);
  }

  /**
   * Invoke the super method for testing listeners with an instance of 
   * AlertWrapper.
   */
  public void testAlertWrapper_listenerHandling() {
    AlertWrapper aw = new AlertWrapper(title);
    tryListenerHandling(aw);
  }
  
  /**
   * Verifies that the toString method has been overridden.
   */
  public void testAlertWrapper_toString() {
    AlertWrapper aw = new AlertWrapper(title);
    assertEquals(UNOVERRIDED_TOSTRING_ERROR, 
        DisplayableVisitor.toString(aw), aw.toString());
  }
  
  /**
   * Verifies the addCommand method in AlertWrapper.
   */
  public void testAlertWrapper_addCommand() {
    AlertWrapper cw = new AlertWrapper(title);
    tryAddCommands(cw);
   }
  
  /**
   * Test to very that the wrapped constructor properly invokes the wrapped 
   * methods.
   */
  public void testFormWrapper_constructors() {
    StringItem stringItem0 = new StringItem("foo1", "foo");
    StringItem stringItem1 = new StringItem("foo2", "foo");
    Item[] items = new Item[]{stringItem0, stringItem1};
    
    FormWrapper fw = new FormWrapper(title);
    assertEquals("Form Title", title, fw.getTitle());
    
    fw = new FormWrapper(title, items);
    assertEquals("Form Title", title, fw.getTitle());
    assertEquals("Items", stringItem0, fw.get(0));
    assertEquals("Items", stringItem1, fw.get(1));
  }
  
  /**
   * Invoke the super method for testing commands with an instance of 
   * FormWrapper.
   */
  public void testFormWrapper_commandHandling() {
    FormWrapper fw = new FormWrapper("form");
    tryCommandHandling(fw);
  }
  
  /**
   * Invoke the super method for testing listeners with an instance of 
   * FormWrapper.
   */
  public void testFormWrapper_listenerHandling() {
    FormWrapper fw = new FormWrapper("form");
    tryListenerHandling(fw);
  }
  
  /**
   * Verifies that the toString method has been overridden.
   */  
  public void testFormWrapper_toString() {
    FormWrapper fw = new FormWrapper(title);
    assertEquals(UNOVERRIDED_TOSTRING_ERROR, 
        DisplayableVisitor.toString(fw), fw.toString());
  }
  
  /**
   * Verifies the addCommand method in FormWrapper.
   */
  public void testFormWrapper_addCommand() {
    FormWrapper fw = new FormWrapper("form");
    tryAddCommands(fw);
   }
  
  /**
   * Invoke the super method for testing commands with an instance of 
   * DummyGameCanvasWrapper.
   */
  public void testGameCanvasWrapper_commandHandling() {
    DummyGameCanvasWrapper cw = new DummyGameCanvasWrapper();
    tryCommandHandling(cw);
  }
  
  /**
   * Invoke the super method for testing listeners with an instance of 
   * DummyGameCanvasWrapper.
   */
  public void testGameCanvasWrapper_listenerHandling() {
    DummyGameCanvasWrapper cw = new DummyGameCanvasWrapper();
    tryListenerHandling(cw);
  }

  /**
   * Verifies that the toString method has been overridden.
   */
  public void testGameCanvasWrapper_toString() {
    GameCanvasWrapper fw = new GameCanvasWrapper(false);
    assertEquals(UNOVERRIDED_TOSTRING_ERROR, 
        DisplayableVisitor.toString(fw), fw.toString());
  }
  
  /**
   * Verifies the addCommand method in GameCanvasWrapper.
   */
  public void testGameCanvasWrapper_addCommand() {
    DummyGameCanvasWrapper cw = new DummyGameCanvasWrapper();
    tryAddCommands(cw);
   }
  
  /**
   * Test to very that the wrapped constructor properly invokes the wrapped 
   * ones.
   */
  public void testListWrapper_constructors() {
    int type = List.IMPLICIT;
    String[] elements = new String[] {"a", "b", "c"};
    
    ListWrapper lw = new ListWrapper(title, type);
    assertEquals("List Title", title, lw.getTitle());
    
    lw = new ListWrapper(title, type, elements, null);
    assertEquals("List Title", title, lw.getTitle());
    assertEquals("List Items 0", elements[0], lw.getString(0));
    assertEquals("List Items 1", elements[1], lw.getString(1));
    assertEquals("List Items 2", elements[2], lw.getString(2));
  }
  
  /**
   * Invoke the super method for testing commands with an instance of 
   * ListWrapper.
   */
  public void testListWrapper_commandHandling() {
    int type = List.IMPLICIT;   
    ListWrapper lw = new ListWrapper(title, type);
    tryCommandHandling(lw);
  }
  
  /**
   * Invoke the super method for testing listeners with an instance of 
   * ListWrapper.
   */
  public void testListWrapper_listenerHandling() {
    int type = List.IMPLICIT;
    ListWrapper lw = new ListWrapper(title, type);
    tryListenerHandling(lw);
  }

  /**
   * Verifies that the toString method has been overridden.
   */
  public void testListWrapper_toStringNoSelection() {
    int type = List.IMPLICIT;
    ListWrapper fw = new ListWrapper(title, type);
    assertEquals(UNOVERRIDED_TOSTRING_ERROR, 
        DisplayableVisitor.toString(fw), fw.toString());
  }
  
  /**
   * Verifies the addCommand method in ListWrapper.
   */
  public void testListWrapper_addCommand() {
    int type = List.IMPLICIT;   
    ListWrapper lw = new ListWrapper(title, type);
    tryAddCommands(lw);
   }
  
  /**
   * Test to very that the wrapped constructor properly invokes the wrapped 
   * methods.
   */
  public void testTextBoxWrapper_constructors() {
    TextBoxWrapper tw = new TextBoxWrapper(title, "text", 
        50, TextField.PASSWORD);
    assertEquals("Form Title", title, tw.getTitle());
    assertEquals("Form Text", text, tw.getString());
    assertEquals("Form Size", 50, tw.getMaxSize());
    assertEquals("Form Constraint", TextField.PASSWORD, tw.getConstraints());
  }
  
  /**
   * Invoke the super method for testing commands with an instance of 
   * TextBoxWrapper.
   */
  public void testTextBoxWrapper_commandHandling() {
    TextBoxWrapper tw = new TextBoxWrapper(title, text, 
        50, TextField.PASSWORD);
    tryCommandHandling(tw);
  }
  
  /**
   * Invoke the super method for testing listeners with an instance of 
   * TextBoxWrapper.
   */
  public void testTextBoxWrapper_listenerHandling() {
    TextBoxWrapper tw = new TextBoxWrapper(title, text, 
        50, TextField.PASSWORD);
    tryListenerHandling(tw);
  }
  
  /**
   * Verifies that the toString method has been overridden.
   */
  public void testTextBoxWrapper_toString() {
    TextBoxWrapper fw = new TextBoxWrapper(title, text, 
        50, TextField.PASSWORD);
    assertEquals(UNOVERRIDED_TOSTRING_ERROR, 
        DisplayableVisitor.toString(fw), fw.toString());
  }
  
  /**
   * Verifies the addCommand method in TextBoxWrapper.
   */
  public void testTextBoxWrapper_addCommand() {
    TextBoxWrapper tw = new TextBoxWrapper(title, text, 
        50, TextField.PASSWORD);
    tryAddCommands(tw);
   }
  
  /**
   * Invoke the super method for testing commands with an instance of 
   * CanvasWrapper.
   */
  public void testCanvasWrapper_commandHandling() {
    DummyCanvasWrapper cw = new DummyCanvasWrapper();
    tryCommandHandling(cw);
  }
  
  /**
   * Invoke the super method for testing listeners with an instance of 
   * CanvasWrapper.
   */
  public void testCanvasWrapper_listenerHandling() {
    DummyCanvasWrapper cw = new DummyCanvasWrapper();
    tryListenerHandling(cw);
  }
  
  /**
   * Verifies that the toString method has been overridden.
   */
  public void testCanvasWrapper_toString() {
    DummyCanvasWrapper fw = new DummyCanvasWrapper();
    assertEquals(UNOVERRIDED_TOSTRING_ERROR, 
        DisplayableVisitor.toString(fw), fw.toString());
  }
  
  /**
   * Verifies the addCommand method in CanvasWrapper.
   */
  public void testCanvasWrapper_addCommand() {
    DummyCanvasWrapper cw = new DummyCanvasWrapper();
    tryAddCommands(cw);
   }
  
  /**
   * Verifies the addCommand method in the Wrapper classes.
   */
  protected void tryAddCommands(DisplayableWrapper wrapperUnderTest) {
    Command cmd = null;
    Command[] registeredCommands = null;
    
    registeredCommands = wrapperUnderTest.getAllCommands();
    assertEquals("The initial list of command must be empty.", 0, 
        registeredCommands.length);
    
    cmd = addCommandToWrapper(wrapperUnderTest, "Action1");
    registeredCommands = wrapperUnderTest.getAllCommands();
    assertEquals("Command add failed.", 1, 
        registeredCommands.length);
    assertEquals("Wrong reference in added command.", cmd, 
        registeredCommands[0]);
  }

  /**
   * CanvasWrapper is abstract. To test it we need a dummy implementation.
   * 
   * @author Michele Sama
   *
   */
  protected class DummyCanvasWrapper extends CanvasWrapper {

    protected void paint(Graphics arg0) {     
    }
    
  }
  
  /**
   * GameCanvasWrapper has a protected constructor. 
   * To test it we need a dummy implementation
   * 
   * @author Michele Sama
   *
   */
  protected class DummyGameCanvasWrapper extends GameCanvasWrapper {

    protected DummyGameCanvasWrapper() {
      super(true);
    }
  } 
  
  /**
   * @see j2meunit.framework.TestCase#suite()
   */
  public Test suite() {
    TestSuite suite = new TestSuite();

    // Alert
    suite.addTest(new DisplayableWrapperTest("testAlertWrapper_addCommand") {
      public void runTest() {
        testAlertWrapper_addCommand();
      }
    });
    
    suite.addTest(new DisplayableWrapperTest(
        "testAlertWrapper_commandHandling") {
      public void runTest() {
        testAlertWrapper_commandHandling();
      }
    });
    
    suite.addTest(new DisplayableWrapperTest("testAlertWrapper_constructors") {
      public void runTest() {
        testAlertWrapper_constructors();
      }
    });
    
    suite.addTest(new DisplayableWrapperTest(
        "testAlertWrapper_listenerHandling") {
      public void runTest() {
        testAlertWrapper_listenerHandling();
      }
    });
    
    suite.addTest(new DisplayableWrapperTest("testAlertWrapper_toString") {
      public void runTest() {
        testAlertWrapper_toString();
      }
    });
    
    // Canvas
    suite.addTest(new DisplayableWrapperTest("testCanvasWrapper_addCommand") {
      public void runTest() {
        testCanvasWrapper_addCommand();
      }
    });
    
    suite.addTest(new DisplayableWrapperTest(
        "testCanvasWrapper_commandHandling") {
      public void runTest() {
        testCanvasWrapper_commandHandling();
      }
    });
    
    suite.addTest(new DisplayableWrapperTest(
        "testCanvasWrapper_listenerHandling") {
      public void runTest() {
        testCanvasWrapper_listenerHandling();
      }
    });
    
    suite.addTest(new DisplayableWrapperTest("testCanvasWrapper_toString") {
      public void runTest() {
        testCanvasWrapper_toString();
      }
    });
    
    // Form
    suite.addTest(new DisplayableWrapperTest("testFormWrapper_addCommand") {
      public void runTest() {
        testFormWrapper_addCommand();
      }
    });
    
    suite.addTest(new DisplayableWrapperTest(
        "testFormWrapper_commandHandling") {
      public void runTest() {
        testFormWrapper_commandHandling();
      }
    });
    
    suite.addTest(new DisplayableWrapperTest("testFormWrapper_constructors") {
      public void runTest() {
        testFormWrapper_constructors();
      }
    });
    
    suite.addTest(new DisplayableWrapperTest(
        "testFormWrapper_listenerHandling") {
      public void runTest() {
        testFormWrapper_listenerHandling();
      }
    });
    
    suite.addTest(new DisplayableWrapperTest("testFormWrapper_toString") {
      public void runTest() {
        testFormWrapper_toString();
      }
    });
    
    // GameCanvas
    suite.addTest(new DisplayableWrapperTest(
        "testGameCanvasWrapper_addCommand") {
      public void runTest() {
        testGameCanvasWrapper_addCommand();
      }
    });
    
    suite.addTest(new DisplayableWrapperTest(
        "testGameCanvasWrapper_commandHandling") {
      public void runTest() {
        testGameCanvasWrapper_commandHandling();
      }
    });
    
    suite.addTest(new DisplayableWrapperTest(
        "testGameCanvasWrapper_listenerHandling") {
      public void runTest() {
        testGameCanvasWrapper_listenerHandling();
      }
    });
    
    suite.addTest(new DisplayableWrapperTest("testGameCanvasWrapper_toString") {
      public void runTest() {
        testGameCanvasWrapper_toString();
      }
    });
    
    // List
    suite.addTest(new DisplayableWrapperTest("testListWrapper_addCommand") {
      public void runTest() {
        testListWrapper_addCommand();
      }
    });
    
    suite.addTest(new DisplayableWrapperTest(
        "testListWrapper_commandHandling") {
      public void runTest() {
        testListWrapper_commandHandling();
      }
    });
    
    suite.addTest(new DisplayableWrapperTest("testListWrapper_constructors") {
      public void runTest() {
        testListWrapper_constructors();
      }
    });
    
    suite.addTest(new DisplayableWrapperTest(
        "testListWrapper_listenerHandling") {
      public void runTest() {
        testListWrapper_listenerHandling();
      }
    });
    
    suite.addTest(new DisplayableWrapperTest(
        "testListWrapper_toStringNoSelection") {
      public void runTest() {
        testListWrapper_toStringNoSelection();
      }
    });
    
    // TextBox
    suite.addTest(new DisplayableWrapperTest("testTextBoxWrapper_addCommand") {
      public void runTest() {
        testTextBoxWrapper_addCommand();
      }
    });
    
    suite.addTest(new DisplayableWrapperTest(
        "testTextBoxWrapper_commandHandling") {
      public void runTest() {
        testTextBoxWrapper_commandHandling();
      }
    });
    
    suite.addTest(new DisplayableWrapperTest("testTextBoxWrapper_constructors") {
      public void runTest() {
        testTextBoxWrapper_constructors();
      }
    });
    
    suite.addTest(new DisplayableWrapperTest(
        "testTextBoxWrapper_listenerHandling") {
      public void runTest() {
        testTextBoxWrapper_listenerHandling();
      }
    });
    
    suite.addTest(new DisplayableWrapperTest("testTextBoxWrapper_toString") {
      public void runTest() {
        testTextBoxWrapper_toString();
      }
    });
    
    return suite;
  }
}
