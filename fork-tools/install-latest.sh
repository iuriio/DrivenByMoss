#!/bin/bash
# Fork tool (Iuri): downloads the .bwextension of the latest successful build of the 'iuri'
# branch and installs it. The file is replaced with a rename (not overwritten in place), which
# lets a running Bitwig pick up the new version without a restart.
#
# Usage: fork-tools/install-latest.sh [RUN_ID]    (needs the GitHub CLI 'gh')

set -euo pipefail

REPO=iuriio/DrivenByMoss
DEST="$HOME/Documents/Bitwig Studio/Extensions"
RUN=${1:-$(gh run list -R "$REPO" -w fork-build.yml -b iuri -s success -L 1 --json databaseId --jq '.[0].databaseId')}

TMP=$(mktemp -d)
trap 'rm -rf "$TMP"' EXIT
gh run download "$RUN" -R "$REPO" -n DrivenByMoss-iuri -D "$TMP"

mkdir -p "$DEST"
cp "$TMP/DrivenByMoss.bwextension" "$DEST/.DrivenByMoss.bwextension.tmp"
mv -f "$DEST/.DrivenByMoss.bwextension.tmp" "$DEST/DrivenByMoss.bwextension"
echo "Installed build $RUN to $DEST"
