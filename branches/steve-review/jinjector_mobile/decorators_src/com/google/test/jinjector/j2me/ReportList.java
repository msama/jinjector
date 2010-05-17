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

import com.google.test.jinjector.ResultDisplayerStrategy;
import com.google.test.jinjector.util.Log;

import j2meunit.framework.TestFailure;
import j2meunit.framework.TestResult;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;
import javax.microedition.midlet.MIDlet;

/**
 * Contains a list of report from executed tests.
 * 
 * <p> The reports are contained into a convenience container of class 
 * {@link ReportContainer} and are displayed into a {@link ReportTextBox}.
 * 
 * @author Michele Sama
 */
public class ReportList implements CommandListener, ResultDisplayerStrategy {

  private static final String TITLE = "Errors and Failures";
  private static final String SELECT = "Select";
  private static final String BACK = "Back";
  private static Image red;
  private static Image blue;
  private static Image white;
  
  /**
   * The current size of reports displayed. The current value is used as ID for
   * new added reports.
   */
  private int reportSize = 0;
  
  /**
   * Contains all the added {@link ReportContainer}s addressed by ID.  
   */
  private Hashtable reports = new Hashtable();
  
  private Displayable previousDisplayable;
  
  private MIDlet midlet;
  
  private List resultDisplayable = new List(TITLE, List.IMPLICIT);
  
  /**
   * Initializes all the images. 
   */
  static {
    try {
      red = Image.createImage(ReportList.class.
          getResourceAsStream("runnable_error.png"));
      blue = Image.createImage(ReportList.class.
          getResourceAsStream("runnable_failed.png"));
      white = Image.createImage(ReportList.class.
          getResourceAsStream("runnable_passed.png"));
    } catch (IOException e) {
      Log.logThrowable(ReportList.class.getName(), e);
    }
  }
  
  /**
   * Creates an instance using the given MIDlet.
   * 
   * @param midlet The current {@link MIDlet}.
   */
  public ReportList(MIDlet midlet) {
    this.midlet = midlet;
    resultDisplayable.addCommand(new Command(BACK, Command.BACK, 1));
    resultDisplayable.addCommand(new Command(SELECT, Command.ITEM, 1));
    resultDisplayable.setCommandListener(this);
  }
  
  /**
   * Adds an error to this list.
   * 
   * @param message The message to be displayed.
   * @param description The detailed description.
   */
  public void addError(String message, String description) {
    createNewReportContainer(message, description);
    resultDisplayable.append(message, red);
  }
  
  /**
   * Adds a fault to this list.
   * 
   * @param message The message to be displayed.
   * @param description The detailed description.
   */
  public void addFault(String message, String description) {
    createNewReportContainer(message, description);
    resultDisplayable.append(message, blue);
  }
  
  /**
   * Adds a successful test to this list.
   * 
   * @param message The message to be displayed.
   * @param description The detailed description.
   */
  public void addSuccess(String message, String description) {
    createNewReportContainer(message, description);
    resultDisplayable.append(message, white);
  }

  /**
   * Create a new report and uses an incremental id which is going to be used 
   * to get the instance from the GUI.
   * 
   * <p> this is only necessary BECAUSE {@link List#append(String, Image)} do 
   * not accept objects.
   * 
   * @param message The message to be displayed.
   * @param description The detailed description.
   */
  private void createNewReportContainer(String message, String description) {
    synchronized (reports) {
      ReportContainer report = new ReportContainer(reportSize, message, 
          description);
      reports.put(new Integer(reportSize), report);
      reportSize++;
    }
  }
  
  /**
   * Gets the {@link ReportContainer} addressed by the specified ID.
   * 
   * @param id The id of the {@link ReportContainer}.
   * @return The corrisponding {@link ReportContainer}.
   */
  private ReportContainer getReportContainer(int id) {
    return (ReportContainer) reports.get(new Integer(id));
  }

