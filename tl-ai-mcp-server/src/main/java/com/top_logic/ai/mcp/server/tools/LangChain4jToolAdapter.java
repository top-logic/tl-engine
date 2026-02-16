/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.top_logic.ai.service.tools.registry.ToolImplementation;
import com.top_logic.ai.service.tools.registry.ToolRegistry;
import com.top_logic.basic.Logger;
import com.top_logic.basic.json.JSON;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchemaElement;
import dev.langchain4j.service.tool.ToolExecutor;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;

/**
 * Adapter that converts langchain4j tools to MCP tool specifications.
 * <p>
 * This adapter bridges the langchain4j tool system (used by in-app AI agents) with the MCP
 * protocol, allowing external AI clients to use the same tools via HTTP.
 * </p>
 * <p>
 * The adapter converts:
 * </p>
 * <ul>
 * <li>{@link ToolSpecification} → {@link io.modelcontextprotocol.spec.McpSchema.Tool} (tool
 * metadata and input schema)</li>
 * <li>{@link ToolExecutor} → MCP call handler (execution logic)</li>
 * </ul>
 */
public class LangChain4jToolAdapter {

	/**
	 * Prefix used for adapted langchain4j tool names in MCP.
	 * <p>
	 * LangChain4j tools use qualified names like "factory.tool", but MCP tool names should use
	 * hyphens. This prefix helps identify adapted tools.
	 * </p>
	 */
	private static final String TOOL_NAME_PREFIX = "tl-";

	/**
	 * Adapts a single langchain4j {@link ToolImplementation} to an MCP tool specification.
	 *
	 * @param tool
	 *        The langchain4j tool implementation to adapt.
	 * @param qualifiedName
	 *        The qualified name of the tool from the registry (e.g., "model-browser.listModules").
	 * @return An MCP tool specification that wraps the langchain4j tool.
	 */
	public static McpServerFeatures.SyncToolSpecification adapt(ToolImplementation tool, String qualifiedName) {
		ToolSpecification spec = tool.getSpecification();

		// Convert qualified name to MCP-compatible name (dots to hyphens)
		String mcpToolName = TOOL_NAME_PREFIX + qualifiedName.replace('.', '-');

		// Convert langchain4j JSON schema to MCP format
		String inputSchemaJson = convertToJsonSchema(spec.parameters());

		McpJsonMapper jsonMapper = McpJsonMapper.getDefault();

		McpSchema.Tool mcpTool = McpSchema.Tool.builder()
			.name(mcpToolName)
			.title(spec.name())
			.description(spec.description())
			.inputSchema(jsonMapper, inputSchemaJson)
			.build();

		ToolExecutor executor = tool.getExecutor();

		return McpServerFeatures.SyncToolSpecification.builder()
			.tool(mcpTool)
			.callHandler((exchange, request) -> handleToolCall(exchange, request, executor))
			.build();
	}

	/**
	 * Creates MCP tool specifications for all tools registered in the {@link ToolRegistry}.
	 * <p>
	 * This method should only be called when the {@link ToolRegistry} is active.
	 * </p>
	 *
	 * @return List of MCP tool specifications for all registered langchain4j tools.
	 */
	public static List<McpServerFeatures.SyncToolSpecification> adaptAllTools() {
		List<McpServerFeatures.SyncToolSpecification> result = new ArrayList<>();

		if (!ToolRegistry.Module.INSTANCE.isActive()) {
			Logger.info("ToolRegistry is not active, skipping langchain4j tool adaptation",
				LangChain4jToolAdapter.class);
			return result;
		}

		ToolRegistry registry = ToolRegistry.getInstance();
		for (String qualifiedName : registry.getToolNames()) {
			try {
				ToolImplementation tool = registry.getTool(qualifiedName);
				if (tool != null) {
					result.add(adapt(tool, qualifiedName));
					Logger.info("Adapted langchain4j tool '" + qualifiedName + "' for MCP",
						LangChain4jToolAdapter.class);
				}
			} catch (Exception ex) {
				Logger.error("Failed to adapt langchain4j tool '" + qualifiedName + "': " + ex.getMessage(),
					ex, LangChain4jToolAdapter.class);
			}
		}

		return result;
	}

	/**
	 * Handles an MCP tool call by delegating to the langchain4j executor.
	 *
	 * @param exchange
	 *        The MCP server exchange.
	 * @param request
	 *        The MCP tool call request.
	 * @param executor
	 *        The langchain4j tool executor.
	 * @return The result of the tool execution.
	 */
	private static McpSchema.CallToolResult handleToolCall(
			McpSyncServerExchange exchange,
			CallToolRequest request,
			ToolExecutor executor) {

		try {
			// Convert MCP arguments (Map) to JSON string for langchain4j
			Map<String, Object> arguments = request.arguments();
			String argumentsJson = (arguments != null && !arguments.isEmpty())
				? JSON.toString(arguments)
				: "{}";

			// Create langchain4j tool execution request
			ToolExecutionRequest executionRequest = ToolExecutionRequest.builder()
				.name(request.name())
				.arguments(argumentsJson)
				.build();

			// Execute the tool
			String result = executor.execute(executionRequest, null);

			// Return result (not an error)
			return new McpSchema.CallToolResult(result, false);

		} catch (Exception ex) {
			Logger.error("Error executing langchain4j tool '" + request.name() + "': " + ex.getMessage(),
				ex, LangChain4jToolAdapter.class);
			String errorJson = "{\"error\":\"" + escapeJsonString(ex.getMessage()) + "\"}";
			return new McpSchema.CallToolResult(errorJson, true);
		}
	}

