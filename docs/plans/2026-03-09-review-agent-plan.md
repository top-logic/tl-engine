# Review Agent Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add a background Review Agent that silently monitors agent interactions and creates Gitea tickets when quality issues are detected.

**Architecture:** A configured agent (`ReviewAgent`) triggered by `ChatProcessor` after each main agent turn. Uses a `GiteaIssueTool` (via `ReflectiveToolFactory`) to create tickets. Runs on a background thread, never blocks the user-facing response. Notifications surface via a new `review_alert` SSE event type.

**Tech Stack:** Java 17+, LangChain4j `@Tool` annotations, `java.net.http.HttpClient` for Gitea API, TopLogic configuration framework, msgbuf for DTOs.

---

### Task 1: Create `GiteaIssueTool` Java class

The tool class that the Review Agent will call to create Gitea issues. Uses LangChain4j `@Tool` annotation pattern (same as `ModelBrowserTool`, `TLScriptTool`, etc.).

**Files:**
- Create: `tl-ai-service/src/main/java/com/top_logic/ai/service/tools/gitea/GiteaIssueTool.java`

**Step 1: Create the GiteaIssueTool class**

This class uses `@Tool` annotated methods (discovered by `ReflectiveToolFactory`). It needs config for the Gitea connection, so it reads from system properties / environment variables (same pattern as API keys in the project: `%GITEA_URL%`, `%GITEA_TOKEN%`, etc.).

```java
/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.tools.gitea;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.top_logic.basic.Logger;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

/**
 * AI tool for creating issues in a Gitea repository.
 *
 * <p>
 * Used by the Review Agent to report quality issues found during conversation analysis. Calls the
 * Gitea REST API ({@code POST /api/v1/repos/{owner}/{repo}/issues}).
 * </p>
 *
 * <p>
 * Connection parameters are read from environment variables:
 * </p>
 * <ul>
 * <li>{@code tl_gitea_url} - Base URL of the Gitea instance</li>
 * <li>{@code tl_gitea_token} - API token for authentication</li>
 * <li>{@code tl_gitea_repo_owner} - Repository owner</li>
 * <li>{@code tl_gitea_repo_name} - Repository name</li>
 * </ul>
 */
public class GiteaIssueTool {

	private static final Duration TIMEOUT = Duration.ofSeconds(30);

	private final HttpClient _httpClient;

	private final String _baseUrl;

	private final String _token;

	private final String _repoOwner;

	private final String _repoName;

	/**
	 * Creates a new {@link GiteaIssueTool}.
	 *
	 * <p>
	 * Reads connection parameters from environment variables. If not set, the tool will return an
	 * error message when invoked.
	 * </p>
	 */
	public GiteaIssueTool() {
		_httpClient = HttpClient.newBuilder()
			.connectTimeout(TIMEOUT)
			.build();
		_baseUrl = resolveEnv("tl_gitea_url", "");
		_token = resolveEnv("tl_gitea_token", "");
		_repoOwner = resolveEnv("tl_gitea_repo_owner", "");
		_repoName = resolveEnv("tl_gitea_repo_name", "");
	}

	/**
	 * Creates a Gitea issue reporting a quality problem found during conversation review.
	 *
	 * @param title
	 *        Short summary of the issue.
	 * @param category
	 *        One of: incorrect-answer, bad-delegation, tool-misuse, unhelpful-response,
	 *        inefficiency.
	 * @param severity
	 *        One of: low, medium, high.
	 * @param description
	 *        Detailed analysis of what went wrong, including the specific message or trace excerpt
	 *        and expected vs actual behavior.
	 * @param suggestedFix
	 *        What the agent should have done instead. May be empty.
	 * @return A confirmation message with the issue URL, or an error description.
	 */
	@Tool(name = "create_gitea_issue",
		value = "Creates a Gitea issue to report a quality problem found during conversation review. "
			+ "Call this when you identify an issue with the agent's behavior: incorrect answers, "
			+ "bad delegations, tool misuse, unhelpful responses, or inefficiency.")
	public String createGiteaIssue(
			@P("Short summary of the issue") String title,
			@P("Category: incorrect-answer, bad-delegation, tool-misuse, unhelpful-response, or inefficiency") String category,
			@P("Severity: low, medium, or high") String severity,
			@P("Detailed analysis of what went wrong") String description,
			@P("What the agent should have done instead (optional)") String suggestedFix) {
		if (_baseUrl.isEmpty() || _token.isEmpty() || _repoOwner.isEmpty() || _repoName.isEmpty()) {
			return "Error: Gitea connection not configured. "
				+ "Set environment variables: tl_gitea_url, tl_gitea_token, tl_gitea_repo_owner, tl_gitea_repo_name.";
		}

		String body = buildIssueBody(category, severity, description, suggestedFix);
		String jsonPayload = buildJsonPayload(title, body);

		String url = _baseUrl + "/api/v1/repos/" + _repoOwner + "/" + _repoName + "/issues";

		try {
			HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.header("Content-Type", "application/json")
				.header("Authorization", "token " + _token)
				.timeout(TIMEOUT)
				.POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
				.build();

			HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() >= 200 && response.statusCode() < 300) {
				Logger.info("Review Agent created Gitea issue: " + title, GiteaIssueTool.class);
				return "Issue created successfully: " + title;
			} else {
				Logger.warn("Gitea API returned status " + response.statusCode() + ": " + response.body(),
					GiteaIssueTool.class);
				return "Error creating issue: HTTP " + response.statusCode() + " - " + response.body();
			}
		} catch (Exception ex) {
			Logger.error("Failed to create Gitea issue", ex, GiteaIssueTool.class);
			return "Error creating issue: " + ex.getMessage();
		}
	}

	private static String buildIssueBody(String category, String severity, String description, String suggestedFix) {
		StringBuilder sb = new StringBuilder();
		sb.append("## AI Review Report\n\n");
		sb.append("**Category:** ").append(category).append("\n");
		sb.append("**Severity:** ").append(severity).append("\n\n");
		sb.append("### Issue Description\n").append(description).append("\n\n");
		if (suggestedFix != null && !suggestedFix.isBlank()) {
			sb.append("### Suggested Fix\n").append(suggestedFix).append("\n\n");
		}
		sb.append("---\n*Generated by Review Agent*\n");
		return sb.toString();
	}

	private static String buildJsonPayload(String title, String body) {
		// Manual JSON construction to avoid Jackson dependency (project uses msgbuf)
		return "{\"title\":" + escapeJson(title) + ",\"body\":" + escapeJson(body) + "}";
	}

	private static String escapeJson(String value) {
		if (value == null) {
			return "\"\"";
		}
		return "\"" + value
			.replace("\\", "\\\\")
			.replace("\"", "\\\"")
			.replace("\n", "\\n")
			.replace("\r", "\\r")
			.replace("\t", "\\t")
			+ "\"";
	}

	private static String resolveEnv(String key, String defaultValue) {
		// TopLogic convention: environment variables use underscore-separated keys
		String value = System.getenv(key);
		if (value != null && !value.isEmpty()) {
			return value;
		}
		// Also try system property
		value = System.getProperty(key);
		if (value != null && !value.isEmpty()) {
			return value;
		}
		return defaultValue;
	}

}
```

