# Migrating from `LLMService` to the four agent services

Gitea #691. `LLMService` held everything an agent run reads from configuration - how the models are
reached, which agent runs on which model, what an agent is told about the application, and what a run
may cost and keep about a person. Every save in the Service Editor wrote all of it as one snapshot,
and every restart of it fetched the model catalog again. It is now four services, cut by who sets
them:

| Service | Service Editor | Set by | Holds |
|---|---|---|---|
| `com.top_logic.tl.ai.core.provider.LLMProviderService` | LLM providers | operations | which models exist and how to reach them: providers, model catalog, failover |
| `com.top_logic.tl.ai.core.agent.AgentRuntimeService` | Agent runtime | operations, development | which agent runs how: default and per-agent model, per-agent parameters, the default turn limit, capacity, chat entry point, security context |
| `com.top_logic.tl.ai.core.agent.AgentApplicationService` | Agent application knowledge | development, in the application's source | what the agent knows about the application: name, environment wording, object-id hints, prompt contributors (the `TL_init` application context among them) |
| `com.top_logic.tl.ai.core.agent.AgentPolicyService` | Agent policy | administration | what a run may cost and what an agent may keep about a person: token budget, user memory, preference text |

`AgentRuntimeService` depends on `LLMProviderService`: restarting the providers restarts it, and its
start checks that every model it names is exposed by exactly one provider (or by the provider named
for it). The other two depend on nothing; restarting one of them touches nothing else.

## An old file stops the application

The engine has no alias for a renamed service. A configuration that still names
`com.top_logic.tl.ai.core.provider.LLMService` is therefore read only to be refused:
`LLMProviderService` does not start, and with it the application, with a message that names the file
and, for every section the file sets, where it now belongs. There is no release in which the old place
is still read - a file that half moved would otherwise be read from two places.

A file that sets nothing (only empty elements such as `<model-catalog/>`) is not refused, but it is
read by nobody: delete it.

Also rename the module key if the application lists it in `ModuleSystem`:
`com.top_logic.tl.ai.core.provider.LLMService$Module` is now
`com.top_logic.tl.ai.core.provider.LLMProviderService$Module`.

## Where every property went

