---
description: Read before changing what an agent run may cost or what its requests look like - the run limits (<capacity> on AgentRuntimeService with chat-workers, chat-queue, concurrent-runs-per-account, subagent-workers; <budget> on AgentPolicyService with token-budget, money-budget, budget-window-hours; RunLimits, BudgetAllowance), cost accounting (RunCosts, CostCalculator, CostConversion), and the three rules that keep the providers' prompt caches hitting (stable environment block, <turn-context>, stableToolCount).
order: 63
---

# Run limits and prompt caching

## Run limits

The ceilings an administrator may put on what this installation carries and what one account may spend (package `limits/`) are configured in `<capacity>` on `AgentRuntimeService` (*Agent runtime* in the service editor) and in `<budget>` on `AgentPolicyService` (*Agent policy*). **All of them are off by default**: the modules run in applications with a thousand users on hardware they know nothing about, and any figure they picked would refuse work the installation allowed before.

```xml
<config service-class="com.top_logic.tl.ai.core.agent.AgentRuntimeService">
	<instance class="com.top_logic.tl.ai.core.agent.AgentRuntimeService">
		<capacity chat-workers="0" chat-queue="0" concurrent-runs-per-account="0" subagent-workers="0"/>
	</instance>
</config>

<config service-class="com.top_logic.tl.ai.core.agent.AgentPolicyService">
	<instance class="com.top_logic.tl.ai.core.agent.AgentPolicyService">
		<budget token-budget="0" money-budget="0" budget-window-hours="24">
			<role-budgets>
				<role-budget name="tl.admin" tokens="0" money="0"/>
			</role-budgets>
		</budget>
	</instance>
</config>
```

