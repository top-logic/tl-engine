/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.mcp;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.Logger;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema;

/**
 * Manages a single MCP server connection.
 *
 * <p>
 * Handles transport creation, client initialization, tool discovery, and graceful shutdown.
 * Tool lists are cached and refreshed when the server sends a {@code tools/list_changed}
 * notification.
 * </p>
 */
class McpClientConnection {

	private final String _serverName;

	private final McpTransportConfig _transportConfig;

	private final int _toolTimeoutSeconds;

	private McpSyncClient _client;

	private volatile List<McpSchema.Tool> _tools = Collections.emptyList();

	/**
	 * Creates a new connection for the given transport configuration.
	 *
	 * @param serverName
	 *        Display name of the MCP server (used in log messages).
	 * @param transportConfig
	 *        The instantiated transport factory.
	 * @param toolTimeoutSeconds
	 *        Timeout in seconds for individual tool calls.
	 */
	McpClientConnection(String serverName, McpTransportConfig transportConfig, int toolTimeoutSeconds) {
		_serverName = serverName;
		_transportConfig = transportConfig;
		_toolTimeoutSeconds = toolTimeoutSeconds;
	}

	/**
	 * Connects to the MCP server and discovers available tools.
	 *
	 * @throws RuntimeException
	 *         if the connection or initialization fails
	 */
	void connect() {
		McpClientTransport transport = _transportConfig.createTransport();
		Duration timeout = Duration.ofSeconds(_toolTimeoutSeconds > 0 ? _toolTimeoutSeconds : 60);

		_client = McpClient.sync(transport)
			.requestTimeout(timeout)
			.clientInfo(new McpSchema.Implementation("tl-ai", "1.0.0"))
			.capabilities(McpSchema.ClientCapabilities.builder().build())
			.toolsChangeConsumer(this::onToolsChanged)
			.build();

		_client.initialize();
		refreshTools();

		Logger.info("Connected to MCP server '" + _serverName + "', discovered "
			+ _tools.size() + " tools.", McpClientConnection.class);
	}

	/**
	 * Refreshes the cached tool list from the MCP server.
	 */
	private void refreshTools() {
		McpSchema.ListToolsResult result = _client.listTools();
		_tools = result.tools() != null ? List.copyOf(result.tools()) : Collections.emptyList();
	}

	/**
	 * Callback for the {@code tools/list_changed} notification.
	 */
	private void onToolsChanged(List<McpSchema.Tool> newTools) {
		_tools = newTools != null ? List.copyOf(newTools) : Collections.emptyList();
		Logger.info("MCP server '" + _serverName + "' tools refreshed, now "
			+ _tools.size() + " tools.", McpClientConnection.class);
	}

	/**
	 * Returns the cached list of tools from this server.
	 */
	List<McpSchema.Tool> tools() {
		return _tools;
	}

	/**
	 * Calls a tool on this server.
	 *
	 * @param toolName
	 *        The original MCP tool name.
	 * @param arguments
	 *        Tool arguments as a map.
	 * @return The tool call result.
	 */
	McpSchema.CallToolResult callTool(String toolName, Map<String, Object> arguments) {
		McpSchema.CallToolRequest request = McpSchema.CallToolRequest.builder()
			.name(toolName)
			.arguments(arguments)
			.build();

		return _client.callTool(request);
	}

	/**
	 * Closes the connection gracefully.
	 */
	void close() {
		if (_client != null) {
			try {
				_client.closeGracefully();
			} catch (Exception e) {
				Logger.warn("Error closing MCP client for '" + _serverName + "': "
					+ e.getMessage(), McpClientConnection.class);
			}
			_client = null;
		}
		_tools = Collections.emptyList();
	}

	/**
	 * Whether the client is connected and initialized.
	 */
	boolean isConnected() {
		return _client != null && _client.isInitialized();
	}
}
