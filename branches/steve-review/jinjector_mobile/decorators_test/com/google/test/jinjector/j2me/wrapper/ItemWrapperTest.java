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

import com.google.test.jinjector.j2me.ItemVisitor;
import com.google.test.jinjector.j2me.J2meTestCaseTest;

import java.util.Calendar;
import java.util.TimeZone;

import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.DateField;
import javax.microedition.lcdui.ImageItem;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;

import j2meunit.framework.Test;
import j2meunit.framework.TestCase;
import j2meunit.framework.TestMethod;
import j2meunit.framework.TestSuite;

/**
 * Tests {@link ChoiceGroupWrapper}, {@link DateFieldWrapper}, 
 * {@link GaugeWrapper}, {@link ImageItemWrapper}, {@link SpacerWrapper},
 * {@link StringItemWrapper}, {@link TextFieldWrapper}.
 * 
 * <p>All of those classes are the duplicated implementation of the same 
 * concept. We have integrated the tests for the various classes here to
 * reduce the number of files
 * 
 * @author Michele Sama
 * @author Rakesh Shah
 */
public class ItemWrapperTest extends TestCase {

  private String text = "text";
  private String label = "label";
  private static final String UNOVERRIDED_TOSTRING_ERROR =
      "Either toString() has not been overridden, or the implementation " +
      "is not consistent.";
  
  /**
   * Constructor from superclass.
   */
  public ItemWrapperTest() {
  }

  /**
   * Constructor from superclass.
   */
  public ItemWrapperTest(String name, TestMethod testMethod) {
    super(name, testMethod);
  }

  /**
   * Constructor from superclass.
   */
  public ItemWrapperTest(String name) {
    super(name);
  }

  
  /**
   * Create a new command and adds it to the specified wrapper.
   * 
   * @param wrapper
   * @param action
   * @return Newly created command
   */
  private Command addCommandToWrapper(ItemWrapper wrapper, 
      String action) {
    Command cmd = new Command(action, Command.ITEM, 1);
    wrapper.addCommand(cmd);
    return cmd;
  }
  
  /**
   * This method will be invoked by test methods for each single 
   * subclass of DisplayableWrapper
   * 
   * @param wrapper The instance under test
   */
  public void tryCommandHandling(ItemWrapper wrapper) {
    int size = 10;
    for (int i = 0; i < size; i++) {
      wrapper.addCommand(new Command("Command" + i, Command.ITEM, 1));
      assertEquals("Number of commands", i + 1, 
          wrapper.getAllCommands().length);
    }
    
    for (int i = size - 1; i >= 0; i--) {
      wrapper.removeCommand(wrapper.getAllCommands()[i]);
      assertEquals("Number of commands", i, wrapper.getAllCommands().length);
    }
  }
  
  /**
   * This method will be invoked by test methods for each single 
   * subclass of ItemWrapper
   * 
   * @param wrapper The instance under test
   */
  public void tryListenerHandling(ItemWrapper wrapper) {
    int size = 10;
    for (int i = 0; i < size; i++) {
      wrapper.setItemCommandListener(new ItemCommandListener() {
        public void commandAction(Command arg0, Item arg1) {
        }
      });
      assertEquals("Number of listeners", i + 1, 
          wrapper.getAllItemCommandListeners().length);
    }
  }
  
  /**
   * Test to very that the wrapped constructor properly invokes the wrapped 
   * ones.
   */
  public void testImageItemWrapper_constructors() {
    ImageItemWrapper iw = new ImageItemWrapper(label, null, 
        ImageItem.LAYOUT_CENTER, text);
    assertEquals("ImageItem Label", label, iw.getLabel());
    assertEquals("ImageItem Layout", ImageItem.LAYOUT_CENTER, iw.getLayout());
    assertEquals("ImageItem Text", text, iw.getAltText());
  }
  
  /**
   * Invoke the super method for testing commands with an instance of 
   * ImageItemWrapper.
   */
  public void testImageItemWrapper_commandHandling() {
    ImageItemWrapper iw = new ImageItemWrapper(label, null, 
        ImageItem.LAYOUT_CENTER, text);
    tryCommandHandling(iw);
  }
  
