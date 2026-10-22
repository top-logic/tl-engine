---
description: Read before changing what an agent run may cost or what its requests look like - the run limits (<capacity> on AgentRuntimeService with chat-workers, chat-queue, concurrent-runs-per-account, subagent-workers; <budget> on AgentPolicyService with token-budget, money-budget, budget-window-hours; RunLimits, BudgetAllowance), cost accounting (RunCosts, CostCalculator, CostConversion), and the three rules that keep the providers' prompt caches hitting (stable environment block, <turn-context>, stableToolCount).
order: 63
---

# Run limits and prompt caching

## Run limits

The ceilings an administrator may put on what this installation carries and what one account may spend (package `limits/`) are configured in `<capacity>` on `AgentRuntimeService` (the chat pool and the per-account cap) and in `<budget>` on `AgentPolicyService` (the token and money budget). **All of them are off by default**: the modules run in applications with a thousand users, and any figure they picked would refuse work the installation allowed before.

- **`chat-workers`** (0 = no ceiling) bounds `ChatRunExecutor`. At 0 it is a cached pool, a worker per send. With a ceiling, a send that finds every worker busy **waits**: `chat-queue` 0 is a queue of no fixed length, because somebody who asked a question wants the answer late rather than not at all. An installation that would rather refuse bounds the queue (`chat-queue="10"`) or keeps none (`chat-queue="-1"`); only then is a send refused, *before* anything is written down, as HTTP 429 with a sentence the client shows.
- **`concurrent-runs-per-account`** (0 = off, 3-5 recommended for a shared installation), **`token-budget`** and **`money-budget`** over `budget-window-hours` (0 = unlimited, per-role overrides possible) are enforced by `RunLimits.admit` in `AgentFactory.runAgent` - the choke point every entry point arrives at. These refuse rather than queue: the account is already being served.
- **Background subagents** queue rather than refuse: a chat's `SubagentJobRegistry` runs at most its cap (4) at once, and **`subagent-workers`** in `<capacity>` (0 = off) bounds them over the whole installation (`SubagentSlots`). A job beyond either stays `QUEUED`, starts in registration order when one ends, and the spawn answer says "queued, position N"; a job cancelled while queued ends `CANCELLED` without running.

A **delegated** run is neither counted nor budgeted separately: it is part of the run that delegated it and shares that run's `BudgetAllowance`. The budget is computed from the recorded `AgentExecution` rows by `AgentExecutionStore.spentSince`, on the store's writer thread; the headroom rides on `ToolContext.budgetAllowance()`, and a run that spends it stops **between turns** with `StopReason.BUDGET_EXHAUSTED` (recorded as `ExecutionStatus.REJECTED`). The turn limit of a run is `max-turns`, see [the agent loop](doc:ai/agent-loop#a-limit-ends-with-an-answer).

### Money

`AgentLoop` prices every request with the model that answered it (`RunCosts`, `CostCalculator` in package `cost/`: input, output, reasoning, cache read and cache write each at its own price, the price tier above 200k tokens per request), and the run records `cost` on its `AgentExecution` **in US dollars**, the currency of the prices. Prices come from the models.dev catalog or from the `*-price` attributes of a `<model-overrides>` entry on `LLMProviderService`; a model without a price costs 0.

A cost is converted only where it is shown or compared with `money-budget`: into the engine's system currency (`CurrencySystem`), at the dated rate of the engine's currency USD valid on the run's day, otherwise at that currency's conversion factor unless it is the 1 every currency is created with (`CostConversion`; *Administration > Basic administration > Currencies*). Without such a rate costs are shown in USD and the money budget is **not** checked - with a rate-limited WARN and a line on the monitor's token tile; the token budget still is. The monitor reads `AgentExecution#displayCost` / `#displayCurrency`. A refusal names the amounts in the user's number format. `tl-ai-core/doc/run-limits.md` has the details.

## Prompt caching

Every provider caches by prefix, over the rendering **tools → system prompt → messages**; Anthropic and Bedrock need an explicit breakpoint, OpenAI-compatible endpoints match the longest common prefix by themselves. Three rules keep the prefix stable, and a change that breaks one of them is a cost regression nothing else reports:

1. The `<environment>` block is **stable** - day granularity, only facts marked `stable`, and the memories it carries are the ones the conversation was opened with. See [the environment block](doc:ai/agent-loop#the-environment-block).
2. Facts that are not stable go **into the turn**, as the `<turn-context>` content block of the user message, behind the whole cached prefix.
3. A loaded skill's tools go **behind the stable tool prefix**, appended in load order and never reordered; `ProviderRequest.stableToolCount` carries that boundary, and the Anthropic and Bedrock request builders put their second breakpoint on it. See [skills and tool overlays](doc:ai/tools-and-skills#skills-and-tool-overlays).

The same reason makes failover switch a run's model for the rest of the run rather than for one turn ([failover](doc:ai/providers#model-health-and-failover)), and makes the final turn at `max-turns` keep the tool list.
