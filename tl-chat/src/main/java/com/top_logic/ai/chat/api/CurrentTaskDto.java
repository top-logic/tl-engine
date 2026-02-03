/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.api;

import java.util.List;

/**
 * Current (in-progress or just-finished) task for JSON API.
 */
public class CurrentTaskDto {

	private String _taskId;
	private String _status;
	private String _description;
	private List<TimelineEventDto> _timeline;
	private String _errorMessage;

	public String getTaskId() {
		return _taskId;
	}

	public void setTaskId(String taskId) {
		_taskId = taskId;
	}

	/** "in_progress", "completed", "error" */
	public String getStatus() {
		return _status;
	}

	public void setStatus(String status) {
		_status = status;
	}

	public String getDescription() {
		return _description;
	}

	public void setDescription(String description) {
		_description = description;
	}

	public List<TimelineEventDto> getTimeline() {
		return _timeline;
	}

	public void setTimeline(List<TimelineEventDto> timeline) {
		_timeline = timeline;
	}

	public String getErrorMessage() {
		return _errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		_errorMessage = errorMessage;
	}
}
