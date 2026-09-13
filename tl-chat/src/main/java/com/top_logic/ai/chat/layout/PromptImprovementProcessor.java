/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import com.top_logic.ai.chat.layout.TaskProgressTracker.PromptImprovementState;
import com.top_logic.tl.ai.core.agent.AgentDefinitionQuery;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.TopLogicSecurityContext;
import com.top_logic.tl.ai.core.agent.TransactionScope;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.execution.AgentEnvironment;
import com.top_logic.tl.ai.core.execution.AgentRunner;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.session.ConversationLog;
import com.top_logic.tl.ai.core.session.ConversationLogListener;
import com.top_logic.tl.ai.core.tools.task.TaskStore;
import com.top_logic.tl.ai.core.agent.approval.UserInteractionStore;
import com.top_logic.tl.ai.tool.api.CancellationToken;
import com.top_logic.util.TLContext;

/**
 * Runs prompt improvement independently from the main chat conversation.
 */
public final class PromptImprovementProcessor {

	/** Maximum accepted draft length in UTF-16 characters. */
	public static final int MAX_DRAFT_LENGTH = 20_000;

	/** The prompt was improved successfully. */
	public static final String STATUS_COMPLETED = "completed";

	/** The prompt-improvement request was cancelled. */
	public static final String STATUS_CANCELLED = "cancelled";

	/** A newer prompt-improvement request replaced this request. */
	public static final String STATUS_STALE = "stale";

	/** Longest first line still treated as a lead-in rather than as part of the prompt. */
	private static final int MAX_LEAD_IN_CHARS = 80;

	/** How often wrapper removal is retried; a lead-in plus a fence needs two passes. */
	private static final int MAX_WRAPPER_PASSES = 3;

	/** Quotation-mark pairs stripped when they enclose the whole answer. */
	private static final String[] QUOTE_PAIRS = { "\"\"", "\u201c\u201d", "\u00ab\u00bb", "\u201e\u201c", "''" };

	private PromptImprovementProcessor() {
		// Static utility.
	}

	/**
	 * Improves the given draft with the agent configured on the tracker.
	 *
	 * <p>
	 * The run gets a fresh {@link Conversation}, ephemeral environment state and no
	 * {@link com.top_logic.tl.ai.core.execution.model.AgentSession}. It therefore cannot append to
	 * the chat transcript or memory, emit chat SSE events, register a main processing state, or
	 * create an agent execution.
	 * </p>
	 *
	 * @throws PromptImprovementException
	 *         If the request or the administrator configuration is invalid.
	 */
	public static Result improve(TaskProgressTracker tracker, String requestId, String draft) {
		return improve(tracker, requestId, draft,
			name -> AgentDefinitionQuery.getInstance().findAgentByName(name) != null,
			PromptImprovementProcessor::runAgent);
	}

	static Result improve(TaskProgressTracker tracker, String requestId, String draft,
			Predicate<String> agentExists, AgentInvocation invocation) {
		String normalizedRequestId = requireRequestId(requestId);
		String normalizedDraft = requireDraft(draft);
		String agentName = configuredAgent(tracker);
		if (!agentExists.test(agentName)) {
			throw new PromptImprovementException(Failure.MISSING_AGENT,
				"Configured prompt-improver agent '" + agentName + "' does not exist.");
		}

		CancellationToken cancellationToken = new CancellationToken();
		PromptImprovementState state =
			new PromptImprovementState(normalizedRequestId, cancellationToken);
		PromptImprovementState previous = tracker.replacePromptImprovementState(state);
		if (previous != null) {
			previous.getCancellationToken().cancel();
		}

		try {
			String improved = invocation.run(agentName, normalizedDraft, cancellationToken);
			if (tracker.getPromptImprovementState() != state) {
				return new Result(normalizedRequestId, "", STATUS_STALE);
			}
			if (cancellationToken.isCancelled()) {
				return new Result(normalizedRequestId, "", STATUS_CANCELLED);
			}
			if (improved == null || improved.isBlank()) {
				throw new IllegalStateException(
					"Prompt-improver agent '" + agentName + "' returned no improved prompt.");
			}
			String cleaned = stripWrapper(improved);
			if (cleaned.isEmpty()) {
				throw new IllegalStateException(
					"Prompt-improver agent '" + agentName + "' returned only a wrapper.");
			}
			return new Result(normalizedRequestId, cleaned, STATUS_COMPLETED);
		} catch (CancellationException ex) {
			return cancelledOrStale(tracker, state);
		} catch (RuntimeException ex) {
			if (cancellationToken.isCancelled()) {
				return cancelledOrStale(tracker, state);
			}
			throw ex;
		} finally {
			tracker.clearPromptImprovementState(state);
		}
	}

