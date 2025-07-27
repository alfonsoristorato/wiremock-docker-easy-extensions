# wiremock-docker-easy-extensions

[![CodeQL](https://github.com/alfonsoristorato/wiremock-docker-easy-extensions/actions/workflows/codeql.yml/badge.svg?branch=main&event=push)](https://github.com/alfonsoristorato/wiremock-docker-easy-extensions/actions/workflows/codeql.yml) [![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE) [![GitHub release (latest by date)](https://img.shields.io/github/v/release/alfonsoristorato/wiremock-docker-easy-extensions)](https://github.com/alfonsoristorato/wiremock-docker-easy-extensions/releases/latest)

## Overview

**wiremock-docker-easy-extensions** is a toolkit designed to simplify building and running custom extensions with the official [WireMock Docker image](https://hub.docker.com/r/wiremock/wiremock).

Its main purpose is to expose a [ready-to-use Docker image](https://github.com/alfonsoristorato/wiremock-docker-easy-extensions/pkgs/container/wiremock-docker-easy-extensions) that packages provided extensions on the fly and spins up `WireMock` with them, along with the necessary mappings and files.

### Why does this exist?

When using the `WireMock` Docker image, adding custom extensions requires you to:
- Package your extension(s) as a JAR, ensuring all dependencies are included.
- Ensure the JAR is built to run with Java 11, as the base WireMock image uses a JRE 11.
- Place the JAR in the correct location and configure WireMock to load it.

This project automates these steps by dynamically generating a dedicated Gradle project, building the extensions to target JVM 11, and packaging them into a single JAR that is then fed to a running WireMock instance.

### WireMock Extensions Docs
For more information on creating extensions, see the [official WireMock documentation](https://wiremock.org/docs/extending-wiremock/).

---

## How it Works

The tool follows these steps:
1.  Reads a `wiremock-docker-easy-extensions-config.yaml` file from a mounted volume.
2.  Dynamically creates a temporary Gradle project inside the container.
3.  Copies your extension source code into this new project.
4.  Generates a `build.gradle.kts` file configured to target JVM 11 and include any specified dependencies.
5.  Builds this project to produce a single JAR containing your extensions, their dependencies and a Java ServiceLoader as WireMock requires this to discover extension classes.
6.  Starts WireMock, automatically loading the newly created JAR.

---

## Requirements
### Directory Structure
In order to use this tool, there will need to be a directory (representing the top-level package of your extensions) with the following structure:
```
rootPackage/
├── __files/                                        # (Required) Directory for WireMock `__files` files.
│   └── response.json                               # (Optional) If provided, it will be copied to the WireMock container's `__files` directory
├── subPackage/                                     # Contains extension source code. Classes can also be in the root.
│   ├── AJavaClass.java
│   └── AKotlinClass.kt
├── mappings/                                       # (Required) Directory for WireMock `mappings` files.
│   └── requests.json                               # (Optional) If provided, it will be copied to the WireMock container's `mappings` directory - worth noting that if there are no mappings, WireMock will not serve any responses.
└── wiremock-docker-easy-extensions-config.yaml     # (Required) Defines extensions, dependencies, and source locations.
```

### `wiremock-docker-easy-extensions-config.yaml` Structure (following the directory structure above)
```yaml
# (Required) The location of the classes relative from where the project JAR will be executed or Docker Base image will mount .
source-files-location: rootPackage/subPackage

# (Required - cannot be empty) A list of classes' names with their extension.
source-files:
  - AJavaClass.java
  - AKotlinClass.kt
  
# (Required - can be empty) A list of dependencies required to run the classes.
dependencies:
  - org.apache.commons:commons-lang3:3.18.0
```

### Extension Language and Compatibility
-   **Supported Languages**: Extensions can be written in either **Java** or **Kotlin**.
-   **Java 11 Compatibility**: The provided source code must be compatible with Java 11. The builder tool compiles the code to target the JVM 11 runtime, which is used by the official WireMock Docker image. This means that language features from newer Java versions are not supported.
-   **Example of an Incompatible Feature**: Using Java's `record` keyword will cause the build to fail, as it was introduced after Java 11.

    ```java
    // This will fail to compile
    public record User(String name, int age) {}
    ```

## How to Use - Docker

The primary way to use this tool is via the pre-built Docker image. This method allows you to build and run your extensions without needing to install Java or Gradle locally.

### Docker Image Tags

The image is published to the [GitHub Container Registry](https://github.com/alfonsoristorato/wiremock-docker-easy-extensions/pkgs/container/wiremock-docker-easy-extensions). The following tags are available:

-   `latest`: Points to the most recent stable (non-pre-release) version.
-   `X.Y.Z` (e.g., `1.0.0`): A specific release version.
-   `X.Y` (e.g., `1.0`): The latest release within a minor version range.
-   `X` (e.g., `1`): The latest release within a major version range.
-   `main`: The latest build from the `main` branch. This is a development version and may be unstable.

### Usage

The Docker image is designed to read the directory structure from a mounted local directory. This directory must be mounted into `/home/config/(name-of-your-root-directory)` inside the container.

For the example above, where the root directory is `rootPackage`, you would mount it to `/home/config/rootPackage`.

#### Docker Run
```sh
docker run -p 8080:8080 \
  -v .:/home/config/rootPackage \
  ghcr.io/alfonsoristorato/wiremock-docker-easy-extensions:<version>
```

#### Docker Compose
```yaml
services:
  wiremock-docker-easy-extensions:
    image: ghcr.io/alfonsoristorato/wiremock-docker-easy-extensions:<version>
    container_name: wiremock-with-runtime-extensions
    ports:
      - "8080:8080"
    volumes:
      - .:/home/config/rootPackage
```
With this setup, any changes to your local source files will be picked up the next time the container is restarted, triggering a new build of your extension JAR automatically.

---

## Local Development

This section is for those who wish to build the `wiremock-docker-easy-extensions` tool itself.

### Prerequisites

- Docker
- Java 21 
- Gradle 

### Building the Tool

The first step is to build the executable JAR for the builder tool.
```sh
./gradlew build
```
This will create the JAR at `build/libs/wiremock-extensions-builder.jar`.

### CLI Commands

#### `build`
To build the extension JAR without running WireMock:
```sh
java -jar build/libs/wiremock-extensions-builder.jar build <path-to-your-config>.yaml
```
If successful, the bundled JAR will be located at `build/extensions/wiremock-extensions-bundled.jar`.

#### `run`
To build the JAR and immediately run it with WireMock in a Docker container:
```sh
java -jar build/libs/wiremock-extensions-builder.jar run <path-to-your-config>.yaml
```

---

## Examples

The `examples` directory contains a fully functional sample setup that follows the required directory structure and demonstrates how to use this tool.

### Included Extensions

There are three example extensions in `examples/example`:
-   `ResponseTransformerExtensionNoDependenciesJava.java`: A simple transformer in Java.
-   `ResponseTransformerExtensionNoDependenciesKotlin.kt`: A simple transformer in Kotlin.
-   `ResponseTransformerExtensionWithDependenciesKotlin.kt`: A Kotlin transformer that uses an external dependency (`org.apache.commons:commons-lang3`).

### Configuration (`wiremock-docker-easy-extensions-config.yaml`)

The example config file is set up to:
-   Read the source files from the `examples/example` directory.
-   Include the `commons-lang3` dependency.

### Mappings

The [examples/mappings/requests.json](examples/mappings/requests.json) file defines three stub mappings.
Each mapping targets a specific URL and uses one of the custom response transformers. For example:

```json
{
  "request": {
    "method": "GET",
    "url": "/ResponseTransformerExtensionNoDependenciesKotlin"
  },
  "response": {
    "status": 200,
    "transformers": ["ResponseTransformerExtensionNoDependenciesKotlin"]
  }
}
```

### How to Run the Example

1.  **Build the tool:**
    ```sh
    ./gradlew build
    ```

2.  **Run the example:**
    Use the `run` command with the example configuration file. This will build the extension JAR and start the WireMock container in one step.
    ```sh
    java -jar build/libs/wiremock-extensions-builder.jar run examples/wiremock-docker-easy-extensions-config.yaml
    ```

3.  **Test with IntelliJ's HTTP Client:**
    Open the [examples/requests.http](examples/requests.http) file in IntelliJ IDEA. This file contains requests for each of the configured endpoints. Click the "run" icon next to each request to send it to the running WireMock instance.

    For example, a `GET` request to `http://localhost:8080/ResponseTransformerExtensionNoDependenciesJava` will return a response with the body `Response from ResponseTransformerExtensionNoDependenciesJava`.

Steps `1` and `2` can also be executed using the provided IntelliJ run configuration:

![IntelliJ-run-command.png](readme-images/IntelliJ-run-command.png)
---


## License

This project is licensed under the [Apache License 2.0](LICENSE), following the same license as WireMock itself.
