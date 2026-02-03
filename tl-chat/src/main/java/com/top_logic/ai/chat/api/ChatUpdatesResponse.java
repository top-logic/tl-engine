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

	public boolean isProcessing() {
		return _processing;
	}

	public void setProcessing(boolean processing) {
		_processing = processing;
	}

	public List<MessageDto> getMessages() {
		return _messages;
	}

	public void setMessages(List<MessageDto> messages) {
		_messages = messages;
	}

	public CurrentTaskDto getCurrentTask() {
		return _currentTask;
	}

	public void setCurrentTask(CurrentTaskDto currentTask) {
		_currentTask = currentTask;
	}
}