**Step 2: Verify it compiles**

Run: `mvn compile -pl tl-ai-service -q`
Expected: BUILD SUCCESS

**Step 3: Commit**

```bash
git add tl-ai-service/src/main/java/com/top_logic/ai/service/tools/gitea/GiteaIssueTool.java
git commit -m "feat: add GiteaIssueTool for Review Agent ticket creation"
```

---

### Task 2: Register `GiteaIssueTool` in ToolRegistry XML

**Files:**
- Modify: `tl-ai-service/src/main/webapp/WEB-INF/conf/tl-ai-openai.conf.config.xml` (inside `<tool-factories>` block, after line 83)

**Step 1: Add factory entry**

Add this entry inside `<tool-factories>`, after the existing `model-modification` factory (after line 83, before the comment block):

```xml
					<factory name="gitea">
						<implementation class="com.top_logic.ai.service.tools.registry.ReflectiveToolFactory"
							tool-class="com.top_logic.ai.service.tools.gitea.GiteaIssueTool"
						/>
					</factory>
```

This registers the tool as `gitea.create_gitea_issue` in the ToolRegistry.

**Step 2: Verify it compiles**

Run: `mvn compile -pl tl-ai-service -q`
Expected: BUILD SUCCESS

**Step 3: Commit**

```bash
git add tl-ai-service/src/main/webapp/WEB-INF/conf/tl-ai-openai.conf.config.xml
git commit -m "feat: register GiteaIssueTool in ToolRegistry"
```

