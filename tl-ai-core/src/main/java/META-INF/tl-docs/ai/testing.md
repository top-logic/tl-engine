---
description: Read before adding or running tests of the AI modules (tl-ai-core, tl-chat, tl-ai-mcp-server, tl-ai-eval, ...) - where the tests live (packages mirroring the production code), the two surefire %regex[] includes in each module POM, TestSuiteReachability, the command forms (mvn test -DskipTests=false -pl tl-ai-core, -Dtest with a fully qualified class name), and the opt-in gates tl.ai.browser.e2e, tl.ai.visual, tl.ai.injection.e2e, tl.ai.vector.e2e, tl.ai.index.model.e2e, tl.ai.eval and tl.ai.research.e2e.
order: 80
---

# Testing the AI modules

## Running the tests

As everywhere in the engine, tests are skipped by default; `-DskipTests=false` turns them on. One command per module runs everything a machine can run without a key, a browser or an external service:

```bash
mvn test -DskipTests=false -pl tl-ai-core
mvn test -DskipTests=false -pl tl-ai-core,tl-chat,tl-ai-mcp-server
```

A single test class is selected with `-Dtest`, which must name the **fully qualified** class name (several are separated by commas):

```bash
mvn test -DskipTests=false -pl tl-ai-core -Dtest=com.top_logic.tl.ai.core.agent.TestAgentLoop
```

## Where the tests are and how they are found

The engine's `tl-parent-core` configures surefire with the single include `test/TestAll.java`, the engine's generic suite, which collects everything below `src/test/java/test`. The AI modules keep most of their tests in packages that **mirror the production code** instead - a test of `com.top_logic.tl.ai.core.agent` sits in `com.top_logic.tl.ai.core.agent`, not in `test.com.top_logic.tl.ai.core.agent`. So every AI module repeats two includes in its own POM (see the comment in `tl-ai-core/pom.xml`):

| Include | Runs |
|---|---|
| `%regex[test[/.]TestAll(\.class)?]` | the engine's generic suite `test/TestAll.java` (file names, layout normalization, JSP content, I18N constants, migration-script consistency, the developer documentation) **plus** everything below `src/test/java/test/`, recursively |
| `%regex[com[/.]top_logic[/.](ai\|tl[/.]ai)[/.](.*[/.])?(Test[A-Z0-9][^/.$]*\|[^/.$]*Test)(\.class)?]` | the module's own tests in the mirrored packages, named `Test<Something>` or `<Something>Test` |

Both are regular expressions, and each accepts a class in two spellings: as the class file path and as the class name. Surefire selects the classes by their path and then filters that selection again in the test JVM, where it matches a `%regex[]` include against the class name; with the path spelling alone, or with `test/TestAll.java` next to a regex, the build runs no test at all and passes with "Tests run: 0". A path pattern like `com/**/Test*.java` would not do either: surefire prefixes it with `**/`, so it would match the engine's own test classes in the `tl-basic` test jar and this module's `test/com/...` classes a second time. A `%regex[]` include is anchored at the start of the path and matches exactly the two package roots the AI modules own.

**Adding a test.** Put it next to the code it tests, in the mirrored package, and name it `Test<Something>` or `<Something>Test`. Nothing else is needed - no suite to register it in, no include to extend. `TestSuiteReachability` (in `tl-ai-core`) fails the build if a test class is somewhere no include reaches, or is named so that no collector recognises it, so a test that would silently never run says so instead. Do not write a stub in the `test` package that forwards to it: the class would run twice, which the same test rejects.

A test that needs a knowledge base is still a unit test: wrap it in the H2 setups (`KBSetup.getSingleKBTest`, `ServiceTestSetup`) as the existing ones do, and it runs with the rest. A test that starts agent runs starts the modules `AgentRuntimeService`, `AgentApplicationService` and `AgentPolicyService`. A test that needs something the build may not have - a key, a browser, an external service - belongs behind one of the opt-in gates below, so that it reports as **skipped** rather than failing or, worse, passing without having done anything. The gates are classes next to `AssumptionAwareRunner` in `tl-ai-core/src/test/java/com/top_logic/tl/ai/core/junit` (and beside the tests in `tl-ai-eval` and `tl-ai-index-onnx`); a gate is also asked by a class's `suite()` where the setup would boot the very service the key is missing for.

## Opt-in gates

