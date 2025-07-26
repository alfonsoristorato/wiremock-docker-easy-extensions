# wiremock-docker-easy-extensions

## Overview

**wiremock-docker-easy-extensions** is a toolkit designed to simplify the process of building and packaging custom WireMock extensions for use with the official [WireMock Docker image](https://hub.docker.com/r/wiremock/wiremock).

### Why does this exist?

When using the WireMock Docker image, adding custom extensions requires:
- Packaging extension(s) as a JAR.
- Ensuring the JAR is built for Java 11 (since the base WireMock Docker image uses Java 11).
- Placing the JAR in the correct location and configuring WireMock to load it.

This project automates and streamlines these steps by dynamically generating a dedicated Gradle project for extensions, building them against Java 11, and packaging them into a single JAR.

---

## Features

- **Automated JAR packaging** for Java 11 compatibility.
- **Simple configuration** via a single YAML file.
- **Handles dependencies** for extensions.
- **Docker integration** for seamless local development and testing.

---

## How it Works

This tool works in a few steps:
1.  Reads a `config.yaml` file that is provided.
2.  Dynamically creates a temporary Gradle project.
3.  Copies extension source code into this new project.
4.  Generates a `build.gradle.kts` file configured to use Java 11 and includes any dependencies specified.
5.  Builds this project to produce a single, JAR containing extensions and their dependencies.

---

## Getting Started

### Prerequisites

- Docker
- Java 21 (to run this builder project)
- Gradle (or use the provided wrapper)

### Building the Tool

The first step is to build the builder itself.

```sh
./gradlew build
```

This will create an executable JAR at `build/libs/wiremock-extensions-builder.jar`.

### Configuration

Next, a YAML configuration file needs to be created to define the extensions. See [example config file](examples/docker-example/wiremock-docker-easy-extensions-config.yaml) for a template.

**`config.yaml` structure:**

```yaml
# (Required) The location of your classes relative from where the project JAR will be executed.
files-location: examples/example

# (Required - cannot be empty) A list of your classes names with their extension.
source-files:
  - ResponseTransformerExtensionNoDependenciesJava.java
  - ResponseTransformerExtensionNoDependenciesKotlin.kt
  - ResponseTransformerExtensionWithDependenciesKotlin.kt
  
# (Required - can be empty) A list of dependencies required to run your classes, they will be packaged in the final JAR.
dependencies:
  - org.apache.commons:commons-lang3:3.18.0
```

### Building Your Extensions

Now, the custom WireMock extensions can be built by running the following command:
```sh
java -jar build/libs/wiremock-extensions-builder.jar build <path-to-your-config>.yaml
```

If successful, the bundled JAR will be located in the directory specified in the config (e.g., `build/extensions/wiremock-extensions-bundled.jar`).

### Running WireMock with Extensions - will be removed soon in favour of multi-docker build

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

---

## Usage with Docker (Runtime Volume Mount)

For a more dynamic workflow, it's possible to use a pre-built Docker image and mount the local source files to have the extension JAR built at container startup, then run WireMock with those extensions.

The key is to mount a local directory into `/home/config/` inside the container. The name of the mounted directory inside `/home/config/` should correspond to the top-level package of the extension classes. For example, if the extension's package is `examples.example`, the local directory should be mounted to `/home/config/examples`.

Crucially, the root of this mounted directory must contain a `wiremock-docker-easy-extensions-config.yaml` file. It should also contain `mappings` and `__files` directories. While these can be empty, the `mappings` directory is necessary for WireMock to serve any responses. The paths within the config file must be relative to the container's file system.

### Docker Run Command

Here is an example of how to run the container using `docker run`.

```sh
docker run -p 8080:8080 \
  -v .:/home/config/examples \
  docker-image:version
```

### Docker Compose

Here is an example of how to run the container using `docker-compose`.

**`docker-compose.yaml`:**
```yaml
services:
  wiremock-docker-easy-extensions:
    image: docker-image:version
    container_name: wiremock-with-runtime-extensions
    ports:
      - "8080:8080"
    volumes:
      # Mounts the current directory - assuming it is called `examples` (containing the config, sources, mappings and files)
      # to '/home/config/examples' inside the container.
      - .:/home/config/examples
```

With this setup, any changes to the local source files in the `examples` directory will be picked up the next time the container is restarted, triggering a new build of the extension JAR automatically.

## License

This project is licensed under the [Apache License 2.0](LICENSE).
