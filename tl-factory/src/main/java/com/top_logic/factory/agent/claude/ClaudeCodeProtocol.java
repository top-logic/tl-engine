/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.agent.claude;

import static com.top_logic.factory.agent.claude.StreamJson.*;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.Logger;
import com.top_logic.basic.json.JSON;
import com.top_logic.factory.agent.AgentProtocol;
import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.cost.Money;
import com.top_logic.tl.ai.core.memory.CompactionResult;
import com.top_logic.tl.ai.core.message.TokenUsage;

/**
 * The {@link AgentProtocol} of Claude Code with {@code --input-format stream-json
 * --output-format stream-json --verbose --include-partial-messages}.
 *
 * <p>
 * How the lines of Claude Code become {@link AgentEvent}s:
 * </p>
 * <table>
 * <tr><th>Line</th><th>Events</th></tr>
 * <tr><td>{@code system/init}</td><td>{@link AgentEvent.RunStarted} with the model, once per run;
 * the session ID is taken over.</td></tr>
 * <tr><td>{@code stream_event message_start}</td><td>{@link AgentEvent.TurnCompleted} of the
 * previous request, if any, and {@link AgentEvent.TurnStarted}: a turn is one request with the
 * tools it called.</td></tr>
 * <tr><td>{@code stream_event content_block_delta}</td><td>{@link AgentEvent.TextDelta} for a
 * {@code text_delta}, {@link AgentEvent.ThinkingDelta} for a non-empty
 * {@code thinking_delta}.</td></tr>
 * <tr><td>{@code stream_event message_delta}</td><td>None; its usage is the usage of the
 * turn.</td></tr>
 * <tr><td>{@code assistant}</td><td>{@link AgentEvent.ToolCallStarted} per {@code tool_use} block.
 * Its text blocks were streamed already and are not emitted again - unless the message was not
 * streamed at all, as the messages Claude Code writes itself, e.g. about a failed login.</td></tr>
 * <tr><td>{@code user}</td><td>{@link AgentEvent.ToolCallCompleted}, or
 * {@link AgentEvent.ToolCallFailed} for {@code is_error}, per {@code tool_result} block.</td></tr>
 * <tr><td>{@code system/api_retry}</td><td>{@link AgentEvent.RetryScheduled} with the HTTP status
 * as kind, e.g. {@code API_401}.</td></tr>
 * <tr><td>{@code system/compact_boundary}</td><td>{@link AgentEvent.CompactionStarted} and
 * {@link AgentEvent.CompactionCompleted}.</td></tr>
 * <tr><td>{@code result}</td><td>{@link AgentEvent.TurnCompleted} of the last request,
 * {@link AgentEvent.ProviderError} for a failed run, and {@link AgentEvent.RunCompleted} with the
 * usage of the run and its cost: {@code COMPLETE}, {@code MAX_TURNS}, {@code BUDGET_EXHAUSTED},
 * {@code ABORTED} after a stop of the user, {@code ERROR} otherwise.</td></tr>
 * </table>
 *
 * <p>
 * The lines of a subagent (with a {@code parent_tool_use_id}) are flattened into the run: its tool
 * calls are reported like those of the main agent, its text and its stream events are not - the
 * subagent's answer reaches the main agent as the result of its tool call. Other lines - hooks,
 * status, control responses - are ignored.
 * </p>
 *
 * <p>
 * The cost of a run is the sum of {@code model_usage_since_last_result}; without it, the growth of
 * {@code total_cost_usd}, which counts the session over all its processes.
 * </p>
 */
public class ClaudeCodeProtocol implements AgentProtocol {

	/** Kind of the {@link AgentEvent.ProviderError} of a failed run without HTTP status. */
	public static final String ERROR_KIND_RUN_FAILED = "RUN_FAILED";

	/** Prefix of the kind of the {@link AgentEvent.ProviderError} of a failed API request. */
	public static final String ERROR_KIND_API_PREFIX = "API_";

	private static final String INTERRUPT_ID_PREFIX = "tl-interrupt-";

