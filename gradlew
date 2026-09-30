#!/bin/sh
set -eu

GRADLE_VERSION="9.5.0"
WRAPPER_DIR="${GRADLE_USER_HOME:-$HOME/.gradle}/wrapper/videra-gradle-${GRADLE_VERSION}"
GRADLE_BIN="${WRAPPER_DIR}/gradle-${GRADLE_VERSION}/bin/gradle"

if command -v gradle >/dev/null 2>&1; then
    exec gradle "$@"
fi

if [ -x "${GRADLE_BIN}" ]; then
    exec "${GRADLE_BIN}" "$@"
fi

mkdir -p "${WRAPPER_DIR}"
ARCHIVE="${WRAPPER_DIR}/gradle-${GRADLE_VERSION}-bin.zip"
URL="https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"

if command -v curl >/dev/null 2>&1; then
    curl --fail --location --silent --show-error "${URL}" --output "${ARCHIVE}"
elif command -v wget >/dev/null 2>&1; then
    wget --quiet --output-document="${ARCHIVE}" "${URL}"
else
    echo "Gradle is not installed and neither curl nor wget is available." >&2
    exit 1
fi

rm -rf "${WRAPPER_DIR}/gradle-${GRADLE_VERSION}"
if command -v unzip >/dev/null 2>&1; then
    unzip -q "${ARCHIVE}" -d "${WRAPPER_DIR}"
else
    echo "unzip is required to bootstrap Gradle ${GRADLE_VERSION}." >&2
    exit 1
fi

exec "${GRADLE_BIN}" "$@"
