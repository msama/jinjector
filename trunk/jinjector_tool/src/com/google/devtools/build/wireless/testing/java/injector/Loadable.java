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

package com.google.devtools.build.wireless.testing.java.injector;

import org.objectweb.asm.ClassVisitor;

import java.util.Properties;

/**
 * Defines a runtime-loadable components which can be added to the 
 * instrumentation chain. It specifies an action to be performed before the 
 * instrumentation, a method to add {@link ClassVisitor}s to the chain and an
 * action to be performed on the instrumented jar after the instrumentation.
 * 
 * @author Michele Sama
 *
 */
public abstract class Loadable {

  protected Platform platform;
  
  protected String runId;
  
  /**
   * Defines a set of actions which needs to be performed to load this class.
   * 
   * <p>Derived classes must implement {@link #loadInternal(Properties)} which
   * will be automatically invoked when this method is executed.
   */
  public final void load(Properties properties) {
    platform = Platform.valueOf(
        properties.getProperty(ClassBytecodeLoader.PROPERTY_PLATFORM));
    
    runId = properties.getProperty(ClassBytecodeLoader.PROPERTY_RUN_ID);
    
    loadInternal(properties);
  }
  
  /**
   * Defines the initial setup for this loadable.
   * 
   * @param properties the set of properties from which to load from.
   */
  protected abstract void loadInternal(Properties properties);
  
  /**
   * Prints a description of this {@link Loadable} and the list of 
   * properties it accepts.
   */
  public abstract void printUsage();
  
  /**
   * Allows derived classes to specify an action which should be executed before
   * the instrumentation. This action include additional initializations of
   * components which must exist during the whole instrumentation process.
   */
  public void preOperation() {}
  
  /**
   * Adds at the beginning of the instrumentation chain one (or more) 
   * {@link ClassVisitor}s and returns the new first link of the chain. 
   * 
   * @param cv the first {@link ClassVisitor} of the instrumentation chain
   *     before this Loadable has been abblied.
   * @param classManager the instance of {@link ClassManager} to be used
   *     during the instrumentation.
   * @return the first {@link ClassVisitor} of the instrumentation chain
   *     after this Loadable has been abblied.
   */
  public abstract ClassVisitor operation(ClassVisitor cv, 
      ClassManager classManager);
  
  /**
   * Allows derived classes to specify operations which needs to be performed
   * on the produced jar or on the instrumented classes after the 
   * instrumentation.
   * 
   * @param outputDir the output directory containing all the instrumented 
   *     classes.
   * @param instrumentedJar the produced jar.
   */
  public void postOperation(String outputDir,
      InstrumentedJarCreator instrumentedJar) {}
  
}
