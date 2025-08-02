#!/bin/sh

JAR_VERSION="2.0.0"
JAR_NAME="wiremock-docker-easy-extensions.jar"
JAR_URL="https://github.com/alfonsoristorato/wiremock-docker-easy-extensions/releases/download/${JAR_VERSION}/${JAR_NAME}"
JAR_PATH="./${JAR_NAME}"

if [ ! -f "$JAR_PATH" ]; then
  echo "Downloading $JAR_NAME version $JAR_VERSION from GitHub releases..."
  curl -L -o "$JAR_PATH" "$JAR_URL"
fi

java -jar "$JAR_PATH" run wdee-config.yaml
