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

	private final Object _chat;

	private final String _pendingUserMessage;

	private ChatContext(String agentName, String triggerType, String userMessage,
			String agentResponse, Object chat, String pendingUserMessage) {
		super(agentName, triggerType);
		_userMessage = userMessage;
		_agentResponse = agentResponse;
		_chat = chat;
		_pendingUserMessage = pendingUserMessage;
	}

	/**
	 * Creates a context for post-turn execution with the last user/agent exchange.
	 */
	public static ChatContext lastTurn(String agentName, String userMessage,
			String agentResponse, Object chat) {
		return new ChatContext(agentName, "post-turn", userMessage, agentResponse, chat, null);
	}

	/**
	 * Creates a context for pre-turn execution with the pending user message.
	 *
	 * <p>
	 * The pending user message is the raw text the user submitted, before the primary agent
	 * processes it. Pre-turn agents receive this directly as input without needing an
	 * {@link com.top_logic.ai.service.agents.background.input.InputConfig InputConfig}.
	 * </p>
	 *
	 * @param agentName
	 *        The name of the background agent.
	 * @param pendingUserMessage
	 *        The user's message that is about to be processed.
	 * @param chat
	 *        The chat object (from tl-chat).
	 */
	public static ChatContext preTurn(String agentName, String pendingUserMessage, Object chat) {
		return new ChatContext(agentName, "pre-turn", null, null, chat, pendingUserMessage);
	}

	/** The user's message from the last turn, or {@code null} for pre-turn. */
	public String getUserMessage() {
		return _userMessage;
	}

	/** The agent's response from the last turn, or {@code null} for pre-turn. */
	public String getAgentResponse() {
		return _agentResponse;
	}

	/** The full chat object (from tl-chat), or {@code null}. Cast to {@code Chat}. */
	public Object getChat() {
		return _chat;
	}

	/**
	 * The pending user message for pre-turn execution.
	 *
	 * <p>
	 * This is the raw user text that is about to be processed by the primary agent. Only set for
	 * pre-turn contexts created via {@link #preTurn(String, String, Object)}.
	 * </p>
	 *
	 * @return The pending user message, or {@code null} for post-turn contexts.
	 */
	public String getPendingUserMessage() {
		return _pendingUserMessage;
	}
}