	/**
	 * Removes the wrapper a model may add around a rewritten prompt, whatever the system prompt
	 * forbids.
	 *
	 * <p>
	 * Three shapes are removed, each only where it is unambiguous: a lead-in line that announces the
	 * result ("Here is the improved prompt:"), a markdown fence around the <em>whole</em> answer, and
	 * one pair of quotation marks around the whole answer. Every rule is deliberately narrow, because
	 * a rewritten prompt may legitimately contain a fenced code block, a colon-terminated line, or
	 * quoted text - only a construct that encloses everything is a wrapper.
	 * </p>
	 */
	static String stripWrapper(String text) {
		if (text == null) {
			return "";
		}
		String result = text.strip();
		// Repeated, because a model that adds a lead-in often also fences what follows.
		for (int pass = 0; pass < MAX_WRAPPER_PASSES; pass++) {
			String before = result;
			result = stripLeadIn(result);
			result = stripFence(result);
			result = stripQuotes(result);
			if (result.equals(before)) {
				break;
			}
		}
		return result;
	}

	/**
	 * Drops a first line that only announces the result.
	 *
	 * <p>
	 * Recognised by shape rather than by wording, so it holds in any language: a short line that ends
	 * in a colon and is followed by a blank line and further content. A prompt whose own first line
	 * ends in a colon (a heading the user wrote) is kept, because the rewritten prompt would then
	 * have no blank line separating it.
	 * </p>
	 */
	private static String stripLeadIn(String text) {
		int firstBreak = text.indexOf('\n');
		if (firstBreak < 0) {
			return text;
		}
		String firstLine = text.substring(0, firstBreak).strip();
		if (firstLine.length() > MAX_LEAD_IN_CHARS || !firstLine.endsWith(":")) {
			return text;
		}
		String rest = text.substring(firstBreak + 1);
		if (!rest.startsWith("\n") && !rest.startsWith("\r\n")) {
			return text;
		}
		String body = rest.strip();
		return body.isEmpty() ? text : body;
	}

	/** Unwraps a markdown fence that encloses the whole answer. */
	private static String stripFence(String text) {
		if (!text.startsWith("```") || !text.endsWith("```") || text.length() < 7) {
			return text;
		}
		// A single opening and a single closing fence; anything else is content with its own fences.
		if (countFenceLines(text) != 2) {
			return text;
		}
		int firstBreak = text.indexOf('\n');
		if (firstBreak < 0) {
			return text;
		}
		// An empty body is returned as such: the caller rejects an answer that was only a wrapper,
		// which is better than handing the user back the fence as if it were a prompt.
		return text.substring(firstBreak + 1, text.lastIndexOf("```")).strip();
	}

