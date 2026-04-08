#!/bin/bash

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="${SCRIPT_DIR}"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
TARGET_DIR="/Users/cusgadmin/Library/Application Support/ModrinthApp/profiles/Fabric 26.1/mods/"

JAR_NAME="not-riding-alert-2.4.5.jar"
SOURCE_JAR="${PROJECT_DIR}/build/libs/${JAR_NAME}"
TARGET_JAR="${TARGET_DIR}/${JAR_NAME}"
NATIVE_CACHE_DIR="/Users/cusgadmin/Library/Application Support/ModrinthApp/profiles/Fabric 26.1/config/not-riding-alert/native"

# Rebuild native WebView helper binary (must happen before gradlew build so the JAR includes it)
echo "Rebuilding macOS native WebView helper..."
swiftc -O \
    -o "${PROJECT_DIR}/src/main/resources/native/macos/webview-helper" \
    "${REPO_ROOT}/native/macos/WebViewHelper.swift" \
    -framework WebKit -framework AppKit

echo "Building Non-Riding Alert mod..."
cd "${PROJECT_DIR}"
./gradlew spotlessApply
./gradlew build

if [ ! -f "${SOURCE_JAR}" ]; then
    echo "Error: Build artifact not found at ${SOURCE_JAR}"
    exit 1
fi

# Clear cached native binaries so the updated ones from the JAR get extracted
if [ -d "${NATIVE_CACHE_DIR}" ]; then
    echo "Clearing cached native binaries..."
    rm -rf "${NATIVE_CACHE_DIR}"
fi

echo "Creating target directory if it doesn't exist..."
mkdir -p "${TARGET_DIR}"

verify_jar() {
    local jar_file="$1"
    unzip -tq "${jar_file}" 2>/dev/null
}

MAX_RETRIES=3
RETRY_COUNT=0

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    echo "Copying jar to ${TARGET_DIR}..."
    cp -f "${SOURCE_JAR}" "${TARGET_JAR}"
    
    echo "Verifying copied jar integrity..."
    if verify_jar "${TARGET_JAR}"; then
        echo "Jar verification successful!"
        break
    else
        RETRY_COUNT=$((RETRY_COUNT + 1))
        if [ $RETRY_COUNT -lt $MAX_RETRIES ]; then
            echo "Warning: Jar verification failed (attempt $RETRY_COUNT/$MAX_RETRIES). Retrying..."
            sleep 1
        else
            echo "Error: Failed to copy a valid jar after $MAX_RETRIES attempts"
            echo "Source: ${SOURCE_JAR}"
            echo "Target: ${TARGET_JAR}"
            exit 1
        fi
    fi
done

echo "Build and deployment complete!"
echo "Jar copied to: ${TARGET_JAR}"
