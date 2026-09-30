#!/usr/bin/env bash
#
# Decides which reactor modules a pull-request build has to build and test.
#
# The script compares the PR head with the target branch, maps every changed
# file to the reactor module that contains it, and asks Maven for the modules
# depending on the changed ones. The Jenkinsfile runs it before the build and
# derives the Maven invocations from its result.
#
# Mechanism:
#   1. Changed files: `git diff --name-only --no-renames <base> <head>`, where
#      <base> defaults to `git merge-base <target> <head>`. Renames are split
#      into a deletion and an addition, so a file moved between modules marks
#      both modules as changed.
#   2. Reactor modules: one `mvn -B validate` of the whole reactor (no
#      compilation, a few seconds). Maven (>= 3.9) prints each reactor
#      project as a header `< <groupId>:<artifactId> >` followed by
#      `[INFO]   from <dir>/pom.xml`, which yields the module directories
#      exactly as Maven resolves <modules> and profiles.
#   3. Mode:
#        full     A changed file belongs to the build definition outside all
#                 modules: the root pom.xml (parent of every module),
#                 anything under ci/, the Jenkinsfile, anything under .mvn/.
#        none     No changed file lies in a reactor module (docs/, specs/,
#                 .claude/, README.md, ...).
#        partial  Otherwise. Each changed file is attributed to the nearest
#                 enclosing reactor module directory (nested modules such as
#                 tl-parent-core/internal or test-migrate-apps/test-app-7-4-0
#                 are modules of their own). A file whose directory chain
#                 reaches no reactor module (e.g. of a deleted module) is
#                 ignored; deleting a module also changes the aggregator POM
#                 that listed it, which is a module change.
#   4. Affected modules (partial): `mvn -B validate -pl <changed> -amd`, its
#      projects mapped to directories by <groupId>:<artifactId>. The
#      downstream closure of Maven's project graph contains the modules that
#      depend on a changed module and the modules that inherit from a changed
#      parent POM (e.g. tl-parent-core -> all modules using it as parent), so
#      a parent POM change needs no special treatment.
#   5. Scripted modules: the affected modules whose src/test (at <head>)
#      contains *.script.xml files or a test class building a scripted suite
#      with XmlScriptedTestUtil.suite(...).
#
# Maven evaluates the working tree, so for a correct reactor the working tree
# must be the checkout of <head> (the default). With --head naming another
# commit (replaying historical merges), the reactor of the working tree is used
# and a warning is printed.
#
# Usage:
#   ci/affected-modules.sh [--target <ref>] [--base <rev>] [--head <rev>]
#                          [--output <file>]
#
#   --target <ref>   Branch the PR is merged into (default: origin/master).
#   --base <rev>     Commit to diff against (default: merge-base of <target>
#                    and <head>).
#   --head <rev>     PR head commit (default: HEAD).
#   --output <file>  Write the result to <file> instead of stdout.
#
# Environment:
#   MVN              Maven command (default: mvn).
#
# Output (stdout or --output file): one KEY=value line per key, values
# contain no spaces or quotes:
#   MODE=full|partial|none
#   CHANGED=<comma-separated module dirs, for -pl>   (empty for full and none)
#   AFFECTED=<comma-separated module dirs>           (partial: CHANGED plus all
#            downstream modules in reactor order; full: all reactor modules
#            except the root aggregator; none: empty)
#   SCRIPTED_MODULES=<comma-separated dirs among AFFECTED with scripted tests>
# A human-readable summary goes to stderr.
#
# Exit codes: 0 result written; 1 git or Maven failure; 2 invalid usage.
#
# Use in the Jenkinsfile:
#   none     No Maven build.
#   partial  (1) Build the changed modules and everything they depend on
#                without running tests:
#                  mvn -T 1C install -pl $CHANGED -am -DskipTests=true \
#                      -Dmaven.javadoc.skip=true -Dspotbugs.skip=true
#                skipTests only suppresses test execution; test classes are
#                still compiled and the test-jars that downstream tests depend
#                on (<type>test-jar</type>) are installed. Never use
#                -Dmaven.test.skip=true here, which would not install them.
#            (2) Clean-build and test the changed modules and their dependents:
#                  mvn -T 1C clean install spotbugs:spotbugs -pl $CHANGED -amd \
#                      -DskipTests=false -Dtl.javadoc.aggregate=false \
#                      -DTestAll.scripted=none ...
#                -amd only adds dependents, never dependencies, so `clean`
#                touches only modules of AFFECTED; the modules built only in
#                step (1) are taken from the local repository. Every module of
#                step (2) is recompiled from scratch, tests included.
#            The scripted tests of SCRIPTED_MODULES run in shards with
#            -DTestAll.scripted=<i>/<n>.
#   full     Step (2) without -pl, i.e. the whole reactor.
#
set -euo pipefail

