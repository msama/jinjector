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

import com.google.devtools.build.wireless.testing.java.injector.util.Bytes;
import com.google.devtools.build.wireless.testing.java.injector.util.Closeables;
import com.google.devtools.build.wireless.testing.java.injector.util.Files;
import com.google.devtools.build.wireless.testing.java.injector.util.StringUtil;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.*;
import java.util.*;
import java.util.jar.*;
import java.util.logging.Logger;

/**
 * 
 * Loads, instruments and saves class files into a jar or into a destination 
 * folder.
 * 
 * <p>An alternative implementation would extend a {@link ClassLoader} but that
 * this would have problems if:
 * <ul>
 * <li>the injected class is not in the class path (e.g. j2me classes)
 * <li>the injected class is not compliant with the execution environment (e.g.
 * does not compile with the current Java virtual machine)
 * </ul>
 * 
 * @author Michele Sama
 * 
 */
public class ClassBytecodeLoader {
  
  private static final String ARGUMENT_FILENAME = "properties=";

  /**
   * Specifies a ';' list of jars which will be used as a class base 
   * during the instrumentation.
   */
  public static final String PROPERTY_JARS = "jars";
  
  /**
   * Description for the {@link #PROPERTY_JARS} property. 
   */
  public static final String PROPERTY_JARS_DESCRIPTION = 
      "A ';' separated list of Jar files to be included in " +
      "the instrumentation (e.g. jar1.jar;jar2.jar).";
  
  protected static String[] jarArray = null;
  
  /**
   * Specify the target platform which controls the instrumentation.
   * 
   * <p>At runtime this property will be an instance of
   * {@link Platform}.
   */
  public static final String PROPERTY_PLATFORM = "platform";
  
  /**
   * Description for the {@link #PROPERTY_PLATFORM} property. 
   */
  public static final String PROPERTY_PLATFORM_DESCRIPTION = 
      "Target platform . Accepted values are: " + 
      Arrays.deepToString(Platform.values());
  
  /**
   * Specify where to store instrumented files.
   * 
   * TODO is this still needed or we can simply add files into a jar.
   */
  public static final String PROPERTY_DESTINATION_FOLDER = "destinationFolder";
  
  /**
   * Description for the {@link #PROPERTY_DESTINATION_FOLDER} property. 
   */
  public static final String PROPERTY_DESTINATION_FOLDER_DESCRIPTION = 
      "Destination folder for instrumented bytecode";
   
  /**
   * Specify where to load binary files from.
   */
  //TODO: get rid of this parameter since we can now have a jar as
  // input, currently the ClassManager still needs this folder to know about
  // the class hierarchy.
  public static final String PROPERTY_BINARY_FOLDER = "binaryFolder";
  
  /**
   * Description for the {@link #PROPERTY_BINARY_FOLDER} property. 
   */
  public static final String PROPERTY_BINARY_FOLDER_DESCRIPTION = 
      "Binary folder from which to load bytecode";
  
  /**
   * Used to tag data files included in the jar to make the names unique for 
   * each run.
   */
  public static final String PROPERTY_RUN_ID = "runId";
  
  /**
   * Description for the {@link #PROPERTY_RUN_ID} property. 
   */
  public static final String PROPERTY_RUN_ID_DESCRIPTION = 
      "Id that allows each instrumentation run to be " +
      "identified. This is useful to identify produced output in case of " +
      "multiple executions.";
  
  /**
   * Input jar to instrument (optional, the binaryFolder can also be used).
   * 
   * <p>If an input jar is specified it will be uncompressed in binary folder 
   * as specified by {@link #PROPERTY_BINARY_FOLDER}.
   * 
   * @see #uncompressInputJarIfRequired(String) 
   */
  public static final String PROPERTY_INPUT_JAR = "inputJar";
  
  /**
   * Description for the {@link #PROPERTY_INPUT_JAR} property. 
   */
  public static final String PROPERTY_INPUT_JAR_DESCRIPTION = 
      "Input jar to instrument (optional, the binaryFolder can " +
      "also be used)";
  
  /**
   * File name of the output jar which will contain all the instrumented files.
   */
  public static final String PROPERTY_OUTPUT_JAR = "outputJar";
  
