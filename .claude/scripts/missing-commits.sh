#!/usr/bin/env bash
#
# Show commits from <source> branch that are not (patch-equivalent) in <target>,
# and optionally cherry-pick them onto <target>.
#
# Uses `git cherry` for patch-ID-based comparison, so rebased or cherry-picked
# commits are correctly recognized as already present.
#
# Usage:
#   ./missing-commits.sh <target> <source>                # list only
#   ./missing-commits.sh <target> <source> --apply        # cherry-pick twin-free commits
#   ./missing-commits.sh <target> <source> --apply --all  # cherry-pick all missing commits
#   ./missing-commits.sh -h | --help
#
# Target comes first so changes from several source branches can be collected
# into one target by just varying the second argument.
#
# --apply skips commits that have a same-message twin on <target> (probable
# rebases). --all overrides that and cherry-picks every missing commit.
#
set -euo pipefail

usage() {
    sed -n '3,19p' "$0" | sed 's/^# \{0,1\}//'
    exit "${1:-0}"
}

APPLY=0
APPLY_ALL=0
POS=()
for arg in "$@"; do
    case "$arg" in
        -h|--help) usage 0 ;;
        --apply)   APPLY=1 ;;
        --all)     APPLY_ALL=1 ;;
        -*)        echo "Unknown option: $arg" >&2; usage 2 ;;
        *)         POS+=("$arg") ;;
    esac
done

if [[ ${#POS[@]} -ne 2 ]]; then
    echo "Error: need exactly two branch arguments" >&2
    usage 2
fi

TARGET="${POS[0]}"
SOURCE="${POS[1]}"

for ref in "$SOURCE" "$TARGET"; do
    if ! git rev-parse --verify --quiet "$ref" >/dev/null; then
        echo "Error: revision not found: $ref" >&2
        exit 2
    fi
done

# `git cherry <upstream> <head>`: '+' = patch missing in upstream, '-' = present.
# Output is newest-first; we reverse for chronological cherry-pick order.
mapfile -t MISSING < <(git cherry "$TARGET" "$SOURCE" | awk '$1=="+" {print $2}' | tac)

echo "Commits in '$SOURCE' missing from '$TARGET': ${#MISSING[@]}"
echo
if [[ ${#MISSING[@]} -eq 0 ]]; then
    echo "Nothing to do — '$TARGET' already contains all patches from '$SOURCE'."
    exit 0
fi

# Rebase detector: index the full commit message of every commit on <target>.
# A "missing" commit whose message also appears on <target> is most likely a
# rebased copy whose patch-ID merely drifted (e.g. bundled generated files).
rstrip() { local s="$1"; printf '%s' "${s%"${s##*[![:space:]]}"}"; }

declare -A TARGET_BY_MSG
while IFS= read -r -d '' rec; do
    # git log inserts a '\n' separator between commits; with NUL-delimited
    # records that newline becomes a leading char of the next record.
    rec="${rec#$'\n'}"
    h="${rec%%$'\n'*}"
    m="$(rstrip "${rec#*$'\n'}")"
    [[ -n "${TARGET_BY_MSG[$m]+x}" ]] || TARGET_BY_MSG[$m]="$h"
done < <(git log "$TARGET" --format='%H%n%B%x00')

rebase_count=0
TWIN_FREE=()
for sha in "${MISSING[@]}"; do
    git --no-pager log -1 --format='%h  %s  (%an, %ad)' --date=short "$sha"
    msg="$(rstrip "$(git log -1 --format='%B' "$sha")")"
    twin="${TARGET_BY_MSG[$msg]:-}"
    if [[ -n "$twin" ]]; then
        rebase_count=$((rebase_count + 1))
        echo "    -> likely a rebase: identical message present on '$TARGET' as $(git rev-parse --short "$twin")"
    else
        TWIN_FREE+=("$sha")
    fi
done

if [[ $rebase_count -gt 0 ]]; then
    echo
    echo "Note: $rebase_count of ${#MISSING[@]} commit(s) have a same-message twin on '$TARGET'"
    echo "      and are skipped by --apply (use --all to cherry-pick them anyway)."
fi

if [[ $APPLY -eq 0 ]]; then
    echo
    echo "Re-run with --apply to cherry-pick the ${#TWIN_FREE[@]} twin-free commit(s) onto '$TARGET',"
    echo "or --apply --all to cherry-pick all ${#MISSING[@]}."
    exit 0
fi

# --- apply mode ---
echo

if ! git diff --quiet || ! git diff --cached --quiet; then
    echo "Error: working tree or index is dirty. Commit or stash first." >&2
    exit 1
fi

CURRENT=$(git symbolic-ref --short -q HEAD || echo "")
if [[ "$CURRENT" != "$TARGET" ]]; then
    echo ">>> Checking out '$TARGET' (was on '${CURRENT:-detached}') ..."
    git checkout "$TARGET"
fi

PICK=()
if [[ $APPLY_ALL -eq 1 ]]; then
    PICK=("${MISSING[@]}")
elif [[ ${#TWIN_FREE[@]} -gt 0 ]]; then
    PICK=("${TWIN_FREE[@]}")
fi

if [[ ${#PICK[@]} -eq 0 ]]; then
    echo ">>> Nothing to cherry-pick: every missing commit has a same-message twin on '$TARGET'."
    echo "    Re-run with --all to cherry-pick them anyway."
    exit 0
fi

echo ">>> Cherry-picking ${#PICK[@]} commit(s) onto '$TARGET' ..."
git cherry-pick "${PICK[@]}"
echo ">>> Done."
