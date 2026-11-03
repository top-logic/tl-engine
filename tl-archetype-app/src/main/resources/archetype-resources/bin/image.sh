#!/usr/bin/env bash
#set( $dollar = '$' )
#
# Builds the image of ${DisplayName} (src/main/docker/Dockerfile): the application for production -
# PostgreSQL, the deploy aspect prod - and the image around it, on the runtime image of TopLogic
# top-logic/tl-app-runtime of the TopLogic version of the POM (tl.version).
#
# Usage: bin/image.sh [TAG]       TAG default \${artifactId}:latest
#
# A runtime image this machine does not have yet is built here, from its build context: the Maven
# artifact com.top-logic:tl-app-runtime of the same TopLogic version, which the engine publishes with
# every version. No image registry is needed.
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
	CONTEXT=target/tl-app-runtime
	mvn -B -q dependency:unpack "-Dartifact=com.top-logic:tl-app-runtime:$TL_VERSION" \
		"-DoutputDirectory=$CONTEXT" "-Dmdep.unpack.excludes=META-INF/**"
	docker build --pull -t "$RUNTIME" "$CONTEXT"
fi

docker build -f src/main/docker/Dockerfile --build-arg "TL_VERSION=$TL_VERSION" --build-arg "APP=$APP" -t "$TAG" .
echo "built $TAG"
