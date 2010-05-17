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

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Factory class for reading bytes from or into buffers
 * 
 * @author Michele Sama
 *
 */
public class Bytes {
  
  private static final int BUF_SIZE = 0x1000; // 4K
  
  /**
   * Uninstantiable because it is a factory class.
   */
  private Bytes() {
    // Does nothing.
  }

  /**
   * Copies all bytes from the input stream to the output stream.
   * Does not close or flush either stream.
   *
   * @param from the input stream to read from
   * @param to the output stream to write to
   * @return the number of bytes copied
   * @throws IOException if an I/O error occurs
   */
  public static long copy(InputStream from, OutputStream to)
      throws IOException {
    byte[] buf = new byte[BUF_SIZE];
    long total = 0;
    while (true) {
      int r = from.read(buf);
      if (r == -1) {
        break;
      }
      to.write(buf, 0, r);
      total += r;
    }
    return total;
  }
  
  /**
   * Attempts to read enough bytes from the stream to fill the given byte array.
   * Does not close the stream.
   *
   * @param source the input stream to read from.
   * @param destination the buffer into which the data is read.
   * @throws EOFException if this stream reaches the end before reading all
   *     the bytes.
   * @throws IOException if an I/O error occurs.
   */
  public static void readFully(InputStream source, byte[] destination)
      throws IOException {
    if (read(source, destination, 0, destination.length) 
        != destination.length) {
      throw new EOFException();
    }
  }
  
  /**
   * Reads some bytes from an input stream and stores them into a buffer array
   * {@code b}. This method blocks until {@code length} bytes of input data have
   * been read into the array, or end of file is detected. The number of bytes
   * read is returned, possibly zero. Does not close the stream.
   *
   * <p>Both {@code offset} and {@code length} must be greater or equal to 
   * zero or an {@link IllegalArgumentException} will be thrown.
   * 
   * @param source the input stream to read from
   * @param destination the buffer into which the data is read
   * @param offset the offset into the data
   * @param length the number of bytes to read
   * @return the number of bytes read
   * @throws IOException if an I/O error occurs
   */
  public static int read(InputStream source, byte[] destination,
      int offset, int length) throws IOException {
    if (length < 0) {
      throw new IllegalArgumentException("Length cannot be negative");
    }
    if (offset < 0) {
      throw new IllegalArgumentException("Offset cannot be negative");
    }
    int total = 0;
    while (total < length) {
      int result = source.read(destination, offset + total, length - total);
      if (result == -1) {
        break;
      }
      total += result;
    }
    return total;
  }
  
  /**
   * Reads all bytes from an input stream into a byte array.
   * Does not close the stream.
   *
   * @param in the input stream to read from
   * @return a byte array containing all the bytes from the stream
   * @throws IOException if an I/O error occurs
   */
  public static byte[] toByteArray(InputStream in) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    copy(in, out);
    return out.toByteArray();
  }
  
}
