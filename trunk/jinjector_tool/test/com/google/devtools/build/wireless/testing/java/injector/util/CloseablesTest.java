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

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;

import junit.framework.TestCase;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

/**
 * Unit tests for {@link Closeables} and {@link Flushables}.
 * 
 * @author Michele Sama
 *
 */
public class CloseablesTest extends TestCase {

  private Closeable mockCloseable;
  private CloseableAndFlushable mockCloseableFlushable;
  private Closeable mockFailingCloseable;
  private CloseableAndFlushable mockFailingFlushable;
  private CloseableAndFlushable mockFailingCloseableFlushable;
  
  /**
   * Constructor from superclass.
   */
  public CloseablesTest() {
    super();
  }
  
  /**
   * Constructor from superclass.
   * 
   * @param name the test's name.
   */
  public CloseablesTest(String name) {
    super(name);
  }

  /**
   * Allows to mock an object that implements both Closeable and Flushable.
   */
  private interface CloseableAndFlushable extends Closeable, Flushable {
  }
  
  /**
   * Initialized all the mock objects used in this test.
   * 
   * @see junit.framework.TestCase#setUp()
   */
  @Override 
  protected void setUp() throws Exception {
    mockCloseable = createStrictMock(Closeable.class);
    initialiazeMock(mockCloseable, false, false);
    
    mockCloseableFlushable = createStrictMock(CloseableAndFlushable.class);
    initialiazeMock(mockCloseableFlushable, false, false);
    
    mockFailingCloseable = createStrictMock(Closeable.class);
    initialiazeMock(mockFailingCloseable, true, false);
    
    mockFailingFlushable = createStrictMock(CloseableAndFlushable.class);
    initialiazeMock(mockFailingFlushable, false, true);
    
    mockFailingCloseableFlushable = 
        createStrictMock(CloseableAndFlushable.class);
    initialiazeMock(mockFailingCloseableFlushable, true, true);
  }
  
  /**
   * Configure {@code mock}'s behavior according to its type and to the other 
   * flags.
   * 
   * @param mock the instance to configure.
   * @param failOnClose <code>true</code> if the mock should throw an 
   *     exception if {@link Closeable#close()} is invoked, <code>false</code> 
   *     otherwise.
   * @param failOnFlush <code>true</code> if the mock should throw an exception
   *     if {@link Flushable#flush()} is invoked, <code>false</code> otherwise.
   * @throws IOException if an exception occurs.
   */
  private void initialiazeMock(Closeable mock, boolean failOnClose, 
      boolean failOnFlush) throws IOException {
    reset(mock);
    if (mock instanceof Flushable) {
      ((Flushable) mock).flush();
      if (failOnFlush) {
        
        expectLastCall().andThrow(
            new IOException("This should only appear in the "
            + "logs. It should not be rethrown."));
      }
    }
    mock.close();
    if (failOnClose) {
      expectLastCall().andThrow(
          new IOException("This should only appear in the "
          + "logs. It should not be rethrown."));
    }
    replay(mock);
  }
  
  /**
   * Invoke {@link Closeables#close(Closeable, boolean)} on {@code Closeable}
   * and fails if an exception is expected but not happening or if it is not 
   * expected but it is thrown.
   * 
   * @param closeable the instance to close.
   */
  private void doClose(Closeable closeable, boolean expectThrown) {
    try {
      Closeables.close(closeable, false);
      if (expectThrown) {
        fail("Didn't throw exception.");
      }
    } catch (IOException e) {
      if (!expectThrown) {
        fail("Threw exception");
      }
    }
    verify(closeable);
  }
  
  /**
   * Tests that method {@link Closeables#close(Closeable, boolean)} wont throw 
   * any exception while closing a <code>null</code> reference. 
   * 
   * @throws IOException if <code>null</code> is not handled correctly.
   */
  public void testClose_null() throws IOException {
    Closeables.close(null, true);
    Closeables.close(null, false);
  }
  
  /**
   * Tests that method {@link Closeables#closeQuietly(Closeable)} won't throw 
   * any exception while closing a <code>null</code> reference. 
   * 
   * @throws IOException if <code>null</code> is not handled correctly.
   */
  public void testCloseQuietly_null() throws IOException {
    Closeables.closeQuietly(null);
  }
  
  /**
   * Test method for {@link Closeables#close(java.io.Closeable, boolean)}.
   */
  public void testClose_closeable() {
    doClose(mockCloseable, false);
  }

  /**
   * Test method for {@link Closeables#close(java.io.Closeable, boolean)}.
   */
  public void testClose_flushable() {
    doClose(mockCloseableFlushable, false);
  }
  
  /**
   * Test method for {@link Closeables#close(java.io.Closeable, boolean)}.
   */
  public void testClose_failingCloseable() {
    doClose(mockFailingCloseable, true);
  }
  
  /**
   * Test method for {@link Closeables#close(java.io.Closeable, boolean)}.
   */
  public void testClose_failingFlushable() {
    doClose(mockFailingFlushable, true);
  }
  
  /**
   * Test method for {@link Closeables#close(java.io.Closeable, boolean)}.
   */
  public void testClose_failingCloseableFlushable() {
    doClose(mockFailingCloseableFlushable, true);
  }
  
  /**
   * Test method for {@link Closeables#closeQuietly(java.io.Closeable)}.
   */
  public void testCloseQuietly_closeable() {
    Closeables.closeQuietly(mockCloseable);
  }
  
  /**
   * Test method for {@link Closeables#closeQuietly(java.io.Closeable)}.
   */
  public void testCloseQuietly_flushable() {
    Closeables.closeQuietly(mockCloseableFlushable);
  }

  /**
   * Test method for {@link Closeables#closeQuietly(java.io.Closeable)}.
   */
  public void testCloseQuietly_failingCloseable() {
    Closeables.closeQuietly(mockFailingCloseable);
  }
  
  /**
   * Test method for {@link Closeables#closeQuietly(java.io.Closeable)}.
   */
  public void testCloseQuietly_failingFlushable() {
    Closeables.closeQuietly(mockFailingFlushable);
  }
  
  /**
   * Test method for {@link Closeables#closeQuietly(java.io.Closeable)}.
   */
  public void testCloseQuietly_failingCloseableFlushable() {
    Closeables.closeQuietly(mockFailingCloseableFlushable);
  }
}
