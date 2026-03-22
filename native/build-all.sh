#!/bin/bash
# Builds native webview-helper binaries for all platforms and copies them
# into src/main/resources/native/ for JAR bundling.
#
# Prerequisites:
#   macOS: Xcode command line tools (swiftc)
#   Windows cross-compile: .NET 8+ SDK (brew install dotnet)

set -e
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
RESOURCES_DIR="$SCRIPT_DIR/../src/main/resources/native"

# --- macOS (universal binary: arm64 + x86_64) ---
echo "=== Building macOS webview-helper ==="
cd "$SCRIPT_DIR/macos"
bash build.sh

mkdir -p "$RESOURCES_DIR/macos"
cp webview-helper "$RESOURCES_DIR/macos/webview-helper"
echo "Copied macOS binary to resources ($(du -h "$RESOURCES_DIR/macos/webview-helper" | cut -f1))"

# --- Windows (cross-compile, framework-dependent) ---
echo ""
echo "=== Building Windows webview-helper (cross-compile) ==="
cd "$SCRIPT_DIR/windows"

if ! command -v dotnet &>/dev/null; then
    echo "WARNING: .NET SDK not found. Skipping Windows build."
    echo "Install with: brew install dotnet"
else
    dotnet publish -c Release -r win-x64 --no-self-contained \
        -p:PublishSingleFile=true -o publish 2>&1 | grep -v "warning MSB3277"

    mkdir -p "$RESOURCES_DIR/windows"
    cp publish/webview-helper.exe "$RESOURCES_DIR/windows/webview-helper.exe"
    # WebView2Loader.dll is required at runtime alongside the exe
    cp publish/runtimes/win-x64/native/WebView2Loader.dll "$RESOURCES_DIR/windows/WebView2Loader.dll"
    echo "Copied Windows binaries to resources ($(du -h "$RESOURCES_DIR/windows/webview-helper.exe" | cut -f1) + WebView2Loader.dll)"
fi

echo ""
echo "=== Done ==="
ls -lh "$RESOURCES_DIR/macos/" "$RESOURCES_DIR/windows/" 2>/dev/null
