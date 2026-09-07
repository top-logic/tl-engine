# The MCP endpoint (`/mcp/*`): off until you switch it on, and closed until you configure a credential

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
by default, because publishing it is a deployment decision; enable the module
com.top_logic.ai.mcp.server.MCPServerService$Module to switch it on, and configure an
authentication for it - it serves no request it cannot attribute to an account. See
doc/security/mcp-endpoint.md and issue #261.
```

Off, and *legible*: an administrator who did not mean to disable the endpoint is told what to enable
rather than left with a bare 503.

## The second switch: nothing is served until a credential is configured

Enabling the module is not enough, and that is deliberate.

> **`/mcp/*` serves no request it cannot attribute to an account.** With the module on and no
> `<authentications>` configured, every request - resource read, tool call, `initialize` - is answered
> with **HTTP 503** and a message naming what to configure. There is no state in which the endpoint
> answers an anonymous caller.

`McpAuthentication` is where that decision lives. The OpenAPI server reads an operation that declares
no authentication as *"publish this"*, because a REST path is something an administrator wrote out by
hand. `/mcp/*` exists because the module is on the classpath, nobody wrote it out, and its resource
handlers read the KnowledgeBase - so the default here is the opposite one.

## Configuring who may reach it

The mechanism is not ours: `/mcp/*` reuses the same pluggable, `Person`-resolving HTTP authentication
framework the application's OpenAPI server uses, so there is one credential concept, one admin
surface and one set of secrets in the application. Configure it on `MCPServerService`:

```xml
<config service-class="com.top_logic.ai.mcp.server.MCPServerService">
    <instance>
        <authentications>
            <api-key-authentication domain="mcp" position="header" parameter-name="X-Api-Key"/>
        </authentications>
        <secrets>
            <api-key-server-secret domain="mcp"
                api-key="..."
                user-id="mcp-service"
                description="The MCP client of the reporting pipeline."/>
        </secrets>
    </instance>
</config>
```

Three schemes are available, and a caller may present any one of the configured ones:

| Scheme | Element | What it checks | When to use it |
|---|---|---|---|
| **API key** | `<api-key-authentication>` + `<api-key-server-secret>` | A key in a header, query parameter or cookie, mapped to one account | **The recommended default.** Revocable on its own, attributable to one account, and keeps a person's login out of a client configuration file. Every mainstream MCP client can send a static header |
| **HTTP basic** | `<basic-authentication in-user-context="true"/>` | The account's own password, against its `AuthenticationDevice` | A one-line `curl` smoke test, or a deployment with no key management. Puts a real password in a client config, so not the default |
| **OAuth bearer** | `<token-based-authentication>` + `<server-credentials>` | A bearer token, validated by RFC 7662 introspection against your authorization server | A deployment that already runs an OIDC provider. TopLogic issues no tokens of its own, so this needs one |

**Two rules about the secret:**

- **`user-id` is required in practice.** The framework reads an *empty* `user-id` as "process this
  request in system context" - the engine's super-user bypass. `/mcp/*` refuses that with **HTTP 403**
  rather than honouring it: this endpoint acts as the account it authenticated or it does not act.
- **Prefer a dedicated service account** over a real person's login, so that revoking MCP access does
  not mean disabling somebody's login.

A misconfigured authentication - a scheme with no matching secret, say - authenticates nobody rather
than being skipped, so a configuration mistake cannot widen access.

## What an authenticated caller can then do

Everything the endpoint serves is decided against the authenticated account:

- **Resource reads are filtered by that account's model access rights.** `toplogic://instance/{tid}`
  answers only for an object the account may read, and answers "not found" otherwise - saying "you may
  not read this one" would itself disclose that the object exists. An attribute the account may not
  read is left out and the object still renders. `toplogic://model/.../instances` filters the list
  *before* it counts it, so the total does not disclose a cardinality the account may not see. A
  configured `<dynamic-resources>` TL-Script is evaluated as the account and its result is filtered the
  same way. This is the same read-filtering chokepoint (`ReadFilter`) the data tools go through.
- **Tool calls are authorized against that account's roles**, by the same `<tool-access>` policy as the
  chat path.
- **The run is a background run.** An MCP caller has nobody who could answer an approval prompt, so
  the policy's `<background-allowlist>` is an additional ceiling on top of the account's grants - the
  configured substitute for the prompt. A capability the account holds but the allowlist withholds is
  refused, with a message naming the allowlist. See
  [tool-access-configuration.md](tool-access-configuration.md).
- **An MCP session belongs to the account that opened it.** The transport keys every request after
  `initialize` by the `mcp-session-id` header and keeps no owner of its own, so a second
  authenticated account presenting somebody else's session id is refused with **HTTP 403**.

### What is still open

- **`tools/list` is not filtered per caller.** The MCP Java SDK (1.1.4) answers it from the globally
  registered tool set without reference to the caller, so every *authenticated* caller sees every tool
  name, description and input schema; only execution is authorized. Closing this needs an
  exchange-aware `tools/list` hook the SDK does not offer.
- **No RFC 9728 protected-resource metadata.** Nothing serves
  `/.well-known/oauth-protected-resource`, and the bearer challenge carries no `resource_metadata`, so
  an OAuth client cannot discover this server automatically - it has to be configured. The MCP
  specification makes authorization optional, so this is not a violation; it is a missing convenience.
- **The SDK's `securityValidator` is unset.** That is `Origin`/`Host` checking against DNS rebinding,
  which is independent of authentication and not yet configurable here.
- **An aggregate a TL-Script computes is not filtered.** `all(`X`).size()` is a number by the time the
  read filter sees it (issue #291). An administrator who needs a count hidden denies the `script`
  capability.

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

Configure at least one authentication and its secret as above, and restart. The boot log then states
which schemes the endpoint accepts, at `INFO` on `MCPServerService`. With the module on and no
authentication configured, the same line is a `WARN` saying that every request is refused.

**Still restrict who can reach the path**, at the network or reverse-proxy level, on any deployment
where that is cheap. Authentication makes the endpoint safe to expose; it does not make an exposed
port a good idea.

The service's own settings (`server-name`, `server-version`, `keep-alive-interval`,
`<dynamic-resources>`) are documented on `com.top_logic.ai.mcp.server.MCPServerService.Config`.

## Where this is tested

- `tl-ai-mcp-server`: `TestMcpAuthentication` (nothing configured serves nobody; a scheme's own 401
  challenge), `TestMcpAuthenticationFilter` (what is refused, and that a refused request never reaches
  the protocol), `TestMcpSessionPrincipals` (session ownership).
- `tl-ai-demo`: `SecDemoMcpEndpointTest`, against the security test bed's real accounts and real
  access rights - a resource read filtered per object, an attribute left out, a listing counted after
  filtering, an API key resolved to its account, a tool call authorized and one refused by the
  background allowlist.
