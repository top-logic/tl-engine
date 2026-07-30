#!/usr/bin/env python3
"""
Check (and repair) the `RequiresMigration` keyword on Trac tickets.

A release milestone lists the migration instructions of a release with

    [[TicketQuery(relatedmilestones~=milestone:<name>,keywords~=RequiresMigration)]]

so a ticket only reaches the changelog's migration chapter when it carries
exactly that keyword. Two ways to fall out of it, both silent:

  * the ticket's description has a `== Migration ==` section but no keyword, or
  * the keyword is spelled some other way (`CodeMigration`,
    `RequiresCodeMigration`, `RequiresDataMigration`, ...). Nothing queries those
    spellings, so the instructions are simply missing from the release notes.

Two checks, run both before a release:

    # (a) release set: description section vs. keyword
    .claude/skills/tl-release/scripts/list-release-tickets.sh \\
        | ./.venv/bin/python .claude/skills/tl-release/scripts/check-migration-keywords.py --apply

    # (b) project-wide: deprecated spellings, whenever they were introduced
    ./.venv/bin/python .claude/skills/tl-release/scripts/check-migration-keywords.py --all --apply

Without --apply the script only reports what it would change.

`MigrationTool` is NOT a deprecated spelling -- it marks tickets *about* the
migration tooling and is left alone.

Run with the project venv, OUTSIDE the command sandbox (see trac_client).
"""
import argparse
import re
import sys

from trac_client import connect

KEYWORD = "RequiresMigration"

# Spellings that mean `RequiresMigration` but that no query matches.
DEPRECATED = ("RequiresCodeMigration", "RequiresDataMigration", "CodeMigration", "DataMigration")

MIGRATION_SECTION = re.compile(r"^\s*==+\s*Migration", re.MULTILINE | re.IGNORECASE)


def read_ids(arg_ids):
    ids = list(arg_ids)
    if not sys.stdin.isatty():
        for line in sys.stdin:
            line = line.strip()
            if line and not line.startswith("#"):
                ids.append(line)
    seen = []
    for raw in ids:
        num = int(raw)
        if num not in seen:
            seen.append(num)
    return seen


def keywords_of(attrs):
    """The ticket's keywords as a list (Trac separates them by space or comma)."""
    return [w for w in (attrs.get("keywords") or "").replace(",", " ").split() if w]


def deprecated_ids(trac):
    """Ticket ids carrying any deprecated spelling, project-wide.

    The `~=` operator matches substrings, so a query for the longest spellings
    already covers the shorter ones; the union is taken to stay independent of
    that overlap.
    """
    found = set()
    for kw in DEPRECATED:
        found.update(trac.ticket.query("keywords~=%s&max=0" % kw))
    return sorted(found)


def repair(trac, tid, attrs, apply_changes):
    """Add KEYWORD, drop deprecated spellings. Returns a report line, or None."""
    old = keywords_of(attrs)
    new = [w for w in old if w not in DEPRECATED]
    dropped = [w for w in old if w in DEPRECATED]
    if KEYWORD not in new:
        new.insert(0, KEYWORD)
    if new == old:
        return None

    why = ("replacing %s" % ", ".join(dropped)) if dropped else "description has a Migration section"
    if not apply_changes:
        return "  #%-7d would set %r -> %r  (%s)" % (tid, " ".join(old), " ".join(new), why)

    comment = ("Flagged with `%s` so the ticket's `== Migration ==` section reaches the "
               "release changelog (%s)." % (KEYWORD, why))
    trac.ticket.update(tid, comment,
                       {"keywords": " ".join(new), "_ts": attrs["_ts"], "action": "leave"})
    return "  #%-7d %r -> %r  (%s)" % (tid, " ".join(old), " ".join(new), why)


def main():
    ap = argparse.ArgumentParser(description=__doc__,
                                 formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("ids", nargs="*", help="ticket numbers (or piped via stdin)")
    ap.add_argument("--all", action="store_true",
                    help="additionally sweep the whole project for deprecated spellings")
    ap.add_argument("--apply", action="store_true",
                    help="write the changes (default: dry-run, report only)")
    args = ap.parse_args()

    ids = read_ids(args.ids)
    trac = connect()

    if args.all:
        stale = deprecated_ids(trac)
        print("Project-wide sweep: %d ticket(s) with a deprecated spelling (%s)."
              % (len(stale), ", ".join(DEPRECATED)))
        ids += [t for t in stale if t not in ids]
    elif not ids:
        sys.exit("No ticket IDs given (pass as args, pipe into stdin, or use --all).")

    changed = ok = 0
    for tid in ids:
        attrs = trac.ticket.get(tid)[3]
        has_section = bool(MIGRATION_SECTION.search(attrs.get("description") or ""))
        words = keywords_of(attrs)
        needs = has_section or any(w in DEPRECATED for w in words)
        if not needs:
            continue
        line = repair(trac, tid, attrs, args.apply)
        if line is None:
            ok += 1
            continue
        print(line)
        changed += 1

    # A keyword without a section is not repairable here: either the description is
    # missing its instructions or the keyword does not belong on the ticket.
    for tid in ids:
        attrs = trac.ticket.get(tid)[3]
        if KEYWORD in keywords_of(attrs) and not MIGRATION_SECTION.search(attrs.get("description") or ""):
            print("  #%-7d WARNING: carries %s but has no `== Migration ==` section"
                  % (tid, KEYWORD))

    verb = "Fixed" if args.apply else "Would fix"
    tail = "" if args.apply else "  (dry-run -- pass --apply to write)"
    print("\n%s %d ticket(s); %d already correct.%s" % (verb, changed, ok, tail))


if __name__ == "__main__":
    main()
