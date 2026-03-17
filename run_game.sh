#!/usr/bin/env bash
set -e

GRAALVM_HOME=/nix/store/c8hr2f0b0dm685yx1dkp6bw24bpx495n-graalvm19-ce-22.3.1

JAVAFX_CP="game/lib/javafx-sdk/javafx.base.jar:game/lib/javafx-sdk/javafx.controls.jar:game/lib/javafx-sdk/javafx.graphics.jar:game/lib/javafx-sdk/javafx.fxml.jar"
NATIVE_LIBS="$(pwd)/game/lib/javafx-patched"

if [ ! -d "game/out" ] || [ -z "$(ls -A game/out 2>/dev/null)" ]; then
    bash build.sh
fi

echo "Starting VoxelAI game..."

# Unset Replit's LD_AUDIT to allow normal dynamic library resolution
# and use good library paths directly
unset LD_AUDIT
unset REPLIT_LD_AUDIT
unset REPLIT_RTLD_LOADER

$GRAALVM_HOME/bin/java \
    --module-path "$JAVAFX_CP" \
    --add-modules javafx.controls,javafx.graphics,javafx.fxml \
    -cp "game/out:game/lib/json.jar:$JAVAFX_CP" \
    -Djava.library.path="$NATIVE_LIBS" \
    FXGame
