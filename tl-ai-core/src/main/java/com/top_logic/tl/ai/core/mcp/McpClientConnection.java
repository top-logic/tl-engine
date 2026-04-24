/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.mcp;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.Logger;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.json.McpJsonMapper;
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

	private final McpServerRegistry.McpServerConfig _config;

	private McpSyncClient _client;

	private volatile List<McpSchema.Tool> _tools = Collections.emptyList();

	/**
	 * Creates a new connection for the given server configuration.
	 */
	McpClientConnection(String serverName, McpServerRegistry.McpServerConfig config) {
		_serverName = serverName;
		_config = config;
	}

	/**
	 * Connects to the MCP server and discovers available tools.
	 *
	 * @throws RuntimeException
	 *         if the connection or initialization fails
	 */
	void connect() {
		McpClientTransport transport = createTransport();
		int timeoutSeconds = _config.getToolTimeoutSeconds();
		Duration timeout = Duration.ofSeconds(timeoutSeconds > 0 ? timeoutSeconds : 60);

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
	 * Creates the appropriate transport based on the server configuration.
	 */
	private McpClientTransport createTransport() {
		var transportConfig = _config.getTransport();

		if (transportConfig instanceof StdioTransportConfig stdio) {
			ServerParameters.Builder builder = ServerParameters.builder(stdio.getCommand());

			List<String> args = stdio.getArgs();
			if (args != null && !args.isEmpty()) {
				builder.args(args);
			}

			Map<String, String> env = stdio.getEnv();
			if (env != null) {
				builder.env(env);
			}

			return new StdioClientTransport(builder.build(), McpJsonMapper.getDefault());
		}

		if (transportConfig instanceof HttpTransportConfig http) {
			HttpClientStreamableHttpTransport.Builder builder =
				HttpClientStreamableHttpTransport.builder(http.getUrl());

			String bearerVar = http.getBearerTokenEnvVar();
			if (bearerVar != null && !bearerVar.isEmpty()) {
				String token = System.getenv(bearerVar);
				if (token != null && !token.isEmpty()) {
					builder.customizeRequest(req ->
						req.header("Authorization", "Bearer " + token));
				} else {
					Logger.warn("Bearer token env var '" + bearerVar
						+ "' is not set for MCP server '" + _serverName + "'.",
						McpClientConnection.class);
				}
			}

			Map<String, String> headers = http.getHeaders();
			if (headers != null && !headers.isEmpty()) {
				builder.customizeRequest(req -> {
					for (Map.Entry<String, String> entry : headers.entrySet()) {
						req.header(entry.getKey(), entry.getValue());
					}
				});
			}

			return builder.build();
		}

		throw new IllegalArgumentException(
			"Unsupported transport config type: " + transportConfig.getClass().getName());
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
