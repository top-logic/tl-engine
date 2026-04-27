/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.mcp;

import java.util.List;

import io.modelcontextprotocol.spec.McpSchema;

import junit.framework.TestCase;

/**
 * Tests for {@link McpTool}.
 */
public class TestMcpTool extends TestCase {

	/** */
	public void testNameFormat() {
		McpTool tool = createTool("my-server", "searchDocs", "Search documents", schemaJson());
		assertEquals("mcp__my-server__searchDocs", tool.name());
	}

	/** */
	public void testDescription() {
		McpTool tool = createTool("srv", "tool1", "Does something useful", schemaJson());
		assertEquals("Does something useful", tool.description());
	}

	/** */
	public void testSchema() {
		String inputSchema = "{\"type\":\"object\",\"properties\":{\"query\":{\"type\":\"string\"}}}";
		McpTool tool = createTool("srv", "search", "Search", inputSchema);

		assertEquals("mcp__srv__search", tool.schema().name());
		assertEquals("Search", tool.schema().description());
		assertEquals(inputSchema, tool.schema().inputSchemaJson());
	}

	/** */
	public void testFormatTextResult() {
		McpSchema.CallToolResult result = new McpSchema.CallToolResult(
			List.of(new McpSchema.TextContent("Hello world")), false);

		String formatted = McpTool.formatResult(result);
		assertEquals("Hello world", formatted);
	}

	/** */
	public void testFormatMultipleTextParts() {
		McpSchema.CallToolResult result = new McpSchema.CallToolResult(
			List.of(
				new McpSchema.TextContent("Part 1"),
				new McpSchema.TextContent("Part 2")),
			false);

		String formatted = McpTool.formatResult(result);
		assertEquals("Part 1\nPart 2", formatted);
	}

	/** */
	public void testFormatErrorResult() {
		McpSchema.CallToolResult result = new McpSchema.CallToolResult(
			List.of(new McpSchema.TextContent("Something went wrong")), true);

		String formatted = McpTool.formatResult(result);
		assertEquals("Error: Something went wrong", formatted);
	}

	/** */
	public void testFormatEmptyResult() {
		McpSchema.CallToolResult result = new McpSchema.CallToolResult(List.of(), false);

		String formatted = McpTool.formatResult(result);
		assertEquals("", formatted);
	}

	/** */
	public void testFormatImageContent() {
		McpSchema.CallToolResult result = new McpSchema.CallToolResult(
			List.of(new McpSchema.ImageContent(null, "base64data", "image/png")), false);

		String formatted = McpTool.formatResult(result);
		assertEquals("[image: image/png]", formatted);
	}

	/** */
	public void testModeIsRead() {
		McpTool tool = createTool("srv", "tool1", "desc", schemaJson());
		assertEquals(com.top_logic.tl.ai.tool.api.ToolMode.READ, tool.mode());
	}

	/** */
	public void testIsConcurrencySafe() {
		McpTool tool = createTool("srv", "tool1", "desc", schemaJson());
		assertTrue(tool.isConcurrencySafe());
	}

	private static McpTool createTool(String serverName, String toolName,
			String description, String inputSchemaJson) {
		return new McpTool(serverName, toolName, description, inputSchemaJson, null);
	}

	private static String schemaJson() {
		return "{\"type\":\"object\",\"properties\":{}}";
	}
}