	private static int countFenceLines(String text) {
		int count = 0;
		for (String line : text.split("\n", -1)) {
			if (line.strip().startsWith("```")) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Removes one pair of quotation marks around the whole answer, but only when that pair is the
	 * only one present - otherwise the quotes belong to the prompt.
	 */
	private static String stripQuotes(String text) {
		for (String pair : QUOTE_PAIRS) {
			char open = pair.charAt(0);
			char close = pair.charAt(1);
			if (text.length() < 2 || text.charAt(0) != open || text.charAt(text.length() - 1) != close) {
				continue;
			}
			// The pair must be the only one in the answer; otherwise the quotes are the prompt's own.
			// A symmetric pair (") is one character used twice, an asymmetric one two characters once.
			boolean onlyPair = open == close
				? count(text, open) == 2
				: count(text, open) == 1 && count(text, close) == 1;
			if (!onlyPair) {
				continue;
			}
			return text.substring(1, text.length() - 1).strip();
		}
		return text;
	}

	private static int count(String text, char c) {
		int count = 0;
		for (int i = 0; i < text.length(); i++) {
			if (text.charAt(i) == c) {
				count++;
			}
		}
		return count;
	}

	private static Result cancelledOrStale(TaskProgressTracker tracker, PromptImprovementState state) {
		String status = tracker.getPromptImprovementState() == state ? STATUS_CANCELLED : STATUS_STALE;
		return new Result(state.getRequestId(), "", status);
	}

	private static String requireRequestId(String requestId) {
		if (requestId == null || requestId.isBlank()) {
			throw new PromptImprovementException(Failure.INVALID_REQUEST,
				"Missing or empty 'requestId'.");
		}
		return requestId.trim();
	}

	private static String requireDraft(String draft) {
		if (draft == null || draft.isBlank()) {
			throw new PromptImprovementException(Failure.INVALID_REQUEST,
				"Missing or empty 'draft'.");
		}
		if (draft.length() > MAX_DRAFT_LENGTH) {
			throw new PromptImprovementException(Failure.INVALID_REQUEST,
				"Draft is too long (max " + MAX_DRAFT_LENGTH + " characters).");
		}
		return draft.strip();
	}

	private static String configuredAgent(TaskProgressTracker tracker) {
		String agentName = tracker.getPromptImproverAgentName();
		if (agentName == null || agentName.isBlank()) {
			throw new PromptImprovementException(Failure.DISABLED,
				"Prompt improvement is not enabled for this chat.");
		}
		return agentName.trim();
	}

	private static String runAgent(String agentName, String draft, CancellationToken cancellationToken) {
		Conversation conversation = new Conversation();
		conversation.add(new UserMessage(UUID.randomUUID().toString(), Instant.now(),
			List.of(new TextContent(draft))));

		ConversationLog ephemeralLog = new ConversationLog();
		ConversationLogListener logListener = new ConversationLogListener(ephemeralLog);
		TaskStore taskStore = new TaskStore();
		UserInteractionStore interactionStore = new UserInteractionStore();
		try (TransactionScope transaction = new TransactionScope(cancellationToken);
				AgentEnvironment environment = AgentEnvironment.open(logListener, taskStore,
					interactionStore, ConcurrentHashMap.newKeySet(), cancellationToken)) {
			ToolContext context = new ToolContext(
				TopLogicSecurityContext.forRun(TLContext.currentUser(), false),
				ApprovalCallback.AUTO_APPROVE, transaction, cancellationToken);
			return AgentRunner.run(agentName, conversation, null, context, environment.logListener(),
				null, ephemeralLog, null, null).response();
		}
	}

	/**
	 * Outcome of one prompt-improvement request.
	 *
	 * @param requestId
	 *        Correlation ID supplied by the browser.
	 * @param improvedText
	 *        Improved prompt, or an empty string when no result must be applied.
	 * @param status
	 *        One of {@link #STATUS_COMPLETED}, {@link #STATUS_CANCELLED}, or
	 *        {@link #STATUS_STALE}.
	 */
	public record Result(String requestId, String improvedText, String status) {
		// Pure data.
	}

	@FunctionalInterface
	interface AgentInvocation {
		String run(String agentName, String draft, CancellationToken cancellationToken);
	}

	/** Request or configuration failure category. */
	public enum Failure {
		/** The browser request is malformed. */
		INVALID_REQUEST,

		/** Prompt improvement is not configured. */
		DISABLED,

		/** The configured agent does not exist. */
		MISSING_AGENT
	}

	/**
	 * Reports a rejected prompt-improvement request.
	 */
	public static final class PromptImprovementException extends RuntimeException {

		private final Failure _failure;

		PromptImprovementException(Failure failure, String message) {
			super(message);
			_failure = failure;
		}

		/** Returns the failure category. */
		public Failure getFailure() {
			return _failure;
		}
	}
}
