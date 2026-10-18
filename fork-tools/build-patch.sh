#!/bin/bash
# Fork tool (Iuri): rebuilds only the fork-modified packages and patches them into an official
# DrivenByMoss.bwextension of the SAME upstream version. Use it when Maven Central is unreachable
# and only the APC40 code changed. The normal path is `mvn clean package` (or the GitHub Action).
#
# Usage: fork-tools/build-patch.sh API_JAR BASE_BWEXTENSION [OUT_FILE]
#   API_JAR          - Bitwig extension API classes; the com/bitwig/extension/** entries extracted
#                      from "/Applications/Bitwig Studio.app/Contents/Java/bitwig.jar" work here
#   BASE_BWEXTENSION - the official DrivenByMoss.bwextension to patch
set -euo pipefail

API_JAR="$(realpath "$1")"
BASE="$(realpath "$2")"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT="${3:-$ROOT/target/DrivenByMoss.bwextension}"
WORK="$ROOT/target/patch"
SRC="$ROOT/src/main/java"

rm -rf "$WORK"
mkdir -p "$WORK/classes" "$WORK/jar" "$(dirname "$OUT")"

{
    find "$SRC/de/mossgrabers/controller/akai/apc" -name '*.java'
    find "$SRC/de/mossgrabers/bitwig/controller/akai/apc" -name '*.java'
    grep -rl "^// Fork addition" "$SRC" || true
} > "$WORK/sources.txt"

javac --release 21 -encoding UTF-8 -nowarn -proc:none -implicit:none \
    -cp "$BASE:$API_JAR" -d "$WORK/classes" @"$WORK/sources.txt"

cd "$WORK/jar"
unzip -qo "$BASE"
rm -rf de/mossgrabers/controller/akai/apc de/mossgrabers/bitwig/controller/akai/apc
cp -r "$WORK/classes/." .
rm -f "$OUT"
zip -qr -X "$OUT" META-INF/MANIFEST.MF .
echo "Built $OUT"
