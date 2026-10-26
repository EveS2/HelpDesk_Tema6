#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT
javac -d "$TMP" src/main/java/br/edu/helpdesk/service/RegraDePrazos.java scripts/TestePrazosJdk.java
java -cp "$TMP" TestePrazosJdk
javac -d "$TMP" scripts/VerificarSintaxeJdk.java
java -cp "$TMP" VerificarSintaxeJdk
if command -v mvn >/dev/null 2>&1; then
  mvn clean verify
else
  echo "Maven nao instalado: testes Spring/JaCoCo pendentes."
fi