| Setting | Default | What it does |
|---|---|---|
| `chat-workers` | `0` = no ceiling | how many chat runs execute at the same time |
| `chat-queue` | `0` = a queue of no fixed length | how many sends wait for a free worker once there is a ceiling; `-1` keeps no queue and refuses instead |
| `concurrent-runs-per-account` | `0` = off | how many runs one account may have going, over every entry point; 3-5 is the setting to reach for first in a shared installation |
| `subagent-workers` | `0` = off | how many background subagents run at once over the whole installation |
| `token-budget` | `0` = unlimited | tokens one account may spend in the window |
| `money-budget` | `0` = unlimited | money one account may spend in the window, in the system currency; checked only while a rate of USD is valid, see [money](#money) |
| `budget-window-hours` | `24` | how far back both budgets count - a rolling window |
| `<role-budget name tokens money>` | none | a budget for the holders of one role, `0` unlimited; an unset attribute says nothing about that budget, and an entry that sets neither stops the start of `AgentPolicyService` |

The settings are read through `RunLimitSettings` from the running services, so a changed ceiling takes effect when `AgentRuntimeService` is restarted and a changed budget when `AgentPolicyService` is; with the service not up, every accessor answers the shipped default. The pool size and the queue are read when the pool is first used: changing them needs an application restart.

### The chat pool

`ChatRunExecutor` (`tl-chat`) is the pool the chat's runs execute on, and the configuration decides which pool it builds:

| `chat-workers` | `chat-queue` | The pool |
|---|---|---|
| `0` | - | a cached pool: a worker per send, an idle one let go after a minute; nothing is refused |
| `n` | `0` | `n` workers, an unbounded queue; the rest wait, nothing is refused |
| `n` | `q > 0` | `n` workers, `q` wait, the next is refused |
| `n` | `-1` | `n` workers, the next is refused |

A ceiling makes a send **wait**, because somebody who asked a question wants the answer late rather than not at all. Only the last two rows refuse: `ChatProcessor.sendMessage` asks `ChatRunExecutor.hasRoom()` before it writes anything down and answers `SendResult.rejected(reason)`, which `ChatServlet` turns into HTTP 429 with `{"status":"rejected","reason":"..."}`; the React client throws a `RefusedError` and shows the sentence in the composer's notice strip. Between that check and the submit another request can take the last place; `ChatRunExecutor.submit` then throws `NoRoom`, and the refusal is said in the conversation, under the user's message.

### The per-account cap and the budgets

`RunLimits.admit` in `AgentFactory.runAgent`, the choke point every entry point passes, enforces the per-account cap and both budgets. The run holds a `RunLimits.Admission` and frees its slot when the loop returns. These **refuse** rather than queue: the account is already being served. The chat pre-checks the cap and the budget the same way it pre-checks the pool (`RunLimits.budgetRefusal` from `ChatProcessor.noRoomReason`), so the ordinary case is a 429 with a sentence rather than a run that writes no answer; the pre-check and the run start share one reading of the spend (`RunLimits.SpendReading`, `RunLimits.reuse`).

Not counted are a run without a principal (a test context, a boot step) and a **delegated** run: it is part of the run that delegated it. A delegate spends its parent's budget because its `ToolContext` carries the parent's `BudgetAllowance` (`ToolContext.forDelegation`, `forDetachedSubagent`), which keeps delegation from multiplying the budget. The count is in memory, per node, keyed by the account.

**The token budget.** At run start `AgentExecutionStore.spentSince(owner, since)` sums every recorded run of the account that started within the window - prompt including its cached part, output including reasoning, every execution row once (delegated children carry their own usage). The read runs on the store's writer thread in a fresh interaction, because the calling thread's view is pinned to an older revision, and finds the rows through the session's owner, so it does not grow with the other accounts' runs; while no budget is configured, no read happens. `budget - spent` is the run's headroom; nothing left refuses the run (`RunLimitExceededException`). The headroom rides on `ToolContext.budgetAllowance()`; `AgentLoop` records every turn's usage and cost into it and stops with `StopReason.BUDGET_EXHAUSTED` (recorded as `ExecutionStatus.REJECTED`) at the end of the turn that spent it - never inside one, so the history stays whole and an admitted run always gets one turn. Without an `AgentExecutionStore` there is nothing to count: a budget that cannot be read admits the run and logs an error.

**Role budgets.** `RunLimitSettings.tokenBudget(heldRoleNames)` and `moneyBudget(heldRoleNames)` take the largest figure among the roles the account holds, a role's `0` being unlimited, and fall back to `token-budget` / `money-budget` for an account holding none of them. A role that sets only `tokens` does not change the money budget of its holders. A role grants headroom and never takes it away.

**Background subagents** queue rather than refuse: a chat's `SubagentJobRegistry` runs at most four at once, and `subagent-workers` bounds them over the installation (`SubagentSlots`). A job beyond either stays `QUEUED`, starts in registration order, and the spawn answer says "queued, position N"; a job cancelled while queued ends `CANCELLED` without running. The turn limit of a run is `max-turns`, see [the agent loop](doc:ai/agent-loop#a-limit-ends-with-an-answer).

### Money

`AgentLoop` prices every request when it ends, with the model that answered it (a failover moves the run to another price list). `CostCalculator` (package `cost/`) multiplies each kind of token by its own price; prices are US dollars per million tokens:

| Tokens (`TokenUsage`) | Price | Without that price |
|---|---|---|
| `inputTokens` - the prompt without its cached part | input | - |
| `cacheReadTokens` | cache read | input |
| `cacheWriteTokens` | cache write | input |
| `thinkingTokens` - a share of `outputTokens` | reasoning | output |
| the rest of `outputTokens` | output | - |

Some models have a second price list for a prompt above 200 000 tokens (`context_over_200k` in the catalog), which applies to all tokens of that request - the reason the cost is computed per request. Prices come from the models.dev catalog (`ModelsDevFetcher`; shown in the read-only model metadata of `LLMProviderService`) or from a `<model-overrides>` entry of `LLMProviderService` with `input-price`, `output-price`, `cache-read-price`, `cache-write-price` and `reasoning-price`; an entry that sets an input or an output price replaces the catalog prices of that model. A model without a price costs 0, so a local model is free until it gets a price; `RunCosts` writes one INFO line per model without one.

```xml
<model-overrides>
	<model name="qwen3-32b" input-price="0.20" output-price="0.60"/>
</model-overrides>
```

A run records `cost` on its `AgentExecution` **in US dollars**, unconverted. A cost is converted only where it is shown or compared with `money-budget`: into the engine's system currency (`CurrencySystem`), at the rate of the engine's currency USD for the day the run started (`CostConversion`) - a dated exchange rate valid on that day (`Currency.getConversionFactor(Date)`, entered by a data import or a script, `Currency.saveExchangeRatio`), otherwise the conversion factor of USD (*Administration > Basic administration > Currencies*), unless it is the 1 every currency is created with. An amount is converted as amount × factor(own) / factor(target): with the system currency EUR, a factor of 0.92 makes one dollar 0.92 euro. A system currency in US dollars needs no rate.

**Without a rate nobody is blocked**: costs are shown in USD and the money budget is not checked; the token budget still is. A WARN says so at the start of `AgentPolicyService` and, at most once an hour, when a check is skipped; the monitor's token tile shows the same sentence (`aiCostWarning()`). `AgentExecutionStore.spentSince` converts each run's cost at the rate of its day; a run without a rate for its day leaves the money budget unchecked. The run that starts counts its own costs at today's rate (`MoneyBudget.usdRate` on its `BudgetAllowance`).

### What an administrator and a user see

The run monitor shows the cost next to the tokens: per run `displayCost` / `displayCurrency` (converted at the rate of the run's day, or USD without one) and the recorded USD cost, per session the cost per currency, the cost of the period on the overview's token tile, and the ten accounts with the highest cost on the usage page. A run cut off by a budget is an ordinary row with `stopReason` `BUDGET_EXHAUSTED` and `status` `REJECTED`. A refused run leaves no row; `RunLimits` and `ChatProcessor` log every refusal at INFO, naming the account and the reason:

```
Permission to run denied: account 'jdoe' already has 3 agent runs in progress, which is this installation's limit.
Permission to run denied: account 'jdoe' spent 2145300 tokens in the last 24h, which is its budget of 2000000.
Chat run refused: all 32 chat workers are busy and the queue of 10 is full.
```

The user reads one of six sentences (`com.top_logic.tl.ai.core.limits.I18NConstants`): the pool is full, the account is at its cap, the token budget is spent at run start or ran out mid-run, the money budget is spent at run start or ran out mid-run. The token and run sentences do not name the configured figure; the money sentences name the spent amount and the budget, in the account's number format ("Budget used up: €12.40 of €15.00 in the last 24 hours."). After a stopped run the chat asks `RunLimits.budgetStopNotice` which budget ran out.

### Boundaries

- **Per node, not per cluster**: both per-account ceilings count what this server carries.
- **A detached subagent outlives its parent's slot**: the parent run ends and frees its slot while the child works on a pool thread of `SubagentJobRegistry`. It still spends the parent's allowance; what bounds it is the subagent ceiling, not the per-account cap.
- **A queued send holds only a task**: it has started no run and spent no tokens, which is why the default queue needs no length; the user sees the composer busy.
- **A cost is an estimate**: reported tokens times a public price list. Discounts, batch prices and price changes after the catalog snapshot do not show; audio tokens are not priced.

## Prompt caching

Every provider caches by prefix, over the rendering **tools → system prompt → messages**; Anthropic and Bedrock need an explicit breakpoint, OpenAI-compatible endpoints match the longest common prefix by themselves. Three rules keep the prefix stable, and a change that breaks one of them is a cost regression nothing else reports:

1. The `<environment>` block is **stable** - day granularity, only facts marked `stable`, and the memories it carries are the ones the conversation was opened with. See [the environment block](doc:ai/environment#the-environment-block).
2. Facts that are not stable go **into the turn**, as the `<turn-context>` content block of the user message, behind the whole cached prefix.
3. A loaded skill's tools go **behind the stable tool prefix**, appended in load order and never reordered; `ProviderRequest.stableToolCount` carries that boundary. `AnthropicRequestBuilder` puts a `cache_control` on the last stable tool and `BedrockConverseRequestBuilder` a `cachePoint` after it - only while a skill is loaded, because with nothing behind the boundary the breakpoint on the system block covers the whole array, and a request may carry four breakpoints at most. Whether a breakpoint produces a cache entry is the model's decision: a prefix below the model's minimum cacheable length (512 to 4096 tokens, by model) is not cached, which is no error. See [skills and tool overlays](doc:ai/tools-and-skills#skills-and-tool-overlays).

The same reason makes failover switch a run's model for the rest of the run rather than for one turn ([failover](doc:ai/providers#model-health-and-failover)), and makes the final turn at `max-turns` keep the tool list.
