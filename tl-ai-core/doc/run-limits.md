# Run limits: how many runs at once, and how much they may spend

tl-ai is not a coding CLI. It runs inside a shared application server, on behalf of many users at
once, against metered provider APIs. Until #343 there was no ceiling of any kind and no way to ask
for one: the chat gave every send a thread of its own out of an unbounded pool, and no run was ever
stopped because the account it acted for had spent enough. One user's long analysis was
indistinguishable from a hundred.

This document is the configuration reference for the three ceilings an administrator may now set,
and the description of where each one is enforced. **It sets none of them.** An installation that
upgrades finds exactly the behaviour it had.

## The configuration

One section, `<run-limits>`, beside `<environment>` and `<user-memory>` on `LLMService`:

```xml
<config service-class="com.top_logic.tl.ai.core.provider.LLMService">
    <instance class="com.top_logic.tl.ai.core.provider.LLMService">
        <run-limits
            chat-workers="0"
            chat-queue="0"
            concurrent-runs-per-account="0"
            token-budget="0"
            budget-window-hours="24"
        >
            <role-budgets>
                <role-budget name="tl.admin" tokens="0"/>
            </role-budgets>
        </run-limits>
    </instance>
</config>
```

| Setting | Default | What it does |
|---|---|---|
| `chat-workers` | `0` = **no ceiling** | How many chat runs execute at the same time. `0` is a worker per send. |
| `chat-queue` | `0` = **queue of no fixed length** | How many sends may wait for a free worker, once there is a ceiling. `-1` keeps no queue and refuses instead. |
| `concurrent-runs-per-account` | `0` = off | How many runs one account may have going, over every entry point. |
| `token-budget` | `0` = unlimited | How many tokens one account may spend in the window. |
| `budget-window-hours` | `24` | How far back the budget counts. A rolling window, not a calendar day. |
| `<role-budget name tokens>` | none | A budget for the holders of one role. `tokens="0"` is unlimited. |

### Nothing is limited until it is configured

All three ceilings are off out of the box, and that is the decision this feature turns on. tl-ai is
deployed into applications with a thousand users on hardware this product knows nothing about; every
figure it could pick for them - how many runs at once, how many per account, how many tokens - would
refuse work that the installation allowed the day before the upgrade. The administrator who knows
the machine and the provider contract names the numbers, and until they do, the behaviour is exactly
what it was before #343.

What this feature ships is therefore the *ability* to set a ceiling, plus one decision about what
happens at it:

**A ceiling makes a run wait, it does not turn it away.** With `chat-workers="32"` and the default
`chat-queue="0"`, the 33rd send of the moment waits for a worker and its answer arrives late. That
is what somebody who asked a question wants; "too many runs in progress, please try again" is not,
and it is worse than useless at a moment when the installation is busy enough that they will get the
same answer if they do try again. An installation that would rather refuse says so - `chat-queue`
to a length it accepts, or `-1` for no waiting room at all - and the refusal path below is then what
a user meets.

For a shared installation, `concurrent-runs-per-account="3"` to `"5"` is the setting to reach for
first: it is the one that stops a single account from taking every worker, and unlike a global
ceiling it costs an ordinary user nothing.

The section is read once per question, from the running `LLMService`, through `RunLimitSettings`. As
with `UserMemorySettings`, every accessor answers the shipped default when the service is not up, so
a unit test or a boot step gets a number rather than an exception. The **pool size and the queue**
are read once, when the pool is first used: changing them needs a restart.

## Where each limit is enforced

### 1. The pool - `ChatRunExecutor`

`ChatProcessor.BACKGROUND_EXECUTOR`, a `static` `newCachedThreadPool()` in a layout class, is gone.
The pool is now `ChatRunExecutor`, and which pool it builds is the configuration's decision:

| `chat-workers` | `chat-queue` | The pool |
|---|---|---|
| `0` (default) | - | `0 / MAX_VALUE`, `SynchronousQueue` - the cached pool, spelled out. A worker per send, an idle one let go after a minute. Nothing is ever refused. |
| `n` | `0` (default) | `core == max == n`, `LinkedBlockingQueue` of no fixed length. n run, the rest wait. Nothing is refused. |
| `n` | `q > 0` | `core == max == n`, `LinkedBlockingQueue(q)`. n run, q wait, the next is refused. |
| `n` | `-1` | `core == max == n`, `SynchronousQueue`. n run, the next is refused. |

