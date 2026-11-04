/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.api;


import com.top_logic.ai.chat.api.messages.InteractionResolvedEvent;
import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.ai.chat.layout.ChatProcessor;
import com.top_logic.ai.chat.layout.ChatSessionManager;
import com.top_logic.ai.chat.layout.PendingInteractionRegistry;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.basic.Logger;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tl.ai.core.agent.approval.UserInteractionStore;
import com.top_logic.tl.ai.core.agent.subagent.SubagentInbox;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJob;
import com.top_logic.tl.ai.core.session.ConversationLogListener;
import com.top_logic.tl.ai.core.session.Part;

/**
 * What an account does to its chat runs while they work: answering what a run asks, stopping a run,
 * writing to a background subagent and stopping one.
 *
 * <p>
 * A run's state hangs on the tracker of the browser window that started it, but the run belongs to
 * the account. Each of these is therefore looked up among the runs of <em>this account</em>
 * ({@link PendingInteractionRegistry}), not among the runs of the window the request comes from:
 * a second tab, a reloaded page and a new login all reach a run another window started (#393,
 * #465, #769). A run, a request or a subagent of another account is never found, and answered
 * exactly like one that does not exist, so no answer says which ids exist.
 * </p>
 */
public final class AccountRuns {

	/** The longest message the user may write to a subagent, in characters. */
	public static final int MAX_SUBAGENT_MESSAGE_CHARS = 8000;

	/** The response that approves a tool call once. */
	public static final String APPROVED = "APPROVED";

	/** The response that approves a tool for the rest of the chat. */
	public static final String APPROVED_FOR_SESSION = "APPROVED_FOR_SESSION";

	/** The response that denies a tool call. */
	public static final String REJECTED = "REJECTED";

	/** Separates the user's comment from {@link #REJECTED} in what the waiting run receives. */
	private static final String COMMENT_SEPARATOR = ":";

	private AccountRuns() {
		// Static utility.
	}

	/**
	 * Hands the user's answer to the run of this account that waits for the given request.
	 *
	 * <p>
	 * Everything the decision changes is written before the waiting thread is let go: resolving the
	 * future unparks the agent mid-turn, and it may call the next tool before this call has returned.
	 * A tool approved "for this session" that the run learns of only afterwards would be asked about
	 * again. The account's other windows that listed the request are told it was answered; the run's
	 * own window hears that from the run.
	 * </p>
	 *
	 * @param responder
	 *        The acting account.
	 * @param requestId
	 *        The id of the approval or question.
	 * @param response
	 *        {@link #APPROVED}, {@link #APPROVED_FOR_SESSION}, {@link #REJECTED}, or the answer to a
	 *        question.
	 * @param toolName
	 *        The tool an {@link #APPROVED_FOR_SESSION} approves for the run's chat.
	 * @param comment
	 *        Why a {@link #REJECTED} call was denied; may be <code>null</code>.
	 * @param ownWords
	 *        What the user typed for a choice question, beside the labels in the response (#679).
	 * @return Whether a run of the account was waiting for the request. <code>false</code> for a
	 *         late answer to a request whose run already ended - cancelled, timed out - and for a
	 *         request of another account alike: the user must not see a failure for answering a
	 *         question the system already tore down, and the two must not be told apart.
	 */
	public static boolean respond(Person responder, String requestId, String response, String toolName,
			String comment, String ownWords) {
		if (requestId == null || response == null) {
			return false;
		}
		PendingInteractionRegistry.Waiting run = PendingInteractionRegistry.findWaiting(responder, requestId);
		if (run == null) {
			Logger.info("Ignoring interaction response for request " + requestId
				+ ": no run of this account is waiting for it.", AccountRuns.class);
			return false;
		}
		UserInteractionStore store = run.store();

		// The rejection comment travels in the response, so the approval callback receives it.
		String resolvedResponse = response;
		if (REJECTED.equals(response) && comment != null && !comment.isEmpty()) {
			resolvedResponse = REJECTED + COMMENT_SEPARATOR + comment;
		}

		ConversationLogListener logListener = run.logListener();
		if (logListener != null) {
			if (APPROVED_FOR_SESSION.equals(response)) {
				logListener.resolveApproval(requestId, Part.ApprovalDecision.APPROVED_FOR_SESSION, null);
			} else if (resolvedResponse.startsWith(REJECTED)) {
				logListener.resolveApproval(requestId, Part.ApprovalDecision.DENIED, comment);
			} else {
				logListener.resolveApproval(requestId, Part.ApprovalDecision.APPROVED, null);
			}
		}

		// Into the run's own chat, not the answering window's nor the one its own window shows by
		// now: that chat's next runs are given the set. A subagent whose window has ended has no such
		// set any more.
		if (APPROVED_FOR_SESSION.equals(response) && toolName != null && !toolName.isEmpty()
			&& run.chat() != null) {
			run.chat().addApprovedTool(toolName);
		}

		store.resolve(requestId, resolvedResponse, ownWords);

		notifyInteractionResolved(responder, run.tracker(), requestId, resolvedResponse);
		return true;
	}

