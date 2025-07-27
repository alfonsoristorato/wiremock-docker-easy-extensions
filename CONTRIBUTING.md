# Contributing to wiremock-docker-easy-extensions

First off, thank you for considering contributing! Community feedback is the best way to make this project better.

## How to Contribute

### Reporting Bugs

If you find a bug, please search the [Issues](https://github.com/alfonsoristorato/wiremock-docker-easy-extensions/issues) to see if it has already been reported. If not, please [open a new Bug Report](https://github.com/alfonsoristorato/wiremock-docker-easy-extensions/issues/new?template=bug_report.yml).

### Suggesting Enhancements

If you have an idea for a new feature or an improvement to an existing one, please [open a new Feature Request](https://github.com/alfonsoristorato/wiremock-docker-easy-extensions/issues/new?template=feature_request.yml) to start a discussion.

## Creating a Release (for maintainers)

This project uses GitHub Actions to automatically build and publish a Docker image to the GitHub Container Registry (GHCR) when a new release is created.

To create a new release:

1.  **Navigate to the "Releases" page** in the GitHub repository.
2.  Click on **"Draft a new release"**.
3.  **Create a new tag** for the release. The tag **must** follow semantic versioning (e.g., `v1.0.0`, `v1.2.3-beta`).
4.  Fill in the release title and description.
5.  **Publish the release**. If it's a stable release, make sure the "This is a pre-release" checkbox is unchecked.

Once the release is published, the "Build, Scan and Publish" workflow will automatically trigger. It will:
- Run the CodeQL security scan.
- If the scan passes, it will build the Docker image.
- Tag the image with the version number (e.g., `1.0.0`, `1.2`, `1`).
- If it was a full release (not a pre-release), it will also tag the image as `latest`.
- Push the tagged image to GHCR.

Thank you for helping to improve `wiremock-docker-easy-extensions`!
