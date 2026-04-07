# CLAUDE.md

This file provides guidance to Claude Code when working with code in this repository.

## The Big Picture — Never Forget This

> **Vision:** An Agentic System capable of examining a TopLogic application and editing it. Helping users (humans or other agents) answer their need in the best and most efficient way possible.

Every piece of code must serve this vision. The system must:

1. **Examine** — Tools + persistent memory to see configs, model, BPE processes, services
2. **Edit** — Tools to add/edit/delete configs, model elements, services, layouts, workflows
3. **Secure** — Per-user permissions based on TopLogic security. The agent cannot see or do more than the user is allowed to
4. **Scale** — X concurrent users (people, external agents, other systems) simultaneously
5. **Stay focused** — Deep context management with compaction so the agent never loses track and hallucinates
6. **Be fast** — Parallel tool calls, minimal overhead, streaming responses
7. **Be safe** — Transactional edits with locking and rollback. No race conditions between editors
8. **Be configurable** — Developers define custom agents (system prompt, model, tools, skills) via TopLogic configuration, changes take effect immediately

**Read these documents before any significant architecture work:**
- `doc/ARCHITECTURE-VISION.md` — The north-star design: 10 pillars, clean API interfaces, design principles
- `doc/PRD-agentic-framework.md` — Detailed requirements, migration plan, testing strategy
- `doc/REPORT-framework-comparison.md` — Claude Code vs LangChain4j vs tl-ai deep comparison
- `doc/architecture.md` — Original German architecture doc for TopLogic KI-Integration

## How to Research Agentic Patterns

When implementing agentic features (agent loop, tool system, context management, streaming, permissions, multi-agent orchestration), **do not guess or rely on training data alone**. Use subagents to deep-research reference implementations first:

### Reference Repositories (at /Users/jhu/git/sota-agentic/)

| Repository | What to learn | Key files to study |
|-----------|---------------|-------------------|
| **claude-code-main** | Agent loop, context compaction, 7 recovery paths, permission system, tool builder pattern, streaming, subagent isolation | `src/query.ts`, `src/QueryEngine.ts`, `src/tools.ts`, `src/services/compact/` |
| **langchain4j** | Tool annotation reflection, ToolSpecification, provider abstractions, RAG pipeline, declarative agents | `langchain4j-core/`, `langchain4j/src/.../service/`, `langchain4j-agentic/` |
| **cline** | VS Code integration patterns, diff-based editing, approval workflows | `src/` |
| **opencode** | Go-based agent patterns, terminal UI, tool execution | `internal/` |
| **codex** | OpenAI's agent patterns, sandboxing, tool approval | `codex-cli/src/` |
| **gemini-cli** | Google's agent patterns, streaming, tool handling | `src/` |

### Research Protocol

Before implementing any significant agentic feature:

1. **Spawn a research subagent** for each relevant reference repo:
   ```
   Use Agent tool with subagent_type="Explore" to research the specific pattern
   in /Users/jhu/git/sota-agentic/<repo-name>
   ```

2. **Ask the right questions:**
   - "How does claude-code handle context compaction when approaching the token limit?"
   - "How does langchain4j generate ToolSpecifications from @Tool annotations?"
   - "How does claude-code's permission system decide allow/deny/ask?"

3. **Synthesize findings** before writing code — understand the tradeoffs, not just the implementation

4. **Adapt, don't copy** — These are TypeScript/Go/Python codebases. Extract the *pattern*, implement it idiomatically in Java for TopLogic.

## Project Overview

TopLogic AI Integration — multi-module Maven build on TopLogic 7.10.0-SNAPSHOT.

### Modules

| Module | Parent POM | Purpose |
|--------|-----------|---------|
| **tl-ai-core** | `tl-parent-core` | Custom agentic framework (agent loop, providers, tools, memory, permissions) — **BEING BUILT** |
| **tl-ai-service** | `tl-parent-core` | TopLogic integration: AgentRegistry, domain tools (model, script, layout), skills |
| **tl-ai-mcp-server** | `tl-parent-core` | MCP server exposing TopLogic APIs to external agents |
| **tl-chat** | `tl-parent-app` | Real-time chat interface with SSE streaming |
| **tl-ai-vector-index** | `tl-parent-core` | Semantic search via Qdrant embeddings |
| **tl-ai-demo** | `tl-parent-app` | Demo application bundling all modules |

### Architecture Direction

