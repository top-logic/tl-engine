---
name: run-and-verify
description: Use when changes were made to the chat UI (tl-chat), system prompts, or agentic architecture (tl-ai-core agents/). Starts the app and uses Playwright to verify the chat interface works.
---

# Run and Verify

After changes to the chat interface, system prompts, or agentic architecture, build and start the TopLogic demo app, then use Playwright to log in and verify the chat page.

## When to Use

**Use when you changed any of:**
- `tl-chat/` — chat UI, CSS, JS, servlets, SSE handler
- `tl-ai-core/src/main/java/**/agents/` — orchestrator, agent invoker, specialist agents
- System prompt strings in agent classes (OrchestratorRouter, ModelExplorer, TLScriptAgent)

**Do NOT use for:**
- Model XML changes, MCP server changes, build config, CLAUDE.md edits
- Changes that don't affect the running chat interface

## Steps

### 1. Build

```bash
cd /Users/jhu/git/tl-ai && mvn clean install -DskipTests
```

### 2. Start the App

First check if the app is already running:

```bash
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/tl-ai-demo/
```

If you get `200` or `302`, the app is already up — skip to step 3. Otherwise, start it in a **background shell**:

```bash
cd /Users/jhu/git/tl-ai/tl-ai-demo && mvn
```

Poll every 5 seconds until you get a `200` or `302`. Startup typically takes 30-60 seconds.

### 3. Log In with Playwright

Use the Playwright MCP tools (preferred) or fall back to a Node script with `npx playwright` if MCP tools are not connected.

**Note:** The UI locale is **German**. Sidebar labels are in German (e.g., "Abmelden" for logout).

1. **Navigate** to `http://localhost:8080/tl-ai-demo/`
2. **Log in** — fill `input[name="username"]` with `root`, `input[name="password"]` with `root1234`, click `input[type="submit"]`
3. **Navigate** to the chat page — click **"Chat"** in the left sidebar
4. **Take a screenshot** to confirm the chat page loaded (expect "Chat Demo" header, system welcome message, and message input field)

### 4. Verify

What you verify depends on what changed:

- **Chat UI changes** — screenshot the chat page, check layout and elements are present
- **System prompt changes** — send a test message through the chat and verify the response behavior
- **Agentic architecture changes** — send a message that triggers the changed agent flow, verify tool calls and response appear in the timeline

### 5. Stop the App

Kill the background Maven process when done.

## Reference

| Item | Value |
|------|-------|
| App URL | `http://localhost:8080/tl-ai-demo/` |
| Username | `root` |
| Password | `root1234` |
| Chat page | Left sidebar, **"Chat"** link |
| UI locale | German |
| Login form | `input[name="username"]`, `input[name="password"]`, `input[type="submit"]` |
| Start command | `cd tl-ai-demo && mvn` |
| Build command | `mvn clean install -DskipTests` |
