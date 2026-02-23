#!/usr/bin/env python3
"""
MCP Server for Trac ticket system access via XML-RPC.

This server exposes Trac functionality to Claude Code through the Model Context Protocol.
Credentials are stored in the OS keyring (no plaintext files).
"""

import asyncio
import base64
import os
import sys
import keyring
import xmlrpc.client
from typing import Any

try:
    from mcp.server import Server
    from mcp.types import Tool, TextContent
    from mcp.server.stdio import stdio_server
except ImportError:
    print("Error: MCP SDK not installed. Run: pip install mcp", file=sys.stderr)
    sys.exit(1)


class TracMCPServer:
    """MCP Server for Trac XML-RPC API access."""

    TRAC_URL = "http://tl/trac/login/xmlrpc"

    def __init__(self):
        self.server = Server("trac-server")
        self.trac = None
        self._load_credentials()

    def _load_credentials(self):
        """Load credentials from OS keyring (no plaintext files)."""
        try:
            trac_url = os.environ.get("TRAC_URL", self.TRAC_URL)

            # Keyring config (shared across the team)
            service = os.environ.get("TRAC_KEYRING_SERVICE", "tl-engine-trac-mcp")
            acc_user = os.environ.get("TRAC_KEYRING_ACCOUNT_USER", "username")
            acc_pass = os.environ.get("TRAC_KEYRING_ACCOUNT_PASS", "password")

            username = keyring.get_password(service, acc_user)
            password = keyring.get_password(service, acc_pass)

            if not username or not password:
                print(
                    "Error: Missing Trac credentials in OS keyring.\n"
                    f"  service: {service}\n"
                    f"  username key: {acc_user}\n"
                    f"  password key: {acc_pass}\n"
                    "Run: ./scripts/setup-mcp-credentials.sh trac",
                    file=sys.stderr,
                )
                sys.exit(1)

            class BasicAuthTransport(xmlrpc.client.Transport):
                def send_headers(self, connection, headers):
                    auth = base64.b64encode(f"{username}:{password}".encode("utf-8")).decode("ascii")
                    connection.putheader("Authorization", f"Basic {auth}")
                    super().send_headers(connection, headers)

            self.trac = xmlrpc.client.ServerProxy(
                trac_url,
                transport=BasicAuthTransport(),
                allow_none=True,
            )

        except Exception as e:
            print(f"Error loading credentials from keyring: {e}", file=sys.stderr)
            sys.exit(1)

    def setup_handlers(self):
        """Setup MCP tool handlers."""

        @self.server.list_tools()
        async def list_tools():
            return [
                Tool(
                    name="get_ticket",
                    description="Get complete details of a Trac ticket by number (ID, summary, description, status, owner, reporter, priority, component, etc.)",
                    inputSchema={
                        "type": "object",
                        "properties": {
                            "ticket_id": {
                                "type": "number",
                                "description": "Ticket number (e.g., 29053)"
                            }
                        },
                        "required": ["ticket_id"]
                    }
                ),
                Tool(
                    name="search_tickets",
                    description="Search for Trac tickets using Trac query syntax (e.g., 'status=new&owner=username', 'milestone=7.10.0', 'component=BPE')",
                    inputSchema={
                        "type": "object",
                        "properties": {
                            "query": {
                                "type": "string",
                                "description": "Trac query string (e.g., 'status=new&milestone=7.10.0')"
                            },
                            "max_results": {
                                "type": "number",
                                "description": "Maximum number of results to return (default: 50)",
                                "default": 50
                            }
                        },
                        "required": ["query"]
                    }
                ),
                Tool(
                    name="get_ticket_changelog",
                    description="Get the complete change history for a ticket (all modifications, comments, and updates)",
                    inputSchema={
                        "type": "object",
                        "properties": {
                            "ticket_id": {
                                "type": "number",
                                "description": "Ticket number"
                            }
                        },
                        "required": ["ticket_id"]
                    }
                ),
                Tool(
                    name="create_ticket",
                    description="Create a new Trac ticket",
                    inputSchema={
                        "type": "object",
                        "properties": {
                            "summary": {
                                "type": "string",
                                "description": "Ticket summary/title"
                            },
                            "description": {
                                "type": "string",
                                "description": "Detailed ticket description"
                            },
                            "type": {
                                "type": "string",
                                "description": "Ticket type (defect, enhancement, task, etc.)",
                                "default": "defect"
                            },
                            "priority": {
                                "type": "string",
                                "description": "Priority (trivial, minor, major, critical, blocker)",
                                "default": "major"
                            },
                            "component": {
                                "type": "string",
                                "description": "Component name"
                            },
                            "milestone": {
                                "type": "string",
                                "description": "Target milestone"
                            }
                        },
                        "required": ["summary", "description"]
                    }
                ),
                Tool(
                    name="update_ticket",
                    description="Update an existing Trac ticket (change status, add comment, modify fields)",
                    inputSchema={
                        "type": "object",
                        "properties": {
                            "ticket_id": {
                                "type": "number",
                                "description": "Ticket number to update"
                            },
                            "comment": {
                                "type": "string",
                                "description": "Comment to add to the ticket"
                            },
                            "attributes": {
                                "type": "object",
                                "description": "Ticket attributes to update (e.g., {\"status\": \"closed\", \"resolution\": \"fixed\"})",
                                "additionalProperties": True
                            }
                        },
                        "required": ["ticket_id"]
                    }
                ),
                Tool(
                    name="get_milestones",
                    description="List all milestones in the Trac system",
                    inputSchema={"type": "object", "properties": {}}
                ),
                Tool(
                    name="get_components",
                    description="List all components in the Trac system",
                    inputSchema={"type": "object", "properties": {}}
                )
            ]

        @self.server.call_tool()
        async def call_tool(name: str, arguments: dict) -> list[TextContent]:
            try:
                if name == "get_ticket":
                    ticket_id = int(arguments["ticket_id"])
                    ticket = self.trac.ticket.get(ticket_id)
                    result = self._format_ticket(ticket)

                elif name == "search_tickets":
                    query = arguments["query"]
                    max_results = int(arguments.get("max_results", 50))
                    ticket_ids = self.trac.ticket.query(query)

                    if len(ticket_ids) > max_results:
                        ticket_ids = ticket_ids[:max_results]

                    tickets = []
                    for tid in ticket_ids:
                        ticket = self.trac.ticket.get(tid)
                        tickets.append(self._format_ticket(ticket))

                    result = f"Found {len(ticket_ids)} tickets:\n\n" + "\n\n".join(tickets)

                elif name == "get_ticket_changelog":
                    ticket_id = int(arguments["ticket_id"])
                    changelog = self.trac.ticket.changeLog(ticket_id)
                    result = self._format_changelog(ticket_id, changelog)

                elif name == "create_ticket":
                    summary = arguments["summary"]
                    description = arguments["description"]
                    attributes = {
                        "type": arguments.get("type", "defect"),
                        "priority": arguments.get("priority", "major")
                    }
                    if "component" in arguments:
                        attributes["component"] = arguments["component"]
                    if "milestone" in arguments:
                        attributes["milestone"] = arguments["milestone"]

                    ticket_id = self.trac.ticket.create(summary, description, attributes)
                    result = f"Created ticket #{ticket_id}: {summary}"

                elif name == "update_ticket":
                    ticket_id = int(arguments["ticket_id"])
                    comment = arguments.get("comment", "")
                    attributes = arguments.get("attributes", {})
                    self.trac.ticket.update(ticket_id, comment, attributes)
                    result = f"Updated ticket #{ticket_id}"

                elif name == "get_milestones":
                    milestones = self.trac.ticket.milestone.getAll()
                    result = "Milestones:\n" + "\n".join(f"- {m}" for m in milestones)

                elif name == "get_components":
                    components = self.trac.ticket.component.getAll()
                    result = "Components:\n" + "\n".join(f"- {c}" for c in components)

                else:
                    result = f"Unknown tool: {name}"

                return [TextContent(type="text", text=result)]

            except xmlrpc.client.Fault as e:
                return [TextContent(type="text", text=f"Trac XML-RPC Error: {e.faultString}")]
            except Exception as e:
                return [TextContent(type="text", text=f"Error: {str(e)}")]

    def _format_ticket(self, ticket: tuple) -> str:
        ticket_id, time_created, time_changed, attrs = ticket

        lines = [
            f"Ticket #{ticket_id}",
            f"Summary: {attrs.get('summary', 'N/A')}",
            f"Status: {attrs.get('status', 'N/A')}",
            f"Type: {attrs.get('type', 'N/A')}",
            f"Priority: {attrs.get('priority', 'N/A')}",
            f"Component: {attrs.get('component', 'N/A')}",
            f"Milestone: {attrs.get('milestone', 'N/A')}",
            f"Owner: {attrs.get('owner', 'N/A')}",
            f"Reporter: {attrs.get('reporter', 'N/A')}",
            f"Created: {time_created}",
            f"Modified: {time_changed}",
        ]

        if attrs.get("resolution"):
            lines.append(f"Resolution: {attrs['resolution']}")

        if attrs.get("description"):
            lines.append(f"\nDescription:\n{attrs['description']}")

        return "\n".join(lines)

    def _format_changelog(self, ticket_id: int, changelog: list) -> str:
        lines = [f"Changelog for Ticket #{ticket_id}:\n"]

        for entry in changelog:
            time, author, field, oldvalue, newvalue, permanent = entry
            lines.append(f"[{time}] {author}:")
            lines.append(f"  {field}: {oldvalue} -> {newvalue}")
            lines.append("")

        return "\n".join(lines)

    async def run(self):
        async with stdio_server() as (read_stream, write_stream):
            await self.server.run(
                read_stream,
                write_stream,
                self.server.create_initialization_options()
            )


def main():
    server = TracMCPServer()
    server.setup_handlers()
    asyncio.run(server.run())


if __name__ == "__main__":
    main()
