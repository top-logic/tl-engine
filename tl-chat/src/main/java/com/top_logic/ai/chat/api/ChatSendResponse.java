/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.api;

/**
 * Response body for POST /chat/send.
 */
public class ChatSendResponse {

	private String _taskId;
	private String _status;

	public ChatSendResponse(String taskId, String status) {
		_taskId = taskId;
		_status = status;
	}

	/** Task ID for polling updates. */
	public String getTaskId() {
		return _taskId;
	}

	/** @see #getTaskId() */
	public void setTaskId(String taskId) {
		_taskId = taskId;
	}

	/** e.g. "processing" */
	public String getStatus() {
		return _status;
	}

	/** @see #getStatus() */
	public void setStatus(String status) {
		_status = status;
	}
}