---

### Task 3: Add `ReviewAgent` configuration to AgentRegistry XML

**Files:**
- Modify: `tl-ai-service/src/main/webapp/WEB-INF/autoconf/com.top_logic.ai.service.agents.AgentRegistry.config.xml` (add new `<agent>` element before `<skills>`, around line 394)

**Step 1: Add ReviewAgent config**

Add this agent definition before the `<skills>` element (before line 395):

```xml
			<agent name="ReviewAgent"
				description="Background quality reviewer. Analyzes conversation transcripts for quality issues (incorrect answers, bad delegations, tool misuse, unhelpful responses, inefficiency) and creates Gitea tickets for problems found."
				tools="gitea.create_gitea_issue"
			>
				<system-prompt><![CDATA[# Preamble

You are a **Quality Review Agent** for a TopLogic AI assistant. Your job is to review a conversation transcript (user messages, agent responses, and tool/delegation traces) and identify quality issues.

---

# Core Mandate

You receive a serialized conversation turn as input. Analyze it for problems in these categories:

1. **incorrect-answer** — Agent stated facts that are wrong, hallucinated data, or contradicted tool results
2. **bad-delegation** — Agent delegated to the wrong sub-agent, created unnecessary delegation chains, or failed to delegate when it should have
3. **tool-misuse** — Agent called wrong tools, passed incorrect arguments, ignored tool results, or made redundant/unnecessary tool calls
4. **unhelpful-response** — Agent response didn't address the user's question, was vague, or missed key information from tool results
5. **inefficiency** — Agent used excessive tool calls or delegations to accomplish something that could have been done with fewer steps

---

# Analysis Process

1. Read the full conversation transcript carefully
2. For each agent response, check:
   - Does it match the tool results? (no hallucination)
   - Did the agent delegate to the right specialist? (correct routing)
   - Were tool calls appropriate and minimal? (no waste)
   - Does the response actually help the user? (useful and complete)
3. If you find an issue: call `create_gitea_issue` with structured details
4. If no issues found: respond with "No issues found."

---

# Rules

- Only flag CLEAR issues. Do not flag stylistic preferences or minor wording choices.
- Severity guide: **high** = factually wrong answer or data corruption risk; **medium** = wrong delegation or significant tool misuse; **low** = inefficiency or mildly unhelpful response.
- Include specific evidence in your description: quote the problematic text, reference the tool call that was misused, etc.
- You may create multiple tickets if multiple distinct issues exist.
- Do NOT create a ticket if the conversation went well. "No issues found." is a perfectly valid response.

---

<<AVAILABLE_TOOLS>>]]></system-prompt>
			</agent>
```

**Step 2: Verify it compiles**

Run: `mvn compile -pl tl-ai-service -q`
Expected: BUILD SUCCESS

**Step 3: Commit**

```bash
git add tl-ai-service/src/main/webapp/WEB-INF/autoconf/com.top_logic.ai.service.agents.AgentRegistry.config.xml
git commit -m "feat: add ReviewAgent configuration to AgentRegistry"
```

---

### Task 4: Create `ConversationSerializer` utility

Serializes chat messages and tool timeline into the XML format the Review Agent expects as input.

**Files:**
- Create: `tl-chat/src/main/java/com/top_logic/ai/chat/layout/ConversationSerializer.java`

**Step 1: Create the serializer**

