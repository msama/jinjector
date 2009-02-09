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

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.EmptyVisitor;

import java.io.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ClassManager helps understanding which classes are 
 * part of the same hierarchy.
 * 
 * <p>At instrumentation time jars of the target platform are known 
 * and hierarchies are going to be modified or created during 
 * the instrumentation itself. 
 * 
 * <p>Classes are loaded in the following order:
 * <ul>
 * <li> first: framework classes (which cannot be modified), 
 * <li> then: instrumented classes in reverse order... 
 * <li> last: the original bytecode.
 * </ul>
 * 
 * @author Michele Sama
 * 
 */
public class ClassManager {

  /**
   * Maps the superclass for each class loaded. Interfaces will have 
   * <code>Object</code> as superclass, and <code>Object</code> will be the 
   * only class with <code>null</code> as a superclass.
   * 
   * <p>The value is a single string because in Java multiple inheritance 
   * is not allowed.
   */
  private Map<String, String> classHierarchyMap = new HashMap<String, String>();
  
  /**
   * Maps for each <code>class</code> with its implemented interface, or 
   * <code>null</code> if none.
   * 
   * <p>The value is an array of Strings because in Java multiple 
   * implementation is allowed.
   */
  private Map<String, String[]> implementedInterfaceMap = 
      new HashMap<String, String[]>();
      
  /**
   * Keeps trace of all the loaded interfaces.  
   */    
  private Set<String> interfaces = new HashSet<String>();

  private String[] classpath;
  private String[] jars;

  
  /**
   * {@link ClassAdapter} recreating dependencies between classes.
   * 
   * <p>This field is protected for testing purposes.
   */
  protected HierarchyClassAdapter classAdapter = new HierarchyClassAdapter();
  
  private static Logger logger = Logger.getLogger(ClassManager.class.getName());

  /**
   * Creates a new ClassManager with a specified classpath 
   * and with a specified set of jar files.
   * 
   * <p>At least one of the two parameters must be not <code>null</code>, and it
   * will be used to look for classes. The array of jar can be <code>null</code>
   * if no specific dependence is required. The classpath can be 
   * <code>null</code> if the source and instrumented files are contained in 
   * one of the specified jars. 
   * 
   * @param classpath The classpath.
   * @param jars The jarfiles.
   * @throws IllegalArgumentException if both the arguments are 
   *    <code>null</code>.
   */
  public ClassManager(String[] classpath, String[] jars) {
    if (classpath == null && jars == null) {
      throw new IllegalArgumentException("A set of directories and/or a set " +
          "of jar files are required.");
    }
    this.classpath = classpath;
    this.jars = jars;
  }
  
  /**
   * Creates an instance of {@link ClassManager} which will look for classes 
   * in a specified set of jar files.
   * 
   * <p>This is a convenience constructor which can be used instead of 
   * ClassManager(null, [instrumented, original, ...]);
   * 
   * @param instrumentedJar the path to the jar file containing the 
   *    instrumented files.
   * @param originalJar the jar file containing the original bytecode.
   * @param jarsInClasspath an array of <code>String</code> containing the path
   *    of jar files to include in the class path. 
   */
  public ClassManager(String instrumentedJar, String originalJar, 
      String[] jarsInClasspath) {
    jars = new String[jarsInClasspath.length + 2];
    jars[0] = instrumentedJar;
    jars[1] = originalJar;
    for (int i = 0; i < jarsInClasspath.length; i++) {
      jars[i+2] = jarsInClasspath[i];
    }
  }

  /**
   * Checks if assignee can be assigned from a given type.
   * 
   * <p>This method is a substitute for {@link Class#isAssignableFrom(Class)}
   * which cannot be used because the target class is not loadable.
   * 
   * <p> Determines if the class or interface represented by the assignee 
   * {@link Class} object is either the same as, or is a superclass 
   * or superinterface of, the class or interface represented by the target
   *  {@link Class} parameter. 
   * 
   * <p> It returns <code>true</code> if so; otherwise it returns 
   * <code>false</code>. 
   * 
   * <p> In the original method if this {@link Class}  object represents a 
   * primitive type, this method returns <code>true</code> if the specified 
   * {@link Class} parameter is exactly this Class object; otherwise it 
   * returns <code>false</code>. This is not really necessary here.
   * 
   * <p> Specifically, this method tests whether the type represented by the 
   * second specified Class parameter can be converted to the type represented 
   * by this first Class object via an identity conversion or via a widening 
   * reference conversion. 
   * 
   * <p> See The Java Language Specification, sections 5.1.1 and 5.1.4 , 
   * for details. 
   * 
   * @param assignee The class which needs to be assigned.
   * @param target The type in which it should be assigned.
   * @return true if it is possible to assign assignee from target.
   */
  public boolean isAssignableFrom(String assignee, String target) {
    if (isInterface(assignee)) {
      return isInterfaceAssignableFrom(assignee, target);
    } else {
      return isClassAssignableFrom(assignee, target);
    }
  }
  
