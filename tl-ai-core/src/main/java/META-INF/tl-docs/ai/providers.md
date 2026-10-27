---
description: Read before configuring or changing how agent runs reach language models - the four configuration services LLMProviderService, AgentRuntimeService, AgentApplicationService and AgentPolicyService, the provider implementations (LLMProvider, Anthropic, OpenAI, Mistral, xAI, Bedrock, chat completions, local), the model catalog, and model health and failover (ModelAvailability, <failover>, aiModelHealth).
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

A configuration that names the service `com.top_logic.tl.ai.core.provider.LLMService`, which these four services split up, stops the application at the start of `LLMProviderService`, with a message naming the file and where each section it sets belongs; `tl-ai-core/doc/llm-service-migration.md` lists where every property went. Code reaches the providers through `LLMProviderService.getInstance()`; `resolveModelAssignment` and `getDefaultModel` are on `AgentRuntimeService`. `TestAgentRuntimeService`, `TestAgentApplicationService`, `TestAgentPolicyService` and `TestOldLLMServiceConfiguration` pin it.

## The provider system

`LLMProvider` (package `com.top_logic.tl.ai.core.provider`) is a streaming-first interface: every provider streams, there are no blocking request/response calls. Implementations, each with a `...ProviderConfig` that is a `<provider>` of `LLMProviderService`:

- `anthropic/` - the Anthropic Messages API,
- `openai/` - OpenAI,
- `mistral/` - Mistral,
- `xai/` - xAI,
- `bedrock/` - Amazon Bedrock (Converse API),
- `chatcompletions/` and `local/` - OpenAI-compatible chat-completions endpoints, for local models and custom providers with a configurable base URL.

All use the Java `HttpClient` and msgbuf JSON (`AbstractStreamingProvider`, `SSEReader`, `StreamHandler`). A provider's models are discovered from its endpoint (`ModelDiscoveryHelper`) or listed in its `models` attribute. The model catalog (`<model-catalog/>`) supplies model metadata - context window, output limit, prices - from a bundled copy of models.dev, refreshed from the remote catalog in the background unless `fetch-remote-catalog="false"`; `<model-overrides>` add what the catalog does not know. The resolved metadata is state of the running service, not part of its configuration: `LLMProviderService#catalog()` answers it as an immutable snapshot, each model with its origin (bundled, remote, override) and the state of the remote load (disabled, loading, loaded, failed). An `api-key` is an encrypted property, so a key in plain text carries the prefix `unencrypted:`; see [the chapter introduction](doc:ai#configuring-the-llm-providers) for the environment variables.

`ProviderException.Kind` classifies a failure (`ErrorClassifier`): `QUOTA`, `CAPACITY`, `TRANSIENT`, `INVALID_REQUEST`, `CONTEXT_WINDOW_EXCEEDED` and others; the chat turns the kind into the message the user reads.

## Model health and failover

A model that could not answer is remembered (package `provider/health/`). `ModelAvailability`, a component of `LLMProviderService`, turns the `ProviderException.Kind` the providers produce into a mark: `QUOTA` is sticky, `CAPACITY` holds for about 30 seconds, `TRANSIENT` costs one attempt per turn, and a weaker cause never replaces a stronger mark that still holds. `INVALID_REQUEST` and `CONTEXT_WINDOW_EXCEEDED` mark nothing: they would fail the same way on the next model.

The `<failover>` section of `LLMProviderService` says where a run goes instead, per model name. It is empty by default, and an empty section changes nothing. Where one is configured, `AgentLoop.callRouted` moves the run to the next healthy model of the chain **for the rest of the run** - not for the turn, because every provider caches by prefix - rebuilding the provider, the limits and the sampling options for the target while the tool schemas and the response format travel unchanged. Every model passed over is named on the `AgentEvent.ModelSwitched`, in a `MODEL_CHANGED` transcript part and in the chat's notice line; `AgentExecution.model` is corrected to the model that answered. The TL-Script functions `aiModelHealth()` and `aiModelHealthReset(model)` read and clear the marks. `tl-ai-core/doc/model-failover.md` has the details.
