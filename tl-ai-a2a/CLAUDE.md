# tl-ai-a2a

The A2A v1.0 endpoint and the client tools - see `README.md` for what an application configures.

## Layout

- `protocol/` - the wire format as JSON trees (`Json`, `A2AJson`), task states, error codes, artifact
  references and sections. No KnowledgeBase access.
- `server/` - the JSON-RPC binding and the task lifecycle (`A2ARequestHandler`, `A2ATask`), testable
  against a scripted `TaskBackend`; `AgentRunBackend` is the tl-ai side (runs, sessions, `askUser` as
  `INPUT_REQUIRED`, persistence in the session metadata).
- `client/` - `A2AClient` (HTTP + SSE), `RemoteExchange` (one ask/reply/follow), `A2AClientTools` (the
  tools).

## Rules

- Keep `protocol/`, `A2ARequestHandler`, `A2AClient` and `RemoteExchange` free of KnowledgeBase access, so
  their tests need no booted application. Everything that touches the KnowledgeBase runs on an agent
  run's thread in the caller's `TLContext`.
- Authentication is tl-ai-core's `EndpointAuthentication`, the same as `/mcp/*`; this module never
  depends on `tl-ai-mcp-server`.
- Nothing application-specific here: an application adds its behaviour through the hooks
  (`A2ASubjectResolver`, `A2AArtifactSource`, `A2AArtifactContributor`, `A2ADataHandler`).
