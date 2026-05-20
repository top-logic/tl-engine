#!/usr/bin/env bash
# Creates a .mvn/maven.config with an absolute path to the workspace-local
# Maven repository and disables remote snapshot updates from Nexus.
#
# This is needed in git worktrees where the relative path ".m2/repository"
# resolves differently when Maven is invoked from a subdirectory (e.g. app
# modules).
#
# Usage: .claude/scripts/local-m2.sh

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
MVN_DIR="${REPO_ROOT}/.mvn"
LOCAL_REPO="${REPO_ROOT}/.m2/repository"

mkdir -p "${MVN_DIR}"
cat > "${MVN_DIR}/maven.config" <<EOF
-Dmaven.repo.local=${LOCAL_REPO}
-nsu
EOF

echo "Wrote ${MVN_DIR}/maven.config:"
cat "${MVN_DIR}/maven.config"
