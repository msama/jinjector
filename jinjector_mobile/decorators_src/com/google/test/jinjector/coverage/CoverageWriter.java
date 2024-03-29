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

import java.io.IOException;

/**
 * Coverage report writer. 
 * 
 * <p>A class implementing this interface should be able to write a coverage 
 * report into the given path.
 *
 * @author Olivier Gaillard
 */
public interface CoverageWriter {
  
 /**
  * Writes a report using data from the CoverageManager.
  *
  * @param path the path to write the coverage report to
  * @throws IOException if an error occurs while writing the report.
  */
  public void writeFullReport(String path) throws IOException;
}