| System property | Gate class | What runs | Needs |
|---|---|---|---|
| `tl.ai.browser.e2e=true` | `BrowserE2e` | the browser tests of `tools/web` (`TestBrowser*`, `TestBrowserUrlProvenance`) with a real Chromium | Playwright's full Chromium build in `~/.cache/ms-playwright`, not `chrome-headless-shell` (a plain `playwright install chromium` supplies both); tests that reach public sites skip themselves when DNS does not resolve |
| `tl.ai.visual=true` | `VisualHarness` | the `Visual*` classes, which assert nothing and render a transcript to stdout for a human to read | `ANTHROPIC_API_KEY` for the two that call the API; they skip without it |
| `tl.ai.injection.e2e=true` | `PromptInjectionE2e` | `TestPromptInjectionBarrier` - a real model in front of an injected tool result, asserting which tools ran - and `TestResearchInjection` - a research worker in front of a hostile page and search result | `ANTHROPIC_API_KEY`; `TestResearchInjection` needs `OPENAI_API_KEY`, and `-Dtl.ai.injection.openai.model=...` chooses its model |
| `tl.ai.vector.e2e=true` | `VectorIndexE2e` | `VectorIndexIntegrationTest` in `tl-ai-demo` - real Mistral embeddings stored in a real JVector index | an embedding key |
| `tl.ai.index.model.e2e=true` | `OnnxModelE2e` | the model test of `tl-ai-index-onnx` with a real embedding model | `-Dtl.ai.index.model.dir=` naming a directory with the model files, which no build downloads |
| `tl.ai.eval=true` | `EvalE2e` | `TestEvalPmMatrix` - the model eval with real models | provider keys (`tl_anthropic_api_key` or `ANTHROPIC_API_KEY`, ...) and money |
| `tl.ai.research.e2e=true` | `ResearchE2e` | `TestResearchEval` - the research questions with real models and the real web | `tl_openai_api_key`, `tl_anthropic_api_key`, `tl_brave_api_key`, each `unencrypted:<key>` |

Without its property, a gated test reports as skipped, never as passed. Examples:

```bash
mvn test -DskipTests=false -pl tl-ai-core -Dtl.ai.browser.e2e=true \
    -Dtest=com.top_logic.tl.ai.core.tools.web.TestBrowserTool,com.top_logic.tl.ai.core.tools.web.TestBrowserUrlProvenance

mvn test -DskipTests=false -pl tl-ai-core -Dtl.ai.injection.e2e=true \
    -Dtest=com.top_logic.tl.ai.core.tools.web.TestPromptInjectionBarrier

mvn test -DskipTests=false -pl tl-ai-demo -Dtl.ai.vector.e2e=true \
    -Dtest=com.top_logic.ai.demo.VectorIndexIntegrationTest

mvn test -DskipTests=false -pl tl-ai-index-onnx -Dtl.ai.index.model.e2e=true \
    -Dtl.ai.index.model.dir=$HOME/.cache/tl-ai-index-models/snowflake-arctic-embed-m-v2.0
```

## The model eval

`tl-ai-eval` puts the same business tasks to every configured model through the agent loop and grades the database each run left behind - or, for a "show me" task, what it presented. Its **plumbing runs in every build**, against a provider whose answers a test writes, so the TL-Script expressions of the scenario file and the graders are exercised at no cost:

```bash
mvn test -DskipTests=false -pl tl-ai-eval
```

The **real models** spend real money and are behind `-Dtl.ai.eval=true`. Without it `TestEvalPmMatrix` reports as skipped; the gate is asked by the class's `suite()` as well, so a build that does not run the eval does not boot the agent stack to report a skip.

```bash
mvn test -DskipTests=false -pl tl-ai-eval -Dtl.ai.eval=true -Dtest=com.top_logic.tl.ai.eval.TestEvalPmMatrix
```

The CSV lands in `tl-ai-eval/target/eval/`. How to add a scenario, how to read the CSV and - most importantly - what the numbers may not be used for is in `tl-ai-eval/README.md`. The short version: with a handful of scenarios this shows that a model is unusable with this harness, never that one good model is three points better than another.

### The research questions

`research-scenarios.config.xml` in `tl-ai-eval` puts research questions to the assistant with `TL_deepResearch` and grades the answer: `<citations>` deterministically (read sources cited, hosts, links to unread pages), `<judge>` by a pinned judge model of another family against a rubric. Real models and the real web, so behind `-Dtl.ai.research.e2e=true`; the graders run in every build against a scripted model and judge (`TestResearchPlumbing`). An arm can put the workers on another model than the lead (`<agent-models>`, `subagent-workers` for a server that takes one request at a time); the CSV's `subagent*` columns count what the workers spent. `-Dtl.ai.research.arms=` selects arms, `-Dtl.ai.research.scenarios=` scenarios and `-Dtl.ai.research.repeats=` the repetitions.

```bash
export tl_openai_api_key=unencrypted:... tl_anthropic_api_key=unencrypted:... tl_brave_api_key=unencrypted:...
mvn test -DskipTests=false -pl tl-ai-eval -Dtl.ai.research.e2e=true \
    -Dtest=com.top_logic.tl.ai.eval.TestResearchEval \
    -Dtl.ai.research.scenarios=fact-nobel-phd -Dtl.ai.research.repeats=1
```

## The security test bed

`tl-ai-demo` carries the agent security test bed behind one line of its `WEB-INF/conf/metaConf.txt` (`secdemo.config.xml`): roles, accounts, a model, model access rights and a tool access policy, with one JUnit test per behaviour (`SecDemo*Test`) against a real application boot. `tl-ai-demo/doc/secdemo/testbed.md` maps every behaviour to a persona, a thing to type and an expected result; the [worked example](doc:ai/security/role-model-worked-example) explains the role model it is built on.