`allowCoreThreadTimeOut` is on for the bounded pools, so a quiet installation does not hold the
pool's worth of stacks overnight; the cached pool has a core of zero and needs no such setting.

It is a class and not a TopLogic service on purpose: a thread pool has no lifecycle of its own
beyond the application's, nothing to inject and nothing for another component to look up, and making
it a `ConfiguredManagedClass` would add a service every application has to register in order to get
something it cannot run without. What it did need was a *configured* size, and that lives where the
installation's other ceilings live.

**Queue-full behaviour is explicit**, and only the bottom two rows of that table can reach it.
`ChatProcessor.sendMessage` asks `ChatRunExecutor.hasRoom()` *before* it writes anything down -
before the user's message is added to the conversation, before it is persisted, before a turn is
opened - and answers `SendResult.rejected(reason)`. `ChatServlet`
turns that into **HTTP 429** with `{"status":"rejected","reason":"…"}`; the React client throws a
`RefusedError` (a `ForbiddenError`, so every caller that already shows a refusal as information
shows this one too) and the composer puts the sentence in the notice strip, not sticky - the answer
is to send again in a moment. The classic control shows it as a system bubble.

The check is advisory, because between it and the submit another request can take the last place.
That path is caught too: `ChatRunExecutor.submit` throws `NoRoom`, and there - the only place where
the user's message is already in the conversation - the refusal is said in the conversation, under
their own message.

### 2. The per-account concurrency cap - `RunLimits.admit`

Enforced in `AgentFactory.runAgent`, which is the choke point every entry point arrives at: the chat,
`AgentRunner`, the scheduler and the TL-Script functions. The run holds a `RunLimits.Admission` in a
try-with-resources and frees its slot when the loop returns.

Two runs are **not** counted:

- A run that cannot name a principal (`RunAuthorization.principal() == null`: a test context, a boot
  step). There is nobody to attribute it to, and refusing it would take the framework's own runs
  down with the users'.
- A **delegated** run - `ToolContext.delegationDepth() > 0`. It is part of the run that delegated it,
  which was already counted. Counting it again would make a user's ceiling depend on how an agent
  happens to be built: a cap of 3 would refuse the first agent that delegates three times.

Unlike the pool, this cap **refuses**; there is no queue behind it. A run held back until the
account's other runs finish is a run whose own ceiling did the waiting, and the account is by
definition already being served - which is not the case when the installation as a whole is full.

Today a delegation does not reach `runAgent` at all: `DelegationTool` builds its loop through
`AgentLoopFactory.buildAgentLoop` and runs it directly, and `SubagentSpawner` goes through
`AgentRunner.run(loop, …)`. The depth check in `admit` is therefore not what makes delegation free
today - it is what keeps it free if a future path does route a child through `runAgent`. What makes
a delegate spend its parent's budget rather than its own is that its `ToolContext` carries the
parent's `TokenAllowance` (`ToolContext.forDelegation`, `forDetachedSubagent`).

The count is in memory, per node, keyed by the account's object name. That is the same scope the
thread pool it shares has; a cluster-wide count would need a store, and the ticket's point - that one
account cannot take a node down - is answered per node.

`ChatProcessor.sendMessage` pre-checks it the same way it pre-checks the pool, so the ordinary case
is a 429 with a sentence rather than a turn that fails.

### 3. The token budget - `TokenAllowance`

Also decided in `RunLimits.admit`, and in two halves:

**At run start.** `AgentExecutionStore.tokensSpentSince(owner, since)` sums every recorded run of
that account which *started* within the window - prompt including its cached part, output and
thinking. Every execution row counts once, delegated children included: they carry their own usage
and belong to the same session, so summing rows is the account's true spend; a sum that added each
parent's children to the parent would count them twice here. The read happens **on the
store's writer thread**, in a fresh interaction at the current revision, for the reason #405
established: the calling thread's view is pinned to an older one.

