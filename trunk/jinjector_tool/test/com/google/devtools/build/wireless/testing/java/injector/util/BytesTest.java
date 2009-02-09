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

import junit.framework.TestCase;

import java.io.*;
import java.util.Arrays;

/**
 * Test case for {@link Bytes}.
 * 
 * @author Michele Sama
 *
 */
public class BytesTest extends TestCase {

  final static int BUFFER_SIZE = 10;
  
  /**
   * Constructor from superclass.
   */
  public BytesTest() {
  }

  /**
   * Constructor from superclass.
   * 
   * @param name the test's name.
   */
  public BytesTest(String name) {
    super(name);
  }

  /**
   * Contains assertions used in this test case.
   */
  private final static class Assert {

    /**
     * Uninstantiable because it is a utility class
     */
    private Assert() {
      // Nothing to do
    }
    
    public static void lengthRead(long expected, long found) {
      assertEquals("The number of bytes read should match the " +
          "expected value.", expected, found);
    }
    
    public static void valuesReadIntoArray(byte[] expected, byte[] found) {
      assertTrue("The values read should match the expected values.", 
          Arrays.equals(expected, found));
    }
  }
  
  /**
   * Tests method {@link Bytes#copy(InputStream, OutputStream)}.
   * 
   * @throws IOException if an error occurs while copying from the stream.
   */
  public void testCopy() throws IOException {
    byte[] source = newTestByteArray(BUFFER_SIZE);
    InputStream input = newTestInputStream(source);
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    long copied = Bytes.copy(input, output);
    
    BytesTest.Assert.lengthRead(BUFFER_SIZE, copied);
    BytesTest.Assert.valuesReadIntoArray(source, output.toByteArray());
  }
  
  /**
   * Tests method {@link Bytes#read(InputStream, byte[], int, int)}
   * 
   * @throws IOException
   */
  public void testRead() throws IOException {
    byte[] b = new byte[3 * BUFFER_SIZE];
    InputStream source = newTestInputStream(BUFFER_SIZE);
    
    int readed = Bytes.read(source, b, BUFFER_SIZE, BUFFER_SIZE);
    BytesTest.Assert.lengthRead(BUFFER_SIZE, readed);
    
    byte[] expectedResult = new byte[3 * BUFFER_SIZE];
    for (byte i = 0; i < BUFFER_SIZE; i++) {
      expectedResult[i + BUFFER_SIZE] = i;
    }
    BytesTest.Assert.valuesReadIntoArray(expectedResult, b);
  }
  
  /**
   * Tests method {@link Bytes#readFully(InputStream, byte[])} with a stream 
   * of the exact size of the buffer. This will happen when loading a file in 
   * memory.
   * 
   * @throws IOException if an error occurs while reading from the buffer.
   */
  public void testReadFully_sameSize() throws IOException {
    byte[] b = new byte[BUFFER_SIZE];
    byte[] source = newTestByteArray(BUFFER_SIZE);
    
    Bytes.readFully(newTestInputStream(source), b);
    
    BytesTest.Assert.valuesReadIntoArray(source, b);
  }
  
  /**
   * Tests method {@link Bytes#readFully(InputStream, byte[])} with a buffer
   * smaller than the stream. 
   * 
   * @throws IOException if an error occurs while reading from the buffer.
   */
  public void testReadFully_bufferSmaller() throws IOException {
    byte[] b = new byte[BUFFER_SIZE];
    byte[] source = newTestByteArray(BUFFER_SIZE + 1);
    
    Bytes.readFully(newTestInputStream(source), b);
    
    BytesTest.Assert.valuesReadIntoArray(newTestByteArray(BUFFER_SIZE), b);
  }
  
  /**
   * Tests method {@link Bytes#readFully(InputStream, byte[])} with a stream 
   * shorter than the buffer. This should not happen and an exception is thrown.
   * 
   * @throws IOException if an error occurs while reading from the buffer. 
   */
  public void testReadFully_streamShorter() throws IOException {
    byte[] b = new byte[BUFFER_SIZE];

    try {
      Bytes.readFully(newTestInputStream(BUFFER_SIZE - 1), b);
      fail("Reading from a stream shorter than the buffer" +
          " should have thrown an exception.");
    } catch (EOFException ex) {
      // Ok. 
    }
  }

  /**
   * Tests method {@link Bytes#toByteArray(InputStream)}.
   * 
   * @throws IOException if something goes wrong while reading the stream.
   */
  public void testToByteArray() throws IOException {
    byte[] source = newTestByteArray(BUFFER_SIZE);
    InputStream input = newTestInputStream(source);
    byte[] b = Bytes.toByteArray(input);
    BytesTest.Assert.valuesReadIntoArray(source, b);
  }
  
  /**
   * Creates an {@link InputStream} for testing containing bytes with 
   * increasing values, starting from 0 to {@code size - 1}.
   * 
   * @param size the number of bytes in the returned stream.
   * @return the created stream.
   */
  private InputStream newTestInputStream(int size) {
    byte[] b = newTestByteArray(size);
    return newTestInputStream(b);
  }
  
  /**
   * Crates an {@link InputStream} from the given byte array.
   * 
   * @param b the source byte array.
   * @return the created stream.
   */
  private InputStream newTestInputStream(byte[] b) {
    return new ByteArrayInputStream(b);
  }
  
  /**
   * Creates an array of bytes with increasing values from 0 to 
   * {@code size - 1}.
   * 
   * @param size the array's size.
   * @return the generated array.
   */
  private byte[] newTestByteArray(int size) {
    byte[] b = new byte[size];
    for (byte i = 0; i < size; i++) {
      b[i] = i;
    }
    return b;
  }
}
