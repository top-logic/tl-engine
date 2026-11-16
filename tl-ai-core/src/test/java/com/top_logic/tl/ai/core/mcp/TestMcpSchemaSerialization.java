/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.mcp;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import io.modelcontextprotocol.spec.McpSchema;

/**
 * Tests for {@link McpServerRegistry#serializeInputSchema(Map)}.
 *
 * <p>
 * Pins the JSON output for a representative schema with nested objects, arrays, numbers, booleans
 * and special characters, and that every keyword of the {@link McpSchema.Tool#inputSchema() input
 * schema} a server sent is kept.
 * </p>
 */
public class TestMcpSchemaSerialization extends TestCase {

	/** A {@code null} schema serializes to the canonical empty object schema. */
	public void testNullSchema() {
		assertEquals("{\"type\":\"object\",\"properties\":{}}",
			McpServerRegistry.serializeInputSchema(null));
	}

	/** A missing type defaults to {@code "object"}; absent properties serialize as {@code {}}. */
	public void testDefaultsAndEmptyProperties() {
		Map<String, Object> schema = Map.of();
		assertEquals("{\"type\":\"object\",\"properties\":{}}",
			McpServerRegistry.serializeInputSchema(schema));
	}

	/** Nested objects, arrays, numbers, booleans, required and additionalProperties. */
	public void testRepresentativeSchema() {
		Map<String, Object> textProp = new LinkedHashMap<>();
		textProp.put("type", "string");
		textProp.put("description", "line1\nwith \"quote\" and \\ backslash");

		Map<String, Object> scoreProp = new LinkedHashMap<>();
		scoreProp.put("type", "number");
		scoreProp.put("minimum", Integer.valueOf(0));
		scoreProp.put("maximum", Double.valueOf(1.5));
		scoreProp.put("enabled", Boolean.TRUE);

		Map<String, Object> items = new LinkedHashMap<>();
		items.put("type", "string");
		Map<String, Object> tagsProp = new LinkedHashMap<>();
		tagsProp.put("type", "array");
		tagsProp.put("items", items);

		Map<String, Object> properties = new LinkedHashMap<>();
		properties.put("text", textProp);
		properties.put("score", scoreProp);
		properties.put("tags", tagsProp);

		Map<String, Object> schema = new LinkedHashMap<>();
		schema.put("type", "object");
		schema.put("properties", properties);
		schema.put("required", List.of("text", "tags"));
		schema.put("additionalProperties", Boolean.FALSE);

		String expected = "{\"type\":\"object\",\"properties\":{"
			+ "\"text\":{\"type\":\"string\",\"description\":\"line1\\nwith \\\"quote\\\" and \\\\ backslash\"},"
			+ "\"score\":{\"type\":\"number\",\"minimum\":0,\"maximum\":1.5,\"enabled\":true},"
			+ "\"tags\":{\"type\":\"array\",\"items\":{\"type\":\"string\"}}"
			+ "},\"required\":[\"text\",\"tags\"],\"additionalProperties\":false}";

		assertEquals(expected, McpServerRegistry.serializeInputSchema(schema));
	}

	/**
	 * Keywords beyond type and properties are kept as the server sent them, in particular the
	 * definitions a {@code $ref} in a property points to.
	 */
	public void testFurtherKeywordsKept() {
		Map<String, Object> properties = new LinkedHashMap<>();
		properties.put("name", Map.of("$ref", "#/$defs/name"));

		Map<String, Object> schema = new LinkedHashMap<>();
		schema.put("required", List.of("name"));
		schema.put("properties", properties);
		schema.put("$defs", Map.of("name", Map.of("type", "string")));
		schema.put("additionalProperties", Boolean.TRUE);

		assertEquals("{\"type\":\"object\",\"properties\":{\"name\":{\"$ref\":\"#/$defs/name\"}},"
			+ "\"required\":[\"name\"],\"$defs\":{\"name\":{\"type\":\"string\"}},"
			+ "\"additionalProperties\":true}",
			McpServerRegistry.serializeInputSchema(schema));
	}
}