  /**
   * Invoke the super method for testing listeners with an instance of 
   * ImageItemWrapper.
   */
  public void testImageItemWrapper_listenerHandling() {
    ImageItemWrapper iw = new ImageItemWrapper(label, null, 
        ImageItem.LAYOUT_CENTER, text);
    tryListenerHandling(iw);
  }
  
  /**
   * Verifies that the toString method has been overridden.
   */
  public void testImageItemWrapper_toString() {
    ImageItemWrapper fw = new ImageItemWrapper(label, null, 
        ImageItem.LAYOUT_CENTER, text);
    assertEquals(UNOVERRIDED_TOSTRING_ERROR, 
        ItemVisitor.toString(fw), fw.toString());
  }
  
  /**
   * Test to very that the wrapped constructor properly invokes the wrapped 
   * ones.
   */
  public void testChoiceGroupWrapper_constructors() {
    String[] elements = new String[] {"a", "b", "c"};
    ChoiceGroupWrapper cw = new ChoiceGroupWrapper(label, 
        ChoiceGroup.MULTIPLE);
    assertEquals("Choice Label", label, cw.getLabel());
    
    cw = new ChoiceGroupWrapper(label, ChoiceGroup.MULTIPLE, elements, null);
    assertEquals("Choice Label", label, cw.getLabel());
    for (int i = 0; i < elements.length; i++) {
      assertEquals("Choice Items", elements[i], cw.getString(i));
    }
  }
  
  /**
   * Invoke the super method for testing commands with an instance of 
   * ChoiceGroupWrapper.
   */
  public void testChoiceGroupWrapper_commandHandling() {
    ChoiceGroupWrapper cw = new ChoiceGroupWrapper(label, 
        ChoiceGroup.MULTIPLE);
    tryCommandHandling(cw);
  }
  
  /**
   * Invoke the super method for testing listeners with an instance of 
   * ChoiceGroupWrapper.
   */
  public void testChoiceGroupWrapper_listenerHandling() {
    ChoiceGroupWrapper cw = new ChoiceGroupWrapper(label, 
        ChoiceGroup.MULTIPLE);
    tryListenerHandling(cw);
  }
 
  /**
   * Verifies that the toString method does not fail if no entry is selected.
   */
  public void testChoiceGroupWrapper_toStringNoSelection() {
    ChoiceGroupWrapper choiceGroup = new ChoiceGroupWrapper(label, 
        ChoiceGroup.MULTIPLE);
    assertEquals(UNOVERRIDED_TOSTRING_ERROR, 
        ItemVisitor.toString(choiceGroup), choiceGroup.toString());
  }
  
  /**
   * Verifies that the toString method has been overridden.
   */
  public void testChoiceGroupWrapper_toString() {
    ChoiceGroupWrapper choiceGroup = new ChoiceGroupWrapper(label, 
        ChoiceGroup.MULTIPLE);
    choiceGroup.append(text, null);
    assertEquals(UNOVERRIDED_TOSTRING_ERROR, 
        ItemVisitor.toString(choiceGroup), choiceGroup.toString());
  }
  
  /**
   * Verifies that the wrapped constructor properly invokes the wrapped 
   * ones.
   */
  public void testDateFieldWrapper_constructors() {
    TimeZone timeZone = TimeZone.getDefault();
    DateFieldWrapper dw = new DateFieldWrapper(label, ChoiceGroup.IMPLICIT);
    assertEquals("DateField Label", label, dw.getLabel());
    assertEquals("DateField mode", ChoiceGroup.IMPLICIT, dw.getInputMode());
    
    dw = new DateFieldWrapper(label, ChoiceGroup.IMPLICIT, timeZone);
    assertEquals("DateField Label", label, dw.getLabel());
    assertEquals("DateField Mode", ChoiceGroup.IMPLICIT, dw.getInputMode());
  }
  
  /**
   * Invoke the super method for testing commands with an instance of 
   * DateFieldWrapper.
   */
  public void testDateFieldWrapper_commandHandling() {
    DateFieldWrapper dw = new DateFieldWrapper(label, ChoiceGroup.IMPLICIT);
    tryCommandHandling(dw);
  }
  
