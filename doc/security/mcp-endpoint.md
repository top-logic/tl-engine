# The MCP endpoint (`/mcp/*`) is off until you switch it on

**Audience:** whoever deploys or administers a TopLogic application that has `tl-ai-mcp-server` on its
classpath.
**Companion:** [agent-security-architecture.md](agent-security-architecture.md) section 3.7 is the
design; this page is what you have to know before you turn the endpoint on.

---

## The state you get

`tl-ai-mcp-server` ships with its own service module switched **off**:

```xml
<!-- tl-ai-mcp-server/src/main/webapp/WEB-INF/conf/tl-ai-mcp-server.conf.config.xml -->
<config service-class="com.top_logic.basic.module.ModuleSystem">
    <instance>
        <modules>
            <module key="com.top_logic.ai.mcp.server.MCPServerService$Module" value="false"/>
        </modules>
    </instance>
</config>
```

The servlet mapping is static - `MCPTransportServlet` is bound to `/mcp/*` by this module's
`web-fragment.xml` whether the service runs or not - so the path exists but serves nothing. Every
request to it, of any method, is answered with **HTTP 503** and a message naming the switch:

```
HTTP ERROR 503 MCP server not available: MCPServerService is not started. This endpoint is off
by default, because it authenticates nobody (issue #261); enable the module
com.top_logic.ai.mcp.server.MCPServerService$Module to switch it on.
```

Off, and *legible*: an administrator who did not mean to disable the endpoint is told what to enable
rather than left with a bare 503.

## Why it is off

> **The endpoint authenticates nobody.** Turning it on publishes the application's model **and its
> business data** to everyone who can reach the port. There is no setting that adds authentication;
> that is issue **#261**, and it is not built yet.

Concretely, with the service running:

- `MCPTransportServlet` is registered with **no filter and no `<security-constraint>`**, and the MCP
  SDK's `securityValidator` is the default that validates nothing. Whoever reaches the port speaks the
  protocol.
- Every resource handler reads the KnowledgeBase inside
  `ThreadContextManager.inSystemInteraction(...)` - the engine's **super-user bypass**. Model access
  rights do not apply to those reads, by construction, and the caller nobody authenticated gets the
  answer.
  - `InstanceResource` (`toplogic://instance/{tid}`) returns **every attribute of any object** whose
    TID is guessed or observed.
  - `TypeInstancesResource` enumerates **every instance of any type**.
  - `ModuleTypesResource`, `TypePartsResource`, `EnumerationClassifiersResource`,
    `TypeUsagesResource`, `ConfigSchemaResource` and any configured
    `<dynamic-resources>` disclose the application model and whatever their TL-Script computes.
- `tools/list` discloses **every registered tool** - name, description and full input schema.

The one thing that stays closed is tool **execution**: `McpToolAdapter` refuses every tool call
unconditionally, in every deployment, whatever `<tool-access>` says, because an unauthenticated caller
has no account to be authorized as. `McpToolContexts.refusalReason()` is where that refusal is worded.
This is *not* a policy decision an application can reverse, and it does not cover the resource surface
described above - the resource surface was never governed by tool-access policy at all.

## Switching it on

Add the module override to any application configuration file - an aspect, or your
`WEB-INF/conf/<app>.config.xml`:

```xml
<config service-class="com.top_logic.basic.module.ModuleSystem">
    <instance>
        <modules>
            <module key="com.top_logic.ai.mcp.server.MCPServerService$Module" value="true"/>
        </modules>
    </instance>
</config>
```

Restart the application. The boot log then carries a warning that repeats the exposure above, at
`WARN` on `MCPServerService`, at every start - it is a property of the running deployment, not a
one-time note.

**Before you do this on a reachable network**, do the thing the application cannot do for you:
restrict who can reach `/mcp/*`, at the network or reverse-proxy level - bind the connector to
localhost, or put the path behind a proxy that authenticates and authorizes. An endpoint reachable from
an untrusted network is an unauthenticated, unfiltered read of your business data with model access
rights bypassed. Treat the switch as "publish this application's data to this network segment", because
that is what it does.

The service's own settings (`server-name`, `server-version`, `keep-alive-interval`,
`<dynamic-resources>`) are unchanged by this and are documented on
`com.top_logic.ai.mcp.server.MCPServerService.Config`.

## What changes when #261 lands

`/mcp/*` gets authentication and resolves its caller to a `Person`; the adapter builds a real
`ToolContext` for that account, tool execution stops being refused and becomes authorized by the same
policy as the chat path, and the resource surface goes through the same read filter as the data tools.
At that point the default can be revisited. Until then, off is the only honest default.