  /**
   * Checks if a class can be assigned from a specific type.
   * 
   * @param assignee The type into which the instance is going to be assigned.
   * @param target The type from which the instance is going to be assigned.
   * @return <code>true</code> if the assignment is possible
   */
  private boolean isClassAssignableFrom(String assignee, String target) {
    while (target != null) {
      if (assignee.equals(target) || isImplementing(target, assignee)) {
        return true;
      }
      target = getSuperclass(target);
    }
    return false;
  }
  
  /**
   * Checks if an interface can be assigned from a specific type.
   * 
   * <p> In Java an interface can extend from multiple interfaces. ASM loads 
   * interfaces with <code>Object</code> as a superclass and all the extended 
   * super interfaces as a list of implemented interfaces.
   * 
   * @param assignee The type into which the instance is going to be assigned.
   * @param target The type from which the instance is going to be assigned.
   * @return <code>true</code> if the assignment is possible
   */
  private boolean isInterfaceAssignableFrom(String assignee, String target) {
    if (assignee.equals(target) || isImplementing(target, assignee)) {
      return true;
    }
    String[] parents = implementedInterfaceMap.get(target);
    for (int i = 0; i < parents.length; i++) {
      if (isInterfaceAssignableFrom(assignee, parents[i])) {
        return true;
      }
    }
    return false;
  }

  /**
   * Tells if the specified class/interface interface implements the specified 
   * interface.
   * 
   * @param child The class/interface to be verified
   * @param target The target interface
   * @return <code>true</code> if the class implements the target interface.
   */
  public boolean isImplementing(String child, String target) {
    // Preconditions
    if (!isInterface(target)) {
      return false;
    }
    if (implementedInterfaceMap.containsKey(child) == false) {
      reloadClass(child);
    }
    String[] interfaceArray = implementedInterfaceMap.get(child);
    // child does not implement any interface.
    if (interfaceArray == null) {
      return false;
    }
    
    for (String s : interfaceArray) {
      if (s.equals(target)) {
        return true;
      }
    }
    
    return false;
  }

  /**
   * Try to reload a class from a list of specified jars.
   * 
   * @param internalClassName The class to load
   * @return <code>true</code> if the class has been found in one of the jar, 
   *    <code>false</code> otherwise or if there are no jar.
   * @throws IllegalArgumentException if the classname is <code>null</code>.
   */
  protected boolean reloadFromJar(String internalClassName) {
    if (internalClassName == null) {
      throw new IllegalArgumentException("Classname cannot be null.");
    }
    if (jars == null) {
      return false;
    }
    JarFile jar = null;
    for (String j : jars) {
      
      // Explore each jar file  
      try {
        jar = new JarFile(j);
      } catch (IOException e) {
        logger.log(Level.SEVERE, 
            "Exception while opening jar file: " + j + " " + e.getMessage(), e);
        // in case of exception skips this file and try the next.
        continue;
      }

      Enumeration<JarEntry> entries = jar.entries();
      final String internalClass = internalClassName + ".class";
      while (entries.hasMoreElements()) {
        JarEntry entry = entries.nextElement();
        // skips non class files.
        if (entry.getName().equals(internalClass) == false) {
          continue;
        }
        
        byte[] bytecode = null;
        InputStream jis = null;
        try {
          jis = jar.getInputStream(entry);
          bytecode = Bytes.toByteArray(jis);
        } catch (IOException e) {
          logger.log(Level.SEVERE, 
              "Exception while opening " + internalClassName + 
              ".class :" + e.getMessage(), e);
          continue;
        } finally {
          Closeables.closeQuietly(jis);
        } 
        ClassReader cr = new ClassReader(bytecode);
        cr.accept(classAdapter, 0);
        return true;
      }
      
      // If it has been opened close the jar file.
      if (jar != null) {
        /* JarFile is not implementing Closable, so Closeables.closeQuietly()
         * cannot be used.
         */
        try {
          jar.close();
        } catch (IOException e) {
          // nothing to be done
        }
      }  
    }
    return false;
  }

  /**
   * Converts a class name to a file name and search for it in specified
   * class paths exactly in the specified order.
   * 
   * @param internalClassName the class to find with '/' instead of '.'.
   * @return <code>true</code> if the class has been loaded correctly, 
   *    <code>false</code> otherwise, or if the pool of directories 
   *    was <code>null</code>
   * @throws IllegalArgumentException if the classname is <code>null</code>.
   */
  protected boolean reloadClassFromDirs(String internalClassName) {
    if (internalClassName == null) {
      throw new IllegalArgumentException("Classname cannot be null.");
    }
    if (classpath == null) {
      return false;
    }
    File file = null;
    for (String path : classpath) {
      file = new File(path, internalClassName + ".class");
      if (file.exists()) {
        try {
          byte[] bytecode = Files.toByteArray(file);
          ClassReader cr = new ClassReader(bytecode);
          cr.accept(classAdapter, 0);
          return true;
        } catch (IOException e) {
          logger.log(Level.SEVERE, 
              "An exception ocurred while loading class from dirs: " + 
              e.getMessage(), e);
          continue;
        }
      } 
    }
    return false;
  }

