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

import java.io.*;

/**
 * Utility class performing basic action on instances of {@link File}.
 * 
 * @author Michele Sama
 *
 */
public class Files {

  /**
   * Asserts basic preconditions on {@link File}s.
   */
  public static class Assert {
    
    /**
     * Uninstantiable because it is a utility class.
     */
    private Assert() {
      // Do nothing.
    }
    
    public static void notNull(File f) {
      if (f == null) {
        throw new IllegalArgumentException("Destination file cannot be null.");
      }
    }
    
    public static void isDir(File f) {
      if (!f.isDirectory()) {
        throw new IllegalArgumentException("Destination " + f + 
            " cannot be a file.");
      }
    }
    
    public static void isFile(File f) {
      if (f.isDirectory()) {
        throw new IllegalArgumentException("Destination " + f + 
            " cannot be a directory.");
      }
    }
  }
  
  /**
   * Uninstantiable because it is a utility class.
   */
  public Files() {
    // Do nothing.
  }

  /**
   * Overrides {@code destination} whit the bytes contained in {@code source}.
   * 
   * <p>If the file already exist it is first deleted then recreated.
   * 
   * @param source the source bytes.
   * @param destination the destination file.
   * @throws IOException If {@code destination} is a <code>null</code> or it is
   *     a directory or if an exception occurs while writing the content. 
   */
  public static void overwrite(byte[] source, File destination) throws IOException {
    Files.Assert.notNull(destination);
    Files.Assert.isFile(destination);

    if (destination.exists()) {
      destination.delete();
    }
    destination.createNewFile();
    FileOutputStream outputStream = new FileOutputStream(destination, false);
    outputStream.write(source);
    Closeables.close(outputStream, false);
  }
  
  /**
   * Reads all bytes from a file into a byte array.
   *
   * @param file the file to read from
   * @return a byte array containing all the bytes from file
   * @throws IllegalArgumentException if the file is bigger than the largest
   *     possible byte array (2^31 - 1)
   * @throws IOException if an I/O error occurs
   */
  public static byte[] toByteArray(File file) throws IOException {
    if (file.length() > Integer.MAX_VALUE) {
      throw new IllegalArgumentException("File " + file + " is too big and" +
          " cannot be stored into an array.");
    }
    if (file.length() == 0) {
      // Some special files are zero length but have content nonetheless.
      return Bytes.toByteArray(new FileInputStream(file));
    } else {
      // Avoid an extra allocation and copy.
      byte[] b = new byte[(int) file.length()];
      boolean threw = true;
      InputStream in = new FileInputStream(file);
      try {
        Bytes.readFully(in, b);
        threw = false;
      } finally {
        Closeables.close(in, threw);
      }
      return b;
    }
  }

  /**
   * Copies all the bytes from one file to another.
   *
   * @param from the source file
   * @param to the destination file
   * @throws IOException if an I/O error occurs
   */
  public static void copy(File from, File to) throws IOException {
    FileInputStream inputStream = new FileInputStream(from);
    FileOutputStream outputStream = new FileOutputStream(to);
    Bytes.copy(inputStream, outputStream);
    Closeables.close(inputStream, false);
    Closeables.close(outputStream, false);
  }
}
