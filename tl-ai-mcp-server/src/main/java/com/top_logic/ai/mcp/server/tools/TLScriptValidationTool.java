/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server.tools;

import java.util.Map;

import com.top_logic.ai.mcp.server.util.JsonResponseBuilder;
import com.top_logic.ai.service.tools.script.TLScriptExecutor;
import com.top_logic.ai.service.tools.script.messages.ValidationResult;
import com.top_logic.basic.thread.ThreadContextManager;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;

/**
 * MCP tool for validating TLScript code.
 * <p>
 * This tool performs complete TLScript validation matching the TLScript console behavior:
 * <ul>
 * <li>Syntax validation (parser errors)</li>
 * <li>Model reference validation (undefined classes/properties/types)</li>
 * <li>Type checking (type compatibility)</li>
 * </ul>
 * </p>
 */
public class TLScriptValidationTool {

	/** Tool name for the TLScript validation functionality. */
	public static final String TOOL_NAME = "validate-tlscript";

	/** Tool description. */
	private static final String DESCRIPTION =
		"Validate TLScript code syntax, model references, and type compatibility. " +
			"Performs the same validation as the TLScript console editor.";

	/** JSON Schema for the tool input. */
	private static final String INPUT_SCHEMA_JSON = """
			{
			  "type": "object",
			  "properties": {
			    "script": {
			      "type": "string",
			      "description": "TLScript code to validate. Can be a single expression or multiple expressions wrapped in {}."
			    }
			  },
			  "required": ["script"],
			  "additionalProperties": false
			}
			""";

	/**
	 * Creates the MCP tool specification for TLScript validation.
	 *
	 * @return The tool specification that can be registered with the MCP server.
	 */
	public static McpServerFeatures.SyncToolSpecification createSpecification() {
		McpJsonMapper jsonMapper = McpJsonMapper.getDefault();

		McpSchema.Tool tool = McpSchema.Tool.builder()
			.name(TOOL_NAME)
			.title("Validate TLScript")
			.description(DESCRIPTION)
			.inputSchema(jsonMapper, INPUT_SCHEMA_JSON)
			.build();

		return McpServerFeatures.SyncToolSpecification.builder()
			.tool(tool)
			.callHandler(TLScriptValidationTool::handleToolRequest)
			.build();
	}

	/**
	 * Handles tool requests for validating TLScript code.
	 *
	 * @param exchange
	 *        The MCP server exchange for interacting with the client.
	 * @param request
	 *        The tool call request (includes tool name, arguments, etc.).
	 * @return The result of the validation operation.
	 */
	private static McpSchema.CallToolResult handleToolRequest(
			McpSyncServerExchange exchange,
			CallToolRequest request) {

		Map<String, Object> arguments = request.arguments();

		return ThreadContextManager.inSystemInteraction(
			TLScriptValidationTool.class,
			() -> validateScript(arguments));
	}

	/**
	 * Validates TLScript code based on the given arguments.
	 *
	 * @param arguments
	 *        The tool call arguments containing the script to validate.
	 * @return The result indicating validation success or failure with error details.
	 */
	private static McpSchema.CallToolResult validateScript(Map<String, Object> arguments) {
		// Extract script parameter
		final String script;
		try {
			script = ToolArgumentUtil.requireNonEmptyString(
				arguments,
				"script",
				"TLScript code");
		} catch (ToolArgumentUtil.ToolInputException e) {
			return createToolErrorResult(e.getMessage());
		}

		// Use shared validation logic from TLScriptExecutor
		ValidationResult result = TLScriptExecutor.validate(script);

		// Use ValidationResult's built-in JSON serialization
		String jsonResult = result.toString();
		return new McpSchema.CallToolResult(jsonResult, false);
	}

	/**
	 * Creates an error result for tool execution failures.
	 *
	 * @param errorMessage
	 *        The error message describing the failure.
	 * @return The error result with {@code isError} flag set to true.
	 */
	private static McpSchema.CallToolResult createToolErrorResult(String errorMessage) {
		String errorJson = JsonResponseBuilder.buildToolErrorJson(errorMessage);
		return new McpSchema.CallToolResult(errorJson, true);
	}
}