```java
/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.util.List;

/**
 * Serializes conversation data into XML format for the Review Agent.
 *
 * <p>
 * Produces a {@code <review-request>} XML document containing user messages, agent responses, and
 * the tool/delegation timeline from a completed interaction turn.
 * </p>
 */
public class ConversationSerializer {

	private static final int MAX_CHARS = 50_000;

	/**
	 * Serializes the last interaction turn for automatic review.
	 *
	 * @param userMessage
	 *        The user's message text.
	 * @param agentResponse
	 *        The agent's response text.
	 * @param task
	 *        The completed {@link ChatTask} containing timeline events.
	 * @return XML string for the Review Agent input.
	 */
	public static String serializeLastTurn(String userMessage, String agentResponse, ChatTask task) {
		StringBuilder sb = new StringBuilder();
		sb.append("<review-request>\n");

		sb.append("  <user-messages>\n");
		sb.append("    <message>").append(escapeXml(userMessage)).append("</message>\n");
		sb.append("  </user-messages>\n");

		sb.append("  <agent-responses>\n");
		sb.append("    <response>").append(escapeXml(agentResponse)).append("</response>\n");
		sb.append("  </agent-responses>\n");

		sb.append("  <tool-timeline>\n");
		if (task != null) {
			for (ChatTimelineEvent event : task.getTimelineEvents()) {
				serializeTimelineEvent(sb, event);
			}
		}
		sb.append("  </tool-timeline>\n");

		sb.append("</review-request>");

		return truncateIfNeeded(sb.toString());
	}

	/**
	 * Serializes the full chat history for manual review.
	 *
	 * @param chat
	 *        The {@link Chat} containing all entries.
	 * @return XML string for the Review Agent input.
	 */
	public static String serializeFullChat(Chat chat) {
		StringBuilder sb = new StringBuilder();
		sb.append("<review-request>\n");

		StringBuilder userMsgs = new StringBuilder();
		StringBuilder agentMsgs = new StringBuilder();
		StringBuilder timeline = new StringBuilder();

		for (ChatEntry entry : chat.getEntries()) {
			if (entry instanceof ChatEntry.MessageEntry me) {
				ChatMessage msg = me.message();
				if ("user".equalsIgnoreCase(msg.getUser())) {
					userMsgs.append("    <message>").append(escapeXml(msg.getText())).append("</message>\n");
				} else {
					agentMsgs.append("    <response>").append(escapeXml(msg.getText())).append("</response>\n");
				}
			} else if (entry instanceof ChatEntry.TaskSnapshotEntry tse) {
				ChatTask task = tse.task();
				for (ChatTimelineEvent event : task.getTimelineEvents()) {
					serializeTimelineEvent(timeline, event);
				}
			}
		}

		sb.append("  <user-messages>\n").append(userMsgs).append("  </user-messages>\n");
		sb.append("  <agent-responses>\n").append(agentMsgs).append("  </agent-responses>\n");
		sb.append("  <tool-timeline>\n").append(timeline).append("  </tool-timeline>\n");

		sb.append("</review-request>");

		return truncateIfNeeded(sb.toString());
	}

	private static void serializeTimelineEvent(StringBuilder sb, ChatTimelineEvent event) {
		switch (event.getKind()) {
			case TOOL_CALL -> {
				ChatToolCall tc = event.getToolCall();
				sb.append("    <tool-call name=\"").append(escapeXml(tc.getToolName())).append("\">\n");
				sb.append("      <args>").append(escapeXml(String.valueOf(tc.getArgs()))).append("</args>\n");
				String result = tc.getResultJson();
				if (result != null) {
					// Truncate individual results to keep total size manageable
					if (result.length() > 2000) {
						result = result.substring(0, 2000) + "...[truncated]";
					}
					sb.append("      <result>").append(escapeXml(result)).append("</result>\n");
				}
				sb.append("    </tool-call>\n");
			}
			case AGENT_CALL -> {
				sb.append("    <agent-call from=\"").append(escapeXml(event.getFromAgent()))
					.append("\" to=\"").append(escapeXml(event.getToAgent())).append("\">\n");
				sb.append("      <input>").append(escapeXml(event.getInputSummary())).append("</input>\n");
				String resultSummary = event.getResultSummary();
				if (resultSummary != null) {
					if (resultSummary.length() > 2000) {
						resultSummary = resultSummary.substring(0, 2000) + "...[truncated]";
					}
					sb.append("      <result>").append(escapeXml(resultSummary)).append("</result>\n");
				}
				sb.append("    </agent-call>\n");
			}
		}
	}

	private static String truncateIfNeeded(String xml) {
		if (xml.length() <= MAX_CHARS) {
			return xml;
		}
		return xml.substring(0, MAX_CHARS) + "\n<!-- truncated -->\n</review-request>";
	}

	private static String escapeXml(String text) {
		if (text == null) {
			return "";
		}
		return text
			.replace("&", "&amp;")
			.replace("<", "&lt;")
			.replace(">", "&gt;")
			.replace("\"", "&quot;");
	}

}
```

**Step 2: Verify it compiles**

Run: `mvn compile -pl tl-chat -q`
Expected: BUILD SUCCESS

**Step 3: Commit**

```bash
git add tl-chat/src/main/java/com/top_logic/ai/chat/layout/ConversationSerializer.java
git commit -m "feat: add ConversationSerializer for Review Agent input"
```

---