	/**
	 * Tells the account's other chat windows that a request they may be showing has been answered.
	 *
	 * <p>
	 * Best effort, and deliberately silent about failures: the answer has already reached the run,
	 * and a window that misses this frame shows a stale card until it next lists what is pending.
	 * </p>
	 */
	private static void notifyInteractionResolved(Person responder, TaskProgressTracker runWindow,
			String requestId, String outcome) {
		InteractionResolvedEvent event = InteractionResolvedEvent.create()
			.setRequestId(requestId)
			.setOutcome(outcome);
		for (ChatEventHandler handler : ChatEventHandlerRegistry.findOthers(responder, runWindow)) {
			try {
				handler.sendInteractionResolved(event);
			} catch (RuntimeException ex) {
				Logger.info("Could not tell a window that interaction " + requestId
					+ " was answered: " + ex.getMessage(), AccountRuns.class);
			}
		}
	}

	/**
	 * Stops the run of the given chat of this account, wherever it was started.
	 *
	 * <p>
	 * A chat that is named is looked up among the runs of this account
	 * ({@link PendingInteractionRegistry#findBySession}), which is what makes the stop hit the chat
	 * the user is looking at rather than whatever else the asking window happens to be running. A
	 * stop that names no chat stops what the asking window runs: the stop button is the way out of a
	 * run that is going wrong, and must not start refusing requests.
	 * </p>
	 *
	 * <p>
	 * The run's subagents are cancelled with it, so that the work it spawned does not go on in the
	 * background; their cancellation also ends a subagent's wait on a question. Cooperative
	 * cancellation only, never an interrupt of the agent thread: that thread may be reading the
	 * database, and an interrupt during a read closes H2's file for the whole application.
	 * </p>
	 *
	 * @param window
	 *        The tracker of the conversation the stop was pressed in.
	 * @param owner
	 *        The acting account.
	 * @param sessionId
	 *        The chat to stop; <code>null</code> for the run of the asking window.
	 * @return Whether a run was stopped; <code>false</code> for a chat of another account, a chat
	 *         that does not exist and a chat with no run alike.
	 */
	public static boolean cancel(TaskProgressTracker window, Person owner, String sessionId) {
		ChatProcessor.AIProcessingState state = window.getAnyActiveProcessingState();
		TaskProgressTracker runWindow = window;
		if (sessionId != null) {
			PendingInteractionRegistry.PendingRun run = PendingInteractionRegistry.findBySession(owner, sessionId);
			if (run != null) {
				state = run.state();
				runWindow = run.tracker();
			} else if (state != null && !sessionId.equals(state.getSessionId())) {
				// This window runs something, but not the chat the user pressed stop in - and the
				// account has no run there. Stopping the other one would stop a chat the user is not
				// even looking at.
				state = null;
			}
		}
		if (state == null) {
			return false;
		}

		runWindow.cancelRun(state);
		return true;
	}

	/** What became of a message the user wrote to a subagent. */
	public enum SubagentMessageStatus {
		/** The subagent reads it at the start of its next turn. */
		SENT("sent"),