	private static final String NO_MODEL = "";

	private static final String LINE_END = "\n";

	private static final String TOOL_ABANDONED = "The run ended before the tool returned.";

	private final Clock _clock;

	private String _sessionId;

	private int _interrupts;

	private double _lastTotalCost;

	// ---- State of the current run ----

	private boolean _running;

	private String _runId;

	private AgentEventListener _sink;

	private boolean _started;

	private boolean _cancelRequested;

	private int _turn;

	private boolean _turnOpen;

	private TokenUsage _turnUsage = TokenUsage.EMPTY;

	private TokenUsage _runUsage = TokenUsage.EMPTY;

	private final Set<String> _streamedMessages = new HashSet<>();

	private final Map<String, ToolCall> _openTools = new LinkedHashMap<>();

	private final StringBuilder _lastText = new StringBuilder();

	private String _answer;

	/**
	 * A tool call that has not returned yet.
	 */
	private record ToolCall(String name, Instant started) {
		// Plain data.
	}

	/**
	 * Creates a {@link ClaudeCodeProtocol}.
	 *
	 * @param clock
	 *        The time source of the events.
	 */
	public ClaudeCodeProtocol(Clock clock) {
		_clock = clock;
	}

	@Override
	public String userMessage(String text) {
		Map<String, Object> block = new LinkedHashMap<>();
		block.put(TYPE, BLOCK_TEXT);
		block.put(TEXT, text);
		Map<String, Object> message = new LinkedHashMap<>();
		message.put(ROLE, ROLE_USER);
		message.put(CONTENT, List.of(block));
		Map<String, Object> line = new LinkedHashMap<>();
		line.put(TYPE, TYPE_USER);
		line.put(MESSAGE, message);
		return JSON.toString(line) + LINE_END;
	}

	@Override
	public String interrupt() {
		Map<String, Object> request = new LinkedHashMap<>();
		request.put(SUBTYPE, SUBTYPE_INTERRUPT);
		Map<String, Object> line = new LinkedHashMap<>();
		line.put(TYPE, TYPE_CONTROL_REQUEST);
		line.put(REQUEST_ID, INTERRUPT_ID_PREFIX + (++_interrupts));
		line.put(REQUEST, request);
		return JSON.toString(line) + LINE_END;
	}

	@Override
	public void startRun(String runId, AgentEventListener sink) {
		_running = true;
		_runId = runId;
		_sink = sink;
		_started = false;
		_cancelRequested = false;
		_turn = 0;
		_turnOpen = false;
		_turnUsage = TokenUsage.EMPTY;
		_runUsage = TokenUsage.EMPTY;
		_streamedMessages.clear();
		_openTools.clear();
		_lastText.setLength(0);
		_answer = null;
	}

	@Override
	public void cancelRequested() {
		_cancelRequested = true;
	}

	@Override
	public String sessionId() {
		return _sessionId;
	}

	@Override
	public String answer() {
		return _answer;
	}

	@Override
	public boolean onLine(String line) {
		if (line.isBlank()) {
			return false;
		}
		Map<?, ?> json;
		try {
			Object parsed = JSON.fromString(line);
			if (!(parsed instanceof Map<?, ?> map)) {
				Logger.warn("Claude Code wrote a line that is no JSON object: " + abbreviate(line),
					ClaudeCodeProtocol.class);
				return false;
			}
			json = map;
		} catch (JSON.ParseException ex) {
			Logger.warn("Claude Code wrote a line that is no JSON: " + abbreviate(line), ClaudeCodeProtocol.class);
			return false;
		}
		String sessionId = string(json, SESSION_ID);
		if (sessionId != null) {
			_sessionId = sessionId;
		}
		if (!_running) {
			return false;
		}
		String type = string(json, TYPE);
		if (type == null) {
			return false;
		}
		boolean main = json.get(PARENT_TOOL_USE_ID) == null;
		switch (type) {
			case TYPE_SYSTEM:
				onSystem(json);
				return false;
			case TYPE_STREAM_EVENT:
				if (main) {
					onStreamEvent(map(json, EVENT));
				}
				return false;
			case TYPE_ASSISTANT:
				onAssistant(map(json, MESSAGE), main);
				return false;
			case TYPE_USER:
				onUser(map(json, MESSAGE));
				return false;
			case TYPE_RESULT:
				onResult(json);
				return true;
			default:
				return false;
		}
	}

