---
description: Read before letting an agent outside the browser drive a running session of the React view UI - the module tl-layout-view-agent, the HTTP endpoint /agent-api (AgentServlet - windows, observe, act, navigate, record, replay), access tokens and invitations (AccessTokens, issue-access-token, revoke-access-token, the Agent access dialog and administration tab), the AgentAccess service with its AgentAuthenticator plug-in, and what an admitted agent may do.
order: 115
---

# Agent access to a running session

The optional module `tl-layout-view-agent` (`com.top_logic.layout.view.agent`) publishes the [headless interface](doc:view-layer/headless-interface) of a running session over HTTP, and admits an agent outside the browser to it with an access token the user issues from that session. An application that does not depend on the module has neither the endpoint nor the tokens.

## The endpoint `/agent-api`

`AgentServlet` is mapped to `/agent-api/*` (in the module's `web-fragment.xml`). Requests and answers are JSON; a window is named by its window name, as `/windows` lists them.

| Request | Answer |
|---|---|
| `GET /windows` | `{"windows": [...]}`, the windows of the session |
| `GET /observe?windowName=W` | the observation of the window (`ScriptingSession.observeJson()`); with `&mode=actions` the flat list of actionable nodes |
| `POST /act` `{"windowName", "address", "command", "arguments"}` | `{"success", "error"?, "observation"}` - the command dispatched to the addressed control, and the settled observation afterwards. `args` is accepted for `arguments`. |
| `POST /navigate` `{"windowName", "url"}` | `{"success", "url", "message"?, "observation"}` - the window's router taken to a route URL |
| `POST /record/start`, `/record/stop` `{"windowName"}` | `{"recording", "steps"}` |
| `GET /record/steps?windowName=W` | `{"recording", "steps"}` |
| `POST /record/assert` `{"windowName", "address", "expect"?}` | the recorder state with an assertion step appended: the `expect`ed entries, or the node's whole current state |
| `POST /replay` `{"windowName", "steps"}` | `{"success", "results", "observation"}` |

A step reads `{"address", "command", "arguments", "description"}`; `/replay` takes the steps in the form `/record/steps` returns them (the `description` is ignored), so a recording is replayed by posting its steps back. An assertion step (`assertState`) is verified, every other step dispatched. Replay stops at the first step that fails - later steps assume the state it should have produced - and `results` ends with that step, carrying its `error` or, for an assertion, its `mismatches` (`key`, `expected`, `actual`). Errors are answered with `{"error": ...}`: an unknown path with 404, a missing parameter or an unknown window with 404 on a `GET` and with 400 on a `POST`, a malformed body with 400.

Each request that reads or changes a window runs like a browser command: in that window's subsession, within an interaction of the session. After a command the model events are synthesized, so the observation in the answer is settled and the caller never waits for asynchronous delivery.

`/navigate` reports `success: false` with a `message` when the router did not reach the URL: a route URL resolves only against the routing participants already displayed, so a route into an area that is not loaded yet stops at its first segment. Such an area is activated by its sidebar item (`selectItem`) first. A navigation refused because a form holds unsaved changes is reported the same way.

## Admission

A browser reaches the endpoint with its session cookie and acts as the logged-in user.

An agent outside the browser has no cookie. It presents a credential as `Authorization: Bearer <credential>`. The `AgentAccess` service asks its configured `AgentAuthenticator`s in turn; the first that recognizes the credential yields an `AgentCredential`: the account, whether it may act, and the key of the session it was admitted to. The request then runs **in that session** - the session the user issued the credential from - with that session's user and permissions. The agent drives the user's own windows; what it does appears in the user's browser.

- A credential that may only observe is refused (403) on every `POST`: it reads `/windows`, `/observe` and `/record/steps` only.
- An agent cannot outlive the session it was admitted to: `AgentAccess` registers the session under the credential's key when the credential is issued and drops the registration when the session ends. A credential of an ended session admits nobody (401).
- With no authenticator configured, only browsers reach the endpoint.

The module configures the token authenticator:

```xml
<config service-class="com.top_logic.layout.view.agent.AgentAccess">
	<instance class="com.top_logic.layout.view.agent.AgentAccess">
		<authenticators>
			<access-token/>
		</authenticators>
	</instance>
</config>
```

Another kind of credential is added by implementing `AgentAuthenticator` (with a `Config` extending `AgentAuthenticator.Config`) and listing it under `authenticators`. It returns `null` for a credential it does not recognize - an expired, revoked or forged one included - and binds the session through `AgentAccess.bindSession(key, session)` when it issues one.

## Access tokens

An access token (`tl.agent:AccessToken`, model `tl.agent`) belongs to the account that issued it and admits an agent to the session it was issued from.

- **Issuing.** The development menu of the app bar has *Agent access* in its `automation` group (overlay `dev-menu.view.xml` of the module), opening `agent-access.view.xml`: the account's own tokens, *New token* and *Withdraw*. *New token* asks for a label, the validity in hours and whether the agent may act (`tl.agent:NewAccessToken`, transient); `<issue-access-token/>` (`IssueAccessTokenAction`) creates the token and returns the **invitation**, which `agent-access-token.view.xml` shows once.
- **The invitation** is the application's agent endpoint and the token in one line, `<host><context>/agent-api tlagt_<id>_<secret>`, so an agent gets both by one copy. The host is the public address (`AliasManager.HOST` where the application configures it, because the address seen inside a request may be a load balancer's).
- **The secret** is stored as a SHA-256 hash only (`secretHash`); the id part (`tokenId`) finds the token without comparing against every hash. The plain secret exists in the invitation and nowhere else: a lost token is withdrawn and issued anew, never recovered - not even by an administrator.
- **Use.** Each admission sets `lastUsed` on the token (in a commit of its own), so the owner sees that an agent uses it.
- **Withdrawing** (`<revoke-access-token/>`, `RevokeAccessTokenAction`) deletes the token; the session's registration is dropped once no valid token refers to it any more. An account withdraws its own tokens; the administration (Administration → *Agent access*, contributed as a section of `admin/admin.view.xml` by the module's overlay, view `admin/system/agent-tokens.view.xml`) lists the tokens of all accounts with the same withdraw command.
- **Expiry.** An expired token admits nobody and leaves the lists. The scheduler task `AccessTokenCleanup` (a `ScriptTask`, daily at 03:17) deletes expired tokens.

## Pitfalls

- **One session, two drivers.** The agent and the user act on the same windows. A command of the agent waits while a browser command of the session runs, and the other way round; the agent sees what the user changed only in its next observation.
- **Observing is not free.** An observation projects the whole tree of the window, within the session's interaction. Poll with `mode=actions` and only as often as needed.
- **Window names.** A window name is a browser tab's, valid as long as that tab's window lives. Read `/windows` again when a request answers *Unknown window*.
- **Acting is acting as the user.** The endpoint adds no permission of its own beyond observe versus act: every command runs the handler the browser's command runs, with that handler's checks.
