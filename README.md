# wiremock-docker-easy-extensions

## Overview

**wiremock-docker-easy-extensions** is a toolkit designed to simplify the process of building and packaging custom WireMock extensions for use with the official [WireMock Docker image](https://hub.docker.com/r/wiremock/wiremock).

### Why does this exist?

When using the WireMock Docker image, adding custom extensions requires you to:
- Package your extension(s) as a JAR.
- Ensure the JAR is built for Java 11 (since the base WireMock Docker image uses Java 11).
- Place the JAR in the correct location and configure WireMock to load it.

This project automates and streamlines these steps by dynamically generating a dedicated Gradle project for your extensions, building them against Java 11, and packaging them into a single JAR.

---

## Features

- **Automated JAR packaging** for Java 11 compatibility.
- **Simple configuration** via a single YAML file.
- **Handles dependencies** for your extensions.
- **Docker integration** for seamless local development and testing.

---

## How it Works

This tool works in a few steps:
1.  Reads a `config.yaml` file that you provide.
2.  Dynamically creates a temporary Gradle project.
3.  Copies your extension source code into this new project.
4.  Generates a `build.gradle.kts` file configured to use Java 11 and includes any dependencies you specified.
5.  Builds this project to produce a single, JAR containing your extensions and their dependencies.

---

## Getting Started

### Prerequisites

- Docker
- Java 21 (to run this builder project)
- Gradle (or use the provided wrapper)

### Building the Tool

First, you need to build the builder itself.

```sh
./gradlew build
```

This will create an executable JAR at `build/libs/wiremock-extension-builder.jar`.

### Configuration

Next, create a YAML configuration file to define your extensions. See [example config file](examples/wiremock-docker-easy-extensions-config.yaml) for a template.

**`config.yaml` structure:**

```yaml
# (Required) The location of your classes relative from where the project JAR will be executed.
files/location: examples/example

# (Required - cannot be empty) A list of your classes names with their extension.
source-files:
  - ResponseTransformerExtensionNoDependenciesJava.java
  - ResponseTransformerExtensionNoDependenciesKotlin.kt
  
# (Required - can be empty) A list of dependencies required to run your classes, they will be packaged in the final JAR.
dependencies:
  - org.apache.commons:commons-lang3:3.18.0

# (Required) This will soon be removed.
use-gradle-wrapper: true

# (Required)
output:
  # (Required) The directory where the generated JAR will be placed.
  dir: "build/extensions"
  # (Required) The name of the generated JAR file.
  jar-name: "wiremock-extensions-bundled.jar"
  
# (Required)
wiremock:
    # (Required) The directory from where to read, and therefore provide to WireMock, the WireMock mappings.
  mappings-dir: examples/mappings
    # (Optional) The directory from where to read, and therefore provide to WireMock, the WireMock files.
  files-dir: examples/__files
```

### Building Your Extensions

With the configuration file ready, we can now build our custom extension JAR.
First we will build the builder project itself.

```sh
./gradlew build 
```
This will create the executable JAR at `build/libs/wiremock-extension-builder.jar`.
Now, we can build your custom WireMock extensions by running the following command:
```sh
java -jar build/libs/wiremock-extension-builder.jar build <path-to-your-config>.yaml
```

If successful, the bundled JAR will be located in the directory specified in your config (e.g., `build/extensions/wiremock-extensions-bundled.jar`).

### Running WireMock with Extensions - will be removed soon in favour of multi-docker build

To build the JAR and immediately run it with WireMock in Docker, use the `run` command:

```sh
java -jar build/libs/wiremock-extension-builder.jar run <path-to-your-config>.yaml
```

---
## License

This project is licensed under the [Apache License 2.0](LICENSE).
