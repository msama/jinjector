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


import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Supports type-checking for byte codes by mapping char describing base types 
 * into an enumerative.
 * 
 * <p> This class also contains a set of support functions.
 * 
 * TODO: reduce this class by using {@link Type}
 * 
 * @author Michele Sama
 *
 */
public enum TypeDescriptor {
  VOID('V', 0, -1, -1, -1),
  BOOLEAN('Z', Opcodes.DUP, Opcodes.ILOAD, Opcodes.ISTORE),
  BYTE('B', Opcodes.DUP, Opcodes.ILOAD, Opcodes.ISTORE),
  SHORT('S', Opcodes.DUP, Opcodes.ILOAD, Opcodes.ISTORE),
  CHAR('C', Opcodes.DUP, Opcodes.ILOAD, Opcodes.ISTORE),
  INTEGER('I', Opcodes.DUP, Opcodes.ILOAD, Opcodes.ISTORE),
  LONG('J', Opcodes.DUP2, Opcodes.LLOAD, Opcodes.LSTORE),
  FLOAT('F', Opcodes.DUP, Opcodes.FLOAD, Opcodes.FSTORE),
  DOUBLE('D', Opcodes.DUP2, Opcodes.DLOAD, Opcodes.DSTORE),
  CLASS('L', Opcodes.DUP, Opcodes.ALOAD, Opcodes.ASTORE),
  ARRAY('[', Opcodes.DUP, Opcodes.ALOAD, Opcodes.ASTORE);
  
  private char description;
  private int frameSize = 0;
  private int dupOpcode = -1;
  private int loadOpcode = -1;
  private int storeOpcode = -1;
  
  /**
   * Creates an instance of {@link TypeDescriptor}.
   * 
   * @param description The description of this type.
   * @param frameSize The frame size of this type.
   * @param dupOpcode The opcode to be used to duplicate this type.
   * @param loadOpcode The opcode to be used to duplicate this type.
   * @param storeOpcode The opcode to be used to duplicate this type.
   */
  private TypeDescriptor(char description, int frameSize, int dupOpcode,
      int loadOpcode, int storeOpcode) {
    this.description = description;
    this.frameSize = frameSize;
    this.dupOpcode = dupOpcode;
    this.loadOpcode = loadOpcode;
    this.storeOpcode = storeOpcode;
  }

  /**
   * Creates an instance of {@link TypeDescriptor}.
   * 
   * @param description The description of this type.
   * @param dupOpcode The opcode to be used to duplicate this type.
   * @param loadOpcode The opcode to be used to duplicate this type.
   * @param storeOpcode The opcode to be used to duplicate this type.
   */
  private TypeDescriptor(char description, int dupOpcode,
      int loadOpcode, int storeOpcode) {
    this(description, (dupOpcode == Opcodes.DUP) ? 1 : 2, dupOpcode,
      loadOpcode, storeOpcode);
  }

  /**
   * Converts this {@link TypeDescriptor} into his char representation.
   * 
   * @return The char representation of this {@link TypeDescriptor}.
   * @throws IllegalStateException if not all the types have been mapped. 
   * @see #getType(char)
   */
  public char getValue() {
    return description;
  }
  
  /**
   * Retrieves the {@link TypeDescriptor} represented by the given String.
   * 
   * @param type A string containing a type.
   * @return The correspective {@link TypeDescriptor}.
   * @throws NullPointerException If the given String is <code>null</code>.
   * @throws IllegalArgumentException If the given String is empty.
   */
  public static TypeDescriptor getType(String type) {
    // Preconditions
    if (type == null) {
      throw new IllegalArgumentException("type cannot be null!");
    } else if ("".equals(type)) {
      throw new IllegalArgumentException("type cannot be empty!");
    }  
    
    TypeDescriptor typeDescriptor = getType(type.charAt(0));
    
    if (type.length() != 1 && 
        typeDescriptor != TypeDescriptor.CLASS &&
        typeDescriptor != TypeDescriptor.ARRAY) {
      throw new IllegalArgumentException(
          String.format("Wrong type requested '%s'", type));
    }
    
    return typeDescriptor;
  }
  
  /**
   * Retrieves the {@link TypeDescriptor} represented by the given 
   * <code>char</code>.
   * 
   * @param c The given <code>char</code>.
   * @return The corrispective {@link TypeDescriptor}.
   * @throws IllegalArgumentException if the given char is not representing a 
   *    type. 
   */
  public static TypeDescriptor getType(char c) {
    switch (c) {
      case 'V':
        return VOID;
      case 'Z': 
        return BOOLEAN;
      case 'B': 
        return BYTE;
      case 'S': 
        return SHORT;
      case 'C': 
        return CHAR;
      case 'I': 
        return INTEGER;
      case 'J': 
        return LONG;
      case 'F':
        return FLOAT;
      case 'D': 
        return DOUBLE;
      case 'L': 
        return CLASS;
      case '[': 
        return ARRAY;
      default:
        throw new IllegalArgumentException("The given description is not " +
            "representing a type: " + c);
    }
  }
  
  /**
   * Return the frame size for each type.
   * 
   * <p>The size of Void is 0, the size of Long and double is 2, the size of any 
   * other base type plus arrays and references is 1.
   * 
   * @return 0 if void, 2 if long or double, 1 otherwise.
   * @throws IllegalStateException if not all the types have been mapped. 
   * 
   * TODO}.
   */
  public int getFrameSize() {
    return frameSize;
  }
  
  /**
   * Return the {@link Opcodes} corresponding to a load instruction for the 
   * current type.
   * 
   * @return The opcode corresponding to the right load instruction.
   * @throws IllegalStateException if the type is <code>void</code>.
   * 
   * TODO}
   */
  public int getLoadOpcode() {
    if (this.equals(VOID)) {
      throw new IllegalStateException("Cannot load VOID");
    }
    return loadOpcode;
  }
  
  /**
   * Return the {@link Opcodes} corresponding to a store instruction for the 
   * current type.
   * 
   * @return The opcode corresponding to the right store instruction.
   * @throws IllegalStateException if the type is <code>void</code>.
   * 
   * TODO}
   */
  public int getStoreOpcode() {
    if (this.equals(VOID)) {
      throw new IllegalStateException("Cannot store VOID");
    }
    return storeOpcode;
  }
  
  /**
   * Return the {@link Opcodes} corresponding to a store instruction for the 
   * current type.
   * 
   * @return The opcode corresponding to the right store instruction.
   * @throws IllegalStateException if the type is <code>void</code>.
   * 
   * TODO}
   */
  public int getDupOpcode() {
    if (this.equals(VOID)) {
      throw new IllegalStateException("Cannot duplicate VOID");
    }
    return dupOpcode;
  }
}
