#!/usr/bin/env bash
# Compiles the mod with JDK 25 and prints the error count. Usage: tools/port/build.sh [gradle task]
cd "$(dirname "$0")/../.."
export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-25.0.4.101-hotspot"
./gradlew.bat ${1:-compileJava} --console=plain > build-log.txt 2>&1
echo "errors: $(grep -c 'error:' build-log.txt)  $(grep -o 'BUILD [A-Z]*' build-log.txt | tail -1)"
