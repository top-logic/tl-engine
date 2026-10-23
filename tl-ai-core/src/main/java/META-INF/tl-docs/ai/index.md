---
description: Read before adding AI agents to a TopLogic application or working on the AI modules (tl-ai-core, tl-ai-tool-api, tl-chat, tl-ai-mcp-server, tl-ai-a2a, tl-ai-views, tl-ai-vector-index, tl-ai-index, tl-ai-eval) - which module provides what, the provider API keys (tl_openai_api_key, tl_anthropic_api_key, ...), LLMProviderService in tl-ai-core.conf.config.xml, and where each topic of the chapter is.
order: 50
---

# AI agents

The AI modules make a TopLogic application accessible to AI agents. An agent can understand the application - its model, its configuration, its data - work with it and configure it, much like an experienced consultant who knows the application. The same set of tools serves three forms of use: a copilot inside the application (the chat), external AI clients connecting over MCP or A2A, and agents that the application runs on its own, on a schedule or on an event. The [forms of use](doc:ai/forms-of-use) article describes them and their interplay.

The core of the modules is a purpose-built streaming agent loop with multi-provider LLM access (Anthropic, OpenAI, Mistral, xAI, Amazon Bedrock and OpenAI-compatible endpoints), a tool system based on annotated Java methods and TL-Script, skills, multi-agent delegation, memory and compaction, and a layered security architecture that makes every run act as exactly one authenticated account.

## Modules

All modules are engine modules with the group id `com.top-logic` and the engine version (`${tl.version}`). An application gets a feature by depending on the module named in the last column; the dependencies of that module come with it.

