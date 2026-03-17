#!/usr/bin/env bash
set -e

GRAALVM_HOME=/nix/store/c8hr2f0b0dm685yx1dkp6bw24bpx495n-graalvm19-ce-22.3.1

JAVAFX_CP="game/lib/javafx-sdk/javafx.base.jar:game/lib/javafx-sdk/javafx.controls.jar:game/lib/javafx-sdk/javafx.graphics.jar:game/lib/javafx-sdk/javafx.fxml.jar"

mkdir -p game/out

find game/src -name "*.java" > /tmp/voxelai-sources.txt

echo "Compiling VoxelAI..."
$GRAALVM_HOME/bin/javac \
    -cp "game/lib/json.jar:$JAVAFX_CP" \
    -d game/out \
    -sourcepath game/src \
    @/tmp/voxelai-sources.txt

echo "Build complete!"
