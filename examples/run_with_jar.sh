#!/bin/sh
set -e

JAR_NAME="wiremock-docker-easy-extensions.jar"
JAR_PATH="./${JAR_NAME}"
REPO_NAME="alfonsoristorato/wiremock-docker-easy-extensions"

# Get the latest release version from GitHub API
LATEST_VERSION=$(curl -s https://api.github.com/repos/$REPO_NAME/releases/latest | grep '"tag_name":' | sed -E 's/.*"([^"]+)".*/\1/')

if [ -z "$LATEST_VERSION" ]; then
  echo "Failed to fetch latest release version"
  exit 1
fi

JAR_URL="https://github.com/$REPO_NAME/releases/download/${LATEST_VERSION}/${JAR_NAME}"

# Download JAR if not already present
if [ ! -f "$JAR_PATH" ]; then
  echo "Downloading $JAR_NAME version $LATEST_VERSION from GitHub releases..."
  curl -L -o "$JAR_PATH" "$JAR_URL"
else
  echo "$JAR_NAME already exists, skipping download"
fi

java -jar "$JAR_PATH" run wdee-config.yaml
