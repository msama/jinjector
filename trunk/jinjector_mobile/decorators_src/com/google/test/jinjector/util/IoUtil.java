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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

/**
 * Utility class for handling streams, readers and writers.
 * 
 * @author Michele Sama
 *
 */
public class IoUtil {

  /**
   * Uninstantiable because it is a utility class.
   */
  private IoUtil() {
  }
  
  /**
   * Closes the given {@link Reader} stream without propagating an 
   * IO exception. 
   *
   * @param closeable the closeable to close
   */
  public static void closeCloseable(Reader closeable) {
    try {
      if (closeable != null) {
        closeable.close();
      }
    } catch (IOException e) {
      Log.log(IoUtil.class, 
          "Caught IOException while closing closable for a Reader"
          + e.getMessage() + ", " + e.toString());
    }
  }
  
  /**
   * Closes the given {@link InputStream} stream without propagating an 
   * IO exception. 
   *
   * @param closeable the closeable to close
   */
  public static void closeCloseable(InputStream closeable) {
    try {
      if (closeable != null) {
        closeable.close();
      }
    } catch (IOException e) {
      Log.log(IoUtil.class, 
          "Caught IOException while closing closable for an InputStream "
          + e.getMessage() + ", " + e.toString());
    }
  }
  
  /**
   * Closes the given {@link OutputStream} stream without propagating an 
   * IO exception. 
   *
   * @param closeable the closeable to close
   */
  public static void closeCloseable(OutputStream closeable) {
    try {
      if (closeable != null) {
        closeable.flush();
        closeable.close();
      }
    } catch (IOException e) {
      Log.log(IoUtil.class, 
          "Caught IOException while closing closable for an OutputStream "
          + e.getMessage() + ", " + e.toString());
    }
  }
  
  /**
   * Closes the given {@link Writer} stream without propagating an 
   * IO exception. 
   *
   * @param closeable the closeable to close
   */
  public static void closeCloseable(Writer closeable) {
    try {
      if (closeable != null) {
        closeable.flush();
        closeable.close();
      }
    } catch (IOException e) {
      Log.log(IoUtil.class, 
          "Caught IOException while closing closable for a Writer "
          + e.getMessage() + ", " + e.toString());
    }
  }

  /**
   * Copies all the bytes from the given InputStream to the given OutputStream
   * then close the input.
   * 
   * @param in the InputStream from which to read the data.
   * @param out the OutputStream to which to write the data.
   * @throws IOException if an I/O exception occurs while reading or writing.
   */
  public static void copyStreamAndClose(InputStream in, OutputStream out)
      throws IOException {
    //  This is value that seems appropriate for a mobile device.
    final int blockSize = 512;
    byte[] buffer = new byte[blockSize];
    int received;
    try {
      while ((received = in.read(buffer)) != -1) {
        out.write(buffer, 0, received);
      }
    } finally {
      closeCloseable(in);
    }
  }
  
  /**
   * Reads all bytes from the given InputStream into a byte array. At the end
   * of this operation, the given input stream will be closed.
   *
   * @param in the InputStream from which to read the data.
   * @return a byte array containing the data read from the InputStream.
   * @throws IOException if an I/O exception occurs while reading.
   */
  public static byte[] readAllBytesAndClose(InputStream in) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try {
      copyStreamAndClose(in, out);
    } finally {
      closeCloseable(out);
    }
    return out.toByteArray();
  }
}