# Output keys.
readonly KEY_MODE=MODE
readonly KEY_CHANGED=CHANGED
readonly KEY_AFFECTED=AFFECTED
readonly KEY_SCRIPTED=SCRIPTED_MODULES

# Modes.
readonly MODE_FULL=full
readonly MODE_PARTIAL=partial
readonly MODE_NONE=none

# Changed paths outside all modules that belong to the build definition.
readonly FULL_BUILD_PATTERN='^(pom\.xml|Jenkinsfile|ci/.*|\.mvn/.*)$'

# Test sources of a module that make it one with scripted tests.
readonly SCRIPT_FILE_PATTERN='/src/test/.*\.script\.xml$'
readonly SCRIPTED_SUITE_PATTERN='XmlScriptedTestUtil\.suite'

MVN="${MVN:-mvn}"

usage() {
    sed -n '/^# Usage:/,/^# Environment:/p' "$0" | sed '$d; s/^# \{0,1\}//' >&2
    exit 2
}

die() {
    echo "!!! $*" >&2
    exit 1
}

TARGET=origin/master
BASE=
HEAD_REV=HEAD
OUTPUT=
while [[ $# -gt 0 ]]; do
    case "$1" in
        --target) [[ $# -ge 2 ]] || usage; TARGET="$2"; shift 2 ;;
        --base)   [[ $# -ge 2 ]] || usage; BASE="$2"; shift 2 ;;
        --head)   [[ $# -ge 2 ]] || usage; HEAD_REV="$2"; shift 2 ;;
        --output) [[ $# -ge 2 ]] || usage; OUTPUT="$2"; shift 2 ;;
        -h|--help) usage ;;
        *) echo "!!! Unknown argument: $1" >&2; usage ;;
    esac
done

ROOT="$(git -C "$(dirname "${BASH_SOURCE[0]}")" rev-parse --show-toplevel)" || die "The script is not inside a git checkout."
cd "$ROOT"

HEAD_SHA="$(git rev-parse --verify --quiet "$HEAD_REV^{commit}")" || die "Unknown head revision: $HEAD_REV"
if [[ -z "$BASE" ]]; then
    git rev-parse --verify --quiet "$TARGET^{commit}" > /dev/null || die "Unknown target: $TARGET"
    BASE_SHA="$(git merge-base "$TARGET" "$HEAD_SHA")" || die "No merge base of $TARGET and $HEAD_REV."
else
    BASE_SHA="$(git rev-parse --verify --quiet "$BASE^{commit}")" || die "Unknown base revision: $BASE"
fi
if [[ "$HEAD_SHA" != "$(git rev-parse HEAD)" ]]; then
    echo ">>> Warning: head $HEAD_SHA is not the checked-out commit; using the reactor of the working tree." >&2
fi

TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT

# Validates the reactor Maven selects for the given arguments and writes one
# line "<groupId>:<artifactId> <pom path>" per reactor project, in reactor
# order, to the file $REACTOR_OUT. The POM path is the one Maven prints below
# the project header; it is relative to the first project of the reactor, so
# it identifies the module directory only in a run of the whole reactor.
reactor_projects() {
    local log="$TMP/mvn.log"
    if ! "$MVN" -B -Denforcer.skip=true "$@" validate > "$log" 2>&1; then
        cat "$log" >&2
        die "Maven failed to validate the reactor: $MVN -B -Denforcer.skip=true $* validate"
    fi
    awk '
        /^\[INFO\] -*< [^ ]+ >-*$/ { ga = $0; sub(/^\[INFO\] -*< /, "", ga); sub(/ >-*$/, "", ga); next }
        /^\[INFO\]   from / && ga != "" { pom = $0; sub(/^\[INFO\]   from /, "", pom); print ga " " pom; ga = "" }
    ' "$log" > "$REACTOR_OUT"
    [[ -s "$REACTOR_OUT" ]] || die "No project headers with '[INFO]   from <pom>' lines in the Maven output; Maven >= 3.9 is required."
}

join_commas() {
    local IFS=,
    echo "$*"
}

# --- 1. Changed files ------------------------------------------------------
CHANGED_FILES=()
while IFS= read -r -d '' f; do
    CHANGED_FILES+=("$f")
done < <(git diff -z --name-only --no-renames "$BASE_SHA" "$HEAD_SHA")

# --- 2. Reactor modules ----------------------------------------------------
# Module directory by "<groupId>:<artifactId>", and the set of module
# directories.
REACTOR_OUT="$TMP/reactor.txt"
declare -A DIR_OF=()
declare -A IS_MODULE=()
ALL_MODULES=()
reactor_projects
while read -r ga pom; do
    case "$pom" in
        pom.xml) continue ;;    # the root aggregator
        */pom.xml) d="${pom%/pom.xml}" ;;
        *) die "Unexpected POM path of $ga in the reactor listing: $pom" ;;
    esac
    DIR_OF["$ga"]="$d"
    IS_MODULE["$d"]=1
    ALL_MODULES+=("$d")
done < "$REACTOR_OUT"

# --- 3. Mode and changed modules -------------------------------------------
full_trigger=
declare -A CHANGED_SET=()
for f in ${CHANGED_FILES[@]+"${CHANGED_FILES[@]}"}; do
    if [[ "$f" =~ $FULL_BUILD_PATTERN ]]; then
        full_trigger="$f"
        continue
    fi
    d="$f"
    while [[ "$d" == */* ]]; do
        d="${d%/*}"
        if [[ -n "${IS_MODULE[$d]:-}" ]]; then
            CHANGED_SET["$d"]=1
            break
        fi
    done
done

CHANGED_MODULES=()
AFFECTED_MODULES=()
if [[ -n "$full_trigger" ]]; then
    MODE="$MODE_FULL"
    AFFECTED_MODULES=(${ALL_MODULES[@]+"${ALL_MODULES[@]}"})
elif [[ ${#CHANGED_SET[@]} -eq 0 ]]; then
    MODE="$MODE_NONE"
else
    MODE="$MODE_PARTIAL"
    # Reactor order, for a stable result.
    for d in ${ALL_MODULES[@]+"${ALL_MODULES[@]}"}; do
        [[ -n "${CHANGED_SET[$d]:-}" ]] && CHANGED_MODULES+=("$d")
    done
    # --- 4. Downstream closure ---------------------------------------------
    reactor_projects -pl "$(join_commas ${CHANGED_MODULES[@]+"${CHANGED_MODULES[@]}"})" -amd
    while read -r ga _; do
        [[ -n "${DIR_OF[$ga]:-}" ]] || die "Project $ga of the downstream reactor is not a module of the full reactor."
        AFFECTED_MODULES+=("${DIR_OF[$ga]}")
    done < "$REACTOR_OUT"
fi

# --- 5. Modules with scripted tests -----------------------------------------
declare -A HAS_SCRIPTED=()
while IFS= read -r f; do
    HAS_SCRIPTED["${f%%/src/test/*}"]=1
done < <(
    git ls-tree -r --name-only "$HEAD_SHA" | grep -E "$SCRIPT_FILE_PATTERN" || true
    git grep -l -E "$SCRIPTED_SUITE_PATTERN" "$HEAD_SHA" -- '*/src/test/*' | sed "s#^$HEAD_SHA:##" || true
)
SCRIPTED_MODULES=()
for d in ${AFFECTED_MODULES[@]+"${AFFECTED_MODULES[@]}"}; do
    [[ -n "${HAS_SCRIPTED[$d]:-}" ]] && SCRIPTED_MODULES+=("$d")
done

# --- Result -----------------------------------------------------------------
{
    echo "$KEY_MODE=$MODE"
    echo "$KEY_CHANGED=$(join_commas ${CHANGED_MODULES[@]+"${CHANGED_MODULES[@]}"})"
    echo "$KEY_AFFECTED=$(join_commas ${AFFECTED_MODULES[@]+"${AFFECTED_MODULES[@]}"})"
    echo "$KEY_SCRIPTED=$(join_commas ${SCRIPTED_MODULES[@]+"${SCRIPTED_MODULES[@]}"})"
} > "${OUTPUT:-/dev/stdout}"

{
    echo ">>> Base ${BASE_SHA:0:10}, head ${HEAD_SHA:0:10}: ${#CHANGED_FILES[@]} changed files, ${#ALL_MODULES[@]} reactor modules."
    case "$MODE" in
        "$MODE_FULL")    echo ">>> Mode $MODE: build definition changed ($full_trigger)." ;;
        "$MODE_NONE")    echo ">>> Mode $MODE: no reactor module changed." ;;
        "$MODE_PARTIAL") echo ">>> Mode $MODE: ${#CHANGED_MODULES[@]} changed modules: ${CHANGED_MODULES[*]}" ;;
    esac
    echo ">>> ${#AFFECTED_MODULES[@]} affected modules."
    echo ">>> Modules with scripted tests: ${SCRIPTED_MODULES[*]:-(none)}"
} >&2
