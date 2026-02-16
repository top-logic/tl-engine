/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server.tools;

import java.util.List;
import java.util.Map;

import com.top_logic.ai.mcp.server.util.JsonResponseBuilder;
import com.top_logic.ai.service.tools.script.TLScriptExecutor;
import com.top_logic.ai.service.tools.script.TLScriptExecutor.ExecutionResult;
import com.top_logic.basic.thread.ThreadContextManager;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;

/**
 * MCP tool for executing TLScript code and returning results.
 * <p>
 * This tool performs complete TLScript validation and execution:
 * <ul>
 * <li>Syntax validation (parser errors)</li>
 * <li>Model reference validation (undefined classes/properties/types)</li>
 * <li>Type checking (type compatibility)</li>
 * <li>Script execution with configurable transaction control</li>
 * <li>Result serialization (primitives, collections, TLObjects)</li>
 * </ul>
 * </p>
 * <p>
 * By default, scripts execute in read-only mode where any modifications are discarded.
 * Set {@code withCommit: true} to execute within a transaction and persist changes.
 * </p>
 */
public class TLScriptExecutionTool {

	/** Tool name for the TLScript execution functionality. */
	public static final String TOOL_NAME = "execute-tlscript";

	/** Tool description. */
	private static final String DESCRIPTION =
		"Execute TLScript code and return results. " +
			"Validates script (syntax, model references, types) before execution. " +
			"Read-only by default; set withCommit=true to persist modifications.";

	/** JSON Schema for the tool input. */
	private static final String INPUT_SCHEMA_JSON = """
			{
			  "type": "object",
			  "properties": {
			    "script": {
			      "type": "string",
			      "description": "TLScript code to execute. Can be a single expression or multiple expressions wrapped in {}."
			    },
			    "withCommit": {
			      "type": "boolean",
			      "description": "If true, execute within transaction and commit changes. If false (default), discard modifications.",
			      "default": false
			    },
			    "args": {
			      "type": "array",
			      "description": "Optional arguments to pass to the script ($arg1, $arg2, ...)",
			      "items": {
			        "anyOf": [
			          {"type": "string"},
			          {"type": "number"},
			          {"type": "boolean"},
			          {"type": "null"}
			        ]
			      }
			    }
			  },
			  "required": ["script"],
			  "additionalProperties": false
			}
			""";

	/**
	 * Creates the MCP tool specification for TLScript execution.
	 *
	 * @return The tool specification that can be registered with the MCP server.
	 */
	public static McpServerFeatures.SyncToolSpecification createSpecification() {
		McpJsonMapper jsonMapper = McpJsonMapper.getDefault();

		McpSchema.Tool tool = McpSchema.Tool.builder()
			.name(TOOL_NAME)
			.title("Execute TLScript")
			.description(DESCRIPTION)
			.inputSchema(jsonMapper, INPUT_SCHEMA_JSON)
			.build();

		return McpServerFeatures.SyncToolSpecification.builder()
			.tool(tool)
			.callHandler(TLScriptExecutionTool::handleToolRequest)
			.build();
	}

	/**
	 * Handles tool requests for executing TLScript code.
	 *
	 * @param exchange
	 *        The MCP server exchange for interacting with the client.
	 * @param request
	 *        The tool call request (includes tool name, arguments, etc.).
	 * @return The result of the execution operation.
	 */
	private static McpSchema.CallToolResult handleToolRequest(
			McpSyncServerExchange exchange,
			CallToolRequest request) {

		Map<String, Object> arguments = request.arguments();

		return ThreadContextManager.inSystemInteraction(
			TLScriptExecutionTool.class,
			() -> executeScript(arguments));
	}

	/**
	 * Executes TLScript code based on the given arguments.
	 *
	 * @param arguments
	 *        The tool call arguments containing the script and options.
	 * @return The result indicating execution success or failure with results/errors.
	 */
	private static McpSchema.CallToolResult executeScript(Map<String, Object> arguments) {
		// Extract parameters
		final String script;
		try {
			script = ToolArgumentUtil.requireNonEmptyString(
				arguments,
				"script",
				"TLScript code");
		} catch (ToolArgumentUtil.ToolInputException e) {
			return createToolErrorResult(e.getMessage());
		}

		boolean withCommit = ToolArgumentUtil.getBooleanArgument(arguments, "withCommit", false);
		List<?> argsList = ToolArgumentUtil.getOptionalObjectList(arguments, "args");

		// Use shared execution logic from TLScriptExecutor
		ExecutionResult execResult = TLScriptExecutor.execute(script, withCommit, argsList);

		// Use ExecutionResult's built-in JSON serialization
		String jsonResult = execResult.toJson();
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
