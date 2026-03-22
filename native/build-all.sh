#!/bin/bash
# Builds native webview-helper binaries for all platforms and copies them
# (along with source files for reviewability) into src/main/resources/native/
# for JAR bundling.
#
# This script is run by CI and can also be run locally before ./gradlew build.
#
# Prerequisites:
#   macOS: Xcode command line tools (swiftc)
#   Windows cross-compile: .NET 8+ SDK (brew install dotnet)

set -e
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
RESOURCES_DIR="$SCRIPT_DIR/../src/main/resources/native"

# --- macOS ---
echo "=== Building macOS webview-helper ==="
mkdir -p "$RESOURCES_DIR/macos"

# Copy source for reviewability (JAR ships source so reviewers can inspect it)
cp "$SCRIPT_DIR/macos/WebViewHelper.swift" "$RESOURCES_DIR/macos/WebViewHelper.swift"

if command -v swiftc &>/dev/null; then
    cd "$SCRIPT_DIR/macos"
    bash build.sh
    cp webview-helper "$RESOURCES_DIR/macos/webview-helper"
    echo "Copied macOS binary to resources ($(du -h "$RESOURCES_DIR/macos/webview-helper" | cut -f1))"
else
    echo "WARNING: swiftc not found. Skipping macOS binary build."
    echo "Source file copied for compile-from-source fallback."
fi

# --- Windows ---
echo ""
echo "=== Building Windows webview-helper ==="
mkdir -p "$RESOURCES_DIR/windows"

# Copy source files for reviewability
cp "$SCRIPT_DIR/windows/WebViewHelper.cs" "$RESOURCES_DIR/windows/WebViewHelper.cs"
cp "$SCRIPT_DIR/windows/WebViewHelper.csproj" "$RESOURCES_DIR/windows/WebViewHelper.csproj"

if command -v dotnet &>/dev/null; then
    cd "$SCRIPT_DIR/windows"
    dotnet publish -c Release -r win-x64 --no-self-contained \
        -p:PublishSingleFile=true -o publish 2>&1 | grep -v "warning MSB3277"
    cp publish/webview-helper.exe "$RESOURCES_DIR/windows/webview-helper.exe"
    cp publish/runtimes/win-x64/native/WebView2Loader.dll "$RESOURCES_DIR/windows/WebView2Loader.dll"
    echo "Copied Windows binaries to resources ($(du -h "$RESOURCES_DIR/windows/webview-helper.exe" | cut -f1) + WebView2Loader.dll)"
else
    echo "WARNING: .NET SDK not found. Skipping Windows binary build."
    echo "Source files copied for compile-from-source fallback."
fi

echo ""
echo "=== Done ==="
ls -lh "$RESOURCES_DIR/macos/" "$RESOURCES_DIR/windows/" 2>/dev/null
