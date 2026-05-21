#!/bin/bash
# Lists the unique Trac ticket numbers referenced by commit messages since the
# last release tag. Used by the tl-release skill to verify that every ticket
# going into a release is closed in Trac.
#
# Usage: list-release-tickets.sh [<since-tag>]
#   <since-tag>  Release tag to compare against (default: latest reachable TL_* tag)
#
# Output: one ticket number per line on stdout; a summary line on stderr.
set -euo pipefail

since="${1:-}"
if [ -z "$since" ]; then
	since="$(git describe --tags --abbrev=0 --match 'TL_*' HEAD)"
fi

count="$(git rev-list --count "$since"..HEAD)"
echo "# Tickets in commits ${since}..HEAD (${count} commits)" >&2

git log --format='%s%n%b' "$since"..HEAD \
	| grep -oE 'Ticket #[0-9]+' \
	| grep -oE '[0-9]+' \
	| sort -u -n
