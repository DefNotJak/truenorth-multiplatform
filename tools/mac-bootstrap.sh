#!/usr/bin/env bash
# Prepare a fresh/rented macOS box to build this project's iOS app.
#
#   bash tools/mac-bootstrap.sh
#
# Installs entirely under $HOME, so it works on managed cloud Macs
# where you don't have admin rights. Safe to re-run.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TOOLS_DIR="$HOME/truenorth-tools"
JDK_VERSION=21          # Gradle 9.1 / AGP 9.0.1 need 17+; 21 is the current LTS
ANDROID_PLATFORM=36     # matches android-compileSdk in gradle/libs.versions.toml
ANDROID_BUILD_TOOLS=36.0.0

say() { printf '\n=== %s\n' "$*"; }
have() { command -v "$1" >/dev/null 2>&1; }

say "Checking Xcode"
if ! have xcodebuild; then
    echo "Xcode is not installed. Install it from the App Store before re-running."
    echo "On a managed cloud Mac it is usually preinstalled; check /Applications."
    exit 1
fi
xcodebuild -version | head -1
# The command line tools must point at the real Xcode, not the standalone CLT.
if ! xcode-select -p 2>/dev/null | grep -q "Xcode.app"; then
    echo "NOTE: xcode-select points at $(xcode-select -p 2>/dev/null || echo 'nothing')."
    echo "      If the build fails, run: sudo xcode-select -s /Applications/Xcode.app"
fi

say "Checking for a JDK 17 or newer"
jdk_ok() {
    local home="${1:-}"
    [ -n "$home" ] && [ -x "$home/bin/javac" ] || return 1
    local major
    major="$("$home/bin/javac" -version 2>&1 | sed -E 's/javac ([0-9]+).*/\1/')"
    [ "${major:-0}" -ge 17 ] 2>/dev/null
}

JAVA_HOME_FOUND=""
for candidate in \
    "${JAVA_HOME:-}" \
    "$(/usr/libexec/java_home -v "$JDK_VERSION" 2>/dev/null || true)" \
    "$(/usr/libexec/java_home 2>/dev/null || true)" \
    "$TOOLS_DIR/jdk/Contents/Home" \
    "/Applications/Android Studio.app/Contents/jbr/Contents/Home"
do
    if jdk_ok "$candidate"; then JAVA_HOME_FOUND="$candidate"; break; fi
done

if [ -z "$JAVA_HOME_FOUND" ]; then
    say "Downloading Temurin JDK $JDK_VERSION (no admin rights needed)"
    arch="$(uname -m)"; [ "$arch" = "arm64" ] && arch="aarch64"
    mkdir -p "$TOOLS_DIR"
    curl -fL --retry 3 -o "$TOOLS_DIR/jdk.tar.gz" \
        "https://api.adoptium.net/v3/binary/latest/${JDK_VERSION}/ga/mac/${arch}/jdk/hotspot/normal/eclipse"
    rm -rf "$TOOLS_DIR/jdk" && mkdir -p "$TOOLS_DIR/jdk"
    tar -xzf "$TOOLS_DIR/jdk.tar.gz" -C "$TOOLS_DIR/jdk" --strip-components=1
    rm -f "$TOOLS_DIR/jdk.tar.gz"
    JAVA_HOME_FOUND="$TOOLS_DIR/jdk/Contents/Home"
fi
export JAVA_HOME="$JAVA_HOME_FOUND"
export PATH="$JAVA_HOME/bin:$PATH"
echo "JAVA_HOME=$JAVA_HOME"
javac -version

say "Locating the Android SDK"
ANDROID_SDK=""
for candidate in \
    "${ANDROID_HOME:-}" "${ANDROID_SDK_ROOT:-}" \
    "$HOME/Library/Android/sdk"
do
    if [ -n "$candidate" ] && [ -d "$candidate" ]; then ANDROID_SDK="$candidate"; break; fi
done

if [ -z "$ANDROID_SDK" ]; then
    ANDROID_SDK="$HOME/Library/Android/sdk"
    echo "No existing SDK found. Creating one at $ANDROID_SDK"
    mkdir -p "$ANDROID_SDK"
fi

SDKMANAGER="$(find "$ANDROID_SDK/cmdline-tools" -name sdkmanager -type f 2>/dev/null | head -1 || true)"
if [ -z "$SDKMANAGER" ]; then
    say "Installing Android command line tools"
    # If this URL 404s, grab the current "Command line tools only" mac link from
    # https://developer.android.com/studio#command-line-tools-only and pass it as:
    #   CMDLINE_TOOLS_URL=<url> bash tools/mac-bootstrap.sh
    url="${CMDLINE_TOOLS_URL:-https://dl.google.com/android/repository/commandlinetools-mac-11076708_latest.zip}"
    curl -fL --retry 3 -o /tmp/cmdline-tools.zip "$url"
    rm -rf /tmp/cmdline-tools-extract
    unzip -q /tmp/cmdline-tools.zip -d /tmp/cmdline-tools-extract
    mkdir -p "$ANDROID_SDK/cmdline-tools"
    rm -rf "$ANDROID_SDK/cmdline-tools/latest"
    mv /tmp/cmdline-tools-extract/cmdline-tools "$ANDROID_SDK/cmdline-tools/latest"
    rm -rf /tmp/cmdline-tools.zip /tmp/cmdline-tools-extract
    SDKMANAGER="$ANDROID_SDK/cmdline-tools/latest/bin/sdkmanager"
fi
echo "sdkmanager: $SDKMANAGER"

say "Installing Android platform $ANDROID_PLATFORM and build tools $ANDROID_BUILD_TOOLS"
export ANDROID_HOME="$ANDROID_SDK"
export ANDROID_SDK_ROOT="$ANDROID_SDK"
yes | "$SDKMANAGER" --sdk_root="$ANDROID_SDK" --licenses >/dev/null 2>&1 || true
"$SDKMANAGER" --sdk_root="$ANDROID_SDK" \
    "platform-tools" \
    "platforms;android-$ANDROID_PLATFORM" \
    "build-tools;$ANDROID_BUILD_TOOLS"

say "Writing local.properties"
# Gitignored on purpose: it holds machine-specific paths.
cat > "$REPO_ROOT/local.properties" <<PROPS
sdk.dir=$ANDROID_SDK
PROPS
cat "$REPO_ROOT/local.properties"

say "Warming up Gradle and compiling the shared framework for the simulator"
cd "$REPO_ROOT"
chmod +x ./gradlew
./gradlew --no-daemon :shared:compileKotlinIosSimulatorArm64

say "Done"
cat <<NEXT

Add these to your shell so Xcode's build phase can find them:

  echo 'export JAVA_HOME="$JAVA_HOME"'              >> ~/.zshrc
  echo 'export ANDROID_HOME="$ANDROID_SDK"'         >> ~/.zshrc
  echo 'export PATH="\$JAVA_HOME/bin:\$PATH"'        >> ~/.zshrc

Then open the project in Xcode:

  open "$REPO_ROOT/iosApp/iosApp.xcodeproj"

NEXT
