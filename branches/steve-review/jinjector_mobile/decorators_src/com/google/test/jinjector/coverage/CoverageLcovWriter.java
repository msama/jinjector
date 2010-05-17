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


import com.google.test.jinjector.coverage.CoverageManager.CoverageDataFile;
import com.google.test.jinjector.util.FileConnectionUtil;
import com.google.test.jinjector.util.IoUtil;
import com.google.test.jinjector.util.LineReader;
import com.google.test.jinjector.util.Log;
import com.google.test.jinjector.util.StringUtil;

import java.io.*;

import javax.microedition.io.file.FileConnection;

/**
 * Creates a report using the lcov format.
 *
 * <p> Here is the format of the file:
 * <br>For each source file, there is a section containing file name and coverage data:
 * <ul>
 *   <li>SF:&lt;absolute path to the source file&gt;
 *   <li>FN:&lt;line number of function start&gt;,&lt;function name&gt; for each function
 *   <li>DA:&lt;line number&gt;,&lt;execution count&gt; for each instrumented line
 *   <li>LH:&lt;number of lines with an execution count&gt; greater than 0
 *   <li>LF:&lt;number of instrumented lines&gt;
 * </ul>
 * Sections are separated by:
 * <pre>end_of_record</pre>
 *
 * @author Olivier Gaillard
 * @author Michele Sama
 */
public class CoverageLcovWriter implements CoverageWriter {
  public static final String FILENAME_INSTRUMENTED_LINES =
      "coverageInstrumentedLines.txt";

  public static final char COMMENT = '#';

  public static final char SEPARATOR = '\t';

  private final CoverageManager coverageManager;
  private final CoverageDataFile coverageDataFile;

  public CoverageLcovWriter(CoverageManager coverageManager,
      CoverageDataFile coverageDataFile) {
    this.coverageManager = coverageManager;
    this.coverageDataFile = coverageDataFile;
  }

  /**
  * Writes a full report in emma style.
  */
  public void writeFullReport(String path) throws IOException {
    InputStream inputStream = null;
    LineReader lineReader = null;
    FileConnection fileConnection = null;
    PrintStream printStream = null;

    try {
      long writeCoverageReportDuration = - System.currentTimeMillis();
      final String lcovFilename =
          path + coverageDataFile.getLCovOutputFile() + coverageDataFile.getRunId();
      fileConnection = FileConnectionUtil.createAndOpenFile(lcovFilename);
      // The file must be truncated to avoid bugs when overriding a bigger file
      fileConnection.truncate(0);
      printStream = new PrintStream(fileConnection.openOutputStream());

      inputStream = CoverageManager.class.getResourceAsStream(
          FileConnectionUtil.FILE_SEPARATOR +
          FILENAME_INSTRUMENTED_LINES + coverageDataFile.getRunId());
      lineReader = new LineReader(inputStream);

      InstrumentedLineParser lineParser =
        new InstrumentedLineParser(coverageManager, printStream);

      String line = null;
      while ((line = lineReader.readline()) != null) {
        if (line.length() == 0 || line.startsWith("" + COMMENT)) {
          continue;
        }
        lineParser.parseLineAndWriteLineCoverage(line);
      }
      
      // Log that the coverage has been collected succesfully.
      writeCoverageReportDuration += System.currentTimeMillis();
      Log.log(getClass().getName(), "LCOV file [" + lcovFilename +
          "] written in " + writeCoverageReportDuration + "ms");
    } finally {
      IoUtil.closeCloseable(lineReader);
      IoUtil.closeCloseable(inputStream);
      IoUtil.closeCloseable(printStream);
      FileConnectionUtil.close(fileConnection);
    }
  }

  /**
   * Parse a line from the instrumented lines file, collects the output from
   * the coverage manager and writes it in LCOV format.
   */
  static class InstrumentedLineParser {

    private static final byte[] DOT_JAVA_LN = ".java\n".getBytes();
    private static final byte[] END_OF_RECORD_NL = "end_of_record\n".getBytes();

    private static final byte[] DA = "DA:".getBytes();
    private static final byte[] SF = "SF:".getBytes();
    private static final byte[] LH = "LH:".getBytes();
    private static final byte[] LF = "LF:".getBytes();

    private static final byte[] ZERO_LN = ",0\n".getBytes();
    private static final byte[] ONE_LN = ",1\n".getBytes();

    private CoverageManager coverageManager;
    private PrintStream printWriter;

    /**
     * Creates a new instance which collects coverage.
     *
     * <p>Not thread safe.
     *
     * @param coverageManager the coverage manager from which to collect
     *   coverage.
     * @param printWriter the print writer on which to write coverage.
     */
    public InstrumentedLineParser(CoverageManager coverageManager,
        PrintStream printWriter) {
      this.coverageManager = coverageManager;
      this.printWriter = printWriter;
    }

    /**
     * Parse a line and writes line coverage in lcov format.
     *
     * <p>Any execution of this methods constructs lot's of StringBuilders and
     * Strings in order to print them on file, and its execution is quite slow.
     * Moreover it is invoked once per classfile.
     *
     * <p>This implementation contains optimizations to make the execution
     * as fast as possible.
     *
     * <p>Not thread safe.
     *
     * @param line the string to parse
     * @throws IOException if an error occurs while writing on the stream.
     */
    void parseLineAndWriteLineCoverage(String line)
        throws IOException {
      String[] tokens = StringUtil.split(line, SEPARATOR);

      final int index = Integer.parseInt(tokens[0]);
      final int size = Integer.parseInt(tokens[1]);
      final String filename = tokens[2];

      if (size != (tokens.length - 3)) {
        throw new IllegalStateException("The total number of instrumented " +
            "lines: " + (tokens.length - 3) + " does not match the value: " +
            size + " in line " + line);
      }

      // Write the line in lcov
      printWriter.write(SF);
      printWriter.print(filename);
      printWriter.write(DOT_JAVA_LN);
      int coveredLineSize = 0;

      for (int lineIndex = 0; lineIndex < size; lineIndex++) {

        final String lineNumber = tokens[3 + lineIndex];
        printWriter.write(DA);
        printWriter.print(lineNumber);

        boolean covered = coverageManager.isLineCovered(index, lineIndex);

        if (!covered) {
          printWriter.write(ZERO_LN);
        } else {
          printWriter.write(ONE_LN);
          coveredLineSize++;
        }
      }

      printWriter.write(LH);
      printWriter.print(coveredLineSize);
      printWriter.write('\n');
      printWriter.write(LF);
      printWriter.print(size);
      printWriter.write('\n');

      printWriter.write(END_OF_RECORD_NL);
    }

  }
}
