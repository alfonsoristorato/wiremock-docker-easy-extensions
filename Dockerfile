# Stage 1: Build wiremock-easy-extensions jar
FROM gradle:jdk21-alpine AS wiremock-docker-easy-extensions_builder
WORKDIR /home/gradle/src

RUN git -c http.sslVerify=false clone https://github.com/alfonsoristorato/wiremock-docker-easy-extensions.git .

RUN ./gradlew build --no-daemon

# Stage 2: Build the actual WireMock extensions using the tool
FROM wiremock-docker-easy-extensions_builder AS wiremock-docker-easy-extensions_compiler
ARG USER_CONFIG_PATH
COPY --from=wiremock-docker-easy-extensions_builder /home/gradle/src/build/libs/wiremock-extensions-builder.jar .
COPY ${USER_CONFIG_PATH}/ ./${USER_CONFIG_PATH}

RUN java -jar wiremock-extensions-builder.jar build ${USER_CONFIG_PATH}/wiremock-docker-easy-extensions-config.yaml

## Stage 3: Run Wiremock with the built extensions, mappings and files
FROM wiremock/wiremock:latest
ARG USER_CONFIG_PATH

COPY --from=wiremock-docker-easy-extensions_compiler /home/gradle/src/build/extensions/wiremock-extensions-bundled.jar /var/wiremock/extensions/
COPY --from=wiremock-docker-easy-extensions_compiler /home/gradle/src/${USER_CONFIG_PATH}/mappings /home/wiremock/mappings/
COPY --from=wiremock-docker-easy-extensions_compiler /home/gradle/src/${USER_CONFIG_PATH}/__files /home/wiremock/__files/