  /**
   * Description for the {@link #PROPERTY_OUTPUT_JAR} property. 
   */
  public static final String PROPERTY_OUTPUT_JAR_DESCRIPTION = 
      "File name of the output jar.";
  
  /**
   * Contains all the instrumentation properties to be used during the
   * instrumentation which are loaded from a configuration file passed as a 
   * main argument.
   * 
   * <p>General properties are specified in this class. Other {@link Loadable}s 
   * may specify additional properties.
   */
  private Properties properties;
  
  /**
   * A list of {@link Loadable}s instantiated by name from the property file.
   * 
   * @see #loadLoadables()
   */
  private List<Loadable> loadables = new ArrayList<Loadable>();
  
  
  /**
   * Creates a new ClassBytecodeLoader using a given set of properties and 
   * starts the instrumentation.
   * 
   * @param properties the given set of properties containing base parameters
   *     and a list of {@link Loadable}s.
   *   
   * TODO: split this method in two, one constructing the object and one
   *     running the instrumentation.
   */
  public ClassBytecodeLoader(Properties properties) {
    this.properties = properties;
    
    String binaryFolder = properties.getProperty(PROPERTY_BINARY_FOLDER);
    String destinationFolder = properties.getProperty(PROPERTY_DESTINATION_FOLDER);
    
    /*
     * TODO: A possible enhancement is to check all the required 
     *     parameters before reporting the problem(s) to the caller. Otherwise 
     *     the caller only learns one thing at a time by trial and error. 
     */
    // Check if the binary folder has been specified.
    if (binaryFolder == null) {
      throw new RuntimeException("Property " + PROPERTY_BINARY_FOLDER + 
          " is mandatory!");
    }
    
    // Checks if the destination folder has been specified
    if (destinationFolder == null) {
      throw new RuntimeException("Property " + PROPERTY_DESTINATION_FOLDER +
          " is mandatory!");
    }
    
    // Loads an array of referenced jars  
    String propertyJars = properties.getProperty(PROPERTY_JARS);
    if (propertyJars != null) {
      jarArray = StringUtil.split(propertyJars, ";");
    }
    
    String outputJar = properties.getProperty(PROPERTY_OUTPUT_JAR);
    if (outputJar == null) {
      throw new RuntimeException("Property " + PROPERTY_OUTPUT_JAR +
          " is mandatory!");
    } else {
      instrumentedJar = new InstrumentedJarCreator(outputJar);
    }
    
    // Checks and extract the input jar if it exists 
    Manifest manifest = uncompressInputJarIfRequired(binaryFolder);
    
    loadLoadables();
    
    logger.info("Starting instrumentation...");
    
    preOperations();
    
    // Folders are checked recursively by #instrumentFolder(...).
    instrumentFolder(createClassManager(), 
        new File(binaryFolder), new File(destinationFolder));
    postOperations();

    instrumentedJar.closeOutputJar();
  }

  /**
   * Default logger for the injector.
   */
  private static Logger logger = Logger.getLogger(
      ClassBytecodeLoader.class.getName());
  
  private static ClassManager classManager = null;  
  
  /**
   * Jar file produced by the instrumentation.  
   */
  private static InstrumentedJarCreator instrumentedJar;

  /**
   * <p>Entry point of the injector tool.
   * The tool parses a set of java binary files and injects them according to a 
   * set of parameters specified by flags and stores the instrumented class 
   * files in a in a destination folder.
   * 
   * <p>The source folder must exist, the destination folder must exist and the 
   * tool must have write permission to the destination folder. 
   * 
   * <p>Recursively the tool parses the source folder and all its subfolders 
   * and recreates the same structure in the destination folder. 
   * For each file contained in one of the explored folders:
   * <ul>
   * <li>If the file is a java class file it is injected;
   * <li>If the file is a resource (non-java) file it is just copied.
   * </ul>
   * 
   * <p>The code injection is performed using the sax-based ASM Objectweb 
   * library, by creating a chain of ClassVisitors starting from a ClassReader 
   * and terminating with a ClassWriter. The rest of the chain is created on 
   * the fly according to the flags. 
   * 
   * <p>The instrumentation process will inject some additional utility classes. 
   * Those "wrappers" are client side code which have to be compiled and 
   * included in the deployed package by the build system. 
   * 
   * <p>The instrumentation is performed in three steps:
   * <ul>
   * <li>{@link #preOperations()}
   * <li>{@link #instrumentFolder(ClassManager, File, File)}
   * <li>{@link #postOperations()}
   * </ul>
   * 
   * @param args {@value #ARGUMENT_FILENAME} followed by a ';' separated lists 
   *     of file properties files.
   */
  public static void main(String[] args) {
    String filenameList = null;
    for (String s: args) {
      if (s.startsWith(ARGUMENT_FILENAME)) {
        filenameList = s.substring(ARGUMENT_FILENAME.length());
      }
      // TODO: let the user add other properties from command line
    }
    
    if (filenameList == null) {
      throw new IllegalArgumentException("No property files specified! Invoke" +
          " JInjector with " + ARGUMENT_FILENAME + "file1;file2;file3");
    }
    
    Properties properties = new Properties();
    String [] filenames = StringUtil.split(filenameList, ";");
    for (String s : filenames) {
      properties = loadProperties(properties, s);
    }
    
    try {
      new ClassBytecodeLoader(properties);
    } catch(RuntimeException ex) {
      logger.severe("An exception occurred while instrumenting.");
      throw ex;
    }
  }
  
