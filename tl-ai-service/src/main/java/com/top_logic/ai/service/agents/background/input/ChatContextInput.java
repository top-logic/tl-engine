/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents.background.input;

import com.top_logic.basic.config.annotation.Name;

/**
 * Input configuration that uses the chat conversation context.
 *
 * <p>
 * The background agent receives either the last turn or the full conversation history as input,
 * depending on the {@link #getContent()} setting.
 * </p>
 */
public interface ChatContextInput extends InputConfig {

	/** @see #getContent() */
	String CONTENT = "content";

	/**
	 * Defines how much of the chat context to include as input.
	 */
	enum Content {

		/**
		 * Include only the last turn (user message and assistant response).
		 *
		 * <p>
		 * This is the default.
		 * </p>
		 */
		LAST_TURN,

		/**
		 * Include the entire chat conversation history.
		 */
		FULL_CHAT
	}

	/**
	 * The amount of chat context to include as input.
	 *
	 * <p>
	 * Defaults to {@link Content#LAST_TURN}.
	 * </p>
	 *
	 * @return The content scope, never {@code null}.
	 */
	@Name(CONTENT)
	Content getContent();
}
