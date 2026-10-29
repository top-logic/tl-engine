#!/usr/bin/env bash
#
# SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
#
# SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
#
# Builds the runtime image of TopLogic applications (README.md) and tags it with the version of the
# engine of this checkout: top-logic/tl-app-runtime:<version of tl-bom>.
#
# Usage: tl-app-runtime/build.sh [TAG]       TAG default top-logic/tl-app-runtime:<version>

set -euo pipefail

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
VERSION="$(sed -n 's|^\t<version>\(.*\)</version>$|\1|p' "$DIR/../tl-bom/pom.xml" | head -1)"
[ -n "$VERSION" ] || { echo "error: no version in tl-bom/pom.xml" >&2; exit 1; }
TAG="${1:-top-logic/tl-app-runtime:$VERSION}"

docker build --pull -t "$TAG" "$DIR"
echo "built $TAG"