  /**
   * Uncompresses the input jar to the given folder if required.
   *
   * @return the manifest of the input jar (or null if none)
   */
  // TODO: remove the temporary folders used to instrument the code,
  // we should not have to uncompress the jar onto disk.
  private Manifest uncompressInputJarIfRequired(String pathToWriteTo) {
    
    String inputJar = properties.getProperty(PROPERTY_INPUT_JAR);
    if (inputJar == null) {
      return null;
    }

    logger.info("Uncompressing jar file " + inputJar + " to " +
        pathToWriteTo + ".");
    
    JarInputStream input = null;
    try {
      input = new JarInputStream(new FileInputStream(inputJar));
      
      JarEntry entry = input.getNextJarEntry(); 
      while (entry != null) {
        File file = new File(pathToWriteTo, entry.getName());
        logger.info(file.getAbsolutePath());
        if (entry.isDirectory()) {
          if (!file.exists()) {
            /*
             *  This will throw a SecurityException if the program does not 
             *  have write permission on the folder.
             */
            file.mkdirs();
          }

        // Writes jar entry to a file.
        } else {
          // Casting. We won't have files that big in the jar..
          long fullFileSize = entry.getSize();
          if (fullFileSize > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("The file " + entry.getName() + 
                " has size " + fullFileSize + " which exceeds the maximum " +
                "file size: " + Integer.MAX_VALUE);
          }
          final int fileSize = (int) fullFileSize;
          byte[] b = new byte[fileSize];
          Bytes.read(input, b, 0, fileSize);
          Files.overwrite(b, file);
        }

        entry = input.getNextJarEntry();
      }

      return input.getManifest();

    } catch (IOException e) {
      throw new RuntimeException(
          String.format("Cannot open the input jar '%s'.", inputJar), e);

    } finally {
      Closeables.closeQuietly(input);
    }
  }
  
  /**
   * <p> Instruments all the files and all the subfolders of the specified 
   * source folder.
   * 
   * <p> For each entry in the directory:
   * <ul>
   * <li>If the entry is a directory this method is recursively invoked on it.
   * <li>If the entry is a class file it is instrumented.
   * <li>If the entry is any other kind of file it is just copied.
   * </ul>
   * 
   * <p> BEWARE: this function is recursive!
   * 
   * @param classManager The {@link ClassManager} to be used during this
   *    method.
   * @param sourceFolder An instance of {@link File} pointing to the source
   *    folder.
   * @param destFolder An instance of {@link File} pointing to the destination
   *    folder.
   */
  private void instrumentFolder(ClassManager classManager, 
      File sourceFolder, File destFolder) {
    try {
      checkSourceFolder(sourceFolder);
      ensureDestinationFolder(destFolder);
    } catch (IOException e) {
      logger.severe("An exception " + e.getMessage() + 
          " occurred while instrumenting folder " + sourceFolder + 
          " storing files into folder " + destFolder + ".");
      throw new RuntimeException("Instrumentation aborted due to an exception.",
          e);
    }
    File[] children = sourceFolder.listFiles();
    for (File f : children) {
      String filename = f.getName();
      if (f.isFile()) {    
        if (filename.endsWith(".class")) {
          // If the file is a class file then inject it.
          applyInstrumentation(classManager, f, 
              new File(destFolder.getAbsolutePath(), filename));
        } else {
          // If the file is a resource just copy it
          copyResourceFile(f, 
              new File(destFolder.getAbsolutePath(), filename));
        }
      } else {
        // If the file is a directory explore it.
        instrumentFolder(classManager, 
            new File(sourceFolder.getAbsolutePath(), filename), 
            new File(destFolder.getAbsolutePath(), filename));
      }
    }
  }
  
