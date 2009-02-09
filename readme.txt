/* Copyright 2009 Google Inc.
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
 
 JInjector is composed of two different codebases. Which one you need depends on
 how you intend to use JInjector.
 
 The folder "jinjector_mobile" contains the binary of JInjector and the source
 code of additional classes, called "decorators" which will be included in your
 application during the instrumentation. If you intend to use JInjector this is
 probably the source folder that you need to download.
 
 The folder "jinjector_tool" contains the sources of JInjector, an 
 instrumentation tool for Java-based code. The code is Java 6 compatible and it 
 has been tested using the OpenJdk 1.6. You will need to download this sources
 *only and only if*  you intend to extend or modify JInjector. If you intend
 to add you own custom instrumentation modules to JInjector *without* changing 
 JInjector itself you do NOT need this source. You can simply import the jar 
 in your project and include your modules in the properties file. JInjector will
 load them at runtime automatically. See the properties file for more details.
 
