/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.history;

import java.util.Date;

import com.top_logic.ai.chat.layout.ChatStart;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;
import com.top_logic.tl.ai.core.execution.model.AgentExecution;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.execution.model.InteractionRequest;
import com.top_logic.tl.ai.core.execution.model.TlAiExecutionFactory;
import com.top_logic.util.TLContext;
import com.top_logic.util.TLContextManager;

/**
 * TL-Script functions about chats.
 *
 * <p>
 * Example usage in TL-Script - the state of a chat as a row class of the chat list:
 * </p>
 *
 * <pre>
 * s -> switch {
 *     chatActivity($s) == "waiting": "is-waiting";
 *     chatActivity($s) == "news": "has-news";
 *     chatActivity($s) == "running": "is-working";
 *     default: null;
 * }
 * </pre>
 */
@ScriptPrefix("chat")
public class ChatScriptFunctions extends TLScriptFunctions {

	/** {@link #activity(Object)}: a question or an approval of a run of the chat waits for the user. */
	public static final String WAITING = "waiting";

	/** {@link #activity(Object)}: a subagent of the chat ended since a window last showed the chat. */
	public static final String NEWS = "news";

	/** {@link #activity(Object)}: a run of the chat works - the agent or one of its subagents. */
	public static final String RUNNING = "running";

	/**
	 * What goes on in a chat that the user may not see: whether something waits for them, a
	 * subagent has finished since they last looked, or an agent still works there.
	 *
	 * <p>
	 * Subagents outlive the window that started them, so a chat the user has left can still work,
	 * finish or need an answer. This is what a chat list shows about each chat. The answers are
	 * ranked: something that waits for the user comes first, then a result they have not seen, then
	 * work in progress.
	 * </p>
	 *
	 * <p>
	 * "Seen" means a window of the owner showed the chat: when the user opens it, leaves it, or has it
	 * open while the subagent ends.
	 * </p>
	 *
	 * @param session
	 *        The chat, a {@code tl.ai.execution:AgentSession}.
	 * @return {@code "waiting"}, {@code "news"} or {@code "running"}; <code>null</code> when none of
	 *         them applies, or when the argument is no chat.
	 */
	public static String activity(Object session) {
		if (!(session instanceof AgentSession chat)) {
			return null;
		}
		Person user = TLContext.currentUser();
		long lastSeen = ChatHistoryService.lastSeen(chat);
		TLClassifier running = TlAiExecutionFactory.getRUNNINGExecutionStatusClassifier();
		boolean working = false;
		boolean news = false;
		for (TLObject referer : chat.tReferers(TlAiExecutionFactory.getSessionAgentExecutionAttr())) {
			AgentExecution execution = (AgentExecution) referer;
			if (running.equals(execution.getStatus())) {
				if (waitsFor(execution, user)) {
					return WAITING;
				}
				working = true;
			} else if (execution.getParentExecution() != null && endedAfter(execution, lastSeen)) {
				news = true;
			}
		}
		return news ? NEWS : working ? RUNNING : null;
	}

	/**
	 * Starts a chat in the current window: a new conversation about the given object, whose first
	 * message is sent at once, as if the user had typed it into the chat.
	 *
	 * <p>
	 * For a command giving an order - "work on this" - rather than waiting for the user to type.
	 * The conversation is about the given object from its start, so that what answers conversations
	 * about such objects answers the first message too. The message is sent through the chat of the
	 * window named by {@code chatId}: write the result to the {@code input} channel of a
	 * {@code <chat-conversation>} of that chat, and it shows the answer as it is written.
	 * </p>
	 *
	 * @param chatId
	 *        The chat of the window that sends the message, the {@code chat-id} of the chat elements
	 *        that show it.
	 * @param subject
	 *        What the conversation is about, empty for nothing.
	 * @param text
	 *        The first message.
	 * @return The new conversation, a {@code tl.ai.execution:AgentSession}.
	 */
	@Label("Start a chat with a message")
	public static AgentSession start(@Mandatory String chatId, TLObject subject, @Mandatory String text) {
		return ChatStart.start(TLContextManager.getSubSession(), chatId, TLContext.currentUser(), subject, text);
	}

	/** Whether a question or an approval of the given run waits for the given account. */
	private static boolean waitsFor(AgentExecution execution, Person user) {
		TLClassifier pending = TlAiExecutionFactory.getPENDINGInteractionStateClassifier();
		for (TLObject referer : execution.tReferers(TlAiExecutionFactory.getExecutionInteractionRequestAttr())) {
			InteractionRequest request = (InteractionRequest) referer;
			if (pending.equals(request.getState())
				&& (request.getResponder() == null || request.getResponder().equals(user))) {
				return true;
			}
		}
		return false;
	}

	private static boolean endedAfter(AgentExecution execution, long time) {
		Date ended = execution.getEndedAt();
		return ended != null && ended.getTime() > time;
	}
}