**We are replacing LangChain4j with a custom agentic framework (`tl-ai-core`).** This is because:
- LangChain4j's synchronous single-user `UntypedAgent` is architecturally wrong for multi-user enterprise
- We need to own the agent loop for: context compaction, error recovery, permissions, streaming, backpressure
- We depend on LangChain4j beta APIs (`1.11.0-beta19`) which risk breaking
- Provider wrappers are ~400 LOC each — not worth the 60-module dependency

**Keep LangChain4j only for:** `langchain4j-qdrant` (vector indexing). Everything else moves to `tl-ai-core`.

## Build Commands

```bash
mvn clean install                      # Build entire project
mvn clean install -pl tl-ai-core       # Build core framework
mvn clean install -pl tl-ai-service    # Build service layer
mvn test                               # Run all tests
mvn test -pl tl-ai-core                # Test core framework
mvn clean install -DskipTests          # Skip tests
```

## Project Structure

Standard Maven layout per module: `src/main/java/com/top_logic/ai/<module>/`, `src/main/webapp/WEB-INF/` (conf, model, autoconf, layouts), `src/test/java/`.

**Key config files:**
- Model definition: `src/main/webapp/WEB-INF/model/tl.ai.<module>.model.xml`
- Application config: `src/main/webapp/WEB-INF/conf/tl-ai-<module>.conf.xml`
- Auto-config: `src/main/webapp/WEB-INF/autoconf/model.tl.ai.<module>.config.xml`

**TopLogic parent POMs:** `tl-parent-core` for library modules, `tl-parent-app` for application modules.

**Important:** Modules use `web-fragment.xml` (in `src/main/resources/META-INF/`) for servlet registration, not `web.xml`. Only top-level applications use `web.xml`.

## Comments Policy

Default to writing no comments. Only add one when the WHY is non-obvious: a hidden constraint, a subtle invariant, a workaround for a specific bug, behavior that would surprise a reader. If removing the comment wouldn't confuse a future reader, don't write it.

Don't explain WHAT the code does — well-named identifiers already do that. Don't reference the current task, fix, or callers ("used by X", "added for the Y flow", "handles the case from issue #123") — those belong in the PR description and rot as the codebase evolves.

Don't remove existing comments unless you're removing the code they describe or you know they're wrong. A comment that looks pointless may encode a constraint or a lesson from a past bug.

## Verification and Reporting

Before reporting a task complete, verify it actually works: run the test, execute the script, check the output. If you can't verify (no test exists, can't run the code), say so explicitly rather than claiming success.

Report outcomes faithfully: if tests fail, say so with the relevant output; if you did not run a verification step, say that rather than implying it succeeded. Never claim "all tests pass" when output shows failures, never suppress or simplify failing checks to manufacture a green result, and never characterize incomplete or broken work as done. Equally, when a check did pass, state it plainly — do not hedge confirmed results with unnecessary disclaimers or re-verify things you already checked.

If you notice the user's request is based on a misconception, or spot a bug adjacent to what they asked about, say so. You're a collaborator, not just an executor.

## Coding Rules

- **DO NOT** manually edit `messages_en.properties` / `messages_de.properties` (auto-generated by build)
- **DO NOT** add `@author` tags to JavaDoc (use git history)
- **DO NOT** create hand-written POJO DTOs — use msgbuf `.proto` files (see `/create-dto` skill)
- **DO NOT** use Jackson (`ObjectMapper`) — use msgbuf `JsonWriter`/`JsonReader`
- **DO NOT** add Claude signatures or `Co-Authored-By` lines to commits
- **DO NOT** include user prompts or conversation context in commit messages
- **DO** include `@see` on config property constants pointing to getter (see `/toplogic-service` skill)
- Use `RuntimeException` for service startup errors, `ConfigurationError` only for user-facing errors

## Design Principles for tl-ai-core

When writing framework code in `tl-ai-core`, follow these principles (from `doc/ARCHITECTURE-VISION.md`):

1. **Security context flows down, never up** — Framework checks permissions BEFORE calling the tool. Tools receive pre-filtered data.
2. **The agent loop owns error recovery** — Tools and providers just throw. The loop decides what to do (retry, compact, fallback, report to LLM).
3. **Everything is streaming** — Users must see progress. Every tool call, every token, every delegation — streamed via AgentListener.
4. **Tools are pure functions** — `Tool.execute(args, context) → result`. Tools should NOT access global state, start threads, call other tools, or manage transactions.
5. **Configuration is data, not code** — A developer should create a new agent without writing Java code or restarting.
6. **Per-user isolation** — Own memory, own token budget, own transaction. Shared: provider pool, tool registry, agent configs.

## Notes

- Build artifacts excluded from Git (`.gitignore`)
- UTF-8 encoding throughout
- Test framework: JUnit (via TestCase base class)
