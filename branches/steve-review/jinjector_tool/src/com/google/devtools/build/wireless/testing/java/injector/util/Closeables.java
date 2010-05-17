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

package com.google.devtools.build.wireless.testing.java.injector.util;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Performs basic action on instances of {@link Closeable}.
 * 
 * @author Michele Sama
 *
 */
public class Closeables {

  /**
   * Default logger. 
   */
  private static final Logger logger
      = Logger.getLogger(Closeables.class.getName());
  
  /**
   * Uninstantiable because it is a utility class.
   */
  private Closeables() {
    // Do nothing
  }

  /**
   * Close a {@link Closeable}, with control over thrown exceptions. 
   * This is primarily useful in a finally block, where thrown exceptions 
   * may be logged but not propagated (otherwise the original exception will 
   * be lost). 
   * 
   * <p>If the {@code closeable} implements {@link Flushable} then it 
   * is flushed before closing. If flushing throws an exception, 
   * {@link Closeable#close()} is still invoked before continuing. If both the
   * calls throws an exception the one on {@link Closeable#close()} is 
   * propagated.
   *
   * <p>If {@code quietly} is true then no exception is thrown.
   *
   * @param closeable the {@code Closeable} object to be closed, or 
   *     <code>null</code>, in which case this method does nothing.
   * @param quietly if true, don't propagate IO exceptions
   *     thrown by the {@code close} or {@code flush} methods.
   * @throws IOException if {@code quietly} is false and
   *     {@code close} or {@code flush} throws an {@code IOException}.
   */
  public static void close(Closeable closeable, boolean quietly)
      throws IOException {
    if (closeable == null) {
      return;
    }

    IOException exceptionOnFlush = null;

    if (closeable instanceof Flushable) {
      try {
        Flushables.flush((Flushable) closeable, false);
      } catch (IOException e) {
        exceptionOnFlush = e;
        if (quietly) {
          logger.log(Level.WARNING,
              "IOException thrown while flushing Flushable.", exceptionOnFlush);
        }
      }
    }

    try {
      closeable.close();
    } catch (IOException e) {
      if (quietly) {
        logger.log(Level.WARNING,
            "IOException thrown while closing Closeable.", e);
      } else {
        throw e;
      }
    }

    if (exceptionOnFlush != null && !quietly) {
      throw exceptionOnFlush;
    }
  }
  
  /**
   * Close a {@link Closeable} without throwing {@link IOException}s.
   * 
   * <p>If an exception is thrown an error message is logged.
   * 
   * @param closeable the {@link Closeable} to close.
   */
  public static void closeQuietly(Closeable closeable) {
    try {
      close(closeable, true);
    } catch (IOException e) {
      logger.log(Level.SEVERE,
          "IOException thrown while closing quietly.", e);
    }
  }
}