  /**
   * Invoke the super method for testing listeners with an instance of 
   * DateFieldWrapper.
   */
  public void testDateFieldWrapper_listenerHandling() {
    DateFieldWrapper dw = new DateFieldWrapper(label, ChoiceGroup.IMPLICIT);
    tryListenerHandling(dw);
  }
  
  /**
   * Verifies that the toString method when no date is set.
   */
  public void testDateFieldWrapper_toStringNoDate() {
    DateFieldWrapper dateField = 
        new DateFieldWrapper(label, DateField.DATE_TIME);
    assertEquals(UNOVERRIDED_TOSTRING_ERROR, 
        ItemVisitor.toString(dateField), dateField.toString());
  }
  
  /**
   * Verifies that the toString method has been overridden.
   */
  public void testDateFieldWrapper_toString() {
    DateFieldWrapper dateField = 
        new DateFieldWrapper(label, DateField.DATE_TIME);
    dateField.setDate(Calendar.getInstance().getTime());
    assertEquals(UNOVERRIDED_TOSTRING_ERROR, 
        ItemVisitor.toString(dateField), dateField.toString());
  }
  
  /**
   * Verifies the addCommand method in ChoiceGroupWrapper.
   */
  public void testChoiceGroupWrapper_addCommand() {
    ChoiceGroupWrapper cw = new ChoiceGroupWrapper(text, ChoiceGroup.EXCLUSIVE);
    tryAddCommands(cw);
   }
 
  /**
   * Verifies the addCommand method in DateFieldWrapper.
   */
  public void testDateFieldWrapper_addCommand() {
    DateFieldWrapper dw = new DateFieldWrapper(label, ChoiceGroup.IMPLICIT);
    tryAddCommands(dw);
   }
  
  /**
   * Verifies the addCommand method in GaugeWrapper.
   */
  public void testGaugeWrapper_addCommand() {
    GaugeWrapper gw = new GaugeWrapper(label, true, 10, 1);
    tryAddCommands(gw);
   }
  
  /**
   * Verifies the addCommand method in ImageItemWrapper.
   */
  public void testImageItemWrapper_addCommand() {
    ImageItemWrapper iw = new ImageItemWrapper(label, null, 
        ImageItem.LAYOUT_CENTER, text);
    tryAddCommands(iw);
   }
  
  /**
   * Verifies the addCommand method in SpacerWrapper.
   */
  public void testSpacerWrapper_addCommand() {
    SpacerWrapper sw = new SpacerWrapper(1, 10);
    try {
      addCommandToWrapper(sw, "action");
      fail("Adding a command to a Spacer should throw an exception.");
    } catch (IllegalStateException ex){
      // Ok      
    }
   }
  
  /**
   * Verifies the addCommand method in StringItemWrapper.
   */
  public void testStringItemWrapper_addCommand() {
    StringItemWrapper sw = new StringItemWrapper(label, text, StringItem.PLAIN);
    tryAddCommands(sw);
   }
  
  /**
   * Verifies the addCommand method in TextFieldWrapper.
   */
  public void testTextFieldWrapper_addCommand() {
    final int maxFieldSize = text.length() + 10;
    TextFieldWrapper tw = new TextFieldWrapper(label, text, maxFieldSize,
        TextField.ANY);
    tryAddCommands(tw);
   }
  
  /**
   * Verifies the addCommand method in the Wrapper classes.
   */
  protected void tryAddCommands(ItemWrapper testWrapper) {
    String commandName = "A";
    Command cmd0 = new Command("C0", Command.ITEM, 1);
    Command cmd1 = new Command("C1", Command.ITEM, 1);
    Command cmd2 = new Command("C2", Command.ITEM, 1);
    
    Command[] registeredCommands = null;
    
    registeredCommands = testWrapper.getAllCommands();
    assertEquals("The initial list of command must be empty.", 0, 
        registeredCommands.length);
    
    testWrapper.addCommand(cmd0);
    registeredCommands = testWrapper.getAllCommands();
    assertEquals("Command add failed.", 1, 
        registeredCommands.length);
    assertEquals("Wrong reference in added command.", cmd0, 
        registeredCommands[0]);
  }  
  
