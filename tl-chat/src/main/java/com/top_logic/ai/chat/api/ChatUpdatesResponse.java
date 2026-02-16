/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.api;

import java.util.List;

/**
 * Response body for GET /chat/updates.
 */
public class ChatUpdatesResponse {

	private boolean _processing;
	private List<MessageDto> _messages;
	private CurrentTaskDto _currentTask;

	/** Whether an AI task is currently in progress. */
	public boolean isProcessing() {
		return _processing;
	}

	/** @see #isProcessing() */
	public void setProcessing(boolean processing) {
		_processing = processing;
	}

	/** All messages in the chat. */
	public List<MessageDto> getMessages() {
		return _messages;
	}

	/** @see #getMessages() */
	public void setMessages(List<MessageDto> messages) {
		_messages = messages;
	}

	/** Current or most recent task; null if no task. */
	public CurrentTaskDto getCurrentTask() {
		return _currentTask;
	}

	/** @see #getCurrentTask() */
	public void setCurrentTask(CurrentTaskDto currentTask) {
		_currentTask = currentTask;
	}
}