### Task 5: Create `ReviewNotification` record and `ReviewNotificationStore`

Session-scoped store for review alerts that the SSE handler will pick up.

**Files:**
- Create: `tl-chat/src/main/java/com/top_logic/ai/chat/layout/ReviewNotification.java`
- Create: `tl-chat/src/main/java/com/top_logic/ai/chat/layout/ReviewNotificationStore.java`

**Step 1: Create ReviewNotification record**

```java
/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.time.Instant;

/**
 * Immutable notification produced by the Review Agent when a quality issue is found.
 *
 * @param title
 *        Short summary of the issue.
 * @param category
 *        Issue category (e.g., "incorrect-answer", "tool-misuse").
 * @param severity
 *        Issue severity ("low", "medium", "high").
 * @param timestamp
 *        When the review completed.
 */
public record ReviewNotification(String title, String category, String severity, Instant timestamp) {
	// Immutable record — no additional logic needed.
}
```

**Step 2: Create ReviewNotificationStore**

```java
/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Thread-safe store for {@link ReviewNotification} instances within a session.
 *
 * <p>
 * The SSE handler polls this store and emits {@code review_alert} events for any new
 * notifications. Consumed notifications are removed.
 * </p>
 */
public class ReviewNotificationStore {

	private final CopyOnWriteArrayList<ReviewNotification> _notifications = new CopyOnWriteArrayList<>();

	/**
	 * Adds a notification to the store.
	 *
	 * @param notification
	 *        The review notification to store.
	 */
	public void add(ReviewNotification notification) {
		_notifications.add(notification);
	}

	/**
	 * Drains and returns all pending notifications.
	 *
	 * <p>
	 * After this call, the store is empty. Each notification is returned exactly once.
	 * </p>
	 *
	 * @return List of pending notifications (may be empty, never null).
	 */
	public List<ReviewNotification> drain() {
		if (_notifications.isEmpty()) {
			return List.of();
		}
		List<ReviewNotification> result = new ArrayList<>(_notifications);
		_notifications.clear();
		return result;
	}

}
```

**Step 3: Verify it compiles**

Run: `mvn compile -pl tl-chat -q`
Expected: BUILD SUCCESS

**Step 4: Commit**

```bash
git add tl-chat/src/main/java/com/top_logic/ai/chat/layout/ReviewNotification.java \
       tl-chat/src/main/java/com/top_logic/ai/chat/layout/ReviewNotificationStore.java
git commit -m "feat: add ReviewNotification and ReviewNotificationStore"
```

---

### Task 6: Add `ReviewNotificationStore` to `TaskProgressTracker`

The `TaskProgressTracker` is session-scoped — it's the right place to hold the notification store.

**Files:**
- Modify: `tl-chat/src/main/java/com/top_logic/ai/chat/layout/TaskProgressTracker.java`

**Step 1: Read TaskProgressTracker to find where to add the field**

Read the file to understand its structure before modifying.

**Step 2: Add the notification store field and accessor**

Add a field:
```java
private final ReviewNotificationStore _reviewNotifications = new ReviewNotificationStore();
```

Add a getter:
```java
/**
 * Returns the {@link ReviewNotificationStore} for this session.
 */
public ReviewNotificationStore getReviewNotifications() {
    return _reviewNotifications;
}
```

**Step 3: Verify it compiles**

Run: `mvn compile -pl tl-chat -q`
Expected: BUILD SUCCESS

**Step 4: Commit**

```bash
git add tl-chat/src/main/java/com/top_logic/ai/chat/layout/TaskProgressTracker.java
git commit -m "feat: add ReviewNotificationStore to TaskProgressTracker"
```

---

### Task 7: Add review trigger to `ChatProcessor`

The core integration: trigger the Review Agent after each main agent turn.

**Files:**
- Modify: `tl-chat/src/main/java/com/top_logic/ai/chat/layout/ChatProcessor.java`

**Step 1: Read ChatProcessor fully to understand imports and structure**

Read the full file.

**Step 2: Add the `triggerReviewAgent` method**

Add this method to `ChatProcessor` (after `executeOrchestrator`, around line 184):

