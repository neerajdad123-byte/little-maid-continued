#!/usr/bin/env bash
# Build, then apply all line-targeted fixers; repeat while they keep changing something (max 4 rounds).
cd "$(dirname "$0")/../.."
for i in 1 2 3 4; do
  tools/port/build.sh
  out="$(python tools/port/fix_symbols.py build-log.txt; python tools/port/fix_messages.py build-log.txt; python tools/port/fix_api.py src/main/java build-log.txt)"
  echo "$out" | tr '\n' ' '; echo
  echo "$out" | grep -qE "[1-9][0-9]*$" || break
done
