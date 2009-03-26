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

package com.google.devtools.build.wireless.testing.java.injector.coverage;

import com.google.devtools.build.wireless.testing.java.injector.ClassManager;
import com.google.devtools.build.wireless.testing.java.injector.InstrumentedJarCreator;
import com.google.devtools.build.wireless.testing.java.injector.Loadable;
import com.google.devtools.build.wireless.testing.java.injector.coverage.GenerateCoverageInitializationClassAdapter.CoverageInitializationData;
import com.google.devtools.build.wireless.testing.java.injector.util.StringUtil;

import org.objectweb.asm.ClassVisitor;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Loadable for code coverage.
 * 
 * @author Michele Sama
 */
public class CoverageLoadable extends Loadable {

  public static final String LOGGER_NAME = "CoverageLoadable";
  
  /**
   * Activates coverage mapping and specifies the level of coverage.
   * 
   * <p>At runtime this will be converted into a {@link CoverageMode}.
   */
  public static final String PROPERTY_COVERAGE_MODE = "coverage";
  
  /**
   * Defines a human readable description for the 
   * {@value #PROPERTY_COVERAGE_MODE} property. 
   */
  public static final String PROPERTY_COVERAGE_MODE_DESCRIPTION = 
      "Specifies the coverage mode. Accepted values are: " + 
      Arrays.deepToString(CoverageMode.values()) + ".";
  
  private CoverageMode coverage;
  
  /**
   * A ";" separated list of files to include (+/-classnames)")
   */
  public static final String PROPERTY_COVERAGE_INCLUSION_LIST = 
      "coverageInclusionList";
  
  /**
   * Defines a human readable description for the 
   * {@value #PROPERTY_COVERAGE_INCLUSION_LIST} property. 
   */
  public static final String PROPERTY_COVERAGE_INCLUSION_LIST_DESCRIPTION = 
      "Specifies which classes or packages will be included or excluded " +
      "from coverage. The most specific definition will be used. Classes " +
      "must be specified using the internal name and the slash '/' as " +
      "a package separator. Multiple inclusion must be separated by a ';'. " +
      "For instance: '+bar/foo;-bar/foo/Foo' includes all the package " +
      "bar.foo but excludes the class bar.foo.Foo";
  
  private String[] coverageInclusionList;
  
  /**
   * Name of the output file for the line coverage. This file name will be 
   * postponed to a platform-specific file root (e.g. file://localhost/).
   */  
  public static final String PROPERTY_LINECOVERAGE_OUTPUT_FILE =
      "lineCoverageOutputFilename";
  
  /**
   * Defines a human readable description for the 
   * {@value #PROPERTY_LINECOVERAGE_OUTPUT_FILE} property. 
   */
  public static final String PROPERTY_LINECOVERAGE_OUTPUT_FILE_DESCRIPTION = 
      "Specifies the output file in which line coverage information will be " +
      "stored.";
  
  /**
   * Contains the name of the output file name to be used for line coverage.
   */
  private String lineCoverageOutputFilename;
  
  private CoverageStatisticContainer coverageStatisticContainer;
    
  /**
   * Checks if all the required properties have been specified correctly.
   * 
   * @see Loadable#loadInternal(java.util.Properties)
   */
  @Override
  public void loadInternal(Properties properties) {
    coverage = CoverageMode.valueOf(
        properties.getProperty(PROPERTY_COVERAGE_MODE));
    coverageInclusionList = StringUtil.split(
        properties.getProperty(PROPERTY_COVERAGE_INCLUSION_LIST), " ;:,");
    lineCoverageOutputFilename = properties.getProperty(
        PROPERTY_LINECOVERAGE_OUTPUT_FILE);
    
    // Coverage inclusion list is mandatory
    if (coverageInclusionList == null && 
        coverage != CoverageMode.DISABLED) {
      throw new IllegalArgumentException("No coverage inclusion list has " +
      		"been specified.");
    }
    
    // Output file name is mandatory for line coverage.
    if (lineCoverageOutputFilename == null && 
        coverage == CoverageMode.LINE) {
      throw new IllegalArgumentException("No output file name has " +
            "been specified.");
    }
  }

  /**
   * Adds to the instrumentation chain a sequence of adapters which will 
   * instrument the code for code coverage.
   * 
   * <p>Three different operations are required:
   * <ul>
   * <li>Initializing the coverage by creating the relevant data structures in
   * memory at runtime;
   * <li>Collecting coverage information at the end of the execution;
   * <li>Flagging each line as it is executed.
   * </ul>
   * 
   * @see Loadable#operation(org.objectweb.asm.ClassVisitor, ClassManager)
   */
  @Override
  public ClassVisitor operation(ClassVisitor cv, ClassManager classManager) {
    if (coverage != CoverageMode.DISABLED) {
      CoverageInitializationData initData = new CoverageInitializationData(
          coverage, runId, lineCoverageOutputFilename);
      cv = new GenerateCoverageInitializationClassAdapter(platform, cv, initData);
      cv = new GenerateCoverageClassAdapter(platform, cv, classManager);
      cv = new CodeCoverageClassAdapter(
          cv, coverageStatisticContainer, coverageInclusionList, 
          coverage);
    }
    return cv;
  }

  /**
   * Adds index files to the produced jar.
   * 
   * <p>At instrumentation time a full report of lines/methods mapped for 
   * coverage is created. These informations are stored into a set of index 
   * files which this method is adding to the jar. At run time the indexes are
   * used to allocate memory and to map lines/methods as they have been
   * instrumented.
   * 
   * @see Loadable#postOperation(java.lang.String, InstrumentedJarCreator)
   */
  @Override
  public void postOperation(String outputDir, 
      InstrumentedJarCreator instrumentedJar) {
    if (coverage != CoverageMode.DISABLED) {
      try {
        coverageStatisticContainer.generateOutput(
            outputDir, runId, instrumentedJar);
      } catch (IOException e) {
        Logger log = Logger.getLogger(LOGGER_NAME);
        log.fine("Problem writing instrumentation data files: " + 
            e.getMessage());
      }
    }
  }

  /**
   * Initializes a container which counts the number of instrumented 
   * lines/methods. The instance of {@link CoverageStatisticContainer} is used 
   * to index all the instrumented entries.
   * 
   * @see Loadable#preOperation()
   */
  @Override
  public void preOperation() {
    if (coverage != CoverageMode.DISABLED) {
      coverageStatisticContainer = new CoverageStatisticContainer();
    }
  }

  /**
   * Prints a human-readable of this loadable including information about 
   * its properties.
   * 
   * @see Loadable#printUsage()
   */
  @Override
  public void printUsage() {
    Logger log = Logger.getLogger(LOGGER_NAME);
    log.fine(getClass().getCanonicalName());
    log.fine(PROPERTY_COVERAGE_MODE + ":" + PROPERTY_COVERAGE_MODE_DESCRIPTION);
    log.fine(PROPERTY_COVERAGE_INCLUSION_LIST + ":" + 
        PROPERTY_COVERAGE_INCLUSION_LIST_DESCRIPTION);
    log.fine(PROPERTY_LINECOVERAGE_OUTPUT_FILE + ":" + 
        PROPERTY_LINECOVERAGE_OUTPUT_FILE_DESCRIPTION);
  }

}
