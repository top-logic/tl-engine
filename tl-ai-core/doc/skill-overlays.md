# Skill overlays and the prompt cache

A skill is progressive disclosure: an agent advertises a handful of tools plus a catalog of skill
names, and `loadSkill("id")` hands over the skill's instructions and, if it declares any, its tools.
This document is about what that costs in the request, and what bounds it.

It is the resolution of Gitea #346.

## What a load changes in the request

Exactly one thing: the tool array grows.

- The **system prompt does not move.** The `### Available Skills` catalog is generated once, when
  the agent is built, from what the agent declares and what the run's principal may use. Loading a
  skill does not rewrite it.
- The **instructions arrive as a tool result** - an ordinary message, at the end of the
  conversation, compacted like any other turn content.
- The **tools are appended** to the array, behind everything that was already there.

`TestSkillOverlayRequestShape` pins all three against a provider that records its requests.

## Why the tool array is the expensive one

Anthropic renders a request as **tools, then system, then messages**, and re-uses the cached prefix
up to an explicit `cache_control` breakpoint. OpenAI-compatible endpoints cache automatically, by
longest common prefix, over the same rendering. Under both rules the tool array sits at position
zero: it is in front of the system prompt and in front of every message.

So a tool array that changes is the most expensive thing in the request that can change.
Anthropic's own guidance is blunt about it - "adding, removing, or reordering a tool invalidates the
entire cache".[^1] Two consequences shaped the design:

**Appending is survivable; reordering is not.** An appended tool leaves every byte before it
identical, so an OpenAI-compatible endpoint still matches the prefix up to the append point
automatically, and Anthropic can be given a breakpoint there. A tool inserted into the middle - or
the same tools sent in a different order - leaves nothing to match.

**A breakpoint is only useful where the bytes stop being stable.** `ProviderRequest.stableToolCount`
carries that index: how many leading tool definitions are the same on every request of this
conversation. `AgentLoop` computes it from the registry;
`AnthropicRequestBuilder` puts a `cache_control` on the last stable tool, and
`BedrockConverseRequestBuilder` a `cachePoint` after it - but only when a skill is actually loaded,
because with nothing behind the boundary the breakpoint on the system block already covers the whole
array, and only four breakpoints may be placed per request.

Whether that breakpoint produces a cache entry is the model's decision: a prefix shorter than the
model's minimum cacheable length (512-4096 tokens, by model) is silently not cached.[^1] An agent
with three tools gets nothing from it; an agent with ninety does. Neither case is an error.

[^1]: Anthropic, *Prompt caching*, https://platform.claude.com/docs/en/docs/build-with-claude/prompt-caching - render order, the 4-breakpoint limit, the per-model minimum cacheable prefix, and "don't change tools mid-conversation".

## The three layers of `DynamicToolCatalog`

Always advertised in this order, whatever order they were added in:

| Layer | Contents | Stable? |
|---|---|---|
| base | the agent's own `<tools>` and its MCP servers' tools | yes |
| framework overlays | `loadSkill`, `listTools` - what every agent carries | yes |
| skill overlays | one per loaded skill, in load order | no |

The first two are the **stable prefix**, and `stableToolCount` is its size after the run's
permission filter.

The order is enforced by the catalog rather than by call order on purpose. `AgentFactory` builds the
`SkillTool` first - which restores the session's already-loaded skills while it is constructed - and
adds the framework overlays afterwards, because `listTools` reports the `SkillTool`'s state and
cannot be built before it. Advertised in call order, a restored skill would sit in front of
`loadSkill`, where a freshly loaded one never sits: the second run of a session would send the same
tools in a different order and throw the cache away for nothing. That was the real, silent defect
behind #346.

For the same reason the loaded-skill set is ordered end to end - `LoadedSkillsBinding`,
`AgentEnvironment`, `ChatProcessor`, `TaskProgressTracker`, the `SessionMetadata` it persists. A
`ConcurrentHashMap` key set restored the skills in hash order, which is a different order again.

## The ceiling

A chat session's catalog outlives its runs, so an append-only catalog could only grow: an agent that
advertised three tools reached everything its skills could hand it and stayed there for the rest of
the session.

**At most `DynamicToolCatalog.DEFAULT_MAX_SKILL_OVERLAYS` (8) skills keep their tools loaded.**
Loading a ninth drops the overlay whose tools were called longest ago. That is the whole rule: no
new tool for the model to call, no timer, no per-turn bookkeeping. A run that needs a ninth skill
gets it and pays for it with the one it has stopped working with.

An eviction is **stated, not silent**. The `loadSkill` answer names the skills that left ("To make
room, these skills were unloaded and their tools are gone: ... Load one again if you need it."),
because the model read those instructions earlier in the same conversation and would otherwise call
a tool that had quietly disappeared. A call for an evicted tool is still answered with "load the
skill that has it" rather than "no such tool", because `setReachableBySkill` is about what the agent
*could* reach, not about what is loaded.

The ceiling is not the model's business beyond that. There is deliberately **no `unloadSkill` tool**:
an extra tool is advertised in every request of every agent forever, in exchange for a saving the
ceiling already guarantees, and it only works when the model remembers to use it.

## Compaction

A compaction changes the messages, never the tool array.

- **The tools stay.** Taking a capability away in the middle of the task the model loaded it for,
  for a reason it cannot see, is worse than carrying the schemas. Boundedness is the ceiling's job.
- **The instructions may be gone.** A summarizing compaction can replace the turn that carried a
  skill's text. `AgentLoop` therefore tells the registry (`ToolRegistry.historyCompacted()`), the
  catalog passes it on, and `SkillTool` forgets which skills' *content* it has delivered. The next
  `loadSkill` for such a skill delivers the text again instead of answering "already loaded"; its
  tools are not registered twice.

So the two halves of a loaded skill are lost for different reasons and recovered separately: tools
by the ceiling and a reload, text by a compaction and a reload. `TestSkillTool` and
`TestSkillOverlayRequestShape` pin both.

## What a reader can see

`AgentEvent.ContextComposition` - emitted before every provider call - carries `loadedSkills`, the
ids in load order. It rides through the SSE stream (`context_composition`) and is persisted on the
turn, so the transcript of a finished run says which skills each request paid for. It is the field
that attributes a share of `toolDefinitionTokens` to a `loadSkill` the user never saw.

## The three caching rules of this repository

1. **The `<environment>` block is stable** - day granularity, only facts marked `stable`. It sits in
   the system prompt, in front of everything. See `environment-block.md`.
2. **Per-turn facts go in the turn** - the `<turn-context>` block is a content block of the turn's
   user message, behind the whole cached prefix.
3. **Skill tools go behind the prefix** - appended in load order, never reordered, with the
   providers' second cache breakpoint on the boundary.