  /**
   * @see j2meunit.framework.TestCase#suite()
   */
  public Test suite() {
    TestSuite suite = new TestSuite();

    // GroupWrapper
    suite.addTest(new J2meTestCaseTest("testChoiceGroupWrapper_addCommand") {
      public void runTest() {
        testChoiceGroupWrapper_addCommand();
      }
    });
    
    suite.addTest(new J2meTestCaseTest(
        "testChoiceGroupWrapper_commandHandling") {
      public void runTest() {
        testChoiceGroupWrapper_commandHandling();
      }
    });
    
    suite.addTest(new J2meTestCaseTest("testChoiceGroupWrapper_constructors") {
      public void runTest() {
        testChoiceGroupWrapper_constructors();
      }
    });
    
    suite.addTest(new J2meTestCaseTest(
        "testChoiceGroupWrapper_listenerHandling") {
      public void runTest() {
        testChoiceGroupWrapper_listenerHandling();
      }
    });
    
    suite.addTest(new J2meTestCaseTest("testChoiceGroupWrapper_toString") {
      public void runTest() {
        testChoiceGroupWrapper_addCommand();
      }
    });
    
    suite.addTest(new J2meTestCaseTest(
        "testChoiceGroupWrapper_toStringNoSelection") {
      public void runTest() {
        testChoiceGroupWrapper_toStringNoSelection();
      }
    });
    
    // DateField
    suite.addTest(new J2meTestCaseTest("testDateFieldWrapper_addCommand") {
      public void runTest() {
        testDateFieldWrapper_addCommand();
      }
    });
    
    suite.addTest(new J2meTestCaseTest("testDateFieldWrapper_commandHandling") {
      public void runTest() {
        testDateFieldWrapper_commandHandling();
      }
    });
    
    suite.addTest(new J2meTestCaseTest("testDateFieldWrapper_constructors") {
      public void runTest() {
        testDateFieldWrapper_constructors();
      }
    });
    
    suite.addTest(new J2meTestCaseTest(
        "testDateFieldWrapper_listenerHandling") {
      public void runTest() {
        testDateFieldWrapper_listenerHandling();
      }
    });
    
    suite.addTest(new J2meTestCaseTest("testDateFieldWrapper_toString") {
      public void runTest() {
        testDateFieldWrapper_addCommand();
      }
    });
    
    suite.addTest(new J2meTestCaseTest("testDateFieldWrapper_toStringNoDate") {
      public void runTest() {
        testDateFieldWrapper_toStringNoDate();
      }
    });
    
    // ImageItem
    suite.addTest(new J2meTestCaseTest("testImageItemWrapper_addCommand") {
      public void runTest() {
        testImageItemWrapper_addCommand();
      }
    });
    
    suite.addTest(new J2meTestCaseTest("testImageItemWrapper_commandHandling") {
      public void runTest() {
        testImageItemWrapper_commandHandling();
      }
    });
    
    suite.addTest(new J2meTestCaseTest("tesImageItemWrapper_constructors") {
      public void runTest() {
        testImageItemWrapper_constructors();
      }
    });
    
    suite.addTest(new J2meTestCaseTest(
        "testImageItemWrapper_listenerHandling") {
      public void runTest() {
        testImageItemWrapper_listenerHandling();
      }
    });
    
    suite.addTest(new J2meTestCaseTest("testImageItemWrapper_toString") {
      public void runTest() {
        testImageItemWrapper_addCommand();
      }
    });
    
    // Spacer
    suite.addTest(new J2meTestCaseTest("testSpacerWrapper_addCommand") {
      public void runTest() {
        testSpacerWrapper_addCommand();
      }
    });

    // StringItem
    suite.addTest(new J2meTestCaseTest("testStringItemWrapper_addCommand") {
      public void runTest() {
        testStringItemWrapper_addCommand();
      }
    });
    
    // TextField
    suite.addTest(new J2meTestCaseTest("testTextFieldWrapper_addCommand") {
      public void runTest() {
        testTextFieldWrapper_addCommand();
      }
    });
    
    
    return suite;
  }
  
}
