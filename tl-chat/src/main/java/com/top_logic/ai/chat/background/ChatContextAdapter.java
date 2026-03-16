/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.background;

import com.top_logic.ai.chat.layout.Chat;
import com.top_logic.ai.chat.layout.ChatTask;
import com.top_logic.ai.chat.layout.ConversationSerializer;
import com.top_logic.ai.service.agents.background.context.BackgroundContext;
import com.top_logic.ai.service.agents.background.context.ChatContext;
import com.top_logic.ai.service.agents.background.input.ChatContextInput;
import com.top_logic.ai.service.agents.background.input.InputAdapter;

/**
 * Input adapter that serializes chat conversation data for background agents.
 *
 * <p>
 * Lives in {@code tl-chat} because it depends on {@link ConversationSerializer} and the concrete
 * chat domain types ({@link Chat}, {@link ChatTask}).
 * </p>
 */
public class ChatContextAdapter implements InputAdapter {

	private final ChatContextInput.Content _content;

	/**
	 * Creates a new {@link ChatContextAdapter}.
	 *
	 * @param config
	 *        The chat context input configuration specifying the content scope.
	 */
	public ChatContextAdapter(ChatContextInput config) {
		_content = config.getContent();
	}

	@Override
	public String gatherInput(BackgroundContext context) {
		if (!(context instanceof ChatContext chatCtx)) {
			throw new IllegalArgumentException(
				"ChatContextAdapter requires a ChatContext, got: " + context.getClass().getName());
		}
		if (_content == ChatContextInput.Content.LAST_TURN) {
			Object task = chatCtx.getCompletedTask();
			if (task != null && !(task instanceof ChatTask)) {
				throw new IllegalArgumentException(
					"Expected ChatTask but got: " + task.getClass().getName());
			}
			return ConversationSerializer.serializeLastTurn(
				chatCtx.getUserMessage(),
				chatCtx.getAgentResponse(),
				(ChatTask) task);
		} else {
			Object chat = chatCtx.getChat();
			if (!(chat instanceof Chat)) {
				throw new IllegalArgumentException(
					"Expected Chat but got: " + (chat != null ? chat.getClass().getName() : "null"));
			}
			return ConversationSerializer.serializeFullChat((Chat) chat);
		}
	}
}