| Module | Role | An application depends on it to get |
|---|---|---|
| `tl-ai-tool-api` | Shared tool contracts: `Tool`, `ToolResult`, `ToolMode`, `@ToolMethod`, `@P`, `ToolSchema`, `ToolGroups`, the `UntrustedEnvelope` and the service-level `ToolRegistryService`. No implementations. | the ability to define tools in a module that does not depend on the agent framework |
| `tl-ai-core` | The agent framework: the agent loop, the LLM providers, the tool dispatcher and the shipped tools, the MCP client, delegation and subagents, memory and compaction, the security layers, the TL-Script functions (`aiAgentChat`, `aiCallTool`, ...). | agents run from TL-Script, from the scheduler or from code; the base of every other AI module |
| `tl-ai-mcp-server` | Model Context Protocol server at `/mcp/*`: the application's model and objects as MCP resources, and the tools an administrator publishes. Switched off by default, see [the MCP endpoint](doc:ai/security/mcp-endpoint). | external AI clients (Claude Code, Claude Desktop, Cursor) working with the application |
| `tl-ai-a2a` | The A2A protocol (JSON-RPC): configured agents served at `/a2a/{agent}`, and the client tools `a2a.*` for local agents. Closed until an application configures `<agents>` and `<authentications>`. | agents of other systems talking to this application's agents and vice versa |
| `tl-chat` | The agentic chat: REST API and SSE streaming (`/chat/*`), the React chat front end, chat sessions, human-in-the-loop approval, the view elements `<chat-conversation>`, `<chat-rail>`, `<chat-dock>`. | the built-in assistant in React (`.view.xml`) pages |
| `tl-chat-classic` | The React chat in a classic LayoutServlet application: a `LayoutComponent` that writes a React view into a classic tab. | the assistant tab in a classic layout application |
| `tl-ai-views` | Living views: agents build and change the application's `.view.xml` views, checked by the engine before anything is written, recorded as change sets the agent can undo. | agents that design and adapt React views |
| `tl-ai-vector-index` | Semantic search through embedding models and vector stores (Qdrant, JVector), with a background indexing daemon. | the `searchVectorIndex` tool and the TL-Script functions with the prefix `vectorIndex` (`vectorIndexSearch`, ...) |
| `tl-ai-index` | Hybrid search: words (BM25, German and English) and meaning (vectors) in one Lucene index of its own, kept in step with the knowledge base, filtered by the reader's rights inside the query. Off until the application switches its module on. | the tools `index.search` / `index.similar` and the TL-Script functions `aiIndexSearch` / `aiIndexSimilar` |
| `tl-ai-index-onnx` | An in-process embedder for `tl-ai-index`: ONNX Runtime and the Hugging Face tokenizer on the CPU, no embedding server needed. A module of its own because of its native libraries. | hybrid search without an external embedding service |
| `tl-ai-eval` | The model eval harness: the same business tasks put to several models through the agent loop, graded against the database they leave behind. | nothing - it is a test harness, see [testing](doc:ai/testing#the-model-eval) |
| `tl-ai-demo` | The demo application of the AI modules: chat, MCP server, A2A, living views, hybrid search and the security test bed. | nothing - it is an application |

The module dependency order is `tl-ai-tool-api` → `tl-ai-core` → `tl-ai-mcp-server` / `tl-ai-a2a` / `tl-chat` / `tl-ai-vector-index` / `tl-ai-index` / `tl-ai-eval`; `tl-chat` → `tl-chat-classic`; `tl-ai-index` → `tl-ai-index-onnx`; `tl-ai-core` → `tl-ai-views`; `tl-ai-demo` depends on all of them but `tl-ai-eval`. The modules are built with the engine, e.g. `mvn install -pl tl-ai-core` from the engine root.

## Configuring the LLM providers

The providers are configured on `LLMProviderService` (service editor: *LLM providers*). `tl-ai-core` ships `tl-ai-core.conf.config.xml` with the four services an agent run reads its configuration from and an empty provider list; an application names its providers in its own configuration, typically in `WEB-INF/autoconf/com.top_logic.tl.ai.core.provider.LLMProviderService.config.xml`:

```xml
<config service-class="com.top_logic.tl.ai.core.provider.LLMProviderService" config:override="true">
	<instance class="com.top_logic.tl.ai.core.provider.LLMProviderService">
		<providers>
			<provider class="com.top_logic.tl.ai.core.provider.anthropic.AnthropicProviderConfig"
				name="anthropic"
				models="claude-haiku-4-5-20251001,claude-sonnet-4-6"
			>
				<api-key name="tl_anthropic_api_key"/>
			</provider>
			<provider class="com.top_logic.tl.ai.core.provider.openai.OpenAIProviderConfig" name="openai">
				<api-key name="tl_openai_api_key"/>
			</provider>
		</providers>
		<model-catalog/>
	</instance>
</config>
```

The API keys come from environment variables: an `<api-key>` element without `class` attribute is an `EnvironmentSecretSource`, which reads the variable it names (a system property or an environment variable) at each request, and the variable holds the plain key:

```bash
export tl_openai_api_key="sk-..."
export tl_anthropic_api_key="sk-ant-..."
```

This is the recommended form, because the configuration then holds the variable name, not the key: a key resolved from an alias such as `api-key="%ANTHROPIC_API_KEY%"` is part of the effective configuration, and the Service Editor writes it into the file it stores. A variable that is not set does not stop the application; only a request of that provider fails. The chapter [secrets and credentials](doc:credentials) describes all forms a key can take and where it can come from.

The variables `tl_openai_api_key`, `tl_anthropic_api_key`, `tl_mistral_api_key` and `tl_xai_api_key` therefore hold the plain key. `tl-ai-core.conf.xml` still defines the aliases `%OPENAI_API_KEY%`, `%ANTHROPIC_API_KEY%`, `%MISTRAL_API_KEY%` and `%XAI_API_KEY%` for them, for applications whose configuration references the aliases: an alias is read as an encrypted value, so such an application either writes `api-key="unencrypted:%ANTHROPIC_API_KEY%"` (the variable holding the plain key) or, better, replaces the alias by the `<api-key>` element above ([migration](doc:credentials/migration)).

A key given directly in the configuration is an `api-key` attribute. It is an encrypted configuration value, so a key in plain text carries the prefix `unencrypted:` - with no space after the colon, which would become part of the key: `api-key="unencrypted:sk-..."`.

An `api-key` is a shorthand: the provider sends the key in the header its API expects (`x-api-key` for Anthropic, `api-key` for Azure OpenAI with `auth-scheme="API_KEY"`, otherwise `Authorization: Bearer`). The attribute form is a literal key; an `<api-key>` element selects a secret source (`SecretSource`), read at each request, like the variable above or a mounted secret file:

```xml
<provider class="com.top_logic.tl.ai.core.provider.openai.OpenAIProviderConfig" name="openai">
	<api-key class="com.top_logic.basic.credential.FileSecretSource" path="/run/secrets/openai-api-key"/>
</provider>
```

Instead of a key, a provider takes `<credentials>` (a `CredentialProvider`, see [credential providers](doc:credentials/credential-providers)), so that the application owns no long-lived secret; a provider that configures both does not start. A provider with neither sends its requests without credentials, e.g. to a gateway that adds them.

In development, the requests go through the central [credential gateway](doc:credentials/gateway), which checks the token of the developer's device login and adds the provider key of the route (here `/anthropic/`). The OAuth credential providers come from the module `tl-credential-oauth`:

```xml
<provider class="com.top_logic.tl.ai.core.provider.anthropic.AnthropicProviderConfig"
	name="anthropic"
	base-url="https://llm-gateway.example.com/anthropic"
	models="claude-sonnet-4-6"
>
	<credentials class="com.top_logic.credential.oauth.DeviceLoginProvider"
		issuer="https://login.example.com/realms/my-realm"
		client-id="dev-gateway"
		scope="openid offline_access"
	>
		<refresh-token-store class="com.top_logic.credential.oauth.FileRefreshTokenStore"/>
	</credentials>
</provider>
```

In production, the application authenticates itself at the same gateway with the client credentials grant, proving its identity with its Kubernetes service account token:

```xml
<provider class="com.top_logic.tl.ai.core.provider.openai.OpenAIProviderConfig"
	name="openai"
	base-url="https://llm-gateway.example.com/openai"
>
	<credentials class="com.top_logic.credential.oauth.ClientCredentialsProvider"
		issuer="https://login.example.com/realms/my-realm"
		client-id="my-app"
	>
		<client-authentication class="com.top_logic.credential.oauth.ClientAssertionAuthentication">
			<assertion class="com.top_logic.basic.credential.FileSecretSource" path="/var/run/secrets/tokens/keycloak"/>
		</client-authentication>
	</credentials>
</provider>
```

On Azure, Azure OpenAI accepts the token of the managed identity of the application (module `tl-credential-azure`):

```xml
<provider class="com.top_logic.tl.ai.core.provider.openai.AzureOpenAIProviderConfig"
	name="azure-openai"
	base-url="https://my-resource.openai.azure.com"
	models="gpt-5.1"
>
	<credentials class="com.top_logic.credential.azure.ManagedIdentityCredentials"
		resource="https://cognitiveservices.azure.com"
	/>
</provider>
```

A request rejected with HTTP 401 is repeated once with fresh credentials, so that an access token revoked before its announced end does not fail the run.

Which model an agent runs on (`default-model`, `<agent-overrides>`), the turn limit and the chat entry point are set on `AgentRuntimeService`; where a run goes when its model cannot answer is the `<failover>` section of `LLMProviderService`. [Providers and failover](doc:ai/providers) describes the four configuration services and the provider system.

The other HTTP services of the agents authenticate the same way. The search engines of `webSearch` (`BraveSearchEngine`, `TavilySearchEngine` on `WebToolsService`) take an `api-key` - best `<api-key name="tl_brave_api_key"/>`, the variable holding the plain key - or `<credentials>`. A remote MCP server (`HttpMcpTransport`) and a remote A2A agent (`<remote-agent>` of `A2AService`, module `tl-ai-a2a`) take `<credentials>`, e.g. `HeaderCredentials` with `scheme="Bearer"` and an `EnvironmentSecretSource` as `<secret>`. The HTTP embedder of `tl-ai-index` (`OpenAiCompatibleEmbedder`) takes an `api-key`, sent as bearer token, or `<credentials>`. All of them repeat a request rejected with HTTP 401 once with fresh credentials. The vector index of `tl-ai-vector-index` takes its key as `<mistral-api-key name="tl_mistral_api_key"/>` and reads it when it starts; without a key it is disabled - the application starts, a warning is logged, nothing is indexed and a search reports that the index is not available.

The tool `createGiteaIssue` (factory `gitea`, group `egress`) files issues in a Gitea repository named by the environment variables `tl_gitea_url`, `tl_gitea_repo_owner` and `tl_gitea_repo_name`; without them the tool answers with an error and creates nothing. It authenticates with the access token in `tl_gitea_token`, or with the `<credentials>` of its `<tool class="com.top_logic.tl.ai.core.tools.integration.GiteaIssueTool">` in the factory.

Before an agent can read any business data, the application needs a `<security-config>` (model access rights) - the engine denies every operation to role-based accounts without one. [The security chapter](doc:ai/security) explains the two grant tables an application writes, and the [worked example](doc:ai/security/role-model-worked-example) builds both from scratch.

## The chapter

- Concepts: [forms of use](doc:ai/forms-of-use) (built-in assistant, external agent, internal agent), [capabilities](doc:ai/capabilities), [semantic memory](doc:ai/semantic-memory), [AI on a no-code platform](doc:ai/no-code-platform), [extensibility](doc:ai/extensibility).
- Internals, for developers of the AI modules: [the agent loop and its environment](doc:ai/agent-loop), [tools and skills](doc:ai/tools-and-skills), [providers and failover](doc:ai/providers), [run limits and prompt caching](doc:ai/limits-and-caching), [delegation and subagents](doc:ai/delegation), [MCP and A2A](doc:ai/protocols), [memory, compaction and citations](doc:ai/memory-and-citations), [the chat UI and served content](doc:ai/chat-ui).
- [Security](doc:ai/security): the threat model, the enforcement layers, and the administrator's configuration.
- [Testing](doc:ai/testing) and [development rules](doc:ai/development) for the AI modules.
