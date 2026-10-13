#!/usr/bin/env bash
# Builds, starts the headless game test server, and prints the first real errors (OnlyIn warnings filtered).
cd "$(dirname "$0")/../.."
export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-25.0.4.101-hotspot"
timeout 580 ./gradlew.bat runGameTestServer --console=plain > run-log.txt 2>&1
L=run/gameTestServer/logs/latest.log
grep -n "FATAL\|ERROR\|Caused by\|Exception" "$L" | grep -v "OnlyIn\|oshi\|HkeyPerformance\|Negative index in crash" | head -${1:-25}
echo "--- last:"; tail -3 "$L"