| Old (`LLMService`) | New |
|---|---|
| `<providers>` | `LLMProviderService` `<providers>` |
| `<model-catalog>` (`catalog-url`, `<model-overrides>`) | `LLMProviderService` `<model-catalog>` |
| `<model-catalog><llm-configuration>` | nowhere - the model metadata resolved from the bundled catalog, models.dev and `<model-overrides>` is state of the running service (`LLMProviderService#catalog()`), not configuration; a file that still contains the element fails at startup, delete it |
| `<failover>` (`quota-ttl-seconds`, `capacity-ttl-seconds`, `transient-ttl-seconds`, `rate-limit-wait-seconds`, `<chains>`) | `LLMProviderService` `<failover>` |
| `<agent-execution default-model>` | `AgentRuntimeService` `default-model` |
| `<agent-execution default-provider>` | `AgentRuntimeService` `default-provider` |
| `<agent-execution chat-entry-point>` | `AgentRuntimeService` `chat-entry-point` |
| `<agent-execution><agent-overrides>` | `AgentRuntimeService` `<agent-overrides>` (entries unchanged) |
| - (Java constant 60) | `AgentRuntimeService` `max-turns`: the turn limit of an agent whose override names none |
| `<run-limits chat-workers>` | `AgentRuntimeService` `<capacity chat-workers>` |
| `<run-limits chat-queue>` | `AgentRuntimeService` `<capacity chat-queue>` |
| `<run-limits concurrent-runs-per-account>` | `AgentRuntimeService` `<capacity concurrent-runs-per-account>` |
| `<security-context>` | `AgentRuntimeService` `<security-context>` |
| `<environment><application>` | `AgentApplicationService` `<environment><application>` |
| `<environment><template>` | `AgentApplicationService` `<environment><template>` |
| `<object-ids>` | `AgentApplicationService` `<object-ids>` |
| `<prompt-contributors>` | `AgentApplicationService` `<prompt-contributors>` |
| `<run-limits token-budget>` | `AgentPolicyService` `<budget token-budget>` |
| `<run-limits budget-window-hours>` | `AgentPolicyService` `<budget budget-window-hours>` |
| `<run-limits><role-budgets>` | `AgentPolicyService` `<budget><role-budgets>`. An entry with neither `tokens` nor `money` is refused at the start: `<role-budget name="tl.admin"/>` meant an unlimited token budget before, write `tokens="0"` for that (#825) |
| `<user-memory>` (all of it) | `AgentPolicyService` `<user-memory>` |
| `<environment user-preferences>` | `AgentPolicyService` `user-preferences` |
| `<environment max-preferences-length>` | `AgentPolicyService` `max-preferences-length` |

tl-ai ships no default model any more (the `%DEFAULT_MODEL%` placeholder is gone): an application
names the model its agents run on in `AgentRuntimeService`, and a model it names must be one of its
providers'.

## Before and after

Before, one file `WEB-INF/autoconf/com.top_logic.tl.ai.core.provider.LLMService.config.xml`:

```xml
<config service-class="com.top_logic.tl.ai.core.provider.LLMService" config:override="true">
  <instance class="com.top_logic.tl.ai.core.provider.LLMService">
    <providers>
      <provider class="com.top_logic.tl.ai.core.provider.anthropic.AnthropicProviderConfig"
        name="anthropic" api-key="%ANTHROPIC_API_KEY%" models="claude-sonnet-4-6"/>
    </providers>
    <agent-execution chat-entry-point="MyAgent" default-model="claude-sonnet-4-6">
      <agent-overrides>
        <agent name="MyAgent" max-turns="60"/>
      </agent-overrides>
    </agent-execution>
    <environment>
      <application><en>Event management</en></application>
    </environment>
    <run-limits concurrent-runs-per-account="5" token-budget="2000000"/>
    <model-catalog>
      <llm-configuration>...</llm-configuration>
    </model-catalog>
  </instance>
</config>
```

After, one file per service, each named as the Service Editor names it
(`WEB-INF/autoconf/<service class>.config.xml`) and **without** `config:override`: every map is keyed
and merges by key, so a file of the application adds to what tl-ai ships instead of replacing it.

```xml
<!-- com.top_logic.tl.ai.core.provider.LLMProviderService.config.xml -->
<config service-class="com.top_logic.tl.ai.core.provider.LLMProviderService">
  <instance class="com.top_logic.tl.ai.core.provider.LLMProviderService">
    <providers>
      <provider class="com.top_logic.tl.ai.core.provider.anthropic.AnthropicProviderConfig"
        name="anthropic" api-key="%ANTHROPIC_API_KEY%" models="claude-sonnet-4-6"/>
    </providers>
  </instance>
</config>

<!-- com.top_logic.tl.ai.core.agent.AgentRuntimeService.config.xml -->
<config service-class="com.top_logic.tl.ai.core.agent.AgentRuntimeService">
  <instance class="com.top_logic.tl.ai.core.agent.AgentRuntimeService"
    chat-entry-point="MyAgent" default-model="claude-sonnet-4-6">
    <agent-overrides>
      <agent name="MyAgent" max-turns="60"/>
    </agent-overrides>
    <capacity concurrent-runs-per-account="5"/>
  </instance>
</config>

<!-- com.top_logic.tl.ai.core.agent.AgentApplicationService.config.xml -->
<config service-class="com.top_logic.tl.ai.core.agent.AgentApplicationService">
  <instance class="com.top_logic.tl.ai.core.agent.AgentApplicationService">
    <environment>
      <application><en>Event management</en></application>
    </environment>
  </instance>
</config>

<!-- com.top_logic.tl.ai.core.agent.AgentPolicyService.config.xml -->
<config service-class="com.top_logic.tl.ai.core.agent.AgentPolicyService">
  <instance class="com.top_logic.tl.ai.core.agent.AgentPolicyService">
    <budget token-budget="2000000"/>
  </instance>
</config>
```

Each block sits in the usual `<application><services>...</services></application>` frame.

## Java API

| Old | New |
|---|---|
| `LLMService`, `LLMService.Module`, `LLMService.getInstance()` | `LLMProviderService`, `LLMProviderService.Module`, `LLMProviderService.getInstance()` (`buildProvider`, `getModel`, `getProviderId`, `getConfiguredModelNames`, `failoverChain`, `modelHealth` unchanged) |
| `LLMService.getInstance().resolveModelAssignment(agent)`, `resolveModel(agent)` | `AgentRuntimeService.getInstance().resolveModelAssignment(agent)`, `resolveModel(agent)` |
| `LLMService.getInstance().getDefaultModel()`, `getDefaultProvider()` | `AgentRuntimeService.getInstance().getDefaultModel()`, `getDefaultProvider()` |
| `LLMService.getInstance().getEntryPointAgentName()` | `AgentRuntimeService.chatEntryPoint()` |
| `LLMService.promptContributors()` | `AgentApplicationService.promptContributors()` |
| `LLMService.AgentOverride`, `LLMService.AllAgentNameOptions` | `AgentRuntimeService.AgentOverride`, `AgentRuntimeService.AllAgentNameOptions` |
| `RunLimitsConfig` (one section) | `CapacityConfig` (runtime) and `BudgetConfig` (policy); `RunLimitsConfig` is their union, for `RunLimitSettings.override` in tests |
| `AgentParams.DEFAULT_MAX_TURNS` as the run's limit | `AgentRuntimeService.maxTurns()` (the constant is its default) |

A run is refused while any of `AgentRuntimeService`, `AgentApplicationService` and
`AgentPolicyService` is not running - for example during its restart - so that no run starts with the
default security context, no limits, no budget or without the application's standing instructions.
A test that starts agent runs starts all three modules.
