#!/usr/bin/env bash
./gradlew --version
./gradlew --stacktrace --warning-mode=all web-anno:build
./gradlew --stacktrace --warning-mode=all iiif.dynamic:build