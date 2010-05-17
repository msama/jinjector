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

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.logging.Logger;

/**
 * ClassAdapter which exposes all the call to methods and fields-access to 
 * objects. Only objects matching a specific pattern are injected and inside 
 * those only specific methods/field are exposed according to a set of regular 
 * expressions.
 * 
 * @author Michele Sama
 * 
 */
public class WhiteBoxClassAdapter extends ClassAdapter {

  protected Platform targetPlatform;
  
  /**
   * The regular expression containing the class inclusion.
   */
  protected String classInclusionPattern = null;
   
  /**
   * The regular expression which specify method inclusion.
   */
  protected String methodInclusionPattern = null;
  
  /**
   * The regular expression which specify field inclusion.
   */
  protected String fieldInclusionPattern = null;

  protected String currentClass;

  protected boolean isInterface;

  protected static Logger logger = Logger.getLogger("WhiteBoxClassAdapter");
  
  /**
   * Creates a white box class adapter which will delegate a white box method 
   * adapter for visiting each method.
   * 
   * TODO: this is going to be changed ass soon a selector will be 
   *    implemented.
   * 
   * @param cv The nested ClassVisitor.
   * @param classInclusion A String containing class inclusion.
   * @param methodInclusion A String containing method inclusion.
   * @param fieldInclusion A String containing field inclusion.
   * @param platform The target {@link Platform}.
   */
  public WhiteBoxClassAdapter(ClassVisitor cv, String classInclusion,
      String methodInclusion, String fieldInclusion, Platform platform) {
    super(cv);
    targetPlatform = platform;
    classInclusionPattern = classInclusion;
    methodInclusionPattern = methodInclusion;
    fieldInclusionPattern = fieldInclusion;
  }

  /**
   * Visits a class and stores a flag saying if it is an interface.
   */
  @Override
  public void visit(int version, int access, String name, String signature,
      String superName, String[] interfaces) {
    cv.visit(version, access, name, signature, superName, interfaces);
    currentClass = name;
    isInterface = (access & Opcodes.ACC_INTERFACE) != 0;
  }

  /**
   * Creates and returns a white box method adapter is the current class is not 
   * an interface and is the method is not toString, because toString is 
   * currently used by the white box and instrumenting it would create a loop.
   */
  @Override
  public MethodVisitor visitMethod(int access, String name, String desc,
      String signature, String[] exceptions) {
    MethodVisitor mv =
        cv.visitMethod(access, name, desc, signature, exceptions);
    if (!isInterface && mv != null && !name.equals("toString")) {
      mv = new WhiteBoxMethodAdapter(mv, access, currentClass, name, desc, 
          classInclusionPattern, methodInclusionPattern, fieldInclusionPattern,
          targetPlatform);
    }
    return mv;
  }

}
