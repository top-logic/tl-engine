# tl-ai-eval

The same business tasks, put to several models through our own agent loop, graded against the
database they left behind.

The question this module answers is **not** "which model is best". It is "which models work *with
our harness*" - our tool descriptions, our system prompts, our skills, our approval flow. A model
that scores well on a public function-calling benchmark can still fail in front of ninety tools it
has never seen described this way, and that is the failure this finds.

Issue: [#433](https://git.top-logic.com/TopLogic/tl-ai/issues/433).

---

## What is in it

| Piece | Where |
|---|---|
| Scenario model (`<eval>`, `<arm>`, `<scenario>`, `<kb-query>`, `<tool-sequence>`, `<presented>`) | `EvalConfig`, `EvalScenario`, `Expectation` and its three kinds |
| Loading and checking a scenario file | `EvalConfigs` |
| One run of one scenario on one arm | `EvalRunner`, `RunObservation` |
| The whole matrix | `EvalMatrix`, `EvalFixture` |
| Deciding whether it worked | `Graders`, `Verdict`, `RunRecord` |
| Output | `EvalCsv` (raw), `EvalReport` (aggregate) |
| The `pm` fixture and its seven scenarios | `src/test/java/.../PmFixture.java`, `src/test/resources/.../pm-scenarios.config.xml` |
| A model whose answers a test writes | `src/test/java/.../ScriptedProvider.java`, `EvalScripts` |

The runner is production code; the `pm` fixture and its scenarios are test resources. That split is
deliberate: the harness is meant to be pointed at an application's own model and its own scenario
file, while `pm` is this module's own test data - two classes and four rows, small enough that every
expectation can be checked by eye.

---

## Running it

**The plumbing, in every build, at no cost and with no key.** Nothing has to be enabled:

```bash
mvn -pl tl-ai-eval test -DskipTests=false
```

This runs the shipped scenario file end to end against `ScriptedProvider`, a model whose answers
`EvalScripts` writes - through the real `AgentRunner`, the real agent definitions, the real tool
registry, the real approval gate and a real H2 knowledge base. It proves the seven scenarios are
solvable, that every TL-Script expression in the file still compiles and evaluates, and that the
near-misses each scenario exists to catch are in fact caught.

**The real models**, which spend real money:

```bash
source env.sh                      # for ANTHROPIC_API_KEY / tl_anthropic_api_key
mvn -pl tl-ai-eval test -DskipTests=false -Dtl.ai.eval=true -Dtest=TestEvalPmMatrix
```

Seven scenarios × three arms × three repeats. The CSV lands in `target/eval/pm-<timestamp>.csv`
(`-Dtl.ai.eval.out=<file>` puts it elsewhere) and the report is printed. Without
`-Dtl.ai.eval=true` the class reports as **skipped**, never as passed. With it but without a key,
the real arms report as "could not run" - the provider refused *us*, so the model was never asked -
and the scripted arm still exercises the whole pipeline.

---

## Adding a scenario

A scenario is five things: an id, a prompt, an oracle, the expectations, and - optionally - which
arms and how many repeats.

```xml
<scenario id="close-stale-tickets">
    <prompt>Set the status of every ticket of the project Alpha to triaged.</prompt>
    <oracle><![CDATA[listInstances of pm:Project to find Alpha, getInstance to read its two
tickets, then one changeData setting the status of those two - and only
those two. Beta's ticket keeps status "open".]]></oracle>
    <expectations>
        <kb-query label="exactly-alphas-two-tickets-are-triaged">
            <expression><![CDATA[all(`pm:Ticket`).filter(t -> $t.get(`pm:Ticket#status`) == "triaged").size()]]></expression>
            <expected>2</expected>
        </kb-query>
        <tool-sequence label="wrote-through-the-write-tool" required="script-exec.changeData"/>
    </expectations>
</scenario>
```

Four rules, all of them enforced by `EvalConfigs.check` or by a test, because each of them is a way
to get a green matrix that means nothing:

1. **Write the oracle.** It is mandatory, and it is the reference solution in prose: the tool calls a
   competent operator would make, and why the expectations below are the right ones. Without one, a
   scenario every model fails is indistinguishable from a scenario nobody can do - which is how
   published agent benchmarks ended up with tasks that were simply impossible.
2. **Grade the database, not the transcript.** A transcript records what a model claimed; only the
   data records what happened. `<kb-query>` is the primary grader for that reason, and
   `<tool-sequence>` is there for the half the data cannot show - "it never reached for the delete
   tool" looks, in the data, exactly like "it crashed before its first turn".
3. **Say something a run that did nothing would fail.** A scenario made only of prohibitions is
   refused at load time. Pair every `forbidden` with a `<kb-query>` on the state the task should
   leave behind.
4. **Make it discriminate.** `exactly-alphas-two-tickets-are-triaged` counts rather than checking
   Alpha's two, because the interesting failure is a model that triages all three. An expectation
   that only confirms the happy path catches nothing.

Then teach the scripted model to solve it, in `EvalScripts.oracle()`, and add the near-miss you
expect a real model to make. That is what keeps the zero-cost build honest.

**A task that shows rather than writes** (#616) - "show me all tickets as a table", "tickets per
project as a bar chart" - leaves the database as it was, so rule 2 has nothing to grade. What it
produced is the presentation, and `<presented>` grades that: it takes the run's last `present` call,
evaluates the query the model passed (or resolves its ids) itself, and hands the objects - and for
a chart the values the model's function computes for them - to a TL-Script function
`rows -> values -> ...` whose result is compared like a `<kb-query>`'s. `as` and `chart-kind` say
how they must have been shown. The other half is the price: `max-calls` on `<tool-sequence>` bounds
the whole run, every call counted. An eval run has no page, so the arms carry `EvalPresentTool` -
the same checks as a chat page's `present`, without the page.

**A new arm** is one block in the scenario file plus one agent definition and one model assignment
in `src/test/webapp/WEB-INF/conf/tl-ai-eval.test.config.xml` and
`.../autoconf/com.top_logic.tl.ai.core.agent.AgentRuntimeService.config.xml`, whose
`<agent-overrides>` assign each arm its model and which also names the default model. The
`LLMProviderService` file next to it,
`.../autoconf/com.top_logic.tl.ai.core.provider.LLMProviderService.config.xml`, keeps the providers
and the model catalog. One agent definition per
model is not a style choice: a per-run model override exists in Java, but the shipped policy
defaults to `model-access default="assigned"` and refuses a model the agent is not assigned. The
copies of the system prompt that this costs are held equal by `TestEvalArmsAreComparable` - an arm
answering a different prompt is not a measurement of its model.

---

## Reading the CSV

One line per run, in the order the matrix ran them.

| Column | Meaning |
|---|---|
| `scenario` | the scenario id |
| `arm` | the arm name |
| `model`, `agent` | what that arm is |
| `repeat` | 1-based repeat within the cell |
| `outcome` | `PASS`, `FAIL`, `ERROR` or `COULD_NOT_RUN` |
| `turns` | agent-loop iterations |
| `inputTokens` | prompt tokens **including** the cached share |
| `cacheReadTokens` | the cached share of `inputTokens` |
| `outputTokens` | completion tokens, thinking excluded |
| `durationMs` | wall clock of the run |
| `stopReason` | the loop's stop reason, or `NOT_RUN` |
| `toolCalls` | every tool the model asked for, in order, separated by `\|` |
| `failedGraders` | labels of the expectations that did not hold, separated by `\|` |
| `error` | the exception, or why the arm was not started |

Three things about those columns are worth knowing before anybody averages them.

**`COULD_NOT_RUN` is not a failure.** A model whose catalogue entry says it cannot call tools, or a
provider that refuses our key, means the scenario never reached the model. Those rows are excluded
from both pass rates rather than counted as zeros; an arm with no rows left prints `-`, not `0.00`.

**A run passes only if the loop finished.** Every run carries an unwritten expectation,
`run-completed`: a run that threw, hit the turn limit or was aborted fails whatever the data looks
like. Without it, a refusal scenario - which asserts that nothing happened - would be passed by a
run that died in its first turn.

**`inputTokens` is cache-inclusive** because that is what a context window and a price are measured
against, and `cacheReadTokens` stands beside it because prompt caching otherwise makes one route
look cheaper than another for the same work.

The report prints, per cell, **k of n** - never a single pass or fail - and per arm two rates:

- **pass@1**, the share of individual runs that passed: what a user gets on one attempt.
- **pass^k**, the share of cells in which *all* k repeats passed: what a user gets if the task has to
  work every time, which is what an agent inside a business application is actually asked for.

They differ where it matters. An arm that passes two runs of three scores 0.67 and 0.00, and the
second number is the honest one for an unattended workflow. Tokens and milliseconds are printed in
the same row, because a ranking on accuracy alone is not a decision: an arm three points better at
four times the price is not better.

---

## What these numbers may not be used for

**Ranking two arms that both mostly work.** With a handful of scenarios this eval can show that an
arm is *unusable* with our harness - it cannot call our tools, it ignores our policy, it writes more
than it was asked. It cannot tell a three-point difference from noise.

The arithmetic is not close. Detecting a three-percentage-point difference between two models at 80%
power needs on the order of **1,000 scenarios**; repeats barely help, because at 198 questions
raising the repeat count from 1 to 10 moves the smallest detectable difference only from 13% to 7.5%
(Miller, *Adding Error Bars to Evals*, arXiv 2411.00640). **Scenario count, not repeat count, is what
buys resolution.** Repeats buy something else, and it is the thing pass^k reports: reliability.

Nor is lowering the temperature a way out. It does not remove the variance, it moves it into a part
that cannot be reduced, and it can bias the result outright - which is why this module repeats runs
instead.

---

## Deliberately not in this version

Follow-ups, each of which changes what the report can say:

- **Cost in currency.** The price catalogue is already fetched and parsed (`ModelInfo.cost()`), and
  nothing in the codebase reads it. Tokens × price, including the cache tiers and the ">200k" tier,
  with "no price known" as a first-class outcome rather than zero.
- **An answer grader.** There is deliberately no scenario here whose whole answer is a sentence,
  because the two graders are both about the database. A read-only question needs an answer matcher
  or a judge model - and a judge has to be pinned, kept off the primary metric and never allowed to
  judge its own family, because self-enhancement bias is documented and would contaminate exactly the
  cross-vendor comparison this exists for.
- **An `askUser` responder.** An unattended run that reaches `askUser` blocks until the interaction
  timeout. No scenario here triggers it; one that did would need a watcher answering from a script.
- **Resetting a live application.** The fixture here is a fresh H2 knowledge base per suite, reseeded
  before every single run. Doing the same against a running application is a separate problem, and
  the demo's `reset.tlscript` is what exists today.
- **A report UI.** Every run is already an `AgentExecution` row, so the agent activity monitor can
  show a transcript; what is missing is a view that groups them by scenario and arm.