  /**
   * Look for the specified interface in the inner list and return true if 
   * it exists.
   * 
   * @param interfaceName The interface too look for.
   * @return boolean <code>true</code> if an interface with that name exists.
   */
  public boolean isInterface(String interfaceName) {
    if (!interfaces.contains(interfaceName)) {
      reloadClass(interfaceName);
    }
    return interfaces.contains(interfaceName);
  }

  /**
   * Returns the superclass of the specified object or null if none.
   * 
   * @param classToQuery The class to query.
   * @return the superclass or null if the classToQuery refers to 
   *    java.lang.Object.
   * @throws IllegalStateException if the class corresponding to the 
   *    given classname could not be found.
   */
  public String getSuperclass(String classToQuery) {
    if (!classHierarchyMap.containsKey(classToQuery)) {
      reloadClass(classToQuery);
    }
    return classHierarchyMap.get(classToQuery);
  }
  
  /**
   * Try to load a class first from jars then from the ordered set of
   * directories.
   * 
   * <p> This method is protected for testing purposes.
   * 
   * @param className The class to load.
   * @throws IllegalStateException if the class specified by 
   *    <pre>classnName</pre> was not found.
   */
  protected void reloadClass(String className) { 
    if (className == null) {
      throw new IllegalArgumentException("Classname cannot be null. " +
          "This can be happening because ASM uses null as a superclass " +
          "of Object.");
    }
    boolean reloaded =
        reloadFromJar(className) || reloadClassFromDirs(className);
    if (!reloaded) {
      throw new IllegalStateException("Cannot load: " + className 
          + ".\nMaybe the application under test is referencing a library " +
          "that has not been included in the instrumentation.");
    }
  }
  
  /**
   * Cleans the footprint of a preloaded class. This is needed when a class 
   * which was part of a hierarchy has been instrumented because the superclass
   * may have been changed. 
   * 
   * @param classname the name of the class to be clean.
   * @return <code>true</code> if the class had already been loaded, 
   *    <code>false</code> if it had not been loaded.
   */
  // TODO: call this method from the ClassBytecodeLoader
  public boolean cleanLoadedClass(String classname) {
    if (!classHierarchyMap.containsKey(classname)) {
      return false;
    }
    classHierarchyMap.remove(classname);
    implementedInterfaceMap.remove(classname);
    interfaces.remove(classname);
    return true;
  }
  
  /**
   * Explore classes and relate them to their superclass by filling the Map.
   * 
   * @author Michele Sama
   *
   */
  protected class HierarchyClassAdapter extends EmptyVisitor {
    
    /** 
     * Puts visited classes into a map relating each one with its own 
     * superclass.
     */
    @Override
    public void visit(int version, int access, String name, String signature,
        String superName, String[] interf) {

      addClassInHierarchy(name, superName);
      addImplementedInterfaces(name, interf);
      addInterface(access, name);
    }
    
    /**
     * Adds the class name in the hierarchy list.
     * If the super class name is <code>null</code> the class is 
     * {@link Object}, and it will be added anyway.
     * 
     * <p> For interfaces the superclass is set by default as {@link Object}.
     * 
     * @param className The class' name to add.
     * @param superName The superclass' name to add.
     */
    private void addClassInHierarchy(String className, String superName) {
      if (className == null) {
        throw new IllegalArgumentException("Class name cannot be null!");
      }
      if (superName == null && !ClassNames.JAVA_LANG_OBJECT.equals(className)) {
        throw new IllegalArgumentException(
            "Super class name cannot be null for class " + className);
      }
      classHierarchyMap.put(className, superName);
    }
    
    /**
     * Add the class name to the interface list if it is an interface.
     * 
     * @param access The type flag
     * @param name The class' name.
     */
    private void addInterface(int access, String name) {
      if ((access & Opcodes.ACC_INTERFACE) != 0) {
        interfaces.add(name);
      }
    }
    
    /**
     * Adds an array of all the implemented interfaces.
     * 
     * @param className The name of the <code>Class<code> to add.
     * @param interfaceArray An array of implemented interfaces or 
     *     <code>null</code> if empty.
     */
    private void addImplementedInterfaces(String className, 
        String[] interfaceArray){
      if (className == null) {
        throw new IllegalArgumentException("Class name cannot be null!");
      }
      implementedInterfaceMap.put(className, interfaceArray);
    }
  }

}