  /**
   * Instrument a class file and save it into a new location.
   * 
   * @param classManager The {@link ClassManager} to be used during this 
   *    method.
   * @param source The class file to inject.
   * @param dest The destination file in which to save the instrumented byte 
   *    code. 
   * @throws RuntimeException if an error occurs while loading the bytecode 
   *    or while saving the instrumented file.
   */
  private void applyInstrumentation(ClassManager classManager, 
      File source, File dest) {
    try {
      // Creates a classreader which will start the adaptation chain.            
      byte b[] = Files.toByteArray(source);
      logger.info("Instrumenting file " + source.getAbsolutePath() + ".");
      ClassReader cr = new ClassReader(b);
      
      // Creates a ClassWriter which will write the chain in the new file.
      ClassWriter cw = new ClasspathBasedClassWriter(ClassWriter.COMPUTE_FRAMES,
          classManager);
      try {
    	  // Creates and computes the chain of adaptation.
    	  cr.accept(createAdaptationChain(classManager, cw), 0);
    	  b = cw.toByteArray();
      } catch (RuntimeException asmUnsupported) {
        logger.severe("Instrumentation of file " + source + " FAILED, and " +
          "skipped. This error is caused by an unsupported instruction in ASM. " +
          "This is a temporary fix and patch is under development. See " +
          "http://code.google.com/p/jinjector/issues/detail?id=1 for more details.");
      }
      Files.overwrite(b, dest);
      instrumentedJar.addFile(removeBinaryFolderSubstring(source), b);
      
    } catch (IOException e) {
      logger.severe("An exception " + e.getMessage() + 
          " occurred while instrument file " + source + 
          " and saving it in " + dest + ".");
      throw new RuntimeException("An exception occurred while instrument file " 
          + source + " and saving it in " + dest + ".", e);
    }
  }

  /**
   * Copy a resource file into the new location.
   * 
   * @param source The source file to be copied.
   * @param dest The destination file.
   * @throws RuntimeException if an {@link IOException} occurs. 
   */
  private void copyResourceFile(File source, File dest) {
    try {
      Files.copy(source, dest);
      instrumentedJar.addFile(
          removeBinaryFolderSubstring(source), Files.toByteArray(source));
      
      logger.info("Copying file " + source.getAbsolutePath() + ".");
    } catch (IOException e) {
      logger.severe("An exception " + e.getMessage() + " occurred while copying "
          + source + " to " + dest + ".");
      throw new RuntimeException("Execution aborted because it was " +
      		"impossible to copy " + source + " to " + dest, e);
    }
  }

  private File removeBinaryFolderSubstring(File file) {
    final String root = new File(
        properties.getProperty(PROPERTY_BINARY_FOLDER)).getAbsolutePath();
    final String filename = file.getAbsolutePath();
    return new File(filename.substring(root.length() + 1));
  }
  
  /**
   * Verify the source folder by checking that it exists and that it is a 
   * folder. 
   * 
   * @param sourceFile The path to open as a source folder.
   * @throws IOException If sourcePath does not exist or if it is not a 
   *    directory.
   */
  protected static void checkSourceFolder(File sourceFile) throws IOException {
    if (!sourceFile.exists()) {
      throw new IOException(sourceFile.getAbsolutePath() + " does not exists.");
    }
    
    if (!sourceFile.isDirectory()) {
      throw new IOException(sourceFile.getAbsolutePath() + " is not a folder.");
    }
  }
  
