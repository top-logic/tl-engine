#!/bin/sh
#
# SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
#
# SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
#
# Git merge driver for the resource bundles of the engine (META-INF/messages_*.properties,
# WEB-INF/conf/resources/*.properties): files of "key = value" lines, one line per key, sorted by
# key. Two branches that add different keys next to each other conflict in a text merge although
# their changes are independent. This driver merges key by key instead:
#
# - a key that only one side added, changed or removed takes that side's line,
# - a key both sides changed to the same line takes it,
# - a key the two sides changed differently is a real conflict.
#
# The result is sorted by key (byte order, like the generated files). On a real conflict, and for a
# file that is not of the form above (a line without "=", keys out of order), the driver falls back
# to the text merge of "git merge-file", which writes the usual conflict markers.
#
# Registration (once per clone, see the article engine/git-merge-driver):
#
#   git config merge.tl-properties.name "TopLogic resource bundle merge"
#   git config merge.tl-properties.driver 'f=tools/git/merge-properties.sh; if test -f $f; then sh $f %O %A %B %P; else git merge-file %A %O %B; fi'
#
# The registered command falls back to the text merge in a checkout of a branch without this script.
# The same two patterns in "$(git rev-parse --git-common-dir)/info/attributes" make the driver apply
# to branches whose .gitattributes does not name it yet.
# .gitattributes assigns the driver to the files (merge=tl-properties). Without the registration,
# Git uses its text merge for them.
#
# Arguments: <base> <ours> <theirs> [<path>]. The result is written to <ours>; the exit code is 0
# for a clean merge and non-zero for a conflict.

base=$1
ours=$2
theirs=$3
path=${4:-file}

LC_ALL=C
export LC_ALL

# Succeeds if every line of the file has a "=" and the keys are strictly ascending.
keyed() {
	awk '
		{
			i = index($0, "=")
			if (i == 0) { exit 1 }
			key = substr($0, 1, i - 1)
			sub(/[ \t]+$/, "", key)
			if (NR > 1 && key <= last) { exit 1 }
			last = key
		}
	' "$1"
}

text_merge() {
	git merge-file -L "$path (ours)" -L "$path (base)" -L "$path (theirs)" "$ours" "$base" "$theirs"
	exit $?
}

keyed "$base" && keyed "$ours" && keyed "$theirs" || text_merge

merged="$ours.merged.$$"

# Prints "key<TAB>line" for every key of the result, unsorted; exits 1 on a conflicting key.
awk '
	{
		i = index($0, "=")
		key = substr($0, 1, i - 1)
		sub(/[ \t]+$/, "", key)
	}
	FILENAME == ARGV[1] { o[key] = $0; seen[key] = 1; next }
	FILENAME == ARGV[2] { a[key] = $0; seen[key] = 1; next }
	{ b[key] = $0; seen[key] = 1 }
	END {
		for (key in seen) {
			inO = key in o; inA = key in a; inB = key in b
			lineO = inO ? o[key] : ""; lineA = inA ? a[key] : ""; lineB = inB ? b[key] : ""
			if (inA == inB && lineA == lineB) {
				present = inA; line = lineA
			} else if (inA == inO && lineA == lineO) {
				present = inB; line = lineB
			} else if (inB == inO && lineB == lineO) {
				present = inA; line = lineA
			} else {
				exit 1
			}
			if (present) { printf "%s\t%s\n", key, line }
		}
	}
' "$base" "$ours" "$theirs" > "$merged.unsorted" || {
	rm -f "$merged.unsorted"
	text_merge
}

sort -t "$(printf '\t')" -k1,1 "$merged.unsorted" | cut -f2- > "$merged" && mv "$merged" "$ours"
status=$?
rm -f "$merged.unsorted" "$merged"
exit $status
