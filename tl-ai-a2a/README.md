# tl-ai-a2a

Connects tl-ai agents over the [A2A protocol v1.0](https://a2a-protocol.org/latest/specification/)
(JSON-RPC binding):

- **Server**: exposes configured agents as A2A agents - agent card, `SendMessage`,
  `SendStreamingMessage`, `GetTask`, `CancelTask`, `ListTasks`, `SubscribeToTask`.
- **Client**: tools with which a local agent asks remote A2A agents.

Both are closed until an application configures them. `tl-ai-demo` carries a ready aspect,
`a2a-demo.config.xml`, switched off in its `metaConf.txt`.

## Configuration

```xml
<config service-class="com.top_logic.ai.a2a.A2AService">
	<instance class="com.top_logic.ai.a2a.A2AService"
		app="my-app"
		public-url="https://example.com/my-app"
	>
		<!-- Who may call: the schemes of /mcp/*. A run acts as the account the credential names. -->
		<authentications>
			<personal-access-token-authentication domain="tokens"/>
		</authentications>
		<!-- What this application exposes at /a2a/{path}. -->
		<agents>
			<agent path="assistant" agent="TL_SingleAgent" display-name="Assistant" allow-writes="false"
				description="Answers questions about this application.">
				<skills>
					<skill id="answer" name="Answer questions" description="..." tags="questions"/>
				</skills>
			</agent>
		</agents>
		<!-- Whom local agents may call. -->
		<remote-agents>
			<remote-agent name="other" card-url="https://example.com/other/a2a/agent/agent-card.json"
				token-env="OTHER_A2A_TOKEN"/>
		</remote-agents>
	</instance>
</config>
```

| Setting | Meaning |
|---|---|
| `app` | The application's name in artifact references and in the metadata it sends. Default: the context path. |
| `public-url` | Base URL in the agent cards. Default: taken from the request. |
| `max-blocking-seconds` | Longest wait of a blocking `SendMessage` and of `askAgent` (default 1500). |
| `<authentications>`, `<secrets>` | As on `MCPServerService` (the developer article `ai/security/mcp-endpoint`). A calling application uses a personal access token of the account its requests should act as. Nothing configured: every call is answered 503. |
| `<agent>` | `path` is the URL name, `agent` the tl-ai agent, `model` an optional override. `allow-writes="true"` lets A2A runs use the tools that change the application, without approval; otherwise those tools are not offered. The first agent also answers `/.well-known/agent-card.json`. |
| `<remote-agent>` | `card-url` of the remote agent; `token-env` names the environment variable (or system property) whose value is sent as `Authorization: Bearer`. |

Give an agent the client tools by adding `a2a.listAgents`, `a2a.askAgent`, `a2a.replyAgent`,
`a2a.getAgentTask`, `a2a.listAgentConversations` and `a2a.fetchArtifact` to its tool list.

## What a call does

- A **context** is one conversation: an `AgentSession` (kind `BACKGROUND`) owned by the calling account.
  Its state lives in the session's metadata, its runs keep their transcripts, so a follow-up continues it,
  also after a restart. A task that was running when the application stopped reads `FAILED`.
- A **task** is one recorded agent run; its metadata carries the `runId`. The run acts as the calling
  account (`RunEntryPoint.A2A`).
- The calling agent is the run's user: `askUser` moves the task to `INPUT_REQUIRED`, and the next message
  to the task (or its context) answers the question - the same run resumes.
- The final answer is the `COMPLETED` status message and the artifact `answer`.
- One context runs one task at a time; a message to a context whose task is working is refused (`-32004`).

## Client tools

| Tool | Mode | What it does |
|---|---|---|
| `listAgents()` | READ | The remote agents, from their cards. |
| `askAgent(agent, message, conversation?, topic?, refs?, data?)` | EXTERNAL | Sends the message, shows the remote progress, waits until the task ends or asks back (up to `max-blocking-seconds`). Returns the state, the `taskId`, the answer cut at 4000 characters with its reference, the question, and the artifacts as references only. `conversation`: `continue` (default), `new` (with a `topic`), or the topic of an open one. |
| `replyAgent(taskId, message, agent?)` | EXTERNAL | Answers a remote question and waits again. |
| `getAgentTask(taskId, agent?, waitSeconds?)` | READ | Waits for a task that outlived the wait; `waitSeconds=0` only reads. |
| `listAgentConversations()` | READ | The remote conversations of this conversation. |
| `fetchArtifact(ref, section?)` | READ | An artifact, or one section of it, by reference; at most 30000 characters. |

Remote content reaches the model inside `UntrustedEnvelope.A2A_RESULT`. A conversation that is itself
an incoming A2A context calls back in that context, and the caller's conversation subject travels as
`metadata.tlAi = {app, agent, conversationId, subject}`.

## Artifacts by reference

A reference reads `tl-artifact://<app>/<kind>/<id>[@version]`. Every artifact of a task is published as
`tl-artifact://<app>/task/<taskId>/<name>`; an application publishes its own kinds with an
`A2AArtifactSource`. `GET {ctx}/a2a-artifacts?ref=...&section=...` serves them with the same credential
as the JSON-RPC calls; the agent card names that URL in the extension
`https://top-logic.com/a2a/extensions/artifacts/v1`. A section is a Markdown heading or a JSON member path.

## Hooks

Configured on `A2AService`, each a list asked in order:

| Element | Interface | Asked |
|---|---|---|
| `<subject-resolvers>` | `A2ASubjectResolver` | once per incoming context: the subject of its session |
| `<artifact-sources>` | `A2AArtifactSource` | for a reference of this application: the artifact |
| `<artifact-contributors>` | `A2AArtifactContributor` | after a completed task: more artifacts |
| `<data-handlers>` | `A2ADataHandler` | for a data part of an incoming message: the text the agent reads instead |
| `<observers>` | `A2AObserver` | for every step of a conversation in either direction: message, state change, artifact |

Each runs in the `TLContext` of the calling account; one that throws is logged and skipped. An observer
is the exception: it runs on a thread of its own, behind a bounded queue, so it can neither slow down nor
break a conversation. Its `A2AExchange` carries the context id, the task id and the message id - the same
on both sides of a conversation - and the local run id, which a `RunObserver` of tl-ai-core reports. An
observer sees every caller's messages; one that passes them on outside the application must authenticate
that itself.

## Limits

No push notifications, no extended agent card, no file upload parts. The client's topics of remote
conversations are kept in memory. A remote agent must support streaming.
