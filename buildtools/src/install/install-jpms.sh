#!/usr/bin/env bash
./gradlew --version
./gradlew --stacktrace --warning-mode=all vocabulary:build
./gradlew --stacktrace --warning-mode=all web-anno:build
./gradlew --stacktrace --warning-mode=all dynamo:build