	private void onSystem(Map<?, ?> json) {
		String subtype = string(json, SUBTYPE);
		if (SUBTYPE_INIT.equals(subtype)) {
			ensureStarted(string(json, MODEL));
		} else if (SUBTYPE_API_RETRY.equals(subtype)) {
			ensureStarted(null);
			Object status = json.get(ERROR_STATUS);
			String error = string(json, ERROR);
			String kind = status instanceof Number number ? ERROR_KIND_API_PREFIX + number.intValue()
				: error != null ? error.toUpperCase(Locale.ROOT) : ERROR_KIND_RUN_FAILED;
			Object delay = json.get(RETRY_DELAY_MS);
			emit(new AgentEvent.RetryScheduled(_runId, _turn, now(), kind, integer(json, ATTEMPT),
				delay instanceof Number number ? number.longValue() : 0));
		} else if (SUBTYPE_COMPACT_BOUNDARY.equals(subtype)) {
			ensureStarted(null);
			Map<?, ?> metadata = map(json, COMPACT_METADATA);
			int before = integer(metadata, PRE_TOKENS);
			String trigger = string(metadata, TRIGGER);
			if (trigger == null) {
				trigger = "";
			}
			emit(new AgentEvent.CompactionStarted(_runId, _turn, now(), before, trigger));
			emit(new AgentEvent.CompactionCompleted(_runId, _turn, now(),
				new CompactionResult(List.of(), 0, before, 0, trigger)));
		}
	}

	private void onStreamEvent(Map<?, ?> event) {
		String type = string(event, TYPE);
		if (type == null) {
			return;
		}
		switch (type) {
			case EVENT_MESSAGE_START: {
				Map<?, ?> message = map(event, MESSAGE);
				ensureStarted(string(message, MODEL));
				closeTurn();
				String id = string(message, ID);
				if (id != null) {
					_streamedMessages.add(id);
				}
				openTurn();
				_turnUsage = usage(map(message, USAGE), TokenUsage.EMPTY);
				break;
			}
			case EVENT_CONTENT_BLOCK_DELTA: {
				Map<?, ?> delta = map(event, DELTA);
				String deltaType = string(delta, TYPE);
				if (DELTA_TEXT.equals(deltaType)) {
					text(string(delta, TEXT));
				} else if (DELTA_THINKING.equals(deltaType)) {
					String thinking = string(delta, THINKING);
					if (thinking != null && !thinking.isEmpty()) {
						ensureTurn();
						emit(new AgentEvent.ThinkingDelta(_runId, _turn, now(), thinking));
					}
				}
				break;
			}
			case EVENT_MESSAGE_DELTA:
				_turnUsage = usage(map(event, USAGE), _turnUsage);
				break;
			default:
				break;
		}
	}

	private void onAssistant(Map<?, ?> message, boolean main) {
		boolean streamed = _streamedMessages.contains(string(message, ID));
		for (Map<?, ?> block : blocks(message)) {
			String type = string(block, TYPE);
			if (BLOCK_TOOL_USE.equals(type)) {
				String id = string(block, ID);
				String name = string(block, NAME);
				ensureTurn();
				_openTools.put(id, new ToolCall(name, now()));
				Object input = block.get(INPUT);
				emit(new AgentEvent.ToolCallStarted(_runId, _turn, now(), id, name,
					JSON.toString(input == null ? Map.of() : input), null));
			} else if (BLOCK_TEXT.equals(type) && main && !streamed) {
				// A message Claude Code wrote itself, without a request to stream.
				text(string(block, TEXT));
			}
		}
	}

