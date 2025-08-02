#!/bin/sh
set -e

echo "📁 Copying config files from /home/config to /home/wiremock..."
cp -r /home/config/. /home/wiremock/

# Find config subdir
CONFIG_SUBDIR=$(find /home/wiremock -mindepth 1 -maxdepth 1 -type d | head -n 1)
echo "⚙️ Using config subdir: $CONFIG_SUBDIR"

echo "📦 Moving mappings and __files to /home/wiremock..."
cp -r "$CONFIG_SUBDIR/mappings" /home/wiremock/
cp -r "$CONFIG_SUBDIR/__files" /home/wiremock/

echo "♻️ Saving original JAVA setup..."
ORIG_JAVA=$(command -v java)
ORIG_JAVA_HOME=${JAVA_HOME:-$(dirname $(dirname $(readlink -f "$ORIG_JAVA")))}

echo "☕ Switching to JDK for compilation..."
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"

echo "🛠️ Building extensions using wiremock-docker-easy-extensions..."
java -jar wiremock-docker-easy-extensions.jar build "$CONFIG_SUBDIR/wdee-config.yaml"

echo "📦 Copying bundled extensions to /var/wiremock/extensions/"
cp build/extensions/wiremock-extensions-bundled.jar /var/wiremock/extensions/

# Remove the JDK, as it is no longer needed
echo "🧹 Removing JDK..."
apt-get purge -y openjdk-11-jdk >/dev/null 2>&1 && apt-get autoremove -y --purge >/dev/null 2>&1

echo "🔄 Reverting to original Java setup..."
export JAVA_HOME=$ORIG_JAVA_HOME
export PATH=$(dirname "$ORIG_JAVA"):$PATH

echo "🔍 Checking if javac is still available..."
if command -v javac >/dev/null 2>&1; then
  echo "❌ JDK still present: javac found at $(command -v javac)"
else
  echo "✅ JDK successfully removed: javac not found"
fi

echo "🚀 Starting WireMock..."
exec /docker-entrypoint.sh "$@"
