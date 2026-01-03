FROM eclipse-temurin:21.0.9_10-jdk-jammy AS wiremock-docker-easy-extensions_builder
WORKDIR /builder

COPY . .

RUN ./gradlew shadowJar --no-daemon

FROM wiremock/wiremock:3.13.2
COPY --from=wiremock-docker-easy-extensions_builder /builder/build/libs/wiremock-docker-easy-extensions.jar .
COPY --from=wiremock-docker-easy-extensions_builder /builder/entrypoint.sh /entrypoint.sh
COPY --from=wiremock-docker-easy-extensions_builder /root/.gradle /root/.gradle

RUN apt-get update && \
    apt-get install -y openjdk-11-jdk && \
    rm -rf /var/lib/apt/lists/*

ENTRYPOINT ["/entrypoint.sh"]