		/** The subagent has ended and reads no more messages. */
		ENDED("ended"),

		/** There is no such subagent in a chat of this account. */
		UNKNOWN("unknown"),

		/** There was no text. */
		EMPTY("empty");

		private final String _code;

		SubagentMessageStatus(String code) {
			_code = code;
		}

		/** The status as the browser reads it. */
		public String code() {
			return _code;
		}
	}

	/**
	 * What the user's message to a subagent came to.
	 *
	 * @param status
	 *        What became of the message.
	 * @param messageId
	 *        The id of the message, empty unless it was {@link SubagentMessageStatus#SENT}; the frame
	 *        that says it was read names it.
	 */
	public record SubagentMessage(SubagentMessageStatus status, String messageId) {
		// Pure data.
	}

	/**
	 * Leaves the user's message in the inbox of one background subagent, which {@code send_to_subagent}
	 * fills too; the subagent reads it at the start of its next turn.
	 *
	 * @param window
	 *        The tracker of the conversation the message was written in; names the chat when
	 *        {@code sessionId} names none.
	 * @param person
	 *        The acting account.
	 * @param sessionId
	 *        The chat whose lead started the subagent.
	 * @param subagentId
	 *        The subagent's id.
	 * @param parentToolCallId
	 *        The lead's call that started it, which tells it from a namesake.
	 * @param text
	 *        What the user wrote; cut to {@link #MAX_SUBAGENT_MESSAGE_CHARS}.
	 */
	public static SubagentMessage messageSubagent(TaskProgressTracker window, Person person, String sessionId,
			String subagentId, String parentToolCallId, String text) {
		String message = text == null ? "" : text.strip();
		if (message.isEmpty()) {
			return new SubagentMessage(SubagentMessageStatus.EMPTY, "");
		}
		if (message.length() > MAX_SUBAGENT_MESSAGE_CHARS) {
			message = message.substring(0, MAX_SUBAGENT_MESSAGE_CHARS);
		}
		SubagentJob job = findOwnedSubagent(window, person, sessionId, subagentId, parentToolCallId);
		if (job == null) {
			return new SubagentMessage(SubagentMessageStatus.UNKNOWN, "");
		}
		SubagentInbox.Message posted = SubagentInbox.Message.of(SubagentInbox.Sender.USER, message);
		if (job.status().isTerminal() || !job.inbox().post(posted)) {
			return new SubagentMessage(SubagentMessageStatus.ENDED, "");
		}
		return new SubagentMessage(SubagentMessageStatus.SENT, posted.id());
	}

	/**
	 * Stops one background subagent, from its card or from the panel above the composer.
	 *
	 * @return Whether a running subagent was stopped; <code>false</code> for one of another account
	 *         and an unknown one alike.
	 * @see #messageSubagent(TaskProgressTracker, Person, String, String, String, String)
	 */
	public static boolean cancelSubagent(TaskProgressTracker window, Person person, String sessionId,
			String subagentId, String parentToolCallId) {
		SubagentJob job = findOwnedSubagent(window, person, sessionId, subagentId, parentToolCallId);
		if (job == null || job.status().isTerminal()) {
			return false;
		}
		job.cancellationToken().cancel();
		return true;
	}

	/**
	 * The subagent named, found in the named chat of this account, whichever of the account's
	 * windows started it; without a chat named, in the chat the window shows.
	 *
	 * @return The subagent, or <code>null</code> when there is no account, no such chat of this
	 *         account, or no such subagent in it.
	 */
	private static SubagentJob findOwnedSubagent(TaskProgressTracker window, Person person, String sessionId,
			String subagentId, String parentToolCallId) {
		if (person == null || subagentId == null) {
			return null;
		}
		String chat = sessionId == null || sessionId.isBlank() ? window.getActiveSessionId() : sessionId.trim();
		if (chat == null || ChatHistoryService.findOwnedSessionById(chat, person) == null) {
			return null;
		}
		return ChatSessionManager.findSubagent(chat, subagentId, parentToolCallId);
	}
}
