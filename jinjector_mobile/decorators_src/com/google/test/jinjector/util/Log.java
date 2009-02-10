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

package com.google.test.jinjector.util;

/**
 * Implements a basic logger.
 * 
 * @author Michele Sama
 *
 */
public class Log {
  
  /**
   * Uninstantiable because it is a utility class.
   */
  private Log() {
  }
  
  /**
   * Logs a message using the classname as a source.
   * 
   * @param clazz the type to use as a source of the message.
   * @param message the message to be logged.
   */
  public static void log(Class clazz, String message) {
    log(clazz.getName(), message);
  }
  
  /**
   * Logs a message from a given source.
   * 
   * @param source the source of the message.
   * @param message the message to be logged.
   */
  public static void log(String source, String message) {
    System.out.println("[" + source + "]" + message);
  }
  
  /**
   * Logs a {@link Throwable} and prints its stack trace.
   * 
   * @param source the source reporting the throwable.
   * @param t the instance to be logged.
   */
  public static void logThrowable(String source, Throwable t) {
    log(source, t.getMessage());
    t.printStackTrace();
  }

}