  /**
   * Default command listener for this {@link Displayable}.
   * 
   * @see CommandListener#commandAction(Command, Displayable)
   */
  public void commandAction(Command cmd, Displayable dsp) {
    String action = cmd.getLabel();
    
    if (action.equals(BACK)) {
      if (dsp.equals(resultDisplayable)) {
        Display.getDisplay(midlet).setCurrent(previousDisplayable);
      } else {
        Display.getDisplay(midlet).setCurrent(resultDisplayable);
      }
    } else if (action.equals(SELECT)) {
      int index = resultDisplayable.getSelectedIndex();
      if (index >= 0) {
        ReportContainer report = getReportContainer(resultDisplayable.getSelectedIndex()); 
        ReportTextBox box = new ReportTextBox(report);
        box.setCommandListener(this);
        Display.getDisplay(midlet).setCurrent(box);
      }
    }
  }
  
  /**
   * Displays a {@link ReportList} showing results for the executed tests.
   * 
   * <p> At the end of the execution it vibrates. This is useful when 
   * deploying on a real phone to avoid messing up the test when the screen 
   * saver starts.
   * 
   * TODO: this is just a temporary implementation. Test runner needs 
   *    more knowledge about executed tests.
   */
  public void displayResult(TestResult result) {
    previousDisplayable = Display.getDisplay(midlet).getCurrent();
    
    Enumeration en = null;
    en = result.errors();
    while (en.hasMoreElements()) {
      TestFailure failure = (TestFailure) en.nextElement();
      addError(failure.failedTest().toString(), 
          failure.thrownException().getMessage());
    }
    en = result.failures();
    while (en.hasMoreElements()) {
      TestFailure failure = (TestFailure) en.nextElement();
      addFault(failure.failedTest().toString(), 
          failure.thrownException().getMessage());
    }
    // TestResult does not provide a full list of successful tests.
    
    Display.getDisplay(midlet).setCurrent(resultDisplayable);
    Display.getDisplay(midlet).vibrate(2000);
  }
  
  /**
   * Contains the report of an executed test.
   * 
   * @author Michele Sama
   *
   */
  static class ReportContainer {
    int id;
    String message;
    String detailedDescription;
    
    /**
     * @param id The sequential id to identify this report.
     * @param message The message to be displayed.
     * @param detailedDescription The detailed description.
     */
    public ReportContainer(int id, String message, String detailedDescription) {
      this.id = id;
      this.message = message;
      this.detailedDescription = detailedDescription;
    }

    /**
     * Returns a short message for this report.
     * 
     * @return the message of this report.
     */
    public String getMessage() {
      return message;
    }

    /**
     * Returns the detailed description of this report.
     * 
     * @return the detailedDescription of this report.
     */
    public String getDetailedDescription() {
      return detailedDescription;
    }
  }

  /**
   * Gets a reference to the internal hashtable of reports.
   * 
   * <p>This method is for testing purposes only and it shouldn't be invoked 
   * otherwise.
   * 
   * @return the reports
   */
  protected Hashtable getReports() {
    return reports;
  }
  
  /**
   * A specific {@link TextBox} for displaying a 
   * {@link ReportList.ReportContainer}.
   * 
   * @author Michele Sama
   */
  static class ReportTextBox extends TextBox{
    
    /**
     * Creates an instance form a {@link ReportList.ReportContainer}.
     * 
     * @param report The {@link ReportList.ReportContainer} to display.
     */
    public ReportTextBox(ReportContainer report) {
      this(report.getMessage(), report.getDetailedDescription());
    }
    
    /**
     * Creates an instance with a specific message and a specific description.
     * 
     * @param message The message to be displayed.
     * @param description The detailed description.
     */
    public ReportTextBox(String message, String description) {
      super(message, description, description.length(), TextField.ANY);
      addCommand(new Command(BACK, Command.BACK, 1));
    }
  }

}


