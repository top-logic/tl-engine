---
description: Read before configuring or changing how agent runs reach language models - the four configuration services LLMProviderService, AgentRuntimeService, AgentApplicationService and AgentPolicyService, the provider implementations (LLMProvider, Anthropic, OpenAI, Mistral, xAI, Bedrock, chat completions, local), the providers tl-ai-core ships and when a provider without its key is inactive, the default model of a run without one (defaultTarget), the model catalog, model health and failover (ModelAvailability, <failover> with <chains> and <fallbacks>, aiModelHealth, aiModelHealthReset), and migrating a configuration of the former LLMService.
order: 62
---

# Providers and failover

## Four configuration services

An agent run reads its configuration from four services, cut by who sets them, so that saving one in the service editor (which writes a snapshot of its whole configuration) and restarting it touches only what its owner tunes:

| Service (service editor) | Package | Sections | Set by |
|---|---|---|---|
| `LLMProviderService` (*LLM providers*) | `provider/` | `<providers>`, `<model-catalog>`, `<failover>` (including `rate-limit-wait-seconds`) - which models exist and how to reach them | operations |
| `AgentRuntimeService` (*Agent runtime*) | `agent/` | `default-model`, `default-provider`, `<agent-overrides>`, `max-turns` (the turn limit of an agent whose override names none, 60 when unset), `chat-entry-point`, `<capacity>`, `<security-context>`, `<run-observers>` | operations, development |
| `AgentApplicationService` (*Agent application knowledge*) | `agent/` | `<environment>` (`<application>`, `<template>`), `<object-ids>`, `<prompt-contributors>` - development material, kept in the application's source | development |
| `AgentPolicyService` (*Agent policy*) | `agent/` | `<budget>`, `<user-memory>`, `user-preferences`, `max-preferences-length` - what a run may cost and what an agent may keep about a person | administration |

`AgentRuntimeService` depends on `LLMProviderService`, and its start refuses a default or override model that no provider exposes, that more than one provider exposes, or that the provider named for it does not expose. Restarting `LLMProviderService` restarts `AgentRuntimeService`; restarting any of the other three touches nothing else. `chat-workers` and `chat-queue` take effect only at an application restart.

