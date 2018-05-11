#!/usr/bin/env bash
./gradlew --version
./gradlew --stacktrace --warning-mode=all webanno:build
./gradlew --stacktrace --warning-mode=all dynamo:build