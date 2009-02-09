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

import org.objectweb.asm.MethodVisitor;

/**
 * Base class for all the servant.
 * It just keeps a record to the MethodVisitor which own this instance.
 * 
 * <p>A servant is a class which will inject bytecode through the MethodVisitor 
 * which is containing it . By using a set of servant the number of call to the
 * ASM library is reduced and calls can be divided by purpose. 
 * 
 * <p>A servant is useful when injected code is following a model, or must have
 * a specific order, then the servant can verify that the injection is 
 * respecting such rules.
 * 
 * <p>For instance if the injected code will contain an object which must be 
 * first instantiated, then used then destroyed or finalized, a subclass of 
 * servant can be created to verify that such object is only used after the 
 * initialization and before the finalization.
 * 
 * <p>Another possible usage is as a container for commonly used operations.
 * 
 * 
 * @author Michele Sama
 *
 */
public class Servant {

  protected MethodVisitor mv = null;

  /**
   * Creates an instance of the servant which will be serving the given 
   * {@link MethodVisitor}.
   * 
   * @param mv the {@link MethodVisitor} to use.
   */
  public Servant(MethodVisitor mv) {
    this.mv = mv;
  }
  
  
}
