---
description: Read before changing multi-agent work in tl-ai-core or tl-chat - synchronous delegation (DelegationTool, delegate_to_*), background subagents (spawn_subagent, SubagentJobRegistry, SubagentRegistries, wait_for_subagent, SubagentResultDelivery), waking the lead (SubagentWaker), questions of a subagent (askUser, ask_lead, reply_to_subagent), progress and steering (subagent_status, send_to_subagent, cancel_subagent) and the subagent cards of the chat.
order: 64
---

# Delegation and subagents

## Synchronous delegation

`DelegationTool` (`tools/interaction/`) creates an isolated child run of another agent, offered to the model as `delegate_to_<agent>`, up to a delegation depth of 3. The parent passes its `TransactionScope` and its `SecurityContext`, so the child writes into the parent turn's transaction and can never act with more rights than the parent ([tool authorization](doc:ai/security/tool-authorization#background-and-delegated-runs)); the child's events are recorded in the parent's `ConversationLog`. The child's answer reaches the parent inside `<untrusted_subagent_report>`, because a worker repeats what it read.

## Background subagents

`spawn_subagent` is the asynchronous half: fire-and-forget - the turn ends, and the child works on in a `SubagentJobRegistry` bound to the session. Since nobody is there to answer an approval prompt for it, a spawned child is a background run, bounded by the `<background-allowlist>`; a spawn that could only come back empty-handed is refused with a reason ([capability notices](doc:ai/security/capability-notices)).

