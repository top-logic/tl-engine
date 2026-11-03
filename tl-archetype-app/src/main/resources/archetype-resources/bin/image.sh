#!/usr/bin/env bash
#set( $dollar = '$' )
#
# Builds the image of ${DisplayName} (src/main/docker/Dockerfile): the application for production -
# PostgreSQL, the deploy aspect prod - and the image around it, on the runtime image of TopLogic
# top-logic/tl-app-runtime of the TopLogic version of the POM (tl.version).
#
# Usage: bin/image.sh [TAG]       TAG default \${artifactId}:latest
#
# A missing runtime image is built from the TopLogic engine checkout named by TL_ENGINE
# (TL_ENGINE/tl-app-runtime/build.sh), otherwise pulled.
#
# Afterwards target/ holds the production build: a start from the IDE needs a build again.

set -euo pipefail

ROOT="$(cd "$(dirname "${dollar}{BASH_SOURCE[0]}")/.." && pwd)"
TAG="${dollar}{1:-\${artifactId}:latest}"
cd "$ROOT"

TL_VERSION="$(sed -n 's|.*<tl.version>\(.*\)</tl.version>.*|\1|p' pom.xml | head -1)"
[ -n "$TL_VERSION" ] || { echo "error: no tl.version in pom.xml" >&2; exit 1; }
RUNTIME="top-logic/tl-app-runtime:$TL_VERSION"

mvn -B -q clean package -Pwith-postgresql -DdeployAspects=prod

APP="$(ls -d target/*-app)"

if ! docker image inspect "$RUNTIME" > /dev/null 2>&1; then
	if [ -n "${dollar}{TL_ENGINE:-}" ]; then
		"$TL_ENGINE/tl-app-runtime/build.sh" "$RUNTIME"
	else
		docker pull "$RUNTIME"
	fi
fi

docker build -f src/main/docker/Dockerfile --build-arg "TL_VERSION=$TL_VERSION" --build-arg "APP=$APP" -t "$TAG" .
echo "built $TAG"
