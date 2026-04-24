/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.mcp;

import java.util.Map;

import io.modelcontextprotocol.spec.McpSchema;

/**
 * Registry that manages MCP server connections and routes tool calls.
 *
 * <p>
 * {@link McpTool} delegates its {@code execute} calls here. Each registered MCP server is
 * identified by a name and communicates via JSON-RPC over the configured transport.
 * </p>
 */
public interface McpServerRegistry {

	/**
	 * Invokes a tool on the named MCP server.
	 *
	 * @param serverName
	 *        The MCP server name as registered in this registry.
	 * @param toolName
	 *        The tool name as exposed by the MCP server.
	 * @param args
	 *        The deserialized tool arguments.
	 * @return The tool result from the MCP server.
	 */
	McpSchema.CallToolResult callTool(String serverName, String toolName, Map<String, Object> args);

}
