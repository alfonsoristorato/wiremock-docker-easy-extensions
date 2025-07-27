# wiremock-docker-easy-extensions

## Overview

**wiremock-docker-easy-extensions** is a toolkit designed to simplify the process of building and packaging custom WireMock extensions for use with the official [WireMock Docker image](https://hub.docker.com/r/wiremock/wiremock) 
but mainly exposes a [ready to use docker-image](https://github.com/alfonsoristorato/wiremock-docker-easy-extensions/pkgs/container/wiremock-docker-easy-extensions) that packages the extensions provided and spins up `WireMock` with them, along with mappings and files.

### Why does this exist?

When using the `WireMock` Docker image, adding custom extensions requires:
- Packaging extension(s) as a JAR, ensuring that the dependencies needed for the extension classes are included.
- Ensuring the JAR is built to run with Java 11 (since the base WireMock Docker image only has JRE 11).
- Placing the JAR in the correct location and configuring WireMock to load it.

This project automates and streamlines these steps by dynamically generating a dedicated Gradle project for extensions, building the extensions targeting JVM 11, and packaging them into a single JAR.
It is then able to spin up a WireMock instance with these extensions, mappings, and files in a Docker container.

### WireMock extensions docs
WireMock extensions are documented in the [WireMock documentation](https://wiremock.org/docs/extending-wiremock/).

---

## How it Works

This tool works in a few steps:
1.  Reads a `wiremock-docker-easy-extensions-config.yaml` file that is provided.
2.  Dynamically creates a temporary Gradle project.
3.  Copies extensions source code into this new project.
4.  Generates a `build.gradle.kts` file configured to target JVM 11 and includes any dependencies specified.
5.  Builds this project to produce a single JAR containing extensions, their dependencies and service loader files for WireMock to discover them.
6.  Feeds this JAR to the WireMock Docker image, allowing it to run with the custom extensions.
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
files-location: rootPackage/subPackage

# (Required - cannot be empty) A list of classes' names with their extension.
source-files:
  - AJavaClass.java
  - AKotlinClass.kt
  
# (Required - can be empty) A list of dependencies required to run the classes.
dependencies:
  - org.apache.commons:commons-lang3:3.18.0
```

## How to Use 

A docker-image is provided that can be used to build and run WireMock with custom extensions. 
This image is designed to work seamlessly with the WireMock Docker image, allowing you to easily add your own extensions without needing to manually build and configure them.

## Requirements
To use the provided Docker image, you need to ensure that your local directory structure matches the expected format outlined above.
### Mounting Local Directory
The provided Docker image expects to read the provided directory structure from a mounted local directory, which needs to be places into `/home/config/(name-of-your-top-level-package)` inside the container.

For the example above, where the top-level package is `rootPackage`, you would mount your local directory to `/home/config/rootPackage`.

### Docker Run Command

Here is an example of how to run the container using `docker run`.

```sh
docker run -p 8080:8080 \
  -v .:/home/config/rootPackage \
  ghcr.io/alfonsoristorato/wiremock-docker-easy-extensions:<version>
```

### Docker Compose

Here is an example of how to run the container using `docker-compose`.

**`docker-compose.yaml`:**
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

With this setup, any changes to the local source files in the `examples` directory will be picked up the next time the container is restarted, triggering a new build of the extension JAR automatically.

## Getting Started Locally

### Prerequisites

- Docker
- Java 21 
- Gradle 

### Building the Tool

The first step is to build the builder itself.

```sh
./gradlew build
```

This will create an executable JAR at `build/libs/wiremock-extensions-builder.jar`.

### Building Your Extensions - `build` Command

Now, the custom WireMock extensions can be built by running the following command:
```sh
java -jar build/libs/wiremock-extensions-builder.jar build <path-to-your-config>.yaml
```

If successful, the bundled JAR will be located in the directory specified in the config (e.g., `build/extensions/wiremock-extensions-bundled.jar`).

### Running WireMock with Extensions - `run` Command

To build the JAR and immediately run it with WireMock in Docker, use the `run` command:

```sh
java -jar build/libs/wiremock-extensions-builder.jar run <path-to-your-config>.yaml
```

---

## Examples

The `examples` directory contains a fully functional sample setup to demonstrate how to use this tool.

### Included Extensions

There are three example extensions in `examples/example`:
-   `ResponseTransformerExtensionNoDependenciesJava.java`: A simple transformer written in Java with no external dependencies.
-   `ResponseTransformerExtensionNoDependenciesKotlin.kt`: A simple transformer written in Kotlin with no external dependencies.
-   `ResponseTransformerExtensionWithDependenciesKotlin.kt`: A transformer written in Kotlin that uses an external dependency (`org.apache.commons:commons-lang3`) to showcase dependency bundling.

All transformers add a simple message to the response body indicating which transformer was used.

### Configuration (`wiremock-docker-easy-extensions-config.yaml`)

The example config file is set up to:
-   Read the source files from the `examples/example` directory.
-   Include the `commons-lang3` dependency.
-   Build the bundled JAR into `build/extensions/wiremock-extensions-bundled.jar`.
-   Configure WireMock to use mappings from `examples/mappings` and files from `examples/__files`.

### Mappings

The [examples/mappings/requests.json](examples/docker-example/mappings/requests.json) file defines three stub mappings. 
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

2.  **Run WireMock with the extensions:**
    Use the `run` command with the example configuration file. This will build the extension JAR and start the WireMock Docker container in one step.
    ```sh
    java -jar build/libs/wiremock-extensions-builder.jar run examples/wiremock-docker-easy-extensions-config.yaml
    ```

3.  **Test with IntelliJ's HTTP Client:**
    Open the [examples/requests.http](examples/requests.http) file in IntelliJ IDEA. This file contains requests for each of the configured endpoints. Click the "run" icon next to each request to send it to the running WireMock instance.

    For example, sending a `GET` request to `http://localhost:8080/java` will return a response with the body `Response from ResponseTransformerExtensionNoDependenciesJava`, demonstrating that the custom transformer was successfully applied.

Steps `1` and `2` can also be run using the provided IntelliJ run configuration:

![IntelliJ-run-command.png](readme-images/IntelliJ-run-command.png)
---


## License

This project is licensed under the [Apache License 2.0](LICENSE), following the same license as WireMock itself.