	private void onUser(Map<?, ?> message) {
		for (Map<?, ?> block : blocks(message)) {
			if (!BLOCK_TOOL_RESULT.equals(string(block, TYPE))) {
				continue;
			}
			String id = string(block, TOOL_USE_ID);
			ToolCall call = _openTools.remove(id);
			if (call == null) {
				continue;
			}
			Duration duration = Duration.between(call.started(), now());
			String content = toolResult(block.get(CONTENT));
			if (Boolean.TRUE.equals(block.get(IS_ERROR))) {
				emit(new AgentEvent.ToolCallFailed(_runId, _turn, now(), id, call.name(), content, duration));
			} else {
				emit(new AgentEvent.ToolCallCompleted(_runId, _turn, now(), id, call.name(), content, duration));
			}
		}
	}

	private void onResult(Map<?, ?> json) {
		ensureStarted(null);
		abandonTools();
		closeTurn();
		String subtype = string(json, SUBTYPE);
		boolean error = Boolean.TRUE.equals(json.get(IS_ERROR));
		StopReason reason;
		if (SUBTYPE_SUCCESS.equals(subtype) && !error) {
			reason = StopReason.COMPLETE;
		} else if (SUBTYPE_ERROR_MAX_TURNS.equals(subtype)) {
			reason = StopReason.MAX_TURNS;
		} else if (SUBTYPE_ERROR_MAX_BUDGET.equals(subtype)) {
			reason = StopReason.BUDGET_EXHAUSTED;
		} else if (_cancelRequested) {
			reason = StopReason.ABORTED;
		} else {
			reason = StopReason.ERROR;
			emit(new AgentEvent.ProviderError(_runId, _turn, now(), errorKind(json), errorMessage(json, subtype)));
		}
		Object result = json.get(RESULT);
		_answer = reason == StopReason.COMPLETE && result instanceof String text ? text : _lastText.toString();
		// An interrupted run reports no usage; what its requests used so far is the better figure.
		TokenUsage usage = usage(map(json, USAGE), _runUsage);
		complete(reason, usage.totalTokens() == 0 ? _runUsage : usage, cost(json));
	}

	@Override
	public void endRun(StopReason reason, String errorKind, String errorMessage) {
		if (!_running) {
			return;
		}
		ensureStarted(null);
		abandonTools();
		closeTurn();
		if (errorKind != null) {
			emit(new AgentEvent.ProviderError(_runId, _turn, now(), errorKind, errorMessage));
		}
		_answer = _lastText.toString();
		complete(reason, _runUsage, Money.zero(Money.USD));
	}

	private void complete(StopReason reason, TokenUsage usage, Money cost) {
		emit(new AgentEvent.RunCompleted(_runId, _turn, now(), reason, usage, cost));
		_running = false;
		_sink = null;
	}

	private void ensureStarted(String model) {
		if (!_started) {
			_started = true;
			emit(new AgentEvent.RunStarted(_runId, 0, now(), model == null ? NO_MODEL : model));
		}
	}

	private void ensureTurn() {
		ensureStarted(null);
		if (!_turnOpen) {
			openTurn();
		}
	}

	private void openTurn() {
		_turn++;
		_turnOpen = true;
		_turnUsage = TokenUsage.EMPTY;
		_lastText.setLength(0);
		emit(new AgentEvent.TurnStarted(_runId, _turn, now()));
	}

	private void closeTurn() {
		if (_turnOpen) {
			_turnOpen = false;
			_runUsage = _runUsage.add(_turnUsage);
			emit(new AgentEvent.TurnCompleted(_runId, _turn, now(), _turnUsage, List.of()));
		}
	}

	private void text(String text) {
		if (text == null || text.isEmpty()) {
			return;
		}
		ensureTurn();
		_lastText.append(text);
		emit(new AgentEvent.TextDelta(_runId, _turn, now(), text));
	}

	/**
	 * Ends the tool calls that will not return, so that every call has its end.
	 */
	private void abandonTools() {
		for (Map.Entry<String, ToolCall> entry : _openTools.entrySet()) {
			ToolCall call = entry.getValue();
			emit(new AgentEvent.ToolCallFailed(_runId, _turn, now(), entry.getKey(), call.name(), TOOL_ABANDONED,
				Duration.between(call.started(), now())));
		}
		_openTools.clear();
	}

