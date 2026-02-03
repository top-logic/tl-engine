/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.api;

import java.util.concurrent.ScheduledExecutorService;

import com.top_logic.ai.chat.layout.ChatControl;
import com.top_logic.ai.chat.layout.ChatControl.SendResult;
import com.top_logic.ai.chat.layout.TaskProgressTracker;

/**
 * Shared send logic for the chat: used by the TL UI (SendMessage command) and the JSON API
 * (ChatServlet). Delegates to {@link ChatControl#sendMessage}.
 */
public final class ChatSendService {

	private ChatSendService() {
	}

	/**
	 * Sends a user message: adds it to the chat, starts a task, and runs the orchestrator in the
	 * background. When {@code uiExecutor} is null (e.g. for the JSON API), no server-side polling
	 * is scheduled; the client should poll GET /chat/updates.
	 *
	 * @param tracker
	 *        Task progress tracker for this session.
	 * @param userName
	 *        Display name of the user.
	 * @param userMessage
	 *        The message text.
	 * @param repaintRequester
	 *        Called when polling has updates (TL UI); may be null for API-only.
	 * @param uiExecutor
	 *        Top Logic UI executor for server-side polling; null for API-only.
	 * @return The send result (task ID, or busy/error); never null.
	 */
	public static SendResult send(TaskProgressTracker tracker, String userName, String userMessage,
			Runnable repaintRequester, ScheduledExecutorService uiExecutor) {
		return ChatControl.sendMessage(tracker, userName, userMessage, repaintRequester, uiExecutor);
	}
}
