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

import j2meunit.framework.Test;
import j2meunit.framework.TestCase;
import j2meunit.framework.TestSuite;

/**
 * Test for the bitfield class.
 *
 * @author Olivier Gaillard
 * @size small
 */
public class BitFieldTest extends TestCase {

  private static final int MAX_SIZE = 20;
  
  public BitFieldTest() {
  }

  public BitFieldTest(String name) {
    super(name);
  }

  private void assertAllBitsButOneEquals(Bitfield bits, int index, 
      boolean value, int maxsize) {
    for (int i = 0; i < maxsize; i++) {
      if (i != index) {
        assertTrue(bits.get(i) == value);
      } else {
        assertTrue(bits.get(i) != value);
      }
    }
  }

  public void testBitsCanBeSet() {
    Bitfield bits = new Bitfield(MAX_SIZE);
    bits.set(5);

    assertAllBitsButOneEquals(bits, 5, false, MAX_SIZE);
  }

  public void testConsecutiveBitsCanBeSet() {
    Bitfield bits = new Bitfield(MAX_SIZE);
    bits.set(5);
    bits.set(6);

    assertTrue(bits.get(5));
    assertTrue(bits.get(6));
    assertTrue(!bits.get(4));
    assertTrue(!bits.get(7));
  }

  public void testBitsCanBeUnset() {
    Bitfield bits = new Bitfield(20);
    bits.set(5);
    bits.unset(5);
    bits.set(4);

    assertAllBitsButOneEquals(bits, 4, false, MAX_SIZE);
  }

  public void testBitfieldSmallerThanEightBits() {
    Bitfield bits = new Bitfield(3);
    bits.set(2);

    assertAllBitsButOneEquals(bits, 2, false, MAX_SIZE);
  }

  public void testIndexOutOfBoundsExceptionThrown() {
    Bitfield bits = new Bitfield(20);

    // That's ok, no bounds checking is done
    bits.set(24);

    try {
      bits.set(32);
      fail("No memory allocated for this bit, should have thrown an exception.");
    } catch (IndexOutOfBoundsException e) {
      // test pass.
    }
  }

  public void testCannotSetBitWithZeroSizedBitfield() {
    Bitfield bits = new Bitfield(0);

    try {
      bits.set(2);
      fail("Index 2 should not be accessible for a 0-sized bitfield.");
    } catch (IndexOutOfBoundsException e) {
      // test pass.
    }
  }
  
  /**
   * @see j2meunit.framework.TestCase#suite()
   */
  public Test suite() {
    TestSuite suite = new TestSuite();
    
    suite.addTest(new BitFieldTest("testBitfieldSmallerThanEightBits") {
      public void runTest() {
        testBitfieldSmallerThanEightBits();
      }
    });
    
    suite.addTest(new BitFieldTest("testBitsCanBeSet") {
      public void runTest() {
        testBitsCanBeSet();
      }
    });
    
    suite.addTest(new BitFieldTest("testBitsCanBeUnset") {
      public void runTest() {
        testBitsCanBeUnset();
      }
    });
    
    suite.addTest(new BitFieldTest("testCannotSetBitWithZeroSizedBitfield") {
      public void runTest() {
        testCannotSetBitWithZeroSizedBitfield();
      }
    });
    
    suite.addTest(new BitFieldTest("testConsecutiveBitsCanBeSet") {
      public void runTest() {
        testConsecutiveBitsCanBeSet();
      }
    });
    
    suite.addTest(new BitFieldTest("testIndexOutOfBoundsExceptionThrown") {
      public void runTest() {
        testIndexOutOfBoundsExceptionThrown();
      }
    });
    
    return suite;
  }
  
  
}
