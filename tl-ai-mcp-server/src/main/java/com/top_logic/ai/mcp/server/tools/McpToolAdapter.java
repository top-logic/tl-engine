/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;
import com.top_logic.basic.Logger;
import com.top_logic.tl.ai.tool.api.Tool;

import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;

/**
 * Adapter that converts {@link Tool}s from the {@link ToolRegistryService} to MCP tool specifications.
 *
 * <p>
 * This adapter bridges the tl-ai-core tool system (used by in-app AI agents) with the MCP
 * protocol, allowing external AI clients to use the same tools via HTTP.
 * </p>
 */
public class McpToolAdapter {

	/**
	 * Prefix used for adapted tool names in MCP.
	 *
	 * <p>
	 * Tools use qualified names like "factory.tool", but MCP tool names should use hyphens. This
	 * prefix helps identify adapted tools.
	 * </p>
	 */
	private static final String TOOL_NAME_PREFIX = "tl-";

	/**
	 * Adapts a single {@link Tool} to an MCP tool specification.
	 *
	 * @param tool
	 *        The tool to adapt.
	 * @param qualifiedName
	 *        The qualified name of the tool from the registry (e.g., "model-browser.listModules").
	 * @return An MCP tool specification that wraps the tool.
	 */
	public static McpServerFeatures.SyncToolSpecification adapt(Tool tool, String qualifiedName) {
		String mcpToolName = TOOL_NAME_PREFIX + qualifiedName.replace('.', '-');

		McpJsonMapper jsonMapper = McpJsonDefaults.getMapper();

		McpSchema.Tool mcpTool = McpSchema.Tool.builder()
			.name(mcpToolName)
			.title(tool.name())
			.description(tool.description())
			.inputSchema(jsonMapper, tool.schema().inputSchemaJson())
			.build();

		return McpServerFeatures.SyncToolSpecification.builder()
			.tool(mcpTool)
			.callHandler((exchange, request) -> handleToolCall(exchange, request, tool))
			.build();
	}

	/**
	 * Creates MCP tool specifications for all tools registered in the {@link ToolRegistryService}.
	 *
	 * <p>
	 * This method should only be called when the {@link ToolRegistryService} is active.
	 * </p>
	 *
	 * @return List of MCP tool specifications for all registered tools.
	 */
	public static List<McpServerFeatures.SyncToolSpecification> adaptAllTools() {
		List<McpServerFeatures.SyncToolSpecification> result = new ArrayList<>();

		if (!ToolRegistryService.Module.INSTANCE.isActive()) {
			Logger.info("ToolRegistryService is not active, skipping tool adaptation",
				McpToolAdapter.class);
			return result;
		}

		ToolRegistryService registry = ToolRegistryService.getInstance();
		for (String qualifiedName : registry.getToolNames()) {
			try {
				Tool tool = registry.getTool(qualifiedName);
				if (tool != null) {
					result.add(adapt(tool, qualifiedName));
					Logger.info("Adapted tool '" + qualifiedName + "' for MCP",
						McpToolAdapter.class);
				}
			} catch (Exception ex) {
				Logger.error("Failed to adapt tool '" + qualifiedName + "': " + ex.getMessage(),
					ex, McpToolAdapter.class);
			}
		}

		return result;
	}

	/**
	 * Handles an MCP tool call by delegating to the tool.
	 */
	@SuppressWarnings("unchecked")
	private static McpSchema.CallToolResult handleToolCall(
			McpSyncServerExchange exchange,
			CallToolRequest request,
			Tool tool) {

		try {
			Map<String, Object> arguments = request.arguments();
			if (arguments == null) {
				arguments = Map.of();
			}

			String result = tool.execute(arguments, null).text();

			return McpSchema.CallToolResult.builder()
				.content(List.of(new McpSchema.TextContent(result)))
				.build();

		} catch (Exception ex) {
			Logger.error("Error executing tool '" + request.name() + "': " + ex.getMessage(),
				ex, McpToolAdapter.class);
			String errorJson = "{\"error\":\"" + escapeJsonString(ex.getMessage()) + "\"}";
			return McpSchema.CallToolResult.builder()
				.content(List.of(new McpSchema.TextContent(errorJson)))
				.isError(true)
				.build();
		}
	}

	/**
	 * Escapes a string for use in JSON.
	 */
	private static String escapeJsonString(String s) {
		if (s == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (char c : s.toCharArray()) {
			switch (c) {
				case '"':
					sb.append("\\\"");
					break;
				case '\\':
					sb.append("\\\\");
					break;
				case '\n':
					sb.append("\\n");
					break;
				case '\r':
					sb.append("\\r");
					break;
				case '\t':
					sb.append("\\t");
					break;
				default:
					if (c < 32) {
						sb.append(String.format("\\u%04x", (int) c));
					} else {
						sb.append(c);
					}
			}
		}
		return sb.toString();
	}
}