	/**
	 * Converts a langchain4j {@link JsonSchemaElement} to a JSON schema string for MCP.
	 *
	 * @param parameters
	 *        The langchain4j parameter schema (may be null for tools with no parameters).
	 * @return JSON schema string suitable for MCP.
	 */
	private static String convertToJsonSchema(JsonSchemaElement parameters) {
		if (parameters == null) {
			// No parameters - return empty object schema
			return "{\"type\":\"object\",\"properties\":{},\"additionalProperties\":false}";
		}

		if (parameters instanceof JsonObjectSchema objectSchema) {
			return convertObjectSchema(objectSchema);
		}

		// Fallback for other schema types - create a simple wrapper
		return "{\"type\":\"object\",\"properties\":{},\"additionalProperties\":false}";
	}

	/**
	 * Converts a {@link JsonObjectSchema} to its JSON representation.
	 */
	private static String convertObjectSchema(JsonObjectSchema objectSchema) {
		StringBuilder sb = new StringBuilder();
		sb.append("{\"type\":\"object\"");

		// Add properties
		Map<String, JsonSchemaElement> properties = objectSchema.properties();
		if (properties != null && !properties.isEmpty()) {
			sb.append(",\"properties\":{");
			boolean first = true;
			for (Map.Entry<String, JsonSchemaElement> entry : properties.entrySet()) {
				if (!first) {
					sb.append(",");
				}
				first = false;
				sb.append("\"").append(escapeJsonString(entry.getKey())).append("\":");
				sb.append(convertSchemaElement(entry.getValue()));
			}
			sb.append("}");
		} else {
			sb.append(",\"properties\":{}");
		}

		// Add required fields
		java.util.List<String> required = objectSchema.required();
		if (required != null && !required.isEmpty()) {
			sb.append(",\"required\":[");
			boolean first = true;
			for (String req : required) {
				if (!first) {
					sb.append(",");
				}
				first = false;
				sb.append("\"").append(escapeJsonString(req)).append("\"");
			}
			sb.append("]");
		}

		sb.append(",\"additionalProperties\":false}");
		return sb.toString();
	}

	/**
	 * Converts a generic {@link JsonSchemaElement} to its JSON representation.
	 */
	private static String convertSchemaElement(JsonSchemaElement element) {
		if (element instanceof JsonObjectSchema objectSchema) {
			return convertObjectSchema(objectSchema);
		}

		if (element instanceof dev.langchain4j.model.chat.request.json.JsonStringSchema stringSchema) {
			StringBuilder sb = new StringBuilder("{\"type\":\"string\"");
			if (stringSchema.description() != null) {
				sb.append(",\"description\":\"").append(escapeJsonString(stringSchema.description())).append("\"");
			}
			sb.append("}");
			return sb.toString();
		}

		if (element instanceof dev.langchain4j.model.chat.request.json.JsonIntegerSchema intSchema) {
			StringBuilder sb = new StringBuilder("{\"type\":\"integer\"");
			if (intSchema.description() != null) {
				sb.append(",\"description\":\"").append(escapeJsonString(intSchema.description())).append("\"");
			}
			sb.append("}");
			return sb.toString();
		}

		if (element instanceof dev.langchain4j.model.chat.request.json.JsonNumberSchema numSchema) {
			StringBuilder sb = new StringBuilder("{\"type\":\"number\"");
			if (numSchema.description() != null) {
				sb.append(",\"description\":\"").append(escapeJsonString(numSchema.description())).append("\"");
			}
			sb.append("}");
			return sb.toString();
		}

		if (element instanceof dev.langchain4j.model.chat.request.json.JsonBooleanSchema boolSchema) {
			StringBuilder sb = new StringBuilder("{\"type\":\"boolean\"");
			if (boolSchema.description() != null) {
				sb.append(",\"description\":\"").append(escapeJsonString(boolSchema.description())).append("\"");
			}
			sb.append("}");
			return sb.toString();
		}

		if (element instanceof dev.langchain4j.model.chat.request.json.JsonArraySchema arraySchema) {
			StringBuilder sb = new StringBuilder("{\"type\":\"array\"");
			if (arraySchema.description() != null) {
				sb.append(",\"description\":\"").append(escapeJsonString(arraySchema.description())).append("\"");
			}
			if (arraySchema.items() != null) {
				sb.append(",\"items\":").append(convertSchemaElement(arraySchema.items()));
			}
			sb.append("}");
			return sb.toString();
		}

		if (element instanceof dev.langchain4j.model.chat.request.json.JsonEnumSchema enumSchema) {
			StringBuilder sb = new StringBuilder("{\"type\":\"string\"");
			if (enumSchema.description() != null) {
				sb.append(",\"description\":\"").append(escapeJsonString(enumSchema.description())).append("\"");
			}
			if (enumSchema.enumValues() != null && !enumSchema.enumValues().isEmpty()) {
				sb.append(",\"enum\":[");
				boolean first = true;
				for (String val : enumSchema.enumValues()) {
					if (!first) {
						sb.append(",");
					}
					first = false;
					sb.append("\"").append(escapeJsonString(val)).append("\"");
				}
				sb.append("]");
			}
			sb.append("}");
			return sb.toString();
		}

		// Fallback for unknown types
		return "{\"type\":\"string\"}";
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
