#!/bin/bash

# Based on JDK 10 installation script from the junit5 project

set -e

JDK_FEATURE=10
JDK_ARCHIVE=openjdk-${JDK_FEATURE}_linux-x64_bin.tar.gz

cd ~
wget https://download.java.net/java/GA/jdk${JDK_FEATURE}/${JDK_FEATURE}/binaries/${JDK_ARCHIVE}
tar -xzf ${JDK_ARCHIVE}
export JAVA_HOME=~/jdk-${JDK_FEATURE}
export PATH=${JAVA_HOME}/bin:$PATH
cd -
java --version