`budget - spent` is the run's headroom. Nothing left means the run does not start:
`RunLimitExceededException`, which `ChatProcessor` turns into the agent's own answer - the user is
looking at the transcript, and `notifyError` would report a ceiling as a broken application.

**Between turns.** The headroom travels as a `TokenAllowance` on the run's `ToolContext`.
`AgentLoop` records every assistant turn's usage into it and, at the end of each turn body, stops
with `StopReason.BUDGET_EXHAUSTED` when it is spent. Between turns and never inside one: the turn
that has begun is finished and its tool results are written, so the history the next run continues
from is whole. An admitted run therefore always gets at least one turn.

A **delegated** run shares its parent's `TokenAllowance` instance rather than getting one of its
own - which is what stops delegation from multiplying the budget.

The budget needs run recording. An installation without an `AgentExecutionStore` has nothing to
count, and a budget that cannot be read admits the run and logs an error: refusing runs for a spend
nobody measured is the wrong direction.

**What it costs.** One pass over the execution rows per run start, on the writer thread. That is the
reason the budget is off unless an administrator asks for it, and the reason the chat's pre-check
does not ask it a second time.

### Role budgets

`RunLimitSettings.tokenBudget(heldRoleNames)` takes the **largest** figure among the roles the
account holds, falls back to `token-budget` when it holds none of them, and treats a role's `0` as
unlimited. A role therefore grants headroom and never takes it away, which is the only reading under
which holding one more role cannot make an account worse off.

It is deliberately this small: a table of numbers, not a place to express *when* a budget applies.

## What an administrator sees

There is no new view. A run that was cut off by the budget is an ordinary row in the run monitor:
`stopReason` is `BUDGET_EXHAUSTED` and `status` is `REJECTED` - the same family as a response the
validator refused, which keeps the error tile honest about what actually broke.

A run that was **refused** leaves no row, because no run happened. Every refusal is logged at INFO
by `RunLimits` and by `ChatProcessor`, naming the account and the reason:

```
Permission to run denied: account 'jdoe' already has 3 agent runs in progress, which is this
  installation's limit.
Permission to run denied: account 'jdoe' spent 2145300 tokens in the last 24h, which is its budget
  of 2000000.
Chat run refused: all 32 chat workers are busy and the queue of 10 is full.
```

The last of the three only appears in an installation that configured both a ceiling and a bounded
queue; the shipped one has neither.

## What the user sees

Four sentences, shipped in English and German (`com.top_logic.tl.ai.core.limits.I18NConstants`):

| When | Text |
|---|---|
| The pool is full | Too many agent runs are in progress right now. Please try again in a moment. |
| The account is at its cap | You already have {0} agent runs in progress. Please wait for one of them to finish. |
| The budget is spent at run start | Your agent budget for the last {0} hours is used up. It frees up again as those hours pass; an administrator can raise it. |
| The budget ran out mid-run | The answer is incomplete: your agent budget for the last {0} hours ran out while this run was working. |

None of them names the configured figure. It is an administrator's setting, and quoting it invites
the user to argue with a number they cannot change.

## Known limits of this implementation

- **Per node, not per cluster.** Both per-account ceilings count what this server carries.
- **A detached subagent outlives its parent's slot.** `spawn_subagent` returns immediately, the
  parent run ends and frees its slot, and the child keeps working on a pool thread of
  `SubagentJobRegistry`. It is still not counted as a run of its own (it is a delegate) and it still
  spends the parent's allowance, but the concurrency cap does not see it.
- **`AgentLoop.TOOL_WORKERS` and `SubagentJobRegistry`'s pool are still unbounded cached pools**,
  with no setting to bound them. Only the chat's run pool can be given a ceiling here.
- **A queued send holds nothing but a task.** The waiting user sees the composer busy and the run
  has not started, so it has spent no tokens and holds no conversation - which is why the default
  queue needs no length. What it does not do is tell the user *why* the answer is slow; a "waiting
  for a free worker" notice is a follow-up, not part of this.
- **The budget is a scan.** See "What it costs" above.
