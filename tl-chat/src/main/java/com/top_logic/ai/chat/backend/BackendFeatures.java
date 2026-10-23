/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.backend;

/**
 * Which functions of the chat beyond sending a message a conversation's answerer supports.
 *
 * <p>
 * The framework's own agent supports all of them ({@link #INTERNAL}). A
 * {@link ConversationBackend} states what it can do; the chat offers a function the backend does
 * not support neither in the browser nor through its requests.
 * </p>
 *
 * @param regenerate
 *        Whether an answer can be produced again for the same message.
 * @param editMessages
 *        Whether the conversation can be truncated at a message, which editing a sent message
 *        does.
 * @param agentPicker
 *        Whether the user can choose the agent that answers.
 * @param modelPicker
 *        Whether the user can choose the model.
 * @param effortPicker
 *        Whether the user can choose the thinking effort.
 * @param mcpPicker
 *        Whether the user can choose the MCP servers attached to the run.
 * @param attachments
 *        Whether a message may carry files.
 * @param approvals
 *        Whether the run asks the user for approvals and answers to questions in the chat.
 */
public record BackendFeatures(boolean regenerate, boolean editMessages, boolean agentPicker,
		boolean modelPicker, boolean effortPicker, boolean mcpPicker, boolean attachments,
		boolean approvals) {

	/** Every function: what the framework's own agent supports. */
	public static final BackendFeatures INTERNAL = new BackendFeatures(true, true, true, true, true, true, true, true);

	/** Sending a message and nothing beyond. */
	public static final BackendFeatures NONE =
		new BackendFeatures(false, false, false, false, false, false, false, false);

}
