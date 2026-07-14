#!/usr/bin/env python3
"""
Inspect, create or update Trac milestones via the Trac XML-RPC API.

The Trac MCP server (mcp-servers/trac-mcp-server.py) only exposes milestone
*listing* (get_milestones). This helper reuses the same OS-keyring credentials
and calls the ticket.milestone.* XML-RPC methods directly, so the tl-release
skill can document a release milestone.

Run with the project venv, OUTSIDE the command sandbox (keyring needs the OS
Secret Service):

    ./.venv/bin/python .claude/skills/tl-release/scripts/trac-milestone.py <cmd> ...

Commands:
    get    <name>
    create <name> [--description TEXT | --release-description] [--due D] [--completed D]
    update <name> [--description TEXT | --release-description] [--due D] [--completed D]

Dates D are YYYY-MM-DD or YYYY-MM-DDTHH:MM. `update` preserves milestone
attributes that are not passed.

--release-description generates the standard TopLogic release-milestone body.
It queries the custom `relatedmilestones` ticket field with the `~=` (contains)
operator -- NOT plain `=`. A ticket released in several versions carries a
multi-valued marker (e.g. "milestone:TL_8.0.0-alpha3, milestone:TL_8.0.0-alpha4");
`=` matches only single-valued fields and would silently drop such tickets.

Creating/updating a milestone requires TICKET_ADMIN permission on the account.
"""
import argparse
import datetime
import sys

from trac_client import connect


def release_description(name):
    """Standard release-milestone body: TicketQueries over `relatedmilestones`.

    Three sections, matching the format established with TL_8.0.0-alpha4 and
    TL_8.0.0-alpha5:
      * the main list excludes tickets keyworded `Update` (keywords!~=Update),
      * an `== Updates ==` section lists exactly those (keywords~=Update),
      * a `== Migration ==` section lists tickets keyworded RequiresCodeMigration
        or RequiresDataMigration (OR expressed with `|` in the query).
    """
    return (
        "[[TicketQuery(relatedmilestones~=milestone:%s,keywords!~=Update)]]\n"
        "\n"
        "== Updates ==\n"
        "[[TicketQuery(relatedmilestones~=milestone:%s,keywords~=Update)]]\n"
        "\n"
        "== Migration ==\n"
        "[[TicketQuery(relatedmilestones~=milestone:%s,keywords~=RequiresCodeMigration|RequiresDataMigration)]]"
        % (name, name, name)
    )


def parse_dt(value):
    for fmt in ("%Y-%m-%dT%H:%M", "%Y-%m-%d"):
        try:
            return datetime.datetime.strptime(value, fmt)
        except ValueError:
            pass
    sys.exit("Bad date %r (use YYYY-MM-DD or YYYY-MM-DDTHH:MM)." % value)


def build_attrs(args):
    attrs = {}
    if args.release_description:
        attrs["description"] = release_description(args.name)
    elif args.description is not None:
        attrs["description"] = args.description
    if args.due:
        attrs["due"] = parse_dt(args.due)
    if args.completed:
        attrs["completed"] = parse_dt(args.completed)
    return attrs


def add_write_args(parser):
    parser.add_argument("name")
    group = parser.add_mutually_exclusive_group()
    group.add_argument("--description", help="literal description text")
    group.add_argument("--release-description", action="store_true",
                       help="generate the standard release-milestone description")
    parser.add_argument("--due", help="due date YYYY-MM-DD[THH:MM]")
    parser.add_argument("--completed", help="completion date YYYY-MM-DD[THH:MM]")


def main():
    ap = argparse.ArgumentParser(description=__doc__,
                                 formatter_class=argparse.RawDescriptionHelpFormatter)
    sub = ap.add_subparsers(dest="cmd", required=True)

    g = sub.add_parser("get", help="print attributes of an existing milestone")
    g.add_argument("name")

    add_write_args(sub.add_parser("create", help="create a new milestone"))
    add_write_args(sub.add_parser("update", help="update an existing milestone"))

    args = ap.parse_args()
    trac = connect()
    existing = trac.ticket.milestone.getAll()

    if args.cmd == "get":
        milestone = trac.ticket.milestone.get(args.name)
        for key, value in sorted(milestone.items()):
            print("%-12s %r" % (key + ":", value))
        return

    attrs = build_attrs(args)

    if args.cmd == "create":
        if args.name in existing:
            sys.exit("Milestone '%s' already exists (use 'update')." % args.name)
        trac.ticket.milestone.create(args.name, attrs)
        print("Created milestone '%s'." % args.name)

    elif args.cmd == "update":
        if args.name not in existing:
            sys.exit("Milestone '%s' does not exist (use 'create')." % args.name)
        if not attrs:
            sys.exit("Nothing to update -- pass --description/--release-description/--due/--completed.")
        trac.ticket.milestone.update(args.name, attrs)
        print("Updated milestone '%s'." % args.name)


if __name__ == "__main__":
    main()
