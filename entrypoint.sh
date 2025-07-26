#!/bin/sh
set -e

# Copy config files from volume to wiremock
cp -r /home/config/. /home/wiremock/

# Find config subdir
CONFIG_SUBDIR=$(find /home/wiremock -mindepth 1 -maxdepth 1 -type d | head -n 1)
echo "Using config subdir: $CONFIG_SUBDIR"

# Move mappings and __files from config subdir to /home/wiremock
cp -r "$CONFIG_SUBDIR/mappings" /home/wiremock/
cp -r "$CONFIG_SUBDIR/__files" /home/wiremock/

# Save original Java environment - WireMock Temurin jre
ORIG_JAVA=$(command -v java)
ORIG_JAVA_HOME=${JAVA_HOME:-$(dirname $(dirname $(readlink -f "$ORIG_JAVA")))}

## Set Java Home to installed JDK as WireMock only has JRE and we need to compile the JAR
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"

# Run wiremock-extensions-builder
java -jar wiremock-extensions-builder.jar build "$CONFIG_SUBDIR/wiremock-docker-easy-extensions-config.yaml"

# Copy bundled extensions to WireMock extensions folder
cp build/extensions/wiremock-extensions-bundled.jar /var/wiremock/extensions/

# Revert to original JAVA HOME - WireMock Temurin jre
export JAVA_HOME=$ORIG_JAVA_HOME
export PATH=$(dirname "$ORIG_JAVA"):$PATH

# Start the original WireMock entrypoint
exec /docker-entrypoint.sh "$@"
