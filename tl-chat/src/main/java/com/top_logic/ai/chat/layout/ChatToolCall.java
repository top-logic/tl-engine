/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a single tool invocation with its arguments and result.
 *
 * <p>
 * This class is immutable.
 * </p>
 */
public class ChatToolCall {

	/**
	 * Status of a tool call.
	 */
	public enum ToolCallStatus {
		/** Tool call has been created but not yet started. */
		PENDING,

		/** Tool call is currently executing. */
		RUNNING,

		/** Tool call completed successfully. */
		SUCCESS,

		/** Tool call failed with an error. */
		ERROR
	}

	private final String _id;

	private final String _toolName;

	private final Map<String, Object> _args;

	private final String _resultJson;

	private final Instant _startTime;

	private final Instant _endTime;

	private final ToolCallStatus _status;

	/**
	 * Creates a new tool call with RUNNING status.
	 *
	 * @param toolName
	 *        The name of the tool being called.
	 * @param args
	 *        The arguments passed to the tool.
	 */
	public ChatToolCall(String toolName, Map<String, Object> args) {
		this(UUID.randomUUID().toString(), toolName, args, null, Instant.now(), null, ToolCallStatus.RUNNING);
	}

	/**
	 * Creates a new tool call with specified result and status.
	 *
	 * @param toolName
	 *        The name of the tool being called.
	 * @param args
	 *        The arguments passed to the tool.
	 * @param resultJson
	 *        The JSON result from the tool.
	 */
	public ChatToolCall(String toolName, Map<String, Object> args, String resultJson) {
		this(UUID.randomUUID().toString(), toolName, args, resultJson, Instant.now(), Instant.now(),
			ToolCallStatus.SUCCESS);
	}

	private ChatToolCall(String id, String toolName, Map<String, Object> args, String resultJson, Instant startTime,
			Instant endTime, ToolCallStatus status) {
		_id = id;
		_toolName = toolName;
		_args = args != null ? Collections.unmodifiableMap(args) : Collections.emptyMap();
		_resultJson = resultJson;
		_startTime = startTime;
		_endTime = endTime;
		_status = status;
	}

	/**
	 * Returns the unique identifier of this tool call.
	 */
	public String getId() {
		return _id;
	}

	/**
	 * Returns the name of the tool that was called.
	 */
	public String getToolName() {
		return _toolName;
	}

	/**
	 * Returns the arguments passed to the tool.
	 */
	public Map<String, Object> getArgs() {
		return _args;
	}

	/**
	 * Returns the JSON result from the tool, or null if not yet available.
	 */
	public String getResultJson() {
		return _resultJson;
	}

	/**
	 * Returns the time when this tool call started.
	 */
	public Instant getStartTime() {
		return _startTime;
	}

	/**
	 * Returns the time when this tool call completed, or null if not yet complete.
	 */
	public Instant getEndTime() {
		return _endTime;
	}

	/**
	 * Returns the status of this tool call.
	 */
	public ToolCallStatus getStatus() {
		return _status;
	}

	/**
	 * Creates a completed version of this tool call with success status.
	 *
	 * @param resultJson
	 *        The JSON result from the tool.
	 * @return A new tool call with SUCCESS status.
	 */
	public ChatToolCall complete(String resultJson) {
		return new ChatToolCall(_id, _toolName, _args, resultJson, _startTime, Instant.now(), ToolCallStatus.SUCCESS);
	}

	/**
	 * Creates a failed version of this tool call with error status.
	 *
	 * @param errorJson
	 *        The JSON error message.
	 * @return A new tool call with ERROR status.
	 */
	public ChatToolCall error(String errorJson) {
		return new ChatToolCall(_id, _toolName, _args, errorJson, _startTime, Instant.now(), ToolCallStatus.ERROR);
	}
}
