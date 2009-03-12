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

package com.google.test.jinjector.coverage;

import com.google.test.jinjector.util.LineReader;
import com.google.test.jinjector.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.lang.IllegalArgumentException;
import java.util.Vector;

/**
 * Bitfield class. Provides a small footprint and fast read/write operations.
 *
 * <p>Not thread-safe.
 *
 * <p>The minimum size is 32 bits (int size).
 *
 * @author Olivier Gaillard
 * @author Michele Sama
 */
public class Bitfield {
  private static final int INT_BIT_SHIFT = 5;
  private static final int INT_BIT_SIZE = (1 << INT_BIT_SHIFT);
  private static final int INT_BIT_MASK = INT_BIT_SIZE - 1;

  private int[] data = null;

  /**
   * Initializes the bitfield.
   *
   * @param size the number of bits of the bitfield.
   */
  public Bitfield(int size) {
    if (size < 0) {
      throw new IllegalArgumentException("Size must not be negative.");
    }

    data = new int[(size + INT_BIT_SIZE - 1) >>> INT_BIT_SHIFT];
  }

  /**
   * Sets the bit to 1 at the specified index.
   *
   * <p> Does no bounds checking.
   */
  public void set(int index) {
    data[index >>> INT_BIT_SHIFT] |= 1 << (index & INT_BIT_MASK);
  }

  /**
   * Sets the bit to 0 at the specified index.
   *
   * <p> Does no bounds checking.
   */
  public void unset(int index) {
    data[index >>> INT_BIT_SHIFT] &= 0 << (index & INT_BIT_MASK);
  }

  /**
   * Returns true if the bit is set at the specified index.
   */
  public boolean get(int index) {
    final int bit = data[index >>> INT_BIT_SHIFT] & 1 << (index & INT_BIT_MASK);
    return bit != 0;
  }
  
  
  /**
   * Loads an array of Bitfields with size specified in a file containing 
   * the instrumentation report of line coverage.
   * 
   * @param filename the filename from which to read.
   */
  public static Bitfield[] getBitfieldsForLineCoverage(String filename) {
    
    InputStream is = null;
    try {
      is = Bitfield.class.getResourceAsStream(filename);
      if (is != null) {
        return getBitfieldsForLineCoverage(is);
      } else {
        System.out.println("Unable to getResourceAsStream() for " + filename);
        return null;
      }
    } finally {
      /*
       *  Not safe to use IoUtil here. 
       *  @see CoverageManager#getInstance() for more details.
       */
      if (is != null) {
        try {
          is.close();
          is = null;
        } catch (IOException e) {
          Log.log(Bitfield.class, "Caught IOException while closing stream " + e.getMessage());
        }
      }
    }
    
  }
  
  public static Bitfield[] getBitfieldsForLineCoverage(InputStream is) {
    Vector bitfieldVector = new Vector(100);
    LineReader lr = null;
    try {
      lr = new LineReader(is);

      String line;
      /**
       * For each line in the file write a line in the output file and update 
       * the internal table.
       * */
      while ((line = lr.readline()) != null) {
        // Skips comments 
        if (line.startsWith("" + CoverageLcovWriter.COMMENT)) {
          continue;
        }
        /*
         * Parses index and instrumented lines' size
         */
        int separatorPosition1 = line.indexOf(CoverageLcovWriter.SEPARATOR);
        int separatorPosition2 = line.indexOf(CoverageLcovWriter.SEPARATOR, 
            separatorPosition1 + 1);
        
        if ((separatorPosition1 == -1) || (separatorPosition2 == -1)) {
            throw new RuntimeException("There was a problem parsing the " +
              "separators. \nThe values should be positive numbers. " + 
              "Separator 1 [" + separatorPosition1 + "] 2 [" +
              separatorPosition2 + "]\n, line [" + line + "]\n" +
              "The separator is [" + CoverageLcovWriter.SEPARATOR + "]\n");
        }

        try {
          int index = Integer.parseInt(line.substring(0, separatorPosition1));
          int size = Integer.parseInt(
            line.substring(separatorPosition1 + 1, separatorPosition2));
          if (bitfieldVector.size() != index) {
            throw new IllegalStateException("The index of the corresponding " +
              "index is different than the index which will be assigned " +
              "internally. The current implementation assumes files to be " +
              "ordered by index in line coverage index file.");
          }
          Bitfield bitfield = new Bitfield(size);
          bitfieldVector.addElement(bitfield);
        } catch(NumberFormatException nfe) {
          throw new RuntimeException("Line coverage index file contains a " +
            "format different than expected.\n" +
            "The faulty line is:\n" + line + ".\n" +
            "This exception was generated by: " + nfe.getMessage());
        }
      }
    } catch(IOException ioex) {
      throw new RuntimeException("An error occurred while reading lines from " +
          "the instrumented lines index file: " + ioex.getMessage());
    } finally {
      /*
       *  Not safe to use IoUtil here. 
       *  @see CoverageManager#getInstance() for more details.
       */
      if (lr != null) {
        try {
          lr.close();
          lr = null;
        } catch (IOException e) {
          Log.log(Bitfield.class, "Caught IOException while closing reader " + e.getMessage());
        }
      }
    }
    Bitfield[] result = new Bitfield[bitfieldVector.size()];
    bitfieldVector.copyInto(result);
    return result;
  }
}