A configuration that names the service `com.top_logic.tl.ai.core.provider.LLMService`, which these four services split up, stops the application at the start of `LLMProviderService`, with a message naming the file and where each section it sets belongs; [migrating from `LLMService`](#migrating-from-llmservice) lists where every property went. Code reaches the providers through `LLMProviderService.getInstance()`; `resolveModelAssignment` and `getDefaultModel` are on `AgentRuntimeService`. `TestAgentRuntimeService`, `TestAgentApplicationService`, `TestAgentPolicyService` and `TestOldLLMServiceConfiguration` pin it.

## The provider system

`LLMProvider` (package `com.top_logic.tl.ai.core.provider`) is a streaming-first interface: every provider streams, there are no blocking request/response calls. Implementations, each with a `...ProviderConfig` that is a `<provider>` of `LLMProviderService`:

- `anthropic/` - the Anthropic Messages API,
- `openai/` - OpenAI,
- `mistral/` - Mistral,
- `xai/` - xAI,
- `bedrock/` - Amazon Bedrock (Converse API),
- `chatcompletions/` and `local/` - OpenAI-compatible chat-completions endpoints, for local models and custom providers with a configurable base URL.

All use the Java `HttpClient` and msgbuf JSON (`AbstractStreamingProvider`, `SSEReader`, `StreamHandler`). A provider's models are discovered from its endpoint (`ModelDiscoveryHelper`) or listed in its `models` attribute. The model catalog (`<model-catalog/>`) supplies model metadata - context window, output limit, prices - from a bundled copy of models.dev, refreshed from the remote catalog in the background unless `fetch-remote-catalog="false"`; `<model-overrides>` add what the catalog does not know. The resolved metadata is state of the running service, not part of its configuration: `LLMProviderService#catalog()` answers it as an immutable snapshot, each model with its origin (bundled, remote, override) and the state of the remote load (disabled, loading, loaded, failed). Each provider authenticates its requests through one `CredentialProvider` held by `AbstractStreamingProvider` and used by `ModelDiscoveryHelper`: the configured `<credentials>`, else its `api-key` (a `SecretSource`; in the attribute form an encrypted value, so a key in plain text carries the prefix `unencrypted:`) in the header of its API, else none. On HTTP 401 the provider calls `invalidate()` and repeats the request once - nothing has been streamed at that point. See [the chapter introduction](doc:ai#configuring-the-llm-providers) for the environment variables and examples.

`ProviderException.Kind` classifies a failure (`ErrorClassifier`): `QUOTA`, `CAPACITY`, `TRANSIENT`, `INVALID_REQUEST`, `CONTEXT_WINDOW_EXCEEDED` and others; the chat turns the kind into the message the user reads.

## The shipped providers

`tl-ai-core.conf.config.xml` configures `anthropic`, `openai`, `mistral` and `xai`, in this order, each with its key as the named secret `tl_<provider>_api_key` and its models from the alias `%<PROVIDER>_MODELS%` (variable `tl_<provider>_models`, with a default list of `tl-ai-core.conf.xml`); `default-model` of `AgentRuntimeService` is the alias `%AI_DEFAULT_MODEL%` (variable `tl_ai_default_model`, empty by default). [The chapter introduction](doc:ai#configuring-the-llm-providers) lists the defaults and shows how an application changes them. `TestShippedProviders` pins that every shipped model is in the bundled catalog and can call tools.

A provider is **available** when its credentials can authenticate now (`LLMProviderConfig#isAvailable()`, answered by `CredentialProvider#isAvailable()` from its secret sources, without a request). An unavailable provider is configured and exposes its models - routing, the startup checks and the access policy see them - but it is **inactive**: the chat's model picker leaves its models out (`LLMProviderService#isServable(String)`, asked for each request; a model of several providers is offered while one of them has its key), `AgentLoop` never calls it, failover passes over it without a mark, and a run left with no model of an available provider fails with a `TopLogicException` naming the secrets that are missing (`LLMProviderService#noProviderAvailable()`), which the chat shows in the user's language. Availability is asked for each run, so a key provided later serves the next run without a restart.

A run that is assigned no model - no `default-model` (an empty one counts as none) and no model in its agent's override - takes `LLMProviderService#defaultTarget()`: the first model of the first available provider, in configuration order (`AgentRuntimeService#defaultTarget()`). The startup check of `AgentRuntimeService` therefore has nothing to refuse for an empty default model.

## Model health and failover

A model that could not answer is remembered (package `provider/health/`), and a configured chain says where a run goes instead. The two are useful separately: `ModelAvailability` is always on and writes marks the monitor reads; the `<failover>` chain is **empty** by default, and a run whose model has no chain behaves as without failover - one model, the loop's own retries, then failure.

The loop's own retries ask a rate-limited or overloaded model `maxRetries` times (`AgentParams`, `max-retries` of an agent override, default 3) with doubling pauses, never sooner than the provider's `Retry-After`. A **rate-limited** run with no fallback ahead - every rate-limited run without a chain - keeps asking for up to `rate-limit-wait-seconds` (default 180), because what holds the model is usually another request of the same key. A plain server error (500, 503) counts as overloaded and is not waited out that long.

### Marks

`ModelAvailability.causeOf` maps the `ProviderException.Kind` the providers produce:

| Kind | Cause | Effect | Lifetime |
|---|---|---|---|
| `BILLING`, `AUTHENTICATION` | `QUOTA` | the model is not called | `quota-ttl-seconds`, default 1 day |
| `RATE_LIMITED`, `OVERLOADED` | `CAPACITY` | the model is not called | `capacity-ttl-seconds`, default 30 s |
| `UNKNOWN` | `TRANSIENT` | the model keeps one attempt per turn | `transient-ttl-seconds`, default 5 min |
| `INVALID_REQUEST`, `CONTEXT_WINDOW_EXCEEDED` | none | nothing is marked | - |

A malformed request would fail on the next model too, and a context overflow is answered by compaction, so neither marks a model; both are rethrown, and a run with a chain fails on them like one without. **A weaker cause never replaces a stronger mark that still holds** (`TRANSIENT < CAPACITY < QUOTA`, the order of `HealthCause`); equal causes replace, refreshing the lifetime. The single attempt a `TRANSIENT` mark leaves is per turn and claimed atomically. `0` as a lifetime means "until an administrator resets it". `ModelAvailability` is a component of `LLMProviderService` (`LLMProviderService.modelHealth()`), keyed by the model names that service routes and taking its `Clock` from its caller; restarting `LLMProviderService` clears every mark.

### The chain

```xml
<failover quota-ttl-seconds="86400" capacity-ttl-seconds="30" transient-ttl-seconds="300" rate-limit-wait-seconds="180">
	<chains>
		<model name="claude-sonnet-4-6">
			<fallbacks>
				<fallback model="gpt-5.5"/>
				<fallback model="claude-sonnet-4-6" provider="bedrock"/>
			</fallbacks>
		</model>
	</chains>
</failover>
```

- A chain is keyed by the model **name**, whichever provider serves it: the agents that ask for the same model want the same way out.
- A `<fallback>` may name its `provider`, which a model exposed by two providers needs, and which makes the same model served by two accounts or two vendors a fallback for itself.
- A fallback that cannot be routed **refuses the start**, like every other model reference; otherwise it would be found out on the day the primary is down.
- An entry repeating one already in the chain is dropped when the chain is built; the same model on another provider is no repeat.

### What the run does

`AgentLoop.callRouted` wraps the retry-and-compact stack: before each attempt it asks `ModelAvailability` whether the chain's current entry may be called, after a failure it reports it and moves one entry on. A chain of one calls the provider directly. **A switch lasts for the rest of the run**, not for the turn, because every provider caches by prefix; the next run starts at the configured model and is routed by the marks then in force. For the target, `LLMProviderService.buildProvider(model, provider)` builds the provider, `maxOutputTokens` and `contextWindowTokens` become the lower of the run's and the target's, and `ModelOptions.forTarget` rebuilds the sampling options (`thinking_effort` does not reach an OpenAI model, `reasoning_effort` not an Anthropic one, a model without temperature gets none). Tool schemas and the response format travel unchanged - the target's request builder renders them. The conversation memory's budget is not changed: a run that fails over to a smaller context window relies on the reactive compaction of `callProviderWithReactiveCompaction`.

| Where | What an operator sees |
|---|---|
| `AgentEvent.ModelSwitched` | `fromModel`, `toModel`, and a `reason` naming every model skipped and why |
| the transcript | a `Part.SystemEventType.MODEL_CHANGED` part `from -> to: reason`, worded like the chat's notice |
| `AgentExecution.model` / `.provider`, `RunInfo.model()` | corrected to the model that answered |
| the chat | a quiet notice line above the answer |
| the log | one WARN per model passed over, one INFO per switch |
| a run that ran out of chain | a `ProviderError` with the kind of the last model called and a message naming every model and reason |

`aiModelHealth()` returns every mark that still holds, newest first, one map per model with `model`, `cause`, `kind`, `terminal`, `detail`, `markedAt` and `expiresAt` (absent for a mark held until reset); it needs no permission. `aiModelHealthReset("gpt-5.5")` drops one mark, `aiModelHealthReset(null)` all - administrators only; it is the way out of a sticky `QUOTA` mark after a key or budget was fixed outside the application. `TestModelAvailability` pins the lifetimes against an injected clock, `TestModelOptions` the rebuilt options, and `TestModelFailover` the switch on a real `LLMProviderService` with scripted providers, including that a configuration without a chain behaves as without failover.

## Migrating from `LLMService`

Releases of `tl-ai` before its split configured everything an agent run reads on one service, `com.top_logic.tl.ai.core.provider.LLMService`. The engine has no alias for a renamed service, so a configuration that still names it is read only to be refused: `LLMProviderService` and with it the application do not start, with a message naming the file and, for every section the file sets, where it belongs now (`LLMService.refusal`). A file that sets nothing (only empty elements such as `<model-catalog/>`) is not refused but read by nobody: delete it. An application that lists the module key in `ModuleSystem` renames `com.top_logic.tl.ai.core.provider.LLMService$Module` to `com.top_logic.tl.ai.core.provider.LLMProviderService$Module`.

| `LLMService` | now |
|---|---|
| `<providers>`, `<model-catalog>` (`catalog-url`, `<model-overrides>`), `<failover>` | the same sections of `LLMProviderService` |
| `<model-catalog><llm-configuration>` | nowhere - the resolved metadata is state of the running service (`LLMProviderService#catalog()`); a file that still contains it fails at startup |
| `<agent-execution default-model / default-provider / chat-entry-point>` | `AgentRuntimeService` `default-model`, `default-provider`, `chat-entry-point` |
| `<agent-execution><agent-overrides>` | `AgentRuntimeService` `<agent-overrides>`, entries unchanged |
| `<run-limits chat-workers / chat-queue / concurrent-runs-per-account>` | `AgentRuntimeService` `<capacity ...>` |
| `<security-context>` | `AgentRuntimeService` `<security-context>` |
| `<environment><application>`, `<environment><template>`, `<object-ids>`, `<prompt-contributors>` | the same sections of `AgentApplicationService` |
| `<run-limits token-budget / budget-window-hours>`, `<run-limits><role-budgets>` | `AgentPolicyService` `<budget ...>`; a `<role-budget>` without `tokens` and `money` is refused, write `tokens="0"` for an unlimited token budget |
| `<user-memory>` | `AgentPolicyService` `<user-memory>` |
| `<environment user-preferences / max-preferences-length>` | `AgentPolicyService` `user-preferences`, `max-preferences-length` |

Write one file per service, named as the service editor names it (`WEB-INF/autoconf/<service class>.config.xml`) and **without** `config:override`: every map of the four services is keyed and merges by key, so the application's file adds to what `tl-ai-core` ships. Name a provider's key as a secret (`<api-key name="tl_anthropic_api_key"/>`) rather than through an alias such as `%ANTHROPIC_API_KEY%`, which would end up in the file the service editor stores.

| Java API of `LLMService` | now |
|---|---|
| `LLMService`, `LLMService.Module`, `LLMService.getInstance()` | `LLMProviderService`, its `Module` and `getInstance()` (`buildProvider`, `getModel`, `getProviderId`, `getConfiguredModelNames`, `failoverChain`, `modelHealth` unchanged) |
| `resolveModelAssignment(agent)`, `resolveModel(agent)`, `getDefaultModel()`, `getDefaultProvider()` | the same methods of `AgentRuntimeService.getInstance()` |
| `getEntryPointAgentName()` | `AgentRuntimeService.chatEntryPoint()` |
| `promptContributors()` | `AgentApplicationService.promptContributors()` |
| `LLMService.AgentOverride`, `LLMService.AllAgentNameOptions` | `AgentRuntimeService.AgentOverride`, `AgentRuntimeService.AllAgentNameOptions` |
| `RunLimitsConfig` | `CapacityConfig` (runtime) and `BudgetConfig` (policy); `RunLimitsConfig` is their union, for `RunLimitSettings.override` in tests |
| `AgentParams.DEFAULT_MAX_TURNS` as the run's limit | `AgentRuntimeService.maxTurns()`, whose default the constant is |
