#!/usr/bin/env python3
"""
Mark release tickets with a release milestone via the Trac `relatedmilestones`
field.

TopLogic documents which tickets went into a release through the custom Trac
field `relatedmilestones` -- a comma-separated list of `milestone:<name>`
entries. The release milestone's description uses

    [[TicketQuery(relatedmilestones=milestone:<name>)]]

to list exactly those tickets. The standard `milestone` field stays untouched;
it is the roadmap milestone (e.g. TL_8.0.0), not the release marker.

Setting `relatedmilestones` is a manual step performed by whoever does the
release. The existing field value is preserved -- the new entry is appended, so
a ticket can carry several release markers.

Run with the project venv (keyring needs to run OUTSIDE the command sandbox):

    .claude/skills/tl-release/scripts/list-release-tickets.sh \\
        | ./.venv/bin/python .claude/skills/tl-release/scripts/mark-release-tickets.py \\
              TL_8.0.0-alpha4 --apply

Ticket IDs come from positional args and/or stdin (one number per line; lines
starting with '#' are ignored, so list-release-tickets.sh output pipes in
directly). Without --apply the script only reports the intended changes.
"""
import argparse
import sys

from trac_client import connect


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


def main():
    ap = argparse.ArgumentParser(description=__doc__,
                                 formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("milestone", help="release milestone name, e.g. TL_8.0.0-alpha4")
    ap.add_argument("ids", nargs="*", help="ticket numbers (or piped via stdin)")
    ap.add_argument("--apply", action="store_true",
                    help="write the changes (default: dry-run, report only)")
    args = ap.parse_args()

    entry = "milestone:" + args.milestone
    comment = "Markiert für Release %s." % args.milestone
    ids = read_ids(args.ids)
    if not ids:
        sys.exit("No ticket IDs given (pass as args or pipe into stdin).")

    trac = connect()
    changed = skipped = 0
    for tid in ids:
        attrs = trac.ticket.get(tid)[3]
        current = (attrs.get("relatedmilestones") or "").strip()
        entries = [e.strip() for e in current.split(",") if e.strip()]
        if entry in entries:
            print("  #%-7d already marked (%s)" % (tid, current))
            skipped += 1
            continue
        entries.append(entry)
        newval = ", ".join(entries)
        if args.apply:
            trac.ticket.update(tid, comment, {"relatedmilestones": newval})
            print("  #%-7d %r -> %r" % (tid, current, newval))
        else:
            print("  #%-7d would set %r -> %r" % (tid, current, newval))
        changed += 1

    verb = "Marked" if args.apply else "Would mark"
    tail = "" if args.apply else "  (dry-run -- pass --apply to write)"
    print("\n%s %d ticket(s); %d already marked.%s" % (verb, changed, skipped, tail))


if __name__ == "__main__":
    main()
