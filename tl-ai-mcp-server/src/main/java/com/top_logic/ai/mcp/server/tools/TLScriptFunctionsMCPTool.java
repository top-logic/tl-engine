/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server.tools;

import java.util.Map;

import com.top_logic.ai.service.tools.help.TLScriptReferenceTool;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;

/**
 * MCP tool wrapper for {@link TLScriptReferenceTool}.
 * <p>
 * This class wraps the LangChain4j-based {@link TLScriptReferenceTool} to expose it through the
 * MCP (Model Context Protocol) interface. It provides two tools:
 * </p>
 * <ul>
 * <li><b>list-tlscript-functions</b> - Lists all available TLScript functions</li>
 * <li><b>get-tlscript-function-doc</b> - Gets documentation for a specific function</li>
 * </ul>
 */
public class TLScriptFunctionsMCPTool {

	/** Tool name for listing all TLScript functions. */
	public static final String LIST_FUNCTIONS_TOOL = "list-tlscript-functions";

	/** Tool name for getting documentation for a specific function. */
	public static final String GET_FUNCTION_DOC_TOOL = "get-tlscript-function-doc";

	/** Tool description for list functions. */
	private static final String LIST_FUNCTIONS_DESCRIPTION =
		"List all available TLScript functions and methods. " +
			"Returns a list of function names that can be used in TLScript expressions.";

	/** Tool description for get function documentation. */
	private static final String GET_FUNCTION_DOC_DESCRIPTION =
		"Get detailed documentation for a specific TLScript function. " +
			"Returns the function signature, description, and usage information.";

	/** JSON Schema for the list functions tool (no input required). */
	private static final String LIST_FUNCTIONS_INPUT_SCHEMA = """
			{
			  "type": "object",
			  "properties": {},
			  "additionalProperties": false
			}
			""";

	/** JSON Schema for the get function documentation tool input. */
	private static final String GET_FUNCTION_DOC_INPUT_SCHEMA = """
			{
			  "type": "object",
			  "properties": {
			    "functionName": {
			      "type": "string",
			      "description": "Name of the TLScript function to get documentation for"
			    }
			  },
			  "required": ["functionName"],
			  "additionalProperties": false
			}
			""";

	private static final TLScriptReferenceTool _toolInstance = new TLScriptReferenceTool();

	/**
	 * Creates the MCP tool specification for listing TLScript functions.
	 *
	 * @return The tool specification that can be registered with the MCP server.
	 */
	public static McpServerFeatures.SyncToolSpecification createListFunctionsSpecification() {
		McpJsonMapper jsonMapper = McpJsonMapper.getDefault();

		McpSchema.Tool tool = McpSchema.Tool.builder()
			.name(LIST_FUNCTIONS_TOOL)
			.title("List TLScript Functions")
			.description(LIST_FUNCTIONS_DESCRIPTION)
			.inputSchema(jsonMapper, LIST_FUNCTIONS_INPUT_SCHEMA)
			.build();

		return McpServerFeatures.SyncToolSpecification.builder()
			.tool(tool)
			.callHandler(TLScriptFunctionsMCPTool::handleListFunctionsRequest)
			.build();
	}

	/**
	 * Creates the MCP tool specification for getting TLScript function documentation.
	 *
	 * @return The tool specification that can be registered with the MCP server.
	 */
	public static McpServerFeatures.SyncToolSpecification createGetFunctionDocSpecification() {
		McpJsonMapper jsonMapper = McpJsonMapper.getDefault();

		McpSchema.Tool tool = McpSchema.Tool.builder()
			.name(GET_FUNCTION_DOC_TOOL)
			.title("Get TLScript Function Documentation")
			.description(GET_FUNCTION_DOC_DESCRIPTION)
			.inputSchema(jsonMapper, GET_FUNCTION_DOC_INPUT_SCHEMA)
			.build();

		return McpServerFeatures.SyncToolSpecification.builder()
			.tool(tool)
			.callHandler(TLScriptFunctionsMCPTool::handleGetFunctionDocRequest)
			.build();
	}

	/**
	 * Handles tool requests for listing all TLScript functions.
	 *
	 * @param exchange
	 *        The MCP server exchange for interacting with the client.
	 * @param request
	 *        The tool call request (includes tool name, arguments, etc.).
	 * @return The result containing the list of function names.
	 */
	private static McpSchema.CallToolResult handleListFunctionsRequest(
			McpSyncServerExchange exchange,
			CallToolRequest request) {

		String jsonResult = _toolInstance.listTLScriptFunctions();
		return new McpSchema.CallToolResult(jsonResult, false);
	}

	/**
	 * Handles tool requests for getting function documentation.
	 *
	 * @param exchange
	 *        The MCP server exchange for interacting with the client.
	 * @param request
	 *        The tool call request (includes tool name, arguments, etc.).
	 * @return The result containing the function documentation.
	 */
	private static McpSchema.CallToolResult handleGetFunctionDocRequest(
			McpSyncServerExchange exchange,
			CallToolRequest request) {

		Map<String, Object> arguments = request.arguments();

		// Extract function name parameter
		String functionName = null;
		if (arguments != null && arguments.containsKey("functionName")) {
			Object value = arguments.get("functionName");
			if (value instanceof String) {
				functionName = (String) value;
			}
		}

		// If functionName is missing or invalid, pass null to let the tool handle the error
		String jsonResult = _toolInstance.getTLScriptFunctionDocumentation(functionName);
		return new McpSchema.CallToolResult(jsonResult, false);
	}
}
