/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.util.List;

import com.top_logic.ai.chat.api.ApprovalChoice;
import com.top_logic.ai.chat.api.SendRefusal;
import com.top_logic.ai.chat.backend.ConversationBackendService;
import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.base.context.TLSessionContext;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.util.error.TopLogicException;

/**
 * Starts a chat on the server: a new conversation about a given object, whose first message is
 * sent the way the browser sends one.
 *
 * <p>
 * For a command that gives an order rather than a chat the user types into - "work on this" from a
 * button of the page. The conversation is created with its subject, so that a
 * {@link com.top_logic.ai.chat.backend.ConversationBackendResolver} deciding from the subject
 * answers the first message, and it is sent through the chat of the window the command runs in:
 * the window's chat element that shows the conversation afterwards receives the run as it is
 * written, offers the stop and shows it busy, as after a message typed into it. A chat element of
 * that name that the window has not displayed yet finds the run when it is displayed.
 * </p>
 */
public final class ChatStart {

	private ChatStart() {
		// Static utility.
	}

	/**
	 * Creates a conversation about the given subject and sends its first message.
	 *
	 * @param window
	 *        The sub-session of the window whose chat sends the message.
	 * @param conversation
	 *        The chat of that window that sends the message - the {@code chat-id} of the chat
	 *        elements showing it.
	 * @param owner
	 *        The account the conversation belongs to, the one acting in the window.
	 * @param subject
	 *        What the conversation is about, <code>null</code> for nothing.
	 * @param text
	 *        The first message.
	 * @return The new conversation, a run answering the message is in progress.
	 * @throws TopLogicException
	 *         If the message cannot be sent: the agent policy refuses it, the installation has no
	 *         room for a run. The conversation is not kept then.
	 */
	public static AgentSession start(TLSubSessionContext window, String conversation, Person owner,
			TLObject subject, String text) {
		String message = text == null ? "" : text.trim();
		if (message.isEmpty()) {
			throw new TopLogicException(I18NConstants.ERROR_EMPTY_MESSAGE);
		}
		AgentSession session = (AgentSession) ChatHistoryService.createSession(title(message), owner,
			ApprovalChoice.load(), subject);
		if (session == null) {
			throw new TopLogicException(I18NConstants.ERROR_CHAT_NOT_CREATED);
		}
		// The session was written in a transaction of the store's writer thread, at a revision newer
		// than this thread's read view.
		HistoryUtils.updateSessionRevision();

		boolean sent = false;
		try {
			// A conversation a backend answers runs no agent of this application, so the agent
			// policy does not apply to it, as for a message of the browser.
			if (ConversationBackendService.backendFor(session) == null) {
				SendRefusal refusal = SendRefusal.forRun(owner, ChatProcessor.ChatRunOptions.DEFAULT);
				if (refusal != null) {
					throw new TopLogicException(I18NConstants.ERROR_NOT_SENT__REASON.fill(refusal.reason()));
				}
			}
			TaskProgressTracker tracker = tracker(window, conversation, owner);
			String sessionId = session.tHandle().getObjectName().toExternalForm();
			if (!tracker.setActiveSessionId(sessionId, owner)) {
				throw new TopLogicException(I18NConstants.ERROR_CHAT_NOT_CREATED);
			}
			ChatProcessor.SendResult result = ChatProcessor.sendMessage(tracker, window, owner.getName(),
				message, List.of(), ChatProcessor.ChatRunOptions.DEFAULT);
			if (result.getTaskId() == null) {
				throw new TopLogicException(I18NConstants.ERROR_NOT_SENT__REASON.fill(
					result.getReason() == null ? "" : result.getReason()));
			}
			sent = true;
			return session;
		} finally {
			if (!sent) {
				ChatHistoryService.deleteSession(session);
			}
		}
	}

	/**
	 * The chat {@code conversation} of the window, registered for the chat elements of that name
	 * when the window has not displayed one yet.
	 */
	private static TaskProgressTracker tracker(TLSubSessionContext window, String conversation, Person owner) {
		TaskProgressTracker tracker = ChatSessionRegistry.tracker(window, conversation);
		if (tracker == null) {
			tracker = new TaskProgressTracker();
			TLSessionContext session = window.getSessionContext();
			tracker.setWindowId(ChatSessionRegistry.windowId(session, window));
			ChatSessionRegistry.register(window, conversation, tracker);
		}
		if (tracker.getUserName() == null) {
			tracker.setUserName(owner.getName());
		}
		return tracker;
	}

	/** The title of a chat named after its first message, as the browser names a chat it creates. */
	static String title(String message) {
		String plain = message.trim().replaceAll("\\s+", " ");
		return plain.length() > MAX_TITLE ? plain.substring(0, MAX_TITLE) + "…" : plain;
	}

	/** Length of a title taken from a message, before it is cut. */
	private static final int MAX_TITLE = 72;

}
