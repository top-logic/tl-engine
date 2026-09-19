# When a model cannot answer: health marks and the failover chain

`AgentLoop` used to retry the **same** model and then give up. `ErrorClassifier` had been
classifying every provider failure all along, and nothing consumed that classification to route
around a model that was known to be sick. This is what closes that gap (#344).

Two pieces, and they are useful separately:

| Piece | What it does | Default |
|---|---|---|
| `ModelAvailability` | remembers that a model could not answer, and for how long that is worth remembering | always on |
| the `<failover>` chain | says where a run goes instead | **empty** - nothing changes |

A run whose model has no configured chain behaves exactly as it did before: one model, the loop's
own retries, then failure. Marks are still written, because they cost nothing and the monitor reads
them, but with a chain of one there is nowhere to route to.

## What marks a model, and for how long

`ModelAvailability.causeOf` is the whole classification, and it is over the
`ProviderException.Kind` the providers already produce:

| Kind | Cause | Effect | Lifetime |
|---|---|---|---|
| `BILLING`, `AUTHENTICATION` | `QUOTA` | the model is not called | `quota-ttl-seconds`, default **1 day** |
| `RATE_LIMITED`, `OVERLOADED` | `CAPACITY` | the model is not called | `capacity-ttl-seconds`, default **30 s** |
| `UNKNOWN` | `TRANSIENT` | the model keeps **one attempt per turn** | `transient-ttl-seconds`, default **5 min** |
| `INVALID_REQUEST`, `CONTEXT_WINDOW_EXCEEDED` | *none* | nothing is marked | - |

The last row is the important one. A malformed request is this application's fault and would be
malformed for the next model too; a context overflow is answered by compacting, not by changing
models. Marking either would route healthy models away over a fault that travels with the request,
and hide where it came from. Both are rethrown untouched, and a run with a chain fails on them just
like a run without one.

**A weaker cause never replaces a stronger mark that still holds.** A single unclassified blip
during a quota lockout does not turn the lockout into "retry once", and a capacity mark does not
shorten it either. Equal causes replace, which refreshes the lifetime of a fault that is still
happening. The order is `TRANSIENT < CAPACITY < QUOTA`, and it is the declaration order of
`HealthCause`.

**The single attempt a `TRANSIENT` mark leaves is per turn**, claimed atomically, so two turns
running at once cannot both take it and the turn whose call just failed cannot take it again.

### Why this is not a `ManagedClass`

It is a component of `LLMService`, reached through `LLMService.modelHealth()`. The state is keyed by
exactly the model names that service routes and is configured in the same `<failover>` section, so a
service of its own would add a module to the startup graph and a second place that has to agree
about model names. As a plain object it also takes its `Clock` from its caller, which is what lets
`TestModelAvailability` assert every lifetime instead of waiting one out.

Restarting `LLMService` replaces the instance, so it is also the blunt way to clear every mark.

## The chain

```xml
<failover quota-ttl-seconds="86400"
          capacity-ttl-seconds="30"
          transient-ttl-seconds="300">
  <model name="claude-sonnet-5">
    <fallback model="gpt-5"/>
    <fallback model="claude-sonnet-5" provider="bedrock"/>
  </model>
</failover>
```

- The chain is keyed by the model **name**, whichever provider serves it: a fallback is about the
  model an agent asked for, and the agents that ask for the same model want the same way out.
- A `<fallback>` may name its own `provider`, exactly where `agent-execution` needs one - a model
  two provider entries expose has to say which one it means. That is also how the same model served
  by two accounts, or by Anthropic and by Bedrock, becomes a fallback for itself.
- A fallback that cannot be routed **refuses the start**, like any other model reference. It would
  otherwise only be reached on the day the primary is already down, which is the worst moment to
  find out.
- An entry that repeats one already in the chain is dropped when the chain is built: it would spend
  a turn re-trying what just failed. Same model, different provider, is not a repeat.
- `0` as a lifetime means "until an administrator resets it", which is the right setting for
  `quota-ttl-seconds` where somebody watches the monitor.

## What the run does

`AgentLoop.callRouted` wraps the retry-and-compact stack the run loop would otherwise call
directly. Before each attempt it asks `ModelAvailability` whether the chain's current entry may be
called; after a failure it reports the failure and moves one entry on. A chain of one - the default
- short-circuits the whole thing and calls the provider once.

**A switch lasts for the rest of the run, not for the turn.** Every provider caches by prefix, so a
run that alternated between two models would pay the full prompt again on every turn and never read
a cache. Once a run has moved, it stays moved. The next run starts at the configured model again and
is routed by the marks that are then in force - which is usually straight past it, at no cost,
because the mark the last run left is still held.

What is rebuilt for the target, and what is not:

| | |
|---|---|
| the provider instance | `LLMService.buildProvider(model, provider)` |
| `maxOutputTokens`, `contextWindowTokens` | the **lower** of what the run asked for and what the target allows |
| the sampling options | `ModelOptions.forTarget` - `thinking_effort` does not reach an OpenAI model, `reasoning_effort` does not reach an Anthropic one, and a model that takes no temperature is sent none |
| the tool schemas, the response format | nothing. They are provider-neutral; the target's own request builder renders them in its dialect, which is what makes a chain across vendors work |
| the conversation memory's budget | **nothing** - it was sized for the configured model and is owned by the caller. A run that fails over to a smaller context window relies on the reactive compaction in `callProviderWithReactiveCompaction` to notice |

## What an operator sees

| Where | What |
|---|---|
| `AgentEvent.ModelSwitched` | `fromModel`, `toModel`, and a `reason` naming every model skipped on the way and why |
| the transcript | a `Part.SystemEventType.MODEL_CHANGED` part: `from -> to: reason` |
| `AgentExecution.model` / `.provider` | corrected to the model that actually answered, so the monitor's column is not the configured fiction |
| `RunInfo.model()` | the same, so a tool mid-run is told what it is running under |
| the chat | a quiet `notice-line` above the answer, the translated sentence over the server's own line |
| the log | one `WARN` per model passed over, one `INFO` per switch |
| a run that ran out of chain | the `ProviderError` carries the kind of the last model actually called, and a message naming every model and reason |

The transcript part and the chat notice carry the **same** wording, character for character, so a
notice reads the same while it happens as it does after a reload - the lesson of #429.

## Administration

```
aiModelHealth()               // every mark that still holds, newest first
aiModelHealthReset("gpt-5")   // drop one; administrators only
aiModelHealthReset(null)      // drop all
```

`aiModelHealth()` is a read for a monitor and needs no permission: which models are currently
failing is operational state, not anybody's data. It returns one map per marked model, with
`model`, `cause`, `kind`, `terminal`, `detail`, `markedAt` and `expiresAt` (absent for a mark that
holds until it is reset).

`aiModelHealthReset` is the way out of a sticky `QUOTA` mark. A rejected key or an exhausted budget
is fixed outside this application and nothing in here can notice that it was, so an administrator
says so. Administrators only: the marks are what keeps a run off a model that is costing money for
nothing.

## Tests

| Class | What it pins |
|---|---|
| `TestModelAvailability` | every lifetime against an injected clock, the cause ranking, the one attempt per turn, the two kinds that mark nothing, reset |
| `TestModelOptions` | that the sampling options are rebuilt for the target and not carried across |
| `TestModelFailover` | a real `LLMService` on scripted providers with a real `AgentLoop` on top: the switch, the request built for the target, the switch lasting the run, the skipped models and their reasons, the exhausted chain, the request fault that does **not** fail over, the sticky mark routing the next run past, and - the one that matters most - that a configuration without a chain behaves exactly as before |
