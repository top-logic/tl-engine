/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.mcp;

import java.util.List;
import java.util.Map;

import com.top_logic.tl.ai.tool.api.ToolSchema;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolResult;

import io.modelcontextprotocol.spec.McpSchema;

/**
 * Wraps a single MCP tool as a native {@link Tool}.
 *
 * <p>
 * From the agent's perspective, an {@link McpTool} is indistinguishable from a locally defined
 * tool. The {@link #execute} method delegates to the {@link McpServerRegistry} which routes the
 * call to the correct MCP server via JSON-RPC.
 * </p>
 *
 * @see McpServerRegistry
 */
public class McpTool implements Tool {

	private final String _serverName;

	private final String _mcpToolName;

	private final String _description;

	private final String _inputSchemaJson;

	private final McpServerRegistry _registry;

	/**
	 * Creates a new {@link McpTool}.
	 *
	 * @param serverName
	 *        The MCP server name as configured in {@link McpServerRegistry}.
	 * @param mcpToolName
	 *        The original tool name from the MCP server.
	 * @param description
	 *        Human-readable description from the MCP tool definition.
	 * @param inputSchemaJson
	 *        JSON Schema string for the tool's input parameters.
	 * @param registry
	 *        The registry to delegate {@link #execute} calls to, or {@code null} for testing.
	 */
	public McpTool(String serverName, String mcpToolName, String description,
			String inputSchemaJson, McpServerRegistry registry) {
		_serverName = serverName;
		_mcpToolName = mcpToolName;
		_description = description;
		_inputSchemaJson = inputSchemaJson;
		_registry = registry;
	}

	@Override
	public String name() {
		return "mcp__" + sanitize(_serverName) + "__" + sanitize(_mcpToolName);
	}

	/**
	 * Sanitizes a name to match LLM provider tool name constraints ({@code [a-zA-Z0-9_-]}).
	 */
	private static String sanitize(String name) {
		return name.replaceAll("[^a-zA-Z0-9_-]", "_");
	}

	@Override
	public String description() {
		return _description;
	}

	@Override
	public ToolSchema schema() {
		return new ToolSchema(name(), _description, _inputSchemaJson);
	}

	@Override
	public ToolResult execute(Map<String, Object> args, ToolExecutionContext context) {
		McpSchema.CallToolResult result = _registry.callTool(_serverName, _mcpToolName, args);
		return ToolResult.of(formatResult(result));
	}

	@Override
	public ToolMode mode() {
		return ToolMode.READ;
	}

	@Override
	public boolean isConcurrencySafe() {
		return true;
	}

	/**
	 * Formats a {@link io.modelcontextprotocol.spec.McpSchema.CallToolResult} into a string for the
	 * agent.
	 *
	 * <p>
	 * Concatenates text content items, notes non-text items as placeholders. Error results are
	 * prefixed with {@code "Error: "}.
	 * </p>
	 */
	static String formatResult(McpSchema.CallToolResult result) {
		List<McpSchema.Content> content = result.content();
		if (content == null || content.isEmpty()) {
			return Boolean.TRUE.equals(result.isError()) ? "Error: unknown" : "";
		}

		StringBuilder sb = new StringBuilder();
		for (McpSchema.Content item : content) {
			if (sb.length() > 0) {
				sb.append("\n");
			}
			if (item instanceof McpSchema.TextContent text) {
				sb.append(text.text());
			} else if (item instanceof McpSchema.ImageContent image) {
				sb.append("[image: ").append(image.mimeType()).append("]");
			} else if (item instanceof McpSchema.EmbeddedResource resource) {
				McpSchema.ResourceContents rc = resource.resource();
				if (rc instanceof McpSchema.TextResourceContents trc) {
					sb.append(trc.text());
				} else {
					sb.append("[resource: ").append(rc.uri()).append("]");
				}
			} else {
				sb.append("[unsupported content type]");
			}
		}

		if (Boolean.TRUE.equals(result.isError())) {
			return "Error: " + sb;
		}
		return sb.toString();
	}
}
