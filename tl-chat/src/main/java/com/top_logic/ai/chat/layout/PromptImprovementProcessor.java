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
			return new Result(normalizedRequestId, improved.strip(), STATUS_COMPLETED);
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