```java
/**
 * Triggers the Review Agent in the background to analyze the last interaction turn.
 *
 * <p>
 * Runs asynchronously on {@link #BACKGROUND_EXECUTOR}. Never blocks the main agent response.
 * Errors are logged but do not affect the user's chat experience.
 * </p>
 *
 * @param taskTracker
 *        The session's task tracker (provides notification store).
 * @param userMessage
 *        The user's original message.
 * @param agentResponse
 *        The agent's response text.
 * @param completedTask
 *        The completed task containing timeline events.
 */
static void triggerReviewAgent(TaskProgressTracker taskTracker, String userMessage,
		String agentResponse, ChatTask completedTask) {
	// Debounce: skip if a review is already running
	if (!taskTracker.tryStartReview()) {
		Logger.info("Skipping review: another review is already in progress.", ChatProcessor.class);
		return;
	}

	BACKGROUND_EXECUTOR.submit(() -> {
		try {
			AgentRegistry agentRegistry = AgentRegistry.getInstance();
			if (agentRegistry == null) {
				Logger.warn("Review skipped: AgentRegistry not available.", ChatProcessor.class);
				return;
			}

			// Check if ReviewAgent is configured
			if (!agentRegistry.hasAgent("ReviewAgent")) {
				return;
			}

			String reviewInput = ConversationSerializer.serializeLastTurn(
				userMessage, agentResponse, completedTask);

			UntypedAgent reviewAgent = agentRegistry.buildUntypedAgent(
				"ReviewAgent", null, null, java.util.Map.of());

			String reviewResult = ThreadContextManager.inSystemInteraction(ChatProcessor.class,
				() -> (String) reviewAgent.invoke(Map.of("message", reviewInput)));

			Logger.info("Review Agent completed: " + summarize(reviewResult), ChatProcessor.class);

			// If the review agent found issues (it would have called create_gitea_issue),
			// we can detect this from the result text
			if (reviewResult != null && !reviewResult.contains("No issues found")) {
				taskTracker.getReviewNotifications().add(
					new ReviewNotification(
						summarize(reviewResult),
						"review",
						"info",
						java.time.Instant.now()));
			}
		} catch (Exception ex) {
			Logger.error("Review Agent failed (non-critical): " + ex.getMessage(), ex, ChatProcessor.class);
		} finally {
			taskTracker.finishReview();
		}
	});
}

private static String summarize(String text) {
	if (text == null) {
		return "";
	}
	if (text.length() <= 200) {
		return text;
	}
	return text.substring(0, 200) + "...";
}
```

**Step 3: Add the trigger call in `executeOrchestrator`**

In `executeOrchestrator`, after line 175 (after `persistMessagesForSession`) and before the catch block (line 177), add:

```java
			// Trigger background review of this interaction turn
			triggerReviewAgent(taskTracker, trimmed, result,
				taskTracker.getTask(taskId));
```

The exact location is after:
```java
			if (sessionId != null) {
				persistMessagesForSession(sessionId, chat);
			}
```

And before:
```java
		} catch (Exception ex) {
```

**Step 4: Verify it compiles**

Run: `mvn compile -pl tl-chat -q`
Expected: Will fail because `tryStartReview()`, `finishReview()`, and `hasAgent()` don't exist yet. That's OK — we'll add them in the next tasks.

**Step 5: Commit (do this after Tasks 8 and 9 make it compile)**

Defer commit until after Task 8 and Task 9.

---

### Task 8: Add debounce methods to `TaskProgressTracker`

**Files:**
- Modify: `tl-chat/src/main/java/com/top_logic/ai/chat/layout/TaskProgressTracker.java`

**Step 1: Add AtomicBoolean field for debounce**

Add field:
```java
private final java.util.concurrent.atomic.AtomicBoolean _reviewInProgress = new java.util.concurrent.atomic.AtomicBoolean(false);
```

**Step 2: Add tryStartReview and finishReview methods**

```java
/**
 * Attempts to start a review. Returns {@code true} if no review is currently running.
 *
 * @return {@code true} if the review was started, {@code false} if another review is in progress.
 */
public boolean tryStartReview() {
    return _reviewInProgress.compareAndSet(false, true);
}

/**
 * Marks the current review as finished, allowing a new one to start.
 */
public void finishReview() {
    _reviewInProgress.set(false);
}
```

**Step 3: Verify it compiles (still needs Task 9)**

---

### Task 9: Add `hasAgent` method to `AgentRegistry`

**Files:**
- Modify: `tl-ai-service/src/main/java/com/top_logic/ai/service/agents/AgentRegistry.java`

**Step 1: Read AgentRegistry to find where to add the method**

