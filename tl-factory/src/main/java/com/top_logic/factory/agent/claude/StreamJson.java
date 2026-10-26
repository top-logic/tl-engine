/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.agent.claude;

/**
 * Names of the stream-json protocol of Claude Code ({@code --input-format stream-json
 * --output-format stream-json}): message types, subtypes and fields.
 *
 * <p>
 * Every line is one JSON object with a {@link #TYPE}. The events of the Messages API streamed by
 * {@code --include-partial-messages} come as {@link #TYPE_STREAM_EVENT} carrying the API event in
 * {@link #EVENT}.
 * </p>
 */
public final class StreamJson {

	private StreamJson() {
		// Constants only.
	}

	// ---- Fields of every line ----

	/** Field naming the kind of a line. */
	public static final String TYPE = "type";

	/** Field refining the {@link #TYPE} of a {@link #TYPE_SYSTEM} or {@link #TYPE_RESULT} line. */
	public static final String SUBTYPE = "subtype";

	/** Field with the ID under which Claude Code keeps the conversation. */
	public static final String SESSION_ID = "session_id";

	/**
	 * Field with the ID of the tool call of a subagent's lead whose subagent produced the line,
	 * {@code null} for a line of the main agent.
	 */
	public static final String PARENT_TOOL_USE_ID = "parent_tool_use_id";

	// ---- Line types ----

	/** Line type: what Claude Code reports about itself, see {@link #SUBTYPE}. */
	public static final String TYPE_SYSTEM = "system";

	/** Line type: an event of the Messages API, in {@link #EVENT}. */
	public static final String TYPE_STREAM_EVENT = "stream_event";

	/** Line type: a complete content block of an assistant message, in {@link #MESSAGE}. */
	public static final String TYPE_ASSISTANT = "assistant";

	/** Line type: a user message - written to Claude Code, or tool results it reports. */
	public static final String TYPE_USER = "user";

	/** Line type: the end of a run with its outcome, usage and cost. */
	public static final String TYPE_RESULT = "result";

	/** Line type: a request to Claude Code outside the conversation, e.g. to interrupt it. */
	public static final String TYPE_CONTROL_REQUEST = "control_request";

	// ---- System subtypes ----

	/** System subtype: the start of a run, with the {@link #SESSION_ID} and the {@link #MODEL}. */
	public static final String SUBTYPE_INIT = "init";

	/** System subtype: a failed API request is repeated after a delay. */
	public static final String SUBTYPE_API_RETRY = "api_retry";

	/** Field of an {@link #SUBTYPE_API_RETRY} line: the number of the repetition, from 1. */
	public static final String ATTEMPT = "attempt";

	/** Field of an {@link #SUBTYPE_API_RETRY} line: the delay before the repetition. */
	public static final String RETRY_DELAY_MS = "retry_delay_ms";

	/** Field of an {@link #SUBTYPE_API_RETRY} line: the HTTP status of the failed request. */
	public static final String ERROR_STATUS = "error_status";

	/** Field of an {@link #SUBTYPE_API_RETRY} line: the kind of the error, e.g. {@code authentication_failed}. */
	public static final String ERROR = "error";

	/** System subtype: the conversation was compacted, see {@link #COMPACT_METADATA}. */
	public static final String SUBTYPE_COMPACT_BOUNDARY = "compact_boundary";

	/** Field of a {@link #SUBTYPE_COMPACT_BOUNDARY} line. */
	public static final String COMPACT_METADATA = "compact_metadata";

	/** Field of the {@link #COMPACT_METADATA}: {@code auto} or {@code manual}. */
	public static final String TRIGGER = "trigger";

	/** Field of the {@link #COMPACT_METADATA}: the tokens of the conversation before compaction. */
	public static final String PRE_TOKENS = "pre_tokens";

	// ---- Result ----

	/** Result subtype of a run that ended normally. */
	public static final String SUBTYPE_SUCCESS = "success";

	/** Result subtype of a run that reached the turn limit. */
	public static final String SUBTYPE_ERROR_MAX_TURNS = "error_max_turns";

	/** Result subtype of a run that reached the budget limit. */
	public static final String SUBTYPE_ERROR_MAX_BUDGET = "error_max_budget_usd";

	/** Field of a result: whether the run failed, also for subtype {@link #SUBTYPE_SUCCESS}. */
	public static final String IS_ERROR = "is_error";

	/** Field of a result: the final answer text. */
	public static final String RESULT = "result";

	/** Field of a result: the error messages of a failed run. */
	public static final String ERRORS = "errors";

	/** Field of a result: the HTTP status of a failed API request, e.g. 401. */
	public static final String API_ERROR_STATUS = "api_error_status";

