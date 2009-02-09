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

package com.google.devtools.build.wireless.testing.java.injector;

import java.io.*;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

/**
 * Create the output jar including instrumented classes.
 * 
 * @author Olivier Gaillard
 */
public class InstrumentedJarCreator {
  private final JarOutputStream jarOutputStream;
  private final String filename;
  
  /**
   * Creates a new output jar.
   *
   * @param filename the filename of the jar
   */
  public InstrumentedJarCreator(String filename) {
    jarOutputStream = createOutputJar(filename);
    this.filename = filename;
  }
  
  private JarOutputStream createOutputJar(String filename) {
    try {
      FileOutputStream fileOutputSteam = new FileOutputStream(filename);
      
      // Created without a manifest because the application may already have one
      JarOutputStream jar = new JarOutputStream(fileOutputSteam);
      return jar;

    } catch (IOException e) {
      throw new RuntimeException(
          String.format("Cannot create the output jar '%s'", filename), e);
    }
  }
  
  /**
   * Closes the output stream or throw.
   *
   * @throws RuntimeException if the stream cannot be closed.
   */
  public void closeOutputJar() {
    try {
      jarOutputStream.close();
    } catch (Exception e) {
      throw new RuntimeException(
          String.format("Cannot close the output jar '%s'", filename), e);
    }
  }
  
  /**
   * Adds a file to jar.
   *
   * @param file the name of the file
   * @param bytes the content of the file
   *
   * @throws IOException if the file cannot be added
   */
  public void addFile(File file, byte[] bytes) throws IOException {
    JarEntry jarEntry = new JarEntry(file.getPath());
    jarEntry.setTime(file.lastModified());
    jarOutputStream.putNextEntry(jarEntry);
    jarOutputStream.write(bytes);
  }
}
