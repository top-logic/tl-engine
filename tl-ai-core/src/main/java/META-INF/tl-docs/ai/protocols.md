---
description: Read before changing how agents talk to other systems - the MCP client (McpServerRegistry, McpTool, mcp__{server}__{tool}), the MCP server of tl-ai-mcp-server (MCPServerService, McpToolAdapter, toplogic:// resources, <exposed-tools>, <dynamic-resources>), the endpoint authentication shared by /mcp/* and /a2a/* (EndpointAuthentication, personal access tokens), and A2A (A2AService, /a2a/{agent}, the a2a.* client tools, A2AObserver).
order: 65
---

# MCP and A2A

## MCP client

`McpServerRegistry` (package `com.top_logic.tl.ai.core.mcp`, a service of `tl-ai-core`) manages the connections to external MCP servers over stdio or HTTP. `McpTool` wraps a remote tool under the sanitized name `mcp__{server}__{tool}`; an agent definition references MCP servers by name, and their tools become part of the agent's frozen tool base. Which servers a role may reach is decided by `<mcp-access>` ([run access](doc:ai/security/run-access)).

A remote server is untrusted. Its tools cannot be introspected, so every one is `ToolMode.EXTERNAL` - read and write alike, whatever hints the server sends, because a server could otherwise obtain auto-approval for itself by claiming `readOnlyHint: true`. A remote tool declares no group and is denied until an administrator names it in `<tool-groups>`, typically in `egress`; its answer is marked as untrusted data.

## MCP server

`tl-ai-mcp-server` (package `com.top_logic.ai.mcp.server`) serves the application to external AI clients at `/mcp/*`, through `MCPTransportServlet` and `MCPServerService`:

- **Resources** - the application's model and objects: `toplogic://instance/{tid}`, `toplogic://model/modules/...`, the instances of a type, the parts of a type, enumeration classifiers, type usages, configuration schemas (packages `resources/`, `completions/`), plus resource templates an application defines by TL-Script in `<dynamic-resources>` (package `dynamic/`).
- **Tools** - the registry tools `<exposed-tools>` lists, none by default, published through `McpToolAdapter` with a title, a description written for an external model, and MCP `annotations` derived from the tool's mode and group (`McpToolAnnotations`).
- **`<instructions>`**, sent in the answer to `initialize`, and an `allowed-origins` / `allowed-hosts` check against DNS rebinding.

The module ships switched off (`MCPServerService$Module` is `false`). Every request is authenticated to an account and runs in that account's context: resource reads go through the `ReadFilter` chokepoint, tool calls through the same `ToolPermissionFilter` as the chat, and an MCP run is a background run bounded by the `<background-allowlist>`. [The MCP endpoint](doc:ai/security/mcp-endpoint) is the administrator's guide.

## Endpoint authentication

`EndpointAuthentication` (package `com.top_logic.tl.ai.core.access`) builds the authenticator of `/mcp/*` and `/a2a/*` from a service's `<authentications>`. It reuses the OpenAPI server's `Authenticator` framework (HTTP basic, API key, OAuth bearer), and configuring nothing means every request is answered with 503 - neither endpoint ever serves an anonymous caller, and a credential mapped to no account is refused instead of being run in system context. The personal access tokens (`tl.ai.access:AccessToken`, `<personal-access-token-authentication>`, the *Access tokens* settings tab) live there too, so both endpoints accept them; [personal access tokens](doc:ai/security/mcp-endpoint#personal-access-tokens) describes them.

## A2A

`tl-ai-a2a` (package `com.top_logic.ai.a2a`) implements the A2A protocol over JSON-RPC: `A2AService` serves the agents an application lists in `<agents>` at `/a2a/{agent}` (agent card, `SendMessage`, streaming), and gives local agents the client tools `a2a.*` to talk to the agents of other systems. It is closed until an application configures `<agents>` and `<authentications>`.

- One A2A `contextId` is one `AgentSession`, one `taskId` one recorded run acting as the account the caller's credential names (`RunEntryPoint.A2A`).
- The calling agent is the run's user: `askUser` parks the run as in a chat, and the task goes to `INPUT_REQUIRED`; the caller's reply completes the parked question, so the same run resumes.
- Remote answers reach a local model inside `UntrustedEnvelope.A2A_RESULT`, cut short, with artifacts as `tl-artifact://<app>/<kind>/<id>` references that `fetchArtifact` reads section by section.
- A served context lives in its session's metadata and run transcripts and survives a restart.
- An `A2AObserver` (`<observers>` on `A2AService`) learns of every exchange in both directions with context, task and message ids, through the same `ObserverQueue` as the run observers.
- *Administration > A2A* (`tl-ai-a2a/admin/a2a.view.xml`) lists the conversations of both directions from persisted data: served contexts from their sessions, outgoing calls from the run transcripts.

`tl-ai-a2a/README.md` describes the configuration.
