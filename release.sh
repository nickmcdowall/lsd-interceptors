#!/bin/bash
set -e

# To bump versions use:
# ./gradlew markNextVersion -Prelease.incrementer=incrementMinor

# Bumpt patch version for release version by default
 ./gradlew release

# Upload to bintray
 ./gradlew bintrayUpload