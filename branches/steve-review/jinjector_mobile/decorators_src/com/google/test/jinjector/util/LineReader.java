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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * Reads a full line from a stream and skips the end line characters.
 * 
 * @author Michele Sama
 *
 */
public class LineReader extends InputStreamReader {

  /**
   * Defines the size of the internal buffer in bytes. 
   */
  private static int BUFFER_CHAR_SIZE = 128;   
  
  /**
   * Buffer for the line which is being read.
   */
  private StringBuffer buffer = new StringBuffer();
  
  /**
   * The internal array in which characters read from the stream are stored 
   * before adding them to the string buffer. This is used to minimize the 
   * number of appends on the buffer.
   */
  private char[] ch = new char[BUFFER_CHAR_SIZE];
  
  /**
   * Wrap an input stream with an instance of this class.
   * 
   * @param is the nested InputStream
   * @param enc the char encoding
   * @see InputStreamReader
   */
  public LineReader(InputStream is, String enc)
      throws UnsupportedEncodingException {
    super(is, enc);
  }

  /**
   * Wrap an input stream with an instance of this class.
   * 
   * @param is the nested InputStream.
   * @see InputStreamReader
   */
  public LineReader(InputStream is) {
    super(is);
  }

  /**
   * Reads a line from the buffer.
   *
   * <p>Not thread-safe.
   *
   * <p>The line separator sequence changes from platform to platform. The file
   * is written by JInjector at instrumentation time therefore it contains the 
   * line separator sequence from the machine which performed the 
   * instrumentation. It is possible to obtain that sequence by calling 
   * <code>System.getProperty("line.separator");</code> however the line
   * separator in the emulator or in the deployment device may be different 
   * than the one used in the file. 
   *
   * @return The read line or <code>null</code> if nothing was read.
   */
  public String readline() throws IOException {
    try {
      int currentSize = 0;
      int totalSize = 0;
      int k = 0;
      
      /*
       * TODO: instead of reading char by char just read a chunk and 
       *     parse the chars internally to minimize the number of calls to 
       *     read(). 
       */
      // Both in Windows and Unix the line separators end with a \n
      while ((k = read()) != -1 && k != '\n') {
        ch[currentSize++] = (char) k;
        totalSize++;
        if (currentSize == BUFFER_CHAR_SIZE) {
          buffer.append(ch);
          currentSize = 0;
        }
      }

      if (totalSize == 0) {
        return null;
      }

      buffer.append(ch, 0, currentSize);
      // Removes the \r which is used in windows 
      if (buffer.charAt(buffer.length() - 1) == '\r') {
        buffer.deleteCharAt(buffer.length() - 1);
      }
      String result = buffer.toString();
      return result;

    } finally {
      buffer.setLength(0);
    }
  }

  /**
   * Throws an exception if mark is invoked.
   * 
   * @see java.io.InputStreamReader#mark(int)
   */
  public void mark(int arg0) throws IOException {
    throw new IOException("Mark is not supported.");
  }

  /**
   * Marking is not supported.
   * 
   * @see java.io.InputStreamReader#markSupported()
   */
  public boolean markSupported() {
    return false;
  }
  
  /**
   * Nullifies all internal buffers and closes the stream.
   * 
   * @see java.io.InputStreamReader#close()
   */
  public void close() throws IOException {
    buffer = null;
    ch = null;
    super.close();
  }
}
