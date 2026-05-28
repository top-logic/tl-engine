#!/usr/bin/env bash
#
# Configures the Maven local repository for a git worktree.
#
# Writes .mvn/maven.config so that Maven:
#   * WRITES installed artifacts to a worktree-local repository
#     -> build isolation: branch-specific SNAPSHOTs never pollute the
#        shared global repository, and parallel worktrees never clash.
#   * READS missing artifacts from the global repository as a read-only
#     "tail" (maven.repo.local.tail)
#     -> no full rebuild and no re-download when setting up a worktree;
#        only modules actually rebuilt here land in the worktree repo.
#
# The worktree-local repo also fixes a relative-path problem: a bare
# ".m2/repository" resolves differently when Maven runs from a module
# subdirectory (e.g. app modules).
#
# The read-only tail requires Maven 3.9.0+ (Resolver 1.9+). With older
# Maven the tail line is silently ignored as an unknown system property,
# leaving a fully isolated repo that must be populated by a full build.
#
# Usage: .claude/scripts/local-m2.sh
#
# Environment:
#   TL_GLOBAL_M2   Path to the global, read-only Maven repository used as
#                  the tail. Default: ~/.m2/repository

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
MVN_DIR="${REPO_ROOT}/.mvn"
LOCAL_REPO="${REPO_ROOT}/.m2/repository"
GLOBAL_REPO="${TL_GLOBAL_M2:-${HOME}/.m2/repository}"

# This script must only run in a worktree. In the main checkout ".git" is a
# directory; in a linked worktree it is a regular file ("gitdir: ..."). The
# main checkout is meant to write to the global repository directly --
# redirecting it here would wrongly isolate it and leave the tail empty.
if [[ ! -f "${REPO_ROOT}/.git" ]]; then
    echo "ERROR: ${REPO_ROOT} is not a git worktree." >&2
    echo "       Run this only in a linked worktree." >&2
    echo "       The main checkout should use ${GLOBAL_REPO} directly." >&2
    exit 1
fi

mkdir -p "${MVN_DIR}"
{
    echo "-Dmaven.repo.local=${LOCAL_REPO}"
    if [[ "${LOCAL_REPO}" != "${GLOBAL_REPO}" ]]; then
        # Read-only fallback repository (requires Maven 3.9.0+).
        echo "-Dmaven.repo.local.tail=${GLOBAL_REPO}"
        # Trust the tail unconditionally: its artifacts were fetched/installed
        # under a different repository configuration, so skip the per-artifact
        # availability re-check that would otherwise trigger re-downloads.
        echo "-Daether.chainedLocalRepository.ignoreTailAvailability=true"
    fi
    # Do not contact remote repositories for SNAPSHOT updates.
    echo "-nsu"
} > "${MVN_DIR}/maven.config"

echo "Wrote ${MVN_DIR}/maven.config:"
cat "${MVN_DIR}/maven.config"

# Warn if the global repo is missing -- the tail would have nothing to serve.
if [[ "${LOCAL_REPO}" != "${GLOBAL_REPO}" && ! -d "${GLOBAL_REPO}" ]]; then
    echo >&2
    echo "WARNING: global repository ${GLOBAL_REPO} does not exist." >&2
    echo "         Populate it with a full build in the main checkout first." >&2
fi

# Warn if the active Maven is too old for the read-only tail.
if command -v mvn >/dev/null 2>&1; then
    # ".*" at the start tolerates ANSI color codes (system Maven 3.6.x emits
    # them even with -B, so anchoring on "^Apache" would fail).
    mvn_ver="$(mvn -B --version 2>/dev/null \
               | sed -n 's/.*Apache Maven \([0-9][0-9.]*\).*/\1/p' | head -1)"
    if [[ -n "${mvn_ver}" \
          && "$(printf '%s\n3.9.0\n' "${mvn_ver}" | sort -V | head -1)" != "3.9.0" ]]; then
        echo >&2
        echo "WARNING: Maven ${mvn_ver} is active, but the read-only tail" >&2
        echo "         (maven.repo.local.tail) requires Maven 3.9.0 or newer." >&2
        echo "         Until you upgrade, the worktree repo is fully isolated" >&2
        echo "         and must be populated by a full build." >&2
    fi
fi