	private Money cost(Map<?, ?> json) {
		Object total = json.get(TOTAL_COST_USD);
		double totalCost = total instanceof Number number ? number.doubleValue() : _lastTotalCost;
		double runCost;
		Object perModel = json.get(MODEL_USAGE_SINCE_LAST_RESULT);
		if (perModel instanceof Map<?, ?> models) {
			runCost = 0;
			for (Object entry : models.values()) {
				if (entry instanceof Map<?, ?> model && model.get(COST_USD) instanceof Number cost) {
					runCost += cost.doubleValue();
				}
			}
		} else {
			runCost = Math.max(0, totalCost - _lastTotalCost);
		}
		_lastTotalCost = totalCost;
		return new Money(runCost, Money.USD);
	}

	private static String errorKind(Map<?, ?> json) {
		Object status = json.get(API_ERROR_STATUS);
		if (status instanceof Number number) {
			return ERROR_KIND_API_PREFIX + number.intValue();
		}
		String subtype = string(json, SUBTYPE);
		return subtype == null ? ERROR_KIND_RUN_FAILED : subtype.toUpperCase(Locale.ROOT);
	}

	private static String errorMessage(Map<?, ?> json, String subtype) {
		if (json.get(RESULT) instanceof String result && !result.isBlank()) {
			return result;
		}
		if (json.get(ERRORS) instanceof List<?> errors && !errors.isEmpty()) {
			StringBuilder result = new StringBuilder();
			for (Object error : errors) {
				if (result.length() > 0) {
					result.append(LINE_END);
				}
				result.append(error);
			}
			return result.toString();
		}
		return "The agent run failed (" + subtype + ").";
	}

	private static String toolResult(Object content) {
		if (content instanceof String text) {
			return text;
		}
		if (content instanceof List<?> blocks) {
			StringBuilder result = new StringBuilder();
			for (Object block : blocks) {
				if (!(block instanceof Map<?, ?> map)) {
					continue;
				}
				if (result.length() > 0) {
					result.append(LINE_END);
				}
				String type = string(map, TYPE);
				if (BLOCK_TEXT.equals(type)) {
					result.append(string(map, TEXT));
				} else {
					result.append('[').append(type).append(']');
				}
			}
			return result.toString();
		}
		return content == null ? "" : JSON.toString(content);
	}

	private static TokenUsage usage(Map<?, ?> usage, TokenUsage fallback) {
		if (usage.isEmpty()) {
			return fallback;
		}
		return new TokenUsage(
			integer(usage, INPUT_TOKENS),
			integer(usage, OUTPUT_TOKENS),
			integer(map(usage, OUTPUT_TOKENS_DETAILS), THINKING_TOKENS),
			integer(usage, CACHE_READ_INPUT_TOKENS),
			integer(usage, CACHE_CREATION_INPUT_TOKENS));
	}

	private Instant now() {
		return _clock.instant();
	}

	private void emit(AgentEvent event) {
		_sink.onEvent(event);
	}

	private static List<Map<?, ?>> blocks(Map<?, ?> message) {
		if (!(message.get(CONTENT) instanceof List<?> content)) {
			return List.of();
		}
		return content.stream().filter(Map.class::isInstance).<Map<?, ?>> map(block -> (Map<?, ?>) block).toList();
	}

	private static Map<?, ?> map(Map<?, ?> json, String field) {
		return json.get(field) instanceof Map<?, ?> map ? map : Map.of();
	}

	private static String string(Map<?, ?> json, String field) {
		return json.get(field) instanceof String text ? text : null;
	}

	private static int integer(Map<?, ?> json, String field) {
		return json.get(field) instanceof Number number ? number.intValue() : 0;
	}

	private static String abbreviate(String line) {
		int max = 200;
		return line.length() <= max ? line : line.substring(0, max) + "...";
	}

}
