#!/bin/bash
#
# Copyright 2009 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# 
# Author: Julian Harty
#
# Adds extra files and packages required to build jinjector. These files
# include, existing open-sourced packages e.g. common.collections
# For files downloaded using svn we will need to clean up any extraneous svn
# logs, etc.

rm -rf lib/*

wget -O google-collect-snapshot-20080820.zip http://google-collections.googlecode.com/files/google-collect-snapshot-20080820.zip
unzip -j -d lib google-collect-snapshot-20080820.zip \*.jar
rm google-collect-snapshot-20080820.zip


wget -O asm-3.1-bin.zip http://download.forge.objectweb.org/asm/asm-3.1-bin.zip
unzip -j -d lib asm-3.1-bin.zip \*.jar

wget -O junit3.8.1.zip http://heanet.dl.sourceforge.net/sourceforge/junit/junit3.8.1.zip 
unzip -j -d lib junit3.8.1.zip \*.jar

wget -O easymock2.4.zip http://prdownloads.sourceforge.net/easymock/easymock2.4.zip
unzip -j -d lib easymock2.4.zip \*.jar

wget -O easymockclassextension2.4.zip http://prdownloads.sourceforge.net/easymock/easymockclassextension2.4.zip
unzip -j -d lib easymockclassextension2.4.zip \*.jar

wget -O lib/cglib-2.2.jar http://puzzle.dl.sourceforge.net/sourceforge/cglib/cglib-2.2.jar
