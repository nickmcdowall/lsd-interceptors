#!/bin/bash
set -e

# To bump versions use:
# ./gradlew markNextVersion -Prelease.incrementer=incrementMinor

# Bump patch version for release version by default
 ./gradlew release

# Publish to sonatype and then release to maven central
./gradlew publishToSonatype closeAndReleaseStagingRepository