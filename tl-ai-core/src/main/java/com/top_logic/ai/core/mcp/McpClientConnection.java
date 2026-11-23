/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.core.mcp;

import java.net.HttpURLConnection;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.function.Supplier;

import com.top_logic.ai.core.agent.RunCancellationBinding;
import com.top_logic.ai.core.mcp.HttpMcpTransport.CredentialHeaders;
import com.top_logic.basic.ExceptionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.credential.CredentialProvider;

import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.McpHttpClientTransportAuthorizationException;
import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.context.Context;

/**
 * Manages a single MCP server connection.
 *
 * <p>
 * Handles transport creation, client initialization, tool discovery, and graceful shutdown.
 * Tool lists are cached and refreshed when the server sends a {@code tools/list_changed}
 * notification.
 * </p>
 *
 * <p>
 * For each call of the client (initialize, tools/list, tools/call), the credential provider of an
 * {@link HttpMcpTransport} runs on the thread of the caller, before the wait for the server, see
 * {@link #send(Supplier)}.
 * </p>
 */
class McpClientConnection {

	/** The time that the client gets to close, the same as in the synchronous client of the library. */
	private static final Duration CLOSE_TIMEOUT = Duration.ofSeconds(10);

	private final String _serverName;

	private final McpTransportConfig _transportConfig;

	/** The credentials of the requests, <code>null</code> for a transport without credentials. */
	private final CredentialProvider _credentials;

	private final int _toolTimeoutSeconds;

	private McpAsyncClient _client;

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
		_credentials = transportConfig instanceof HttpMcpTransport http ? http.credentials() : null;
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

		_client = McpClient.async(transport)
			.requestTimeout(timeout)
			.clientInfo(McpSchema.Implementation.builder("tl-ai", "1.0.0").build())
			.capabilities(McpSchema.ClientCapabilities.builder().build())
			// On a worker of the library, not on the thread that delivers the notification, as
			// the synchronous client of the library runs a consumer.
			.toolsChangeConsumer(tools -> Mono.<Void> fromRunnable(() -> onToolsChanged(tools))
				.subscribeOn(Schedulers.boundedElastic()))
			.build();

		send(_client::initialize);
		refreshTools();

		Logger.info("Connected to MCP server '" + _serverName + "', discovered "
			+ _tools.size() + " tools.", McpClientConnection.class);
	}

	/**
	 * Refreshes the cached tool list from the MCP server.
	 */
	private void refreshTools() {
		McpSchema.ListToolsResult result = send(_client::listTools);
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
	 * @throws CancellationException
	 *         See {@link #send(Supplier)}.
	 */
	McpSchema.CallToolResult callTool(String toolName, Map<String, Object> arguments) {
		McpSchema.CallToolRequest request = McpSchema.CallToolRequest.builder(toolName)
			.arguments(arguments)
			.build();

		return send(() -> _client.callTool(request));
	}

	/**
	 * Sends one call of the client and waits for its answer.
	 *
	 * <p>
	 * The credential provider runs on the thread of the caller, before the wait that a stop of the
	 * run ends. A credential provider may read the knowledge base, which such a wait must not touch,
	 * and it may take the person from the thread, which a thread of the MCP library does not have.
	 * The headers go to the transport in the {@link McpTransportContext} of the call. When the
	 * server rejects them (HTTP 401), they are invalidated and the call is sent once more with fresh
	 * ones, again from the thread of the caller.
	 * </p>
	 *
	 * @param call
	 *        Creates the call of the client. The call is sent when it is subscribed.
	 * @return The answer of the server.
	 * @throws CancellationException
	 *         If the run is stopped while the call waits for the server, or if another party
	 *         interrupts the thread. In the second case, the interrupt flag stays set.
	 */
	private <T> T send(Supplier<Mono<T>> call) {
		try {
			return await(call.get());
		} catch (RuntimeException ex) {
			if (_credentials == null || !isUnauthorized(ex)) {
				throw ex;
			}
		}
		// Cached credentials (an access token) may have expired or been revoked: one more attempt
		// with fresh ones.
		_credentials.invalidate();
		return await(call.get());
	}

	private <T> T await(Mono<T> call) {
		Context context = Context.of(McpTransportContext.KEY, authenticate());
		try {
			// Stopping the run ends the wait for the server.
			return RunCancellationBinding.whileBlocked(() -> call.contextWrite(context).block());
		} catch (CancellationException ex) {
			// The stop of the run, which took back its interrupt.
			throw ex;
		} catch (InterruptedException ex) {
			throw interrupted(ex);
		} catch (RuntimeException ex) {
			// A wait of the library that ends by an interrupt fails with the interrupt as cause.
			if (cause(ex, InterruptedException.class) != null) {
				throw interrupted(ex);
			}
			throw ex;
		}
	}

	/**
	 * The {@link McpTransportContext} with the headers that the credentials set for one call.
	 */
	private McpTransportContext authenticate() {
		if (_credentials == null) {
			return McpTransportContext.EMPTY;
		}
		Map<String, String> headers = new HashMap<>();
		_credentials.authenticate(headers::put);
		return new CredentialHeaders(headers).context();
	}

	private CancellationException interrupted(Exception ex) {
		// Another party than the run interrupted the thread: that party finds the flag set again.
		Thread.currentThread().interrupt();
		CancellationException result =
			new CancellationException("The call to the MCP server '" + _serverName + "' was interrupted.");
		result.initCause(ex);
		return result;
	}

	private static boolean isUnauthorized(RuntimeException ex) {
		McpHttpClientTransportAuthorizationException rejection =
			cause(ex, McpHttpClientTransportAuthorizationException.class);
		return rejection != null
			&& rejection.getResponseInfo().statusCode() == HttpURLConnection.HTTP_UNAUTHORIZED;
	}

	/**
	 * The first exception of the given type in the chain of causes, <code>null</code> if there is
	 * none.
	 */
	private static <E extends Throwable> E cause(Throwable failure, Class<E> type) {
		return ExceptionUtil.getCauseChain(failure).stream()
			.filter(type::isInstance)
			.map(type::cast)
			.findFirst()
			.orElse(null);
	}

	/**
	 * Closes the connection gracefully.
	 */
	void close() {
		if (_client != null) {
			try {
				_client.closeGracefully().block(CLOSE_TIMEOUT);
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
