#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/../.."
: "${JAVA_HOME:?Set JAVA_HOME to a Java 17+ runtime}"
KOTLINC=${KOTLINC:-kotlinc}
TEMP=$(mktemp -d)
trap 'rm -rf "$TEMP"' EXIT
"$KOTLINC" tests/kotlin/{GlfwWaitFixture,GlfwIdleCheck,GlfwLinkageCheck}.kt -jvm-target 1.8 -include-runtime -d "$TEMP/fixture.jar"
python3 tools/patch_glfw_idle.py "$TEMP/fixture.jar" "$TEMP/overlay"
"$JAVA_HOME/bin/java" -Xverify:all -cp "$TEMP/overlay/crutle-performance.jar:$TEMP/fixture.jar" GlfwIdleCheckKt

python3 tools/patch_glfw_idle.py app/src/main/assets/components/lwjgl3/lwjgl-glfw-classes.jar "$TEMP/real-overlay"
"$JAVA_HOME/bin/java" -Xverify:all -cp "$TEMP/real-overlay/crutle-performance.jar:app/src/main/assets/components/lwjgl3/lwjgl-glfw-classes.jar:$TEMP/fixture.jar" GlfwLinkageCheckKt