  /**
   * Checks that the destination folder is a folder.
   * If the folder does not exists it try to create it.
   * 
   * @param destFile The path to be checked as a destination folder.
   * @throws IOException If the File exists and it is not a folder or if the 
   * folder cannot be created.
   */
  protected static void ensureDestinationFolder(File destFile) 
      throws IOException {  
    // if source is not a folder return
    if (destFile.isFile()) {
      throw new IOException(destFile.getAbsolutePath() + " is not a folder.");
    }
    // try to create the folder
    if (destFile.exists() == false) {
      if (!destFile.mkdirs()) {
        throw new IOException(destFile.getAbsolutePath() 
            + " cannot be created.");
      }
    }
  }
  
  /**
   * Loads instrumentation properties from a configuration file and returns
   * them. 
   * 
   * @param filename the name of the file containing the properties.
   * @return the loaded set of properties.
   */
  private static Properties loadProperties(Properties properties, String filename) {
    try {
      properties.load(new FileInputStream(filename));
    } catch (FileNotFoundException e) {
      logger.severe("Cannot find the properties file: " +
          e.getMessage());
      throw new RuntimeException(e);
    } catch (IOException e) {
      logger.severe("An error occurred while loading the properties file: " +
          e.getMessage());
      throw new RuntimeException(e);
    }
    return properties;
  }
  
  /**
   * Loads all the {@link Loadable} which have been specified in the property
   * file. 
   * 
   * <p>{@link Loadable}s are instantiated by class name and initialized by calling 
   * the {@link Loadable#load(Properties)} method.
   * 
   * <p>If any error occurs the execution is terminated.
   */
  private void loadLoadables() {
    int index = 0;
    String loadableName = null;
    
    while ((loadableName = properties.getProperty("Adapter" + index)) != null) {
      Loadable loadable;
      
      try {
        loadable = (Loadable) (Class.forName(loadableName)).newInstance();
      } catch (Exception ex) {
        throw new RuntimeException("It was not possible to instantiate " +
            "class: " + loadableName + ". Please check the name and try again.",
            ex);
      }
      
      try {
        loadable.load(properties);
      } catch (Exception ex) {
        // Prints the usage of the failing loadable.
        loadable.printUsage();
        throw new RuntimeException("It was impossible to load loadable: " +
            loadableName + ". Please check properties and try again.", ex);
      }
      
      loadables.add(loadable);
      index ++;
    }
  }
  
  /**
   * Creates a chain of ClassAdapters which are going to be used to process the 
   * byte code.
   * The correct sequence of adapters is created by reading all the flags and by
   * adding the opportune adapters to the chain.
   * 
   * <p>To add a new functionality to this tool it is simply required to define a 
   * new Flag as a static field of this class and to create the right adapter in
   * this method.
   * 
   * <p>Please not that the chain of adaptation is bottom-up. The last added 
   * adapter is executed first. If a certain components need to be executed 
   * before others it has to be added after in the chain.
   * 
   * <p>Code coverage should be the first one to be called,
   * which means that it should be the last one to be executed, in order to 
   * cover only real code and not injected ones.
   * 
   * @param classManager The {@link ClassManager} to be used during this 
   *    method.
   * @param cw The nested ClassWriter which will close the chain.
   * @return The ClassVisitor which will start the chain.
   */
  private ClassVisitor createAdaptationChain(ClassManager classManager, 
      ClassWriter cw) {
    ClassVisitor cv = cw;
    
    for (Loadable l : loadables) {
      cv = l.operation(cv, classManager);
    }
        
    return cv;
  }
  
  /**
   * Factory convenience method to create a ClassManager which will load 
   * byte code in the correct order regarding to the instrumentation.
   * 
   * <p>Each step of instrumentation must use a separate 
   * {@link ClassManager} because the hierarchies may have been changed.
   * 
   * @return an instance of ClassManager correctly initialized.
   */
  private ClassManager createClassManager() {
    if (classManager == null) {
      classManager = new ClassManager(
          new String[] {properties.getProperty(PROPERTY_DESTINATION_FOLDER),
              properties.getProperty(PROPERTY_BINARY_FOLDER)}, jarArray);
    } 
    
    return classManager;
  }
   
  /**
   * Initializations which must be performed before the instrumentation.
   */
  private void preOperations() {
    for (Loadable l : loadables) {
      l.preOperation();
    }
  }

  /**
   * Final operations which need to be performed after the instrumentation.
   */
  private void postOperations() {
    for (Loadable l : loadables) {
      l.postOperation(properties.getProperty(PROPERTY_DESTINATION_FOLDER),
          instrumentedJar);
    }
  }
}
