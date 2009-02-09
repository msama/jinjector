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

package com.google.devtools.build.wireless.testing.java.injector.j2me;

import com.google.devtools.build.wireless.testing.java.injector.ClassNames;

/**
 * J2me constant pool.
 * 
 * @author Michele Sama
 *
 */
public class J2meClassNames {

  /**
   * Wrapper base package
   * */
  public static final String J2ME_PKG = 
    (ClassNames.DECORATORS_PKG + "j2me.").replace('.', '/');
  
  /**
   * Wrapper base package
   * */
  public static final String WRAPPER_PKG = 
    (J2ME_PKG + "wrapper.").replace('.', '/');
  
  /**
   * Google related client code.
   * */
  public static final String LOG = "com/google/test/jinjector/util/Log";
  
  /**
   * Command and command listeners.
   * */
  public static final String COMMAND 
  = "javax/microedition/lcdui/Command";
  
  public static final String COMMAND_LISTENER 
  = "javax/microedition/lcdui/CommandListener";
  
  public static final String ITEM_COMMAND_LISTENER 
  = "javax/microedition/lcdui/ItemCommandListener";
  
  
  /**
   * DISPLAYABLES
   * */
  public static final String DISPLAYABLE 
  = "javax/microedition/lcdui/Displayable";
  
  public static final String CANVAS 
  = "javax/microedition/lcdui/Canvas";
  
  public static final String GAMECANVAS 
  = "javax/microedition/lcdui/game/GameCanvas";
  
  public static final String ALERT 
  = "javax/microedition/lcdui/Alert";
  
  public static final String FORM 
  = "javax/microedition/lcdui/Form";
  
  public static final String LIST 
  = "javax/microedition/lcdui/List";
  
  public static final String TEXTBOX
  = "javax/microedition/lcdui/TextBox";
  
  /**
   * Displayable wrappers.
   * */
  public static final String CANVAS_WRAPPER
  = WRAPPER_PKG + "CanvasWrapper";
  
  public static final String GAMECANVAS_WRAPPER  
  = WRAPPER_PKG + "GameCanvasWrapper";
  
  public static final String ALERT_WRAPPER  
  = WRAPPER_PKG + "AlertWrapper";
  
  public static final String FORM_WRAPPER  
  = WRAPPER_PKG + "FormWrapper";
  
  public static final String LIST_WRAPPER  
  = WRAPPER_PKG + "ListWrapper";
  
  public static final String TEXTBOX_WRAPPER 
  = WRAPPER_PKG + "TextBoxWrapper";
  
  
  /**
   * ITEMS
   * */
  public static final String ITEM 
  = "javax/microedition/lcdui/Item";
  
  public static final String CHOICEGROUP 
  = "javax/microedition/lcdui/ChoiceGroup";
  
  public static final String CUSTOMITEM 
  = "javax/microedition/lcdui/CustomItem";
  
  public static final String DATEFIELD 
  = "javax/microedition/lcdui/DateField";
  
  public static final String GAUGE 
  = "javax/microedition/lcdui/Gauge";
  
  public static final String IMAGEITEM 
  = "javax/microedition/lcdui/ImageItem";
  
  public static final String SPACER 
  = "javax/microedition/lcdui/Spacer";
  
  public static final String STRINGITEM 
  = "javax/microedition/lcdui/StringItem";
  
  public static final String TEXTFIELD 
  = "javax/microedition/lcdui/TextField";

  
  /**
   * Item wrappers.
   * */
  public static final String ITEM_WRAPPER 
  = WRAPPER_PKG + "ItemWrapper";
  
  public static final String CHOICEGROUP_WRAPPER 
  = WRAPPER_PKG + "ChoiceGroupWrapper";
  
  public static final String CUSTOMITEM_WRAPPER 
  = WRAPPER_PKG + "CustomItemWrapper";
  
  public static final String DATEFIELD_WRAPPER 
  = WRAPPER_PKG + "DateFieldWrapper";
  
  public static final String GAUGE_WRAPPER 
  = WRAPPER_PKG + "GaugeWrapper";
  
  public static final String IMAGEITEM_WRAPPER 
  = WRAPPER_PKG + "ImageItemWrapper";
  
  public static final String SPACER_WRAPPER 
  = WRAPPER_PKG + "SpacerWrapper";
  
  public static final String STRINGITEM_WRAPPER 
  = WRAPPER_PKG + "StringItemWrapper";
  
  public static final String TEXTFIELD_WRAPPER 
  = WRAPPER_PKG + "TextFieldWrapper";

  
  /**
   * Default file system root.
   * 
   * <p>The root folder changes for each device, and for each emulator.
   * In WTK it is "file://localhost/root1/".
   */
  public static final String FILESYSTEM_ROOT = "file://localhost/";

  public static final String TEST_MIDLET = "com/google/test/TestMidlet";
}