Read the file to find the existing agent lookup method.

**Step 2: Add hasAgent method**

Find the method that looks up agents by name (likely `getAgentConfig(String name)` or similar). Add a public method nearby:

```java
/**
 * Returns whether an agent with the given name is configured.
 *
 * @param name
 *        The agent name to check.
 * @return {@code true} if the agent exists in the registry.
 */
public boolean hasAgent(String name) {
    return _agents.containsKey(name);
}
```

The exact field name for the agent map needs to be confirmed by reading the file — look for the `Map` that stores `AgentConfig` keyed by name.

**Step 3: Verify everything compiles**

Run: `mvn compile -pl tl-ai-service,tl-chat -q`
Expected: BUILD SUCCESS

**Step 4: Commit all of Tasks 7, 8, 9 together**

```bash
git add tl-chat/src/main/java/com/top_logic/ai/chat/layout/ChatProcessor.java \
       tl-chat/src/main/java/com/top_logic/ai/chat/layout/TaskProgressTracker.java \
       tl-ai-service/src/main/java/com/top_logic/ai/service/agents/AgentRegistry.java
git commit -m "feat: trigger ReviewAgent from ChatProcessor after each agent turn"
```

---

### Task 10: Add `review_alert` SSE event type

**Files:**
- Modify: `tl-chat/src/main/java/com/top_logic/ai/chat/api/ChatSSEHandler.java`
- Create: `tl-chat/src/main/java/com/top_logic/ai/chat/api/messages/ReviewAlertEvent.java` (msgbuf `.proto` file — or if simpler, a record + manual JSON)

**Step 1: Read ChatSSEHandler to find the poll-and-emit logic**

Find the method that polls for updates and emits SSE events. Look for where `task_status`, `tool_call` events are emitted.

**Step 2: Create ReviewAlertEvent**

Since the project prefers msgbuf, check if a simple record with manual JSON serialization is acceptable for this lightweight event. If the project has a pattern for SSE event DTOs, follow it.

Create a simple helper to serialize the alert:

```java
/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.api.messages;

import com.top_logic.ai.chat.layout.ReviewNotification;

/**
 * JSON serialization for {@code review_alert} SSE events.
 */
public class ReviewAlertEvent {

	/**
	 * Serializes a {@link ReviewNotification} to JSON.
	 */
	public static String toJson(ReviewNotification notification) {
		return "{\"title\":" + quote(notification.title())
			+ ",\"category\":" + quote(notification.category())
			+ ",\"severity\":" + quote(notification.severity())
			+ ",\"timestamp\":" + quote(notification.timestamp().toString())
			+ "}";
	}

	private static String quote(String value) {
		if (value == null) {
			return "null";
		}
		return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
	}

}
```

**Step 3: Add review alert polling to ChatSSEHandler**

In the polling method, after existing event emission, add:

```java
// Emit review alerts
ReviewNotificationStore reviewStore = _tracker.getReviewNotifications();
List<ReviewNotification> alerts = reviewStore.drain();
for (ReviewNotification alert : alerts) {
    sendEvent("review_alert", ReviewAlertEvent.toJson(alert));
}
```

The `_tracker` reference is the `TaskProgressTracker` — confirm the exact field name by reading the file.

**Step 4: Verify it compiles**

Run: `mvn compile -pl tl-chat -q`
Expected: BUILD SUCCESS

**Step 5: Commit**

```bash
git add tl-chat/src/main/java/com/top_logic/ai/chat/api/ChatSSEHandler.java \
       tl-chat/src/main/java/com/top_logic/ai/chat/api/messages/ReviewAlertEvent.java
git commit -m "feat: emit review_alert SSE events from ReviewNotificationStore"
```

---

### Task 11: Add manual review endpoint to `ChatServlet`

**Files:**
- Modify: `tl-chat/src/main/java/com/top_logic/ai/chat/api/ChatServlet.java`

**Step 1: Add PATH_REVIEW constant and route**

Add constant:
```java
private static final String PATH_REVIEW = "/review";
```

Add route in `doPost` method (after the `PATH_SESSIONS` check, before the 404):
```java
if (pathInfo.equals(PATH_REVIEW) || pathInfo.equals(PATH_REVIEW + "/")) {
    handleReview(req, resp);
    return;
}
```

**Step 2: Add handleReview method**

