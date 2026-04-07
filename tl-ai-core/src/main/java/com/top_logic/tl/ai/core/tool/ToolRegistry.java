/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tool;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

import de.haumacher.msgbuf.io.StringR;
import de.haumacher.msgbuf.json.JsonReader;
import de.haumacher.msgbuf.json.JsonToken;

import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.ToolExecutor;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.ToolState;
import com.top_logic.tl.ai.core.provider.ToolSchema;

/**
 * Central registry of {@link Tool}s.
 *
 * <p>Holds tools by name, produces {@link ToolSchema} lists for the LLM, and
 * implements {@link ToolExecutor} for direct integration with the agent loop.</p>
 *
 * @see Tool
 * @see AnnotatedTools
 */
public class ToolRegistry implements ToolExecutor {

	private final Map<String, Tool> _tools = new LinkedHashMap<>();

	/**
	 * Register a single tool.
	 *
	 * @throws IllegalArgumentException if a tool with the same name is already registered
	 */
	public void register(Tool tool) {
		String name = tool.name();
		if (_tools.containsKey(name)) {
			throw new IllegalArgumentException(
				"Tool already registered: '" + name + "'.");
		}
		_tools.put(name, tool);
	}

	/**
	 * Scan the given object for {@link ToolMethod}-annotated methods and register each
	 * as a {@link Tool}.
	 *
	 * @throws IllegalArgumentException if any tool name collides with an existing registration
	 */
	public void register(Object annotatedProvider) {
		for (Tool tool : AnnotatedTools.from(annotatedProvider)) {
			register(tool);
		}
	}

	/** */
	public Tool getTool(String name) {
		return _tools.get(name);
	}

	/** */
	public List<Tool> tools() {
		return List.copyOf(_tools.values());
	}

	/** */
	public List<Tool> tools(Predicate<Tool> filter) {
		return _tools.values().stream().filter(filter).toList();
	}

	/** */
	public List<ToolSchema> toolSchemas() {
		return _tools.values().stream().map(Tool::schema).toList();
	}

	/** */
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

		// Seam: security check
		// if (context has security) { check context.security().canUseTool(tool) }

		// Seam: approval check
		// if (tool.mode() != ToolMode.READ) { check context.approvalPolicy() }

		try {
			Map<String, Object> args = parseJson(toolCall.arguments());
			String result = tool.execute(args, context);
			Instant end = Instant.now();
			return new ToolResultMessage(
				UUID.randomUUID().toString(), Instant.now(),
				toolCall.id(), toolCall.name(),
				new ToolState.Completed(result, start, end, false));
		} catch (Exception e) {
			String error = e.getMessage() != null ? e.getMessage() : e.getClass().getName();
			return failedResult(toolCall, start, error);
		}
	}

	private static ToolResultMessage failedResult(ToolCall toolCall, Instant start, String error) {
		Instant end = Instant.now();
		return new ToolResultMessage(
			UUID.randomUUID().toString(), Instant.now(),
			toolCall.id(), toolCall.name(),
			new ToolState.Failed(error, start, end));
	}

	/**
	 * Parse a JSON object string into a {@code Map<String, Object>}.
	 */
	static Map<String, Object> parseJson(String json) throws IOException {
		if (json == null || json.isBlank() || "{}".equals(json.trim())) {
			return Map.of();
		}
		try (JsonReader reader = new JsonReader(new StringR(json))) {
			return readObject(reader);
		}
	}

	private static Map<String, Object> readObject(JsonReader reader) throws IOException {
		Map<String, Object> map = new LinkedHashMap<>();
		reader.beginObject();
		while (reader.hasNext()) {
			String name = reader.nextName();
			map.put(name, readValue(reader));
		}
		reader.endObject();
		return map;
	}

	private static List<Object> readArray(JsonReader reader) throws IOException {
		List<Object> list = new ArrayList<>();
		reader.beginArray();
		while (reader.hasNext()) {
			list.add(readValue(reader));
		}
		reader.endArray();
		return list;
	}

	private static Object readValue(JsonReader reader) throws IOException {
		JsonToken token = reader.peek();
		return switch (token) {
			case STRING -> reader.nextString();
			case NUMBER -> {
				double d = reader.nextDouble();
				if (d == Math.floor(d) && !Double.isInfinite(d)) {
					if (d >= Integer.MIN_VALUE && d <= Integer.MAX_VALUE) {
						yield (int) d;
					}
					yield (long) d;
				}
				yield d;
			}
			case BOOLEAN -> reader.nextBoolean();
			case NULL -> {
				reader.nextNull();
				yield null;
			}
			case BEGIN_OBJECT -> readObject(reader);
			case BEGIN_ARRAY -> readArray(reader);
			default -> {
				reader.skipValue();
				yield null;
			}
		};
	}
}
