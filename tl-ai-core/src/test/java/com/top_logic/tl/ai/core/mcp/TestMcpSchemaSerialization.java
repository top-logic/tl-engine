/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.mcp;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import io.modelcontextprotocol.spec.McpSchema;

/**
 * Tests for {@link McpServerRegistry#serializeInputSchema(McpSchema.JsonSchema)}.
 *
 * <p>
 * Pins the JSON output of the msgbuf-based serializer against the literals the previous
 * hand-built {@code StringBuilder} implementation produced, for a representative schema with
 * nested objects, arrays, numbers, booleans and special characters.
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
		McpSchema.JsonSchema schema = new McpSchema.JsonSchema(null, null, null, null, null, null);
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

		McpSchema.JsonSchema schema =
			new McpSchema.JsonSchema("object", properties, List.of("text", "tags"),
				Boolean.FALSE, null, null);

		String expected = "{\"type\":\"object\",\"properties\":{"
			+ "\"text\":{\"type\":\"string\",\"description\":\"line1\\nwith \\\"quote\\\" and \\\\ backslash\"},"
			+ "\"score\":{\"type\":\"number\",\"minimum\":0,\"maximum\":1.5,\"enabled\":true},"
			+ "\"tags\":{\"type\":\"array\",\"items\":{\"type\":\"string\"}}"
			+ "},\"required\":[\"text\",\"tags\"],\"additionalProperties\":false}";

		assertEquals(expected, McpServerRegistry.serializeInputSchema(schema));
	}

	/** A non-empty required list without additionalProperties=false omits the trailing member. */
	public void testRequiredWithoutAdditionalProperties() {
		Map<String, Object> properties = new LinkedHashMap<>();
		Map<String, Object> nameProp = new LinkedHashMap<>();
		nameProp.put("type", "string");
		properties.put("name", nameProp);

		McpSchema.JsonSchema schema =
			new McpSchema.JsonSchema("object", properties, List.of("name"), Boolean.TRUE, null, null);

		assertEquals("{\"type\":\"object\",\"properties\":{\"name\":{\"type\":\"string\"}},"
			+ "\"required\":[\"name\"]}",
			McpServerRegistry.serializeInputSchema(schema));
	}
}
