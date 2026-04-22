#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

./mvnw -q -pl core -am compile

CLASSPATH="core/target/classes:shared/target/classes"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/apache/logging/log4j/log4j-api/2.20.0/log4j-api-2.20.0.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/apache/logging/log4j/log4j-core/2.20.0/log4j-core-2.20.0.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/apache/logging/log4j/log4j-slf4j-impl/2.20.0/log4j-slf4j-impl-2.20.0.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/slf4j/slf4j-api/1.7.36/slf4j-api-1.7.36.jar"

exec java -cp "$CLASSPATH" \
  com.github.javachaos.javaneuralnetwork.core.ConnectFourInteractiveDemo
