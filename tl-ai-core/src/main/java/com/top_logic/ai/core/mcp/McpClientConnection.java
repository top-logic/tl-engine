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
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
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
import io.modelcontextprotocol.json.TypeRef;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema;

import reactor.core.publisher.Mono;
import reactor.util.context.Context;

/**
 * Manages a single MCP server connection.
 *
 * <p>
 * Handles transport creation, client initialization, tool discovery, and graceful shutdown.
 * Tool lists are cached. When the server sends a {@code tools/list_changed} notification, the next
 * caller of {@link #tools()} refreshes the list.
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

	/** Counts the headers that the calls resolved, in the order of their resolution. */
	private final AtomicLong _resolutions = new AtomicLong();

	/**
	 * The headers of the last call that succeeded, for the requests that the library sends by
	 * itself.
	 */
	private final AtomicReference<ResolvedHeaders> _lastSucceeded = new AtomicReference<>(ResolvedHeaders.NONE);

	private final int _toolTimeoutSeconds;

	private McpAsyncClient _client;

	private volatile List<McpSchema.Tool> _tools = Collections.emptyList();

	/** Whether the server said that its tools changed after the last refresh of {@link #_tools}. */
	private volatile boolean _toolsStale;

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
		McpClientTransport transport = observeToolChanges(createTransport());
		Duration timeout = Duration.ofSeconds(_toolTimeoutSeconds > 0 ? _toolTimeoutSeconds : 60);

		// No consumer of changed tools: the library asks the new list by itself, with no caller, and
		// calls a consumer only when the server accepts that request.
		_client = McpClient.async(transport)
			.requestTimeout(timeout)
			.clientInfo(McpSchema.Implementation.builder("tl-ai", "1.0.0").build())
			.capabilities(McpSchema.ClientCapabilities.builder().build())
			.build();

		send(_client::initialize);
		refreshTools();

		Logger.info("Connected to MCP server '" + _serverName + "', discovered "
			+ _tools.size() + " tools.", McpClientConnection.class);
	}

	private McpClientTransport createTransport() {
		return _transportConfig instanceof HttpMcpTransport http
			? http.createTransport(() -> _lastSucceeded.get().headers())
			: _transportConfig.createTransport();
	}

	/**
	 * The given transport, which also marks the tool list as stale when the server says that its
	 * tools changed.
	 *
	 * <p>
	 * The mark is set on the notification itself, so that it does not depend on the request that
	 * the library then sends by itself. The next caller of {@link #tools()} asks the new list with
	 * fresh credentials.
	 * </p>
	 */
	private McpClientTransport observeToolChanges(McpClientTransport transport) {
		return new McpClientTransport() {
			@Override
			public Mono<Void> connect(
					Function<Mono<McpSchema.JSONRPCMessage>, Mono<McpSchema.JSONRPCMessage>> handler) {
				return transport.connect(message -> handler.apply(message.doOnNext(this::observe)));
			}

			private void observe(McpSchema.JSONRPCMessage message) {
				if (message instanceof McpSchema.JSONRPCNotification notification
					&& McpSchema.METHOD_NOTIFICATION_TOOLS_LIST_CHANGED.equals(notification.method())) {
					_toolsStale = true;
				}
			}

			@Override
			public void setExceptionHandler(Consumer<Throwable> handler) {
				transport.setExceptionHandler(handler);
			}

			@Override
			public Mono<Void> sendMessage(McpSchema.JSONRPCMessage message) {
				return transport.sendMessage(message);
			}

			@Override
			public <T> T unmarshalFrom(Object data, TypeRef<T> type) {
				return transport.unmarshalFrom(data, type);
			}

			@Override
			public List<String> protocolVersions() {
				return transport.protocolVersions();
			}

			@Override
			public Mono<Void> closeGracefully() {
				return transport.closeGracefully();
			}

			@Override
			public void close() {
				transport.close();
			}
		};
	}

	/**
	 * Refreshes the cached tool list from the MCP server.
	 */
	private void refreshTools() {
		McpSchema.ListToolsResult result = send(_client::listTools);
		_tools = result.tools() != null ? List.copyOf(result.tools()) : Collections.emptyList();
	}

	/**
	 * Returns the cached list of tools from this server.
	 *
	 * <p>
	 * After the server said that its tools changed, the caller asks the new list first. When that
	 * fails, the old list stays, and the next caller tries again.
	 * </p>
	 *
	 * @throws CancellationException
	 *         See {@link #send(Supplier)}.
	 */
	List<McpSchema.Tool> tools() {
		if (_toolsStale) {
			// Cleared before the request, so that a notification during the request is not lost.
			_toolsStale = false;
			try {
				refreshTools();
				Logger.info("MCP server '" + _serverName + "' tools refreshed, now "
					+ _tools.size() + " tools.", McpClientConnection.class);
			} catch (CancellationException ex) {
				_toolsStale = true;
				throw ex;
			} catch (RuntimeException ex) {
				_toolsStale = true;
				Logger.warn("Failed to refresh the tools of MCP server '" + _serverName + "': "
					+ ex.getMessage(), ex, McpClientConnection.class);
			}
		}
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
		} catch (CancellationException ex) {
			// A stop also ends a call that the server rejected: no invalidation and no repeat.
			throw ex;
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
		ResolvedHeaders credentials = authenticate();
		Context context = Context.of(McpTransportContext.KEY, credentials.headers().context());
		T result;
		try {
			// Stopping the run ends the wait for the server.
			result = RunCancellationBinding.whileBlocked(() -> call.contextWrite(context).block());
		} catch (CancellationException ex) {
			// The stop of the run, before or during the wait. It took back its interrupt.
			throw ex;
		} catch (InterruptedException ex) {
			throw interrupted(ex);
		} catch (RuntimeException ex) {
			// When an interrupt ends a wait of the library, Reactor sets the interrupt flag again.
			if (Thread.currentThread().isInterrupted()) {
				throw interrupted(ex);
			}
			throw ex;
		}
		_lastSucceeded.accumulateAndGet(credentials, ResolvedHeaders::later);
		return result;
	}

	/**
	 * The headers that the credentials set for one call.
	 */
	private ResolvedHeaders authenticate() {
		long order = _resolutions.incrementAndGet();
		if (_credentials == null) {
			return new ResolvedHeaders(order, CredentialHeaders.NONE);
		}
		Map<String, String> headers = new HashMap<>();
		_credentials.authenticate(headers::put);
		return new ResolvedHeaders(order, new CredentialHeaders(headers));
	}

	private CancellationException interrupted(Exception ex) {
		// Another party than the run interrupted the thread: that party finds the flag set.
		Thread.currentThread().interrupt();
		CancellationException result =
			new CancellationException("The call to the MCP server '" + _serverName + "' was interrupted.");
		result.initCause(ex);
		return result;
	}

	private static boolean isUnauthorized(RuntimeException ex) {
		return ExceptionUtil.getCauseChain(ex).stream()
			.filter(McpHttpClientTransportAuthorizationException.class::isInstance)
			.map(McpHttpClientTransportAuthorizationException.class::cast)
			.anyMatch(rejection -> rejection.getResponseInfo().statusCode() == HttpURLConnection.HTTP_UNAUTHORIZED);
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

	/**
	 * The headers of one call, with the order in which the calls resolved them.
	 *
	 * @param order
	 *        Larger for headers that a call resolved later.
	 * @param headers
	 *        The headers.
	 */
	private record ResolvedHeaders(long order, CredentialHeaders headers) {

		/** No headers, before a call succeeded. */
		static final ResolvedHeaders NONE = new ResolvedHeaders(0, CredentialHeaders.NONE);

		/**
		 * The headers that a call resolved later, so that an older call that ends later does not
		 * bring older headers back.
		 */
		static ResolvedHeaders later(ResolvedHeaders a, ResolvedHeaders b) {
			return b.order() > a.order() ? b : a;
		}
	}
}