**The registries belong to the process, not to the chat** (`SubagentRegistries`, one per conversation, and in the chat one per window). `AgentRunner` binds the conversation's registry for every top-level run that has none bound, so TL-Script, the eval and scheduled runs can spawn too, and a chat window that drops or reloads a conversation leaves its children running. Only deleting the chat cancels them: `AgentExecutionStore.deleteSession` stops every registry of the conversation (`SubagentRegistries.removeConversation`: each window's, and the one of a run without a window), whichever path deleted it. A run borrows its registry, which is forgotten when the run ends with no work left in it. A chat window attaches to its registry while it listens and lets go when the window ends (the sub-session's unbound listener); a registry nobody has borrowed for 24 hours is cancelled and forgotten, so results of a closed window that no lead reads do not stay for the life of the process. After a restart the startup sweep fails the children's runs, and a reopened conversation ends their parts as failed (`InterruptedTranscript`). How many subagents run at once is a [run limit](doc:ai/limits-and-caching#run-limits).

### Delivering the result

A child's result reaches the parent on one of two paths, and both render it through `SubagentResultDelivery`, so the parent reads the same block either way: `wait_for_subagent` when the parent asked, and `AgentLoop.injectSubagentResults` at the start of the next turn when it did not.

The injection is a **`UserMessage`**, not a `SystemMessage`: no provider's request builder transmits a system message in the middle of a conversation. It opens with `SubagentResultDelivery.NOTICE`, which says in the text what the role cannot - the user did not write this. The child's own text sits inside `<untrusted_subagent_report>` on every path - injection, wait, status and the synchronous `delegate_to_*` result. Everything finishing in one gap becomes one message, in the order the parent started the subagents. Being an ordinary user message, it is compacted like any other turn content.

The same text is recorded on the turn as a `Part.SystemPart` of type `SUBAGENT_RESULT`: an assistant-side part, so the admin transcript shows it while the chat does not draw it as a user bubble. `ConversationLogReplay` turns that part back into the identical `UserMessage`; it is the one system event replay does not skip. `tl-ai-core/doc/subagent-results.md` has the details.

### Waking the lead

When the parent has no next turn coming, the chat **wakes** it: `SubagentWaker` (`tl-chat`) starts a lead turn that opens with the news - a finished child or an `ask_lead` question - instead of with a user message, drawn as a line in place of the bubble. There are at most five such turns between two messages of the user, none after Stop, and none while a lead turn runs (that turn reads the news itself). The line names each subagent and why it reported back - finished, failed, cancelled, or asks and what - and each name scrolls to that subagent's card: `AgentLoop.injectSubagentNews` says who was delivered, `AgentEvent.LeadWoken` carries it as `SubagentNews`, and the turn records it as a `LEAD_WOKEN` system part, which replay skips.

### Questions of a subagent

A background subagent asks the user - `askUser`, or an approval - through **its job's own store** (`SubagentJob.interactionStore()`), not the store of the turn that started it: that turn ends while the child works on, and the chat unbinds its store with it. The store names the child (`InteractionAsker`), and the chat registers it in `PendingInteractionRegistry` while the job runs, so the poll shows the question with the child's name and `/chat/respond` reaches it from any window of the account.

A child asks its lead with `ask_lead`, a tool only a background subagent's catalog has (`AgentFactory`, the `includeDelegation=false` branch): the question is parked on the job (`LeadQuestion`), the lead is shown it - enveloped - at the start of its next turn together with any results (`SubagentResultDelivery.deliveryText(finished, asking)`), or at once when it waits in `wait_for_subagent`, which a question ends early. The lead answers with `reply_to_subagent`, and the child goes on in the same turn. The question timeout ends the wait with "decide yourself and state the assumption"; a cancel ends it too.

### Progress and steering

What a running child does is kept on its job: the spawner feeds the child's events into a bounded `SubagentProgress` (turns, tokens, the last 20 tool calls with an argument summary and their outcome, the end of its last text), sets `childRunId` at `RunStarted` and hangs the child's `ConversationLog` on the job as its live log. `subagent_status(detail: summary|steps)` reads it, says `WAITING` and for what when the job's store holds a question or an approval, and puts what the child wrote inside `<untrusted_subagent_report>`; `GET /chat/subagents/{id}/transcript` serves the live log until the child ends.

The lead steers a running child with `send_to_subagent` and stops it with `cancel_subagent`. A message goes into the job's `SubagentInbox`, which the spawner binds on the child's thread; `AgentLoop.injectLeadMessages` drains it at the start of each child turn into one user message opening with "[Message from the lead agent]", announced as `AgentEvent.LeadMessageDelivered`, which records it as a `LEAD_MESSAGE` part in the child's own log and which replay turns back into the same message. A message that arrives while the child writes its answer gets it one more turn; the inbox closes in the same step as the check for that, so a later message is refused, and one that found no turn left (limit, failure, cancel) is named in the result the lead reads. A message is text only; it changes no tool, right or model.

### The subagent cards of the chat

The chat shows each child as a card at the tool call that started it: `SubagentStarted` and the job carry `parentToolCallId`, the browser keeps a child's live events per subagent id (`subagentLive`, never in the lead's transcript), and `thread.tsx` draws one `SubagentCard` per child under the steps of the turn that spawned it. The lead's log keeps one `Part.SubagentPart` per child - id, agent, task, parent call, run id, end state, a short summary - written at the start and ended in place, never the child's steps; a reloaded conversation rebuilds the card from it, and `GET /chat/subagents/{id}/transcript?runId=` opens the recorded run when the server holds no job. The lead's run has usually written its transcript before the child ends, so the end is written into that transcript as well (`AgentExecutionStore.replaceTranscriptPart`) before it is announced; the end frames carry `parentToolCallId` and `childRunId`, and every other window holding a copy of the chat reads it back with its next request. `TestSubagentEndInEveryWindow` pins it.

## A child compacts too

A subagent or a synchronous delegate brings no session memory, so `AgentLoop.run(Conversation, CompactionStrategy, ToolContext, AgentEventListener)` wraps its own conversation in a `ConversationMemory` sized to its model's window, with `CompactionStrategies.forDefaultModel()` - the strategy the chat's session memory uses. Both `DelegationTool.executeSync` and `AgentRunner.run(AgentLoop, ...)` (the spawner's path) run that way; `TestChildCompaction` pins it. See [memory and compaction](doc:ai/memory-and-citations).
