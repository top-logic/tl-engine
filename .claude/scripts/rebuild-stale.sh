#!/usr/bin/env bash
#
# Find all reactor modules whose installed artifact in the local Maven
# repository is older than their sources (or missing), then rebuild just
# those modules in the correct order via `mvn install -pl ... -am`.
#
# Usage:
#   ./rebuild-stale.sh            # detect and rebuild
#   ./rebuild-stale.sh --dry-run  # only list stale modules
#
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$ROOT"

DRY_RUN=0
[[ "${1:-}" == "--dry-run" ]] && DRY_RUN=1

OUT="$(mktemp)"
trap 'rm -f "$OUT"' EXIT

echo ">>> Enumerating reactor modules via Maven ..." >&2
# One exec:exec call at the root walks the entire reactor (including nested
# aggregators like tl-parent-engine). Each reactor module prints one line.
# The @@MOD prefix lets us grep it out reliably from any surrounding noise.
mvn -B -q exec:exec \
    -Dexec.executable=echo \
    -Dexec.args='@@MOD ${project.groupId} ${project.artifactId} ${project.version} ${project.packaging} ${settings.localRepository} ${project.basedir}' \
    > "$OUT"

stale_paths=()
stale_names=()
checked=0

while IFS=' ' read -r marker g a v packaging repo basedir; do
    [[ "$marker" == "@@MOD" ]] || continue
    # Skip aggregator-only modules (no jar artifact).
    [[ "$packaging" == "pom" ]] && continue
    checked=$((checked + 1))

    jar="$repo/${g//./\/}/$a/$v/$a-$v.jar"

    # Newest mtime across src/ tree and pom.xml. Missing src/ is fine.
    newest=$(find "$basedir/pom.xml" "$basedir/src" -type f -printf '%T@\n' 2>/dev/null \
             | awk 'BEGIN{m=0} {if ($1>m) m=$1} END{if (m>0) print m}')
    [[ -z "$newest" ]] && continue

    rel="${basedir#$ROOT/}"
    [[ "$rel" == "$basedir" ]] && rel="$basedir"  # outside root: use absolute

    if [[ ! -f "$jar" ]]; then
        stale_paths+=("$rel")
        stale_names+=("$a (no jar)")
        continue
    fi

    jar_mtime=$(find "$jar" -printf '%T@\n')
    if awk -v s="$newest" -v j="$jar_mtime" 'BEGIN{exit !(s>j)}'; then
        stale_paths+=("$rel")
        stale_names+=("$a")
    fi
done < "$OUT"

echo ">>> Checked $checked jar-producing modules." >&2

if [[ ${#stale_paths[@]} -eq 0 ]]; then
    echo ">>> All artifacts up to date." >&2
    exit 0
fi

echo ">>> Stale modules (${#stale_paths[@]}):" >&2
for n in "${stale_names[@]}"; do echo "    - $n" >&2; done

if [[ $DRY_RUN -eq 1 ]]; then
    exit 0
fi

# -pl with comma-separated relative paths, -am to also build the upstream
# dependencies that the stale modules need (in correct reactor order).
IFS=','; list="${stale_paths[*]}"; unset IFS

echo ">>> Running: mvn -B install -pl <stale> -am -DskipTests" >&2
exec mvn -B install -pl "$list" -am -DskipTests