	/** Field of a result: the cost of the session so far, in US dollars, over all its processes. */
	public static final String TOTAL_COST_USD = "total_cost_usd";

	/** Field of a result: the usage per model since the previous result, with its cost. */
	public static final String MODEL_USAGE_SINCE_LAST_RESULT = "model_usage_since_last_result";

	/** Field of an entry of {@link #MODEL_USAGE_SINCE_LAST_RESULT}: the cost in US dollars. */
	public static final String COST_USD = "costUSD";

	/**
	 * Field of an entry of {@link #MODEL_USAGE_SINCE_LAST_RESULT}: the context window of the model,
	 * in tokens.
	 */
	public static final String CONTEXT_WINDOW = "contextWindow";

	// ---- Messages ----

	/** Field of an {@link #TYPE_ASSISTANT} or {@link #TYPE_USER} line: the message. */
	public static final String MESSAGE = "message";

	/** Field of a message: its ID. */
	public static final String ID = "id";

	/** Field of a message or of an init line: the model. */
	public static final String MODEL = "model";

	/** Field of a message: its role. */
	public static final String ROLE = "role";

	/** Field of a message: its content blocks. */
	public static final String CONTENT = "content";

	/** Field of a message, of a {@link #EVENT_MESSAGE_DELTA} and of a result: the token usage. */
	public static final String USAGE = "usage";

	/** Role of a message of the user. */
	public static final String ROLE_USER = "user";

	// ---- Content blocks ----

	/** Content block type: text. */
	public static final String BLOCK_TEXT = "text";

	/** Content block type: a tool call of the model. */
	public static final String BLOCK_TOOL_USE = "tool_use";

	/** Content block type: the result of a tool call. */
	public static final String BLOCK_TOOL_RESULT = "tool_result";

	/** Field of a {@link #BLOCK_TEXT} block. */
	public static final String TEXT = "text";

	/** Field of a {@link #BLOCK_TOOL_USE} block: the name of the tool. */
	public static final String NAME = "name";

	/** Field of a {@link #BLOCK_TOOL_USE} block: the arguments. */
	public static final String INPUT = "input";

	/** Field of a {@link #BLOCK_TOOL_RESULT} block: the ID of the tool call. */
	public static final String TOOL_USE_ID = "tool_use_id";

	// ---- Stream events ----

	/** Field of a {@link #TYPE_STREAM_EVENT} line: the event of the Messages API. */
	public static final String EVENT = "event";

	/** API event: start of a message, i.e. of one request of the run. */
	public static final String EVENT_MESSAGE_START = "message_start";

	/** API event: a piece of a content block, see {@link #DELTA}. */
	public static final String EVENT_CONTENT_BLOCK_DELTA = "content_block_delta";

	/** API event: the end of a message with its final usage. */
	public static final String EVENT_MESSAGE_DELTA = "message_delta";

	/** Field of a {@link #EVENT_CONTENT_BLOCK_DELTA}. */
	public static final String DELTA = "delta";

	/** Delta type: a piece of text. */
	public static final String DELTA_TEXT = "text_delta";

	/** Delta type: a piece of thinking text. */
	public static final String DELTA_THINKING = "thinking_delta";

	/** Field of a {@link #DELTA_THINKING} delta. */
	public static final String THINKING = "thinking";

	// ---- Usage ----

	/** Field of a usage: uncached input tokens. */
	public static final String INPUT_TOKENS = "input_tokens";

	/** Field of a usage: output tokens, thinking included. */
	public static final String OUTPUT_TOKENS = "output_tokens";

	/** Field of a usage: input tokens read from the cache. */
	public static final String CACHE_READ_INPUT_TOKENS = "cache_read_input_tokens";

	/** Field of a usage: input tokens written to the cache. */
	public static final String CACHE_CREATION_INPUT_TOKENS = "cache_creation_input_tokens";

	/** Field of a usage: details of the {@link #OUTPUT_TOKENS}. */
	public static final String OUTPUT_TOKENS_DETAILS = "output_tokens_details";

	/** Field of the {@link #OUTPUT_TOKENS_DETAILS}: the share of thinking. */
	public static final String THINKING_TOKENS = "thinking_tokens";

	// ---- Control requests ----

	/** Field of a {@link #TYPE_CONTROL_REQUEST}: its ID, repeated in the response. */
	public static final String REQUEST_ID = "request_id";

	/** Field of a {@link #TYPE_CONTROL_REQUEST}: the request, with a {@link #SUBTYPE}. */
	public static final String REQUEST = "request";

	/** Control request subtype: stop the current run. */
	public static final String SUBTYPE_INTERRUPT = "interrupt";

}
