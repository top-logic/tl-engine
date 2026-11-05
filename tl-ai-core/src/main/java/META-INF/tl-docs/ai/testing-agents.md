---
description: Read before writing a test of an application's own agent - its agent definition, skills, tools or prompt - that runs without a real model and without a key - the test-jar dependency on tl-ai-core, ScriptedProvider as a <provider> of LLMProviderService in a test configuration with a <model-overrides> entry, ScriptedProvider.install and ScriptedProvider.requests, scripts of replies (ScriptedModel.Script, Script.of, Script.byModel, Reply with text, tool calls, thinking and finish reason, a ProviderException), and ScriptedModel for a test of a bare AgentLoop.
order: 81
---

# Testing an application's agents without a model

An application that ships its own agent - an agent definition, skills, tools, a system prompt - tests it the way the engine tests its own: on **recorded model answers**. A scripted provider stands in for the model, answers each request with a reply the test wrote, and records what was sent, so a test runs in every build, at no cost and with no key. Everything between the test and the provider is real: the agent definition, the tool registry, the approval gate, the transaction scope, the routing of `LLMProviderService`.

The provider ships in the test jar of `tl-ai-core`, which the BOM `tl-bom` manages:

```xml
<dependency>
  <groupId>com.top-logic</groupId>
  <artifactId>tl-ai-core</artifactId>
  <type>test-jar</type>
  <scope>test</scope>
</dependency>
```

How the AI modules' own tests are organized and run is in [testing the AI modules](doc:ai/testing).

## Configuring the scripted provider

`com.top_logic.tl.ai.core.provider.ScriptedProvider` is an ordinary provider of `LLMProviderService` (see [providers](doc:ai/providers)). The test configuration of the application declares it, gives the model it serves its metadata - no catalog knows a scripted model - and puts the agent under test on it:

```xml
<config service-class="com.top_logic.tl.ai.core.provider.LLMProviderService"
  config:override="true"
>
  <instance class="com.top_logic.tl.ai.core.provider.LLMProviderService">
    <providers>
      <provider class="com.top_logic.tl.ai.core.provider.ScriptedProvider"
        models="scripted-model"
        name="scripted"
      />
    </providers>
    <model-catalog fetch-remote-catalog="false">
      <model-overrides>
        <model name="scripted-model"
          context-window="200000"
          max-output="8192"
          reasoning="false"
          temperature="false"
          tool-call="true"
        />
      </model-overrides>
    </model-catalog>
  </instance>
</config>

<config service-class="com.top_logic.tl.ai.core.agent.AgentRuntimeService">
  <instance class="com.top_logic.tl.ai.core.agent.AgentRuntimeService"
    default-model="scripted-model"
  >
    <agent-overrides>
      <agent name="MyApp_Assistant" model="scripted-model" provider="scripted"/>
    </agent-overrides>
  </instance>
</config>
```

The provider has no properties of its own: what it answers is the installed script, not configuration. Credentials configured on it are honored - while they are not available, the provider is inactive, as a real one would be. `tl-ai-eval` configures it this way (`src/test/webapp/WEB-INF/autoconf/com.top_logic.tl.ai.core.provider.LLMProviderService.config.xml`).

## Scripts and replies

A test installs a script before it starts a run:

| Call | Effect |
|---|---|
| `ScriptedProvider.install(script)` | Every subsequent request to any `ScriptedProvider` entry is answered by `script`; the recorded requests are forgotten. Without an installed script a request fails with an `IllegalStateException` saying so. |
| `ScriptedProvider.requests()` | The `ProviderRequest`s received since the last `install`, in order - system prompt, messages, tools offered, model, response format. |

A `ScriptedModel.Script` is a function `reply(request, turn)`. `turn` is the number of assistant messages already in the request's conversation - 0 for the first call, 1 after the first tool result - so one script serves a delegating agent and its sub-agents alike. It returns a `ScriptedModel.Reply`:

- `Reply.text("...")` - an answer of text alone, which ends the run;
- `Reply.of(parts...)` - any mix of `TextContent`, `ToolCall` and `ThinkingContent`; a `ToolCall` makes the loop execute that tool and call again;
- `.withFinishReason(...)` - why the model stopped; by default `tool_use` when the reply calls a tool, else `end_turn` (`ScriptedModel.MAX_TOKENS` plays a reply cut off at the output limit);
- `.withUsage(...)` - the tokens the reply reports.

A script that throws a `ProviderException` plays a provider that refuses - a wrong key, an overloaded model, a request beyond the context window.

Three helpers build scripts: `Script.of(reply0, reply1, ...)` answers turn *n* with the *n*-th reply and fails loudly with an `IllegalStateException` when the run asks for a turn after the last one, so a run that asks more often than the test expected is a finding instead of a made-up answer. `Script.inOrder(reply0, reply1, ...)` answers the *n*-th request with the *n*-th reply, for a run whose conversation does not show every turn - a compaction summarized it away, the loop did not keep an empty reply. `Script.byModel(map)` answers each request with the script of the model it is addressed to, for failover chains and for sub-agents on a model of their own.

## Example

The application's assistant has to look a customer up with its own tool before it answers, and must never send the customer's tax number to the model:

```java
public void testTheAssistantLooksTheCustomerUp() {
    ScriptedProvider.install(Script.of(
        Reply.of(new ToolCall("c1", "findCustomer", "{\"name\": \"Ada\"}")),
        Reply.text("Ada Lovelace is a customer since 2021.")));

    String answer = runAssistant("Since when is Ada a customer?");

    assertEquals("Ada Lovelace is a customer since 2021.", answer);
    List<ProviderRequest> sent = ScriptedProvider.requests();
    assertEquals("One request before the tool call, one after it.", 2, sent.size());
    assertTrue(sent.get(0).tools().stream().anyMatch(tool -> tool.name().equals("findCustomer")));
    assertFalse("The tax number never reaches the model.",
        sent.get(1).messages().toString().contains("DE123456789"));
}
```

`runAssistant` starts the agent the way the application starts it - through its own entry point, or `AgentRunner.run` as `TestSubagentOutsideChat` in `tl-ai-eval` does. A test that starts agent runs needs the modules `AgentRuntimeService`, `AgentApplicationService` and `AgentPolicyService` (see [the agent loop](doc:ai/agent-loop#the-loop)).

## Testing the loop on its own

A test of a tool or of the loop's behaviour that does not need the configured route hands a `ScriptedModel` directly to an `AgentLoop`. It answers from its own script and records its own requests (`requests()`, `reset()`), so several of them can run side by side:

```java
ScriptedModel model = new ScriptedModel(Script.of(
    Reply.of(new ToolCall("c1", "createTask", "{\"subject\": \"Scope\"}")),
    Reply.text("Done.")));
new AgentLoop(params, model, catalog).run(conversation, listener);
assertEquals(2, model.requests().size());
```
