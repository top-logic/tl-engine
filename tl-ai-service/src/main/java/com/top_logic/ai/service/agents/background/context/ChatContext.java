/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents.background.context;

/**
 * Context for chat-scoped background agent execution.
 *
 * <p>
 * Extends {@link BackgroundContext} with chat-specific data: the user message, agent response, and
 * chat object. Types from {@code tl-chat} are stored as {@link Object} to avoid circular module
 * dependencies.
 * </p>
 */
public class ChatContext extends BackgroundContext {

	private final String _userMessage;

	private final String _agentResponse;

	private final Object _completedTask;

	private final Object _chat;

	private ChatContext(String agentName, String triggerType, String userMessage,
			String agentResponse, Object completedTask, Object chat) {
		super(agentName, triggerType);
		_userMessage = userMessage;
		_agentResponse = agentResponse;
		_completedTask = completedTask;
		_chat = chat;
	}

	/**
	 * Creates a context for post-turn execution with the last user/agent exchange.
	 */
	public static ChatContext lastTurn(String agentName, String userMessage,
			String agentResponse, Object completedTask) {
		return new ChatContext(agentName, "post-turn", userMessage, agentResponse, completedTask, null);
	}

	/**
	 * Creates a context for pre-turn execution with the full chat history.
	 */
	public static ChatContext fullChat(String agentName, Object chat) {
		return new ChatContext(agentName, "pre-turn", null, null, null, chat);
	}

	/** The user's message from the last turn, or {@code null} for pre-turn. */
	public String getUserMessage() {
		return _userMessage;
	}

	/** The agent's response from the last turn, or {@code null} for pre-turn. */
	public String getAgentResponse() {
		return _agentResponse;
	}

	/** The completed chat task (from tl-chat), or {@code null}. Cast to {@code ChatTask}. */
	public Object getCompletedTask() {
		return _completedTask;
	}

	/** The full chat object (from tl-chat), or {@code null}. Cast to {@code Chat}. */
	public Object getChat() {
		return _chat;
	}
}