```java
private void handleReview(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    TaskProgressTracker tracker = ChatSessionRegistry.getOrCreate(req.getSession(false));
    if (tracker == null) {
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No active chat session.");
        return;
    }

    Chat chat = tracker.getChat();
    if (chat == null || chat.getEntries().isEmpty()) {
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No chat history to review.");
        return;
    }

    // Trigger manual review with full chat history
    String reviewInput = ConversationSerializer.serializeFullChat(chat);

    ChatProcessor.BACKGROUND_EXECUTOR.submit(() -> {
        try {
            AgentRegistry agentRegistry = AgentRegistry.getInstance();
            if (agentRegistry == null || !agentRegistry.hasAgent("ReviewAgent")) {
                Logger.warn("Manual review skipped: ReviewAgent not configured.", ChatServlet.class);
                return;
            }

            UntypedAgent reviewAgent = agentRegistry.buildUntypedAgent(
                "ReviewAgent", null, null, java.util.Map.of());

            String reviewResult = ThreadContextManager.inSystemInteraction(ChatServlet.class,
                () -> (String) reviewAgent.invoke(Map.of("message", reviewInput)));

            if (reviewResult != null && !reviewResult.contains("No issues found")) {
                tracker.getReviewNotifications().add(
                    new ReviewNotification(
                        reviewResult.length() > 200 ? reviewResult.substring(0, 200) + "..." : reviewResult,
                        "manual-review",
                        "info",
                        java.time.Instant.now()));
            }
        } catch (Exception ex) {
            Logger.error("Manual review failed: " + ex.getMessage(), ex, ChatServlet.class);
        }
    });

    resp.setContentType(CONTENT_TYPE_JSON);
    resp.setStatus(HttpServletResponse.SC_OK);
    resp.getWriter().write("{\"status\":\"review_started\"}");
}
```

**Step 3: Verify it compiles**

Run: `mvn compile -pl tl-chat -q`
Expected: BUILD SUCCESS

**Step 4: Commit**

```bash
git add tl-chat/src/main/java/com/top_logic/ai/chat/api/ChatServlet.java
git commit -m "feat: add POST /chat/review endpoint for manual review trigger"
```

---

### Task 12: Add "Review" button to chat UI (client-side)

**Files:**
- Explore first: Find the chat UI JSP/HTML/JavaScript files. Look in `tl-chat/src/main/webapp/` or `tl-chat/src/main/java/com/top_logic/ai/chat/layout/` for the client-side rendering.

**Step 1: Locate the chat UI files**

Search for files that render the chat interface — look for JavaScript, JSP, or HTML files:
- `tl-chat/src/main/webapp/script/`
- `tl-chat/src/main/java/com/top_logic/ai/chat/layout/ChatControl.java` (may write HTML/JS)

**Step 2: Add a "Review" button**

Add a button to the chat toolbar/header that calls `POST /chat/review`. The exact implementation depends on the UI framework found in Step 1.

**Step 3: Handle `review_alert` SSE event on client**

Add a client-side handler for the `review_alert` event type. When received, display a dismissable notification banner showing the issue title.

**Step 4: Verify the UI renders correctly**

Start the app and verify the button appears and the SSE event is handled.

**Step 5: Commit**

```bash
git add [modified UI files]
git commit -m "feat: add Review button and review_alert notification in chat UI"
```

---

### Task 13: Full integration test

**Step 1: Start the application**

Run: `mvn clean install -pl tl-ai-demo -DskipTests && cd tl-ai-demo && mvn exec:java` (or however the demo app is started)

**Step 2: Verify auto-review triggers**

1. Open the chat UI
2. Send a message that triggers the Orchestrator
3. Wait for the agent to respond
4. Check server logs for "Review Agent completed:" log entry
5. Verify no errors in logs

**Step 3: Verify manual review**

1. Click the "Review" button
2. Check server logs for manual review execution
3. Verify `review_alert` SSE event is received (check browser DevTools Network tab, SSE stream)

**Step 4: Verify Gitea ticket creation (if Gitea is available)**

Set environment variables:
```bash
export tl_gitea_url=https://your-gitea.example.com
export tl_gitea_token=your-api-token
export tl_gitea_repo_owner=owner
export tl_gitea_repo_name=repo
```

Send a message that would trigger a review finding, verify the ticket appears in Gitea.

**Step 5: Commit final state if any adjustments were needed**

```bash
git add -A
git commit -m "fix: integration adjustments for Review Agent"
```
