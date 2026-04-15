/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.top_logic.tl.ai.core.message.TokenUsage;

/**
 * Represents a high-level task in the chat with status tracking and a unified execution timeline
 * (tool calls and agent-to-agent calls).
 *
 * <p>
 * This class is immutable. Use the {@link Builder} to create modified versions.
 * </p>
 */
public class ChatTask {

	/**
	 * Status of a task.
	 */
	public enum TaskStatus {
		/** Task has been created but not yet started. */
		PENDING,

		/** Task is currently being executed. */
		IN_PROGRESS,

		/** Task completed successfully. */
		COMPLETED,

		/** Task failed with an error. */
		ERROR
	}

	private final String _id;

	private final String _description;

	private final TaskStatus _status;

	private final Instant _startTime;

	private final Instant _endTime;

	private final String _errorMessage;

	private final List<ChatTimelineEvent> _timelineEvents;

	private final TokenUsage _totalUsage;

	private final String _thinkingText;

	/**
	 * Creates a new {@link ChatTask} with PENDING status.
	 *
	 * @param description
	 *        The task description.
	 */
	public ChatTask(String description) {
		this(UUID.randomUUID().toString(), description, TaskStatus.PENDING, Instant.now(), null, null,
			new ArrayList<>(), TokenUsage.EMPTY, "");
	}

	private ChatTask(String id, String description, TaskStatus status, Instant startTime, Instant endTime,
			String errorMessage, List<ChatTimelineEvent> timelineEvents, TokenUsage totalUsage, String thinkingText) {
		_id = id;
		_description = description;
		_status = status;
		_startTime = startTime;
		_endTime = endTime;
		_errorMessage = errorMessage;
		_timelineEvents = new ArrayList<>(timelineEvents);
		_totalUsage = totalUsage;
		_thinkingText = thinkingText != null ? thinkingText : "";
	}

	/**
	 * Returns the unique identifier of this task.
	 */
	public String getId() {
		return _id;
	}

	/**
	 * Returns the task description.
	 */
	public String getDescription() {
		return _description;
	}

	/**
	 * Returns the current status of this task.
	 */
	public TaskStatus getStatus() {
		return _status;
	}

	/**
	 * Returns the time when this task was started.
	 */
	public Instant getStartTime() {
		return _startTime;
	}

	/**
	 * Returns the time when this task was completed, or null if not yet complete.
	 */
	public Instant getEndTime() {
		return _endTime;
	}

	/**
	 * Returns the error message if the task failed, or null otherwise.
	 */
	public String getErrorMessage() {
		return _errorMessage;
	}

	/**
	 * Returns an unmodifiable view of the execution timeline (tool calls and agent calls in order).
	 */
	public List<ChatTimelineEvent> getTimelineEvents() {
		return Collections.unmodifiableList(_timelineEvents);
	}

	/**
	 * Returns the total token usage for the run that produced this task, or {@link TokenUsage#EMPTY}.
	 */
	public TokenUsage getTotalUsage() {
		return _totalUsage;
	}

	/**
	 * Returns the accumulated extended-thinking text, or empty string if none.
	 */
	public String getThinkingText() {
		return _thinkingText;
	}

	/**
	 * Returns tool calls from the timeline for backward compatibility.
	 */
	public List<ChatToolCall> getToolCalls() {
		return Collections.unmodifiableList(_timelineEvents.stream()
			.filter(e -> e.getKind() == ChatTimelineEvent.Kind.TOOL_CALL && e.getToolCall() != null)
			.map(ChatTimelineEvent::getToolCall)
			.collect(Collectors.toList()));
	}

	/**
	 * Creates a builder initialized with this task's current state.
	 */
	public Builder toBuilder() {
		return new Builder(this);
	}

	/**
	 * Builder for creating modified versions of a {@link ChatTask}.
	 */
	public static class Builder {
		private String _id;

		private String _description;

		private TaskStatus _status;

		private Instant _startTime;

		private Instant _endTime;

		private String _errorMessage;

		private List<ChatTimelineEvent> _timelineEvents;

		private TokenUsage _totalUsage;

		private String _thinkingText;

		private Builder(ChatTask task) {
			_id = task._id;
			_description = task._description;
			_status = task._status;
			_startTime = task._startTime;
			_endTime = task._endTime;
			_errorMessage = task._errorMessage;
			_timelineEvents = new ArrayList<>(task._timelineEvents);
			_totalUsage = task._totalUsage;
			_thinkingText = task._thinkingText;
		}

		/**
		 * Sets the task status.
		 */
		public Builder withStatus(TaskStatus status) {
			_status = status;
			return this;
		}

		/**
		 * Sets the task end time.
		 */
		public Builder withEndTime(Instant endTime) {
			_endTime = endTime;
			return this;
		}

		/**
		 * Sets the error message.
		 */
		public Builder withError(String error) {
			_errorMessage = error;
			return this;
		}

		/**
		 * Adds a tool call to this task (stored as a TOOL_CALL timeline event).
		 */
		public Builder addToolCall(ChatToolCall toolCall) {
			_timelineEvents.add(ChatTimelineEvent.toolCall(toolCall));
			return this;
		}

		/**
		 * Adds a timeline event (tool call or agent call).
		 */
		public Builder addTimelineEvent(ChatTimelineEvent event) {
			_timelineEvents.add(event);
			return this;
		}

		/**
		 * Sets the total token usage for this task.
		 */
		public Builder withTotalUsage(TokenUsage usage) {
			_totalUsage = usage;
			return this;
		}

		/**
		 * Sets the accumulated extended-thinking text.
		 */
		public Builder withThinkingText(String thinkingText) {
			_thinkingText = thinkingText;
			return this;
		}

		/**
		 * Returns the current list of timeline events (mutable for updates).
		 */
		public List<ChatTimelineEvent> getTimelineEvents() {
			return _timelineEvents;
		}

		/**
		 * Builds the modified task.
		 */
		public ChatTask build() {
			return new ChatTask(_id, _description, _status, _startTime, _endTime, _errorMessage, _timelineEvents,
				_totalUsage, _thinkingText);
		}
	}
}
