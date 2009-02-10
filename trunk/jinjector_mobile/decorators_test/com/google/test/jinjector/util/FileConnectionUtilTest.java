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
import java.util.Enumeration;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;

import j2meunit.framework.Test;
import j2meunit.framework.TestCase;
import j2meunit.framework.TestMethod;
import j2meunit.framework.TestSuite;

/**
 * Tests {@link FileConnectionUtil}
 * 
 * @author Michele Sama
 *
 */
public class FileConnectionUtilTest extends TestCase {

  /**
   * Constructor from superclass.
   */
  public FileConnectionUtilTest() {
  }

  /**
   * Constructor from superclass.
   * 
   * @param name the test's name.
   */
  public FileConnectionUtilTest(String name) {
    super(name);
  }

  /**
   * Constructor from superclass.
   * 
   * @param name the test's name.
   * @param method the test's method.
   */
  public FileConnectionUtilTest(String name, TestMethod method) {
    super(name, method);
  }

  /* (non-Javadoc)
   * @see j2meunit.framework.TestCase#suite()
   */
  public Test suite() {
    TestSuite suite = new TestSuite();
    
    // createDirectoriesRecursively
    suite.addTest(new FileConnectionUtilTest(
        "testCreateDirectoriesRecursively_validFoldername") {
      public void runTest() throws IOException {
        testCreateDirectoriesRecursively_validFoldername();
      }
    });
    
    suite.addTest(new FileConnectionUtilTest(
        "testCreateDirectoriesRecursively_noEndSlash") {
      public void runTest() throws IOException {
        testCreateDirectoriesRecursively_noEndSlash();
      }
    });
    
    suite.addTest(new FileConnectionUtilTest(
        "testCreateDirectoriesRecursively_noDir") {
      public void runTest() throws IOException {
        testCreateDirectoriesRecursively_noDir();
      }
    });
    
    return suite;
  }
  
  
  /**
   * Invokes {@link FileConnectionUtil#createDirectoriesRecursively(String)}
   * with the specified folder name on the first available root and assert that
   * the directories have been created properly.
   * 
   * @param foldername the name of the folder which needs to be created.
   * @throws IOException if an exception occurs while creating the folders.
   */
  private void createFolderAndAssert(String foldername) throws IOException {
    Enumeration roots =  FileSystemRegistry.listRoots();
    if (!roots.hasMoreElements()) {
      fail("Device should have at least one root.");
    }
    String root1 = (String) roots.nextElement();
    String filename = "file:///" + root1 + foldername;
    FileConnectionUtil.createDirectoriesRecursively(filename);
    
    FileConnection fc = null;
    try {
      fc = (FileConnection) Connector.open(filename);

      assertTrue("Folder " + filename + " does not exist.", fc.exists());
      assertTrue(filename + " is not a folder.", fc.isDirectory());
    } finally {
      FileConnectionUtil.close(fc);
      /* Deletes all the created folders recursively
       * Note that setFileConnection does not work if the current folder has
       * been deleted, therefore it cannot be used.
       */
      while (!"".equals(foldername)) {
        try {
          fc = (FileConnection) Connector.open("file:///" + root1 + foldername);
          if (fc.exists()) {
            fc.delete();
          }
        } catch (IOException ex) {
          Log.log(FileConnectionUtilTest.class, 
              "An exception occurred while deleting folder " + fc.getPath() +
              ": " + ex.getMessage());
        } finally {
          FileConnectionUtil.close(fc);
          int index = foldername.lastIndexOf(FileConnectionUtil.FILE_SEPARATOR);
          if (index == -1) {
            break;
          }
          foldername = foldername.substring(0, index);
        }
      }
      
    }
  }  
  
  /**
   * Tests {@link FileConnectionUtil#createDirectoriesRecursively(String)} 
   * with a valid folder name.
   * 
   * @throws IOException if an exception occurs while creating the folder.
   */
  public void testCreateDirectoriesRecursively_validFoldername()
      throws IOException {
    createFolderAndAssert("dir0/dir1/dir2/dir3/");
  }
  
  /**
   * Tests {@link FileConnectionUtil#createDirectoriesRecursively(String)} 
   * with a folder name not containing a termination slash.
   * 
   * @throws IOException if an error occurs while creating the folder.
   */
  public void testCreateDirectoriesRecursively_noEndSlash()
      throws IOException {
    createFolderAndAssert("dirX/dirY/dirZ");
  }
  
  /**
   * Tests {@link FileConnectionUtil#createDirectoriesRecursively(String)} 
   * with no folders.
   * 
   * @throws IOException if an exception occurs while creating the folder.
   */
  public void testCreateDirectoriesRecursively_noDir()
      throws IOException {
    createFolderAndAssert("");
  }
  
}
