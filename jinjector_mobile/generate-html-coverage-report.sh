# Copyright 2009 Google Inc.
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#    http://www.apache.org/licenses/LICENSE-2.0
#    
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# -----------------------------------------------------------------------------
# -----------------------------------------------------------------------------
# Creates HTML report for code coverage collected into an lcov file
#
# If the lcov file contains coverage collected from multiple source folders
# the paths may be incomplete. This happens because the source file names
# are inferred from the bytecode. This script corrects the paths by adding
# the correct folder prefix to each file name.
#
# For example the lines representing two files, one in "src", and one in "test":
#   SF:org/foo/Bar.java
#   SF:org/foo/BarTest.java
# will be replaced with:
#   SF:src/org/foo/Bar.java
#   SF:test/org/foo/BarTest.java
#
# This script also excludes files not found in the specified source folder. This
# prevents genhtml from failing if a source file is not found and it also
# allows you to exclude specific source folders. For instance it is possible to
# exclude coverage of your test code in "test" so coverage is only generated for
# the source code in "src".
#
# If (by mistake) two source files corresponding to classes with the same name
# and package exist in the included folders, the one under the first specified
# folder will be used. 
#
# This script depends on genhtml which is an open source tool which is part of
# the LCOV toolkit.
# See http://ltp.sourceforge.net/coverage/lcov.php for details.
# -----------------------------------------------------------------------------

# The LCOV file in which coverage has been collected
LCOV_FILE=$1

# A temporary file with fixed path
LCOV_FILE_WITH_FIXED_FILEPATH="fixed-$LCOV_FILE"

# The project folder root. Usually "." would work.
PROJECT_FOLDER=$2

# The location in which to save the generated coverage
DESTINATION_FOLDER=$3

# The source folders containing the files of which the report should be
# generated. All the other files will be excluded.
# The default value is: "src test proto genfiles"
INCLUDED_SOURCE_FOLDERS=$4

echo "genhtml.sh patching: $LCOV_FILE in $LCOV_FILE_WITH_FIXED_FILEPATH" 
echo "Including source folders: $INCLUDED_SOURCE_FOLDERS"

# For each java source file in $LCOV_FILE, if the file exists in one of the
# source folders specified in $INCLUDED_SOURCE_FOLDERS then it will be included
# in $LCOV_FILE_WITH_FIXED_FILEPATH with the correct path, otherwise it will be
# excluded. $file_found is set to 1 if the file has been found, 0 otherwise.
file_found=0

for line in `cat $LCOV_FILE`
do
  if [[ "$line" =~ ^SF ]]; then
    file_found=0
    relative_path=${line#'SF:'}
    for location in $INCLUDED_SOURCE_FOLDERS
    do
      file=$CLIENT_FOLDER/$location/$relative_path
      if [ -f $file ]; then
        line="SF:$file"
        echo $line >> $LCOV_FILE_WITH_FIXED_FILEPATH
        file_found=1
        break
      fi
    done
    if [[ $file_found -eq 0 ]]; then
      echo "Excluding $relative_path"  
    fi
  else
    if [[ $file_found -eq 1 ]]; then
      echo $line >> $LCOV_FILE_WITH_FIXED_FILEPATH
    fi
  fi
done
echo "" >> $LCOV_FILE_WITH_FIXED_FILEPATH

# The following command depends on genhtml. Make sure you have installed the
# lcov package or it will fail.
# In Ubuntu/Debian you can install it by running "sudo apt-get install lcov".
# genhtml can be downloaded from http://ltp.sourceforge.net/coverage/lcov.php
if [ ! `which genhtml` ]; then  
  echo "genhtml is required, please install it and make sure it's available on the path" 
  exit 2
fi
genhtml $LCOV_FILE_WITH_FIXED_FILEPATH -o $DESTINATION_FOLDER --prefix $CLIENT_FOLDER

rm $LCOV_FILE_WITH_FIXED_FILEPATH
echo "Coverage stored in " $DESTINATION_FOLDER
