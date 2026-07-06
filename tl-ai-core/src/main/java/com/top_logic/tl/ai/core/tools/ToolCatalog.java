/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.function.Predicate;

import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.ToolState;
import com.top_logic.tl.ai.tool.api.AnnotatedTools;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolArgumentCoercionException;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.tl.ai.tool.api.ToolSchema;
import com.top_logic.tl.ai.tool.api.json.JsonTree;

/**
 * Central catalog of {@link Tool}s.
 *
 * <p>Holds tools by name, produces {@link ToolSchema} lists for the LLM, and
 * implements {@link ToolExecutor} for direct integration with the agent loop.</p>
 *
 * <p>This class is a pure lookup and execution layer. Security checks (coarse
 * permission gating via {@link com.top_logic.tl.ai.core.agent.SecurityContext})
 * are handled by the agent loop before dispatching to this executor.</p>
 *
 * @see Tool
 * @see AnnotatedTools
 */
public class ToolCatalog implements ToolRegistry {

	private final Map<String, Tool> _tools = new LinkedHashMap<>();

	private boolean _frozen;

	/**
	 * Register a single tool.
	 *
	 * <p>Validates the tool's JSON Schema at registration time so that malformed
	 * schemas fail fast rather than producing opaque provider errors at runtime.</p>
	 *
	 * @throws IllegalArgumentException if a tool with the same name is already registered
	 * @throws IllegalArgumentException if the tool's input schema is not valid JSON
	 * @throws IllegalStateException    if this registry has been {@linkplain #freeze() frozen}
	 */
	public void register(Tool tool) {
		checkNotFrozen();
		String name = tool.name();
		if (_tools.containsKey(name)) {
			Tool existing = _tools.get(name);
			throw new IllegalArgumentException(
				"Tool name collision: '" + name + "' is already registered"
				+ " (existing: " + existing.getClass().getSimpleName()
				+ ", new: " + tool.getClass().getSimpleName() + ")."
				+ " Use distinct tool names or check agent tool assignments.");
		}
		validateSchema(tool);
		_tools.put(name, tool);
	}

	private static void validateSchema(Tool tool) {
		String json = tool.schema().inputSchemaJson();
		try {
			parseJson(json);
		} catch (Exception e) {
			throw new IllegalArgumentException(
				"Tool '" + tool.name() + "' has invalid input schema JSON: " + e.getMessage(), e);
		}
	}

	/**
	 * Scan the given object for {@link ToolMethod}-annotated methods and register each
	 * as a {@link Tool}.
	 *
	 * @throws IllegalArgumentException if any tool name collides with an existing registration
	 * @throws IllegalStateException    if this registry has been {@linkplain #freeze() frozen}
	 */
	public void register(Object annotatedProvider) {
		checkNotFrozen();
		for (Tool tool : AnnotatedTools.from(annotatedProvider)) {
			register(tool);
		}
	}

	/**
	 * Freeze this catalog, preventing further registrations.
	 *
	 * <p>After freezing, all read operations are safe for concurrent access from
	 * multiple threads. Any attempt to {@link #register} after freezing throws
	 * {@link IllegalStateException}.</p>
	 *
	 * @return this catalog (for chaining)
	 */
	public ToolCatalog freeze() {
		_frozen = true;
		return this;
	}

	/**
	 * Whether this catalog has been {@linkplain #freeze() frozen}.
	 */
	public boolean isFrozen() {
		return _frozen;
	}

	private void checkNotFrozen() {
		if (_frozen) {
			throw new IllegalStateException("Catalog is frozen — no further registrations allowed.");
		}
	}

	/** Looks up a tool by name, or {@code null} if not registered. */
	@Override
	public Tool getTool(String name) {
		return _tools.get(name);
	}

	/** All registered tools. */
	public List<Tool> tools() {
		return List.copyOf(_tools.values());
	}

	/** Registered tools matching the given predicate. */
	public List<Tool> tools(Predicate<Tool> filter) {
		return _tools.values().stream().filter(filter).toList();
	}

	/** Schemas of all registered tools. */
	public List<ToolSchema> toolSchemas() {
		return _tools.values().stream().map(Tool::schema).toList();
	}

	/** Schemas of registered tools matching the given predicate. */
	@Override
	public List<ToolSchema> toolSchemas(Predicate<Tool> filter) {
		return _tools.values().stream().filter(filter).map(Tool::schema).toList();
	}

	@Override
	public ToolResultMessage execute(ToolCall toolCall, ToolContext context) {
		Instant start = Instant.now();

		Tool tool = _tools.get(toolCall.name());
		if (tool == null) {
			return failedResult(toolCall, start,
				"Unknown tool: '" + toolCall.name() + "'. Available: " + _tools.keySet());
		}

		return executeTool(tool, toolCall, context);
	}

	/**
	 * Execute a resolved tool with JSON argument parsing and error wrapping.
	 *
	 * <p>Package-private: shared between {@link ToolCatalog} and {@link DynamicToolCatalog}.</p>
	 */
	static ToolResultMessage executeTool(Tool tool, ToolCall toolCall, ToolContext context) {
		Instant start = Instant.now();

		Map<String, Object> args;
		try {
			args = parseJson(toolCall.arguments());
		} catch (Exception e) {
			// Never reached the tool body — synthetic, must not trigger a rollback.
			return failedResult(toolCall, start,
				"Invalid JSON arguments for tool '" + toolCall.name() + "': " + sanitizeError(e), true);
		}

		try {
			ToolResult toolResult = ToolExecutions.execute(tool, args, context);
			Instant end = Instant.now();
			return new ToolResultMessage(
				UUID.randomUUID().toString(), Instant.now(),
				toolCall.id(), toolCall.name(),
				new ToolState.Completed(toolResult.text(), start, end, false,
					toolResult.summary(), toolResult.truncationHint()));
		} catch (ToolArgumentCoercionException e) {
			// Thrown while coercing arguments, strictly before the tool method is invoked —
			// synthetic, must not trigger a rollback.
			return failedResult(toolCall, start, sanitizeError(e), true);
		} catch (IllegalArgumentException e) {
			// May have been thrown by the tool's own body after producing side effects —
			// a genuine failure, must still trigger a rollback.
			return failedResult(toolCall, start, sanitizeError(e));
		} catch (CancellationException e) {
			return failedResult(toolCall, start, "Tool execution cancelled.");
		} catch (Exception e) {
			return failedResult(toolCall, start,
				"Tool '" + toolCall.name() + "' failed: " + sanitizeError(e));
		}
	}

	static String sanitizeError(Exception e) {
		String message = e.getMessage();
		if (message == null || message.isEmpty()) {
			return e.getClass().getSimpleName();
		}
		return message;
	}

	static ToolResultMessage failedResult(ToolCall toolCall, Instant start, String error) {
		return failedResult(toolCall, start, error, false);
	}

	static ToolResultMessage failedResult(ToolCall toolCall, Instant start, String error, boolean synthetic) {
		Instant end = Instant.now();
		return new ToolResultMessage(
			UUID.randomUUID().toString(), Instant.now(),
			toolCall.id(), toolCall.name(),
			new ToolState.Failed(error, start, end, synthetic));
	}

	/**
	 * Parse a JSON object string into a {@code Map<String, Object>}.
	 */
	public static Map<String, Object> parseJson(String json) throws IOException {
		if (json == null || json.isBlank() || "{}".equals(json.trim())) {
			return Map.of();
		}
		return JsonTree.parseObject(json);
	}
}
