#!/bin/bash
# Fork tool (Iuri): builds DrivenByMoss.bwextension with plain javac, without Maven.
#
# Normal path is still `mvn clean package -Dbitwig.extension.directory=target`.
# This script exists for environments where Maven Central is unreachable.
#
# Needs two jars:
#   API_JAR  - Bitwig extension API classes (com/bitwig/extension/**), e.g. extracted from
#              "/Applications/Bitwig Studio.app/Contents/Java/bitwig.jar"
#   DEPS_JAR - third-party libraries (jna, jackson, hid, jamepad, nativefiledialogs...),
#              e.g. everything except de/mossgrabers/{bitwig,controller,framework} taken
#              from an official DrivenByMoss.bwextension
#
# Usage: fork-tools/build-offline.sh API_JAR DEPS_JAR [OUT_FILE]
set -euo pipefail

API_JAR="$(realpath "$1")"
DEPS_JAR="$(realpath "$2")"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT="${3:-$ROOT/target/DrivenByMoss.bwextension}"
WORK="$ROOT/target/offline"

rm -rf "$WORK"
mkdir -p "$WORK/classes" "$WORK/jar" "$(dirname "$OUT")"

find "$ROOT/src/main/java" -name '*.java' > "$WORK/sources.txt"
javac --release 21 -encoding UTF-8 -nowarn -proc:none \
    -cp "$API_JAR:$DEPS_JAR" -d "$WORK/classes" @"$WORK/sources.txt"

cd "$WORK/jar"
unzip -qo "$DEPS_JAR"
cp -r "$WORK/classes/." .
cp -r "$ROOT/src/main/resources/." .
VERSION="$(sed -n 's:.*<version>\(.*\)</version>.*:\1:p' "$ROOT/pom.xml" | head -1)"
mkdir -p META-INF
printf 'Manifest-Version: 1.0\nImplementation-Title: DrivenByMoss\nImplementation-Version: %s\n' "$VERSION" > META-INF/MANIFEST.MF
rm -f "$OUT"
zip -qr -X "$OUT" META-INF/MANIFEST.MF .
echo "Built $OUT ($VERSION)"
