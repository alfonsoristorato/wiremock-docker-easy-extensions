#!/bin/sh

DOCKER_IMAGE="ghcr.io/alfonsoristorato/wiremock-docker-easy-extensions:latest"

docker run --rm \
  -p 8080:8080 \
  -v "$(pwd)":/home/config/examples \
  "$DOCKER_IMAGE"
