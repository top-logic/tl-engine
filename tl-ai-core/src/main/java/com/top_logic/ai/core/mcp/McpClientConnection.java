/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.core.mcp;

import java.net.HttpURLConnection;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
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
 * caller of {@link #tools()} starts a refresh of the list.
 * </p>
 *
 * <p>
 * For each request of the client (initialize, tools/list, tools/call), the credential provider of
 * an {@link HttpMcpTransport} runs on the thread of the caller, before the wait for the server, see
 * {@link #send(Supplier)} and {@link #startRefresh()}.
 * </p>
 */
class McpClientConnection {

	/** The time that the client gets to close, the same as in the synchronous client of the library. */
	private static final Duration CLOSE_TIMEOUT = Duration.ofSeconds(10);

	private final String _serverName;

	private final McpTransportConfig _transportConfig;

	/** The credentials of the requests, <code>null</code> for a transport without credentials. */
	private final CredentialProvider _credentials;

	/** Gives each request of credentials and each request of the tool list its order. */
	private final AtomicLong _order = new AtomicLong();

	/**
	 * The headers of the last call that succeeded, for the requests that the library sends by
	 * itself.
	 */
	private final AtomicReference<Ordered<CredentialHeaders>> _lastSucceeded =
		new AtomicReference<>(new Ordered<>(0, CredentialHeaders.NONE));

	private final int _toolTimeoutSeconds;

	private McpAsyncClient _client;

	private final AtomicReference<Ordered<List<McpSchema.Tool>>> _tools =
		new AtomicReference<>(new Ordered<>(0, List.of()));

	/** Whether the server said that its tools changed, and no refresh of {@link #_tools} runs. */
	private final AtomicBoolean _toolsStale = new AtomicBoolean();

	/** Whether the server rejected the credentials of the last refresh of {@link #_tools}. */
	private final AtomicBoolean _refreshRejected = new AtomicBoolean();

	/** The number of refreshes of {@link #_tools} that failed since the last one that succeeded. */
	private final AtomicInteger _refreshFailures = new AtomicInteger();

	/** Whether a refresh failure since the last refresh that succeeded was logged as a warning. */
	private final AtomicBoolean _refreshWarned = new AtomicBoolean();

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
		McpClientTransport transport = new ToolChangeObserver(createTransport(), _toolsStale);
		Duration timeout = Duration.ofSeconds(_toolTimeoutSeconds > 0 ? _toolTimeoutSeconds : 60);

		_client = McpClient.async(transport)
			.requestTimeout(timeout)
			.clientInfo(McpSchema.Implementation.builder("tl-ai", "1.0.0").build())
			.capabilities(McpSchema.ClientCapabilities.builder().build())
			.build();

		send(_client::initialize);
		long order = _order.incrementAndGet();
		setTools(order, send(_client::listTools));

		Logger.info("Connected to MCP server '" + _serverName + "', discovered "
			+ _tools.get().value().size() + " tools.", McpClientConnection.class);
	}

	private McpClientTransport createTransport() {
		return _transportConfig instanceof HttpMcpTransport http
			? http.createTransport(() -> _lastSucceeded.get().value())
			: _transportConfig.createTransport();
	}

	/**
	 * Returns the cached list of tools from this server.
	 *
	 * <p>
	 * Never waits for the server. After the server said that its tools changed, the call starts a
	 * refresh and returns the list in place; a later call gets the new list. The credential
	 * provider runs on the calling thread, outside a cancellable wait, see {@link #startRefresh()}.
	 * </p>
	 */
	List<McpSchema.Tool> tools() {
		if (_toolsStale.compareAndSet(true, false)) {
			startRefresh();
		}
		return _tools.get().value();
	}

	/**
	 * Asks the server for its tool list, and does not wait for the answer.
	 *
	 * <p>
	 * The credential provider runs on the calling thread. The answer comes on a thread of the library
	 * and sets the list and the headers of the last call that succeeded. After a failure, the next
	 * caller of {@link #tools()} starts a new refresh. After a rejection (HTTP 401), that caller
	 * first invalidates the credentials, again on its own thread.
	 * </p>
	 *
	 * <p>
	 * A server that cannot be reached would fail each refresh, so only the first failure in a row
	 * is a warning. A rejection that the next refresh repairs is no failure to report.
	 * </p>
	 */
	private void startRefresh() {
		Ordered<CredentialHeaders> credentials;
		try {
			if (_refreshRejected.compareAndSet(true, false) && _credentials != null) {
				_credentials.invalidate();
			}
			credentials = authenticate();
		} catch (RuntimeException ex) {
			refreshFailed(ex, false);
			return;
		}
		_client.listTools()
			.contextWrite(Context.of(McpTransportContext.KEY, credentials.value().context()))
			.subscribe(
				result -> {
					setTools(credentials.order(), result);
					_lastSucceeded.accumulateAndGet(credentials, Ordered::later);
					_refreshFailures.set(0);
					_refreshWarned.set(false);
					Logger.info("MCP server '" + _serverName + "' tools refreshed, now "
						+ _tools.get().value().size() + " tools.", McpClientConnection.class);
				},
				error -> {
					boolean rejected = _credentials != null && isUnauthorized(error);
					if (rejected) {
						_refreshRejected.set(true);
					}
					refreshFailed(error, rejected);
				});
	}

	/**
	 * Marks the list as stale again, and logs the failure.
	 *
	 * @param rejected
	 *        Whether the server rejected the credentials (HTTP 401).
	 */
	private void refreshFailed(Throwable error, boolean rejected) {
		int failures = _refreshFailures.incrementAndGet();
		// After a first rejection, the next refresh invalidates the credentials and asks again. Only
		// when it fails too, a failure is logged.
		if (failures > 1 || !rejected) {
			String message =
				"Failed to refresh the tools of MCP server '" + _serverName + "': " + error.getMessage();
			if (_refreshWarned.compareAndSet(false, true)) {
				Logger.warn(message, error, McpClientConnection.class);
			} else {
				Logger.debug(message, McpClientConnection.class);
			}
		}
		// Last, so that the next refresh starts after this failure is counted.
		_toolsStale.set(true);
	}

	/**
	 * Puts the given list in place, unless the list in place comes from a request that started
	 * later.
	 */
	private void setTools(long order, McpSchema.ListToolsResult result) {
		List<McpSchema.Tool> tools = result.tools() != null ? List.copyOf(result.tools()) : List.of();
		_tools.accumulateAndGet(new Ordered<>(order, tools), Ordered::later);
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
		Ordered<CredentialHeaders> credentials = authenticate();
		Context context = Context.of(McpTransportContext.KEY, credentials.value().context());
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
		_lastSucceeded.accumulateAndGet(credentials, Ordered::later);
		return result;
	}

	/**
	 * The headers that the credentials set for one request.
	 */
	private Ordered<CredentialHeaders> authenticate() {
		long order = _order.incrementAndGet();
		if (_credentials == null) {
			return new Ordered<>(order, CredentialHeaders.NONE);
		}
		Map<String, String> headers = new HashMap<>();
		_credentials.authenticate(headers::put);
		return new Ordered<>(order, new CredentialHeaders(headers));
	}

	private CancellationException interrupted(Exception ex) {
		// Another party than the run interrupted the thread: that party finds the flag set.
		Thread.currentThread().interrupt();
		CancellationException result =
			new CancellationException("The call to the MCP server '" + _serverName + "' was interrupted.");
		result.initCause(ex);
		return result;
	}

	private static boolean isUnauthorized(Throwable failure) {
		return ExceptionUtil.getCauseChain(failure).stream()
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
		_tools.set(new Ordered<>(_order.incrementAndGet(), List.of()));
	}

	/**
	 * Whether the client is connected and initialized.
	 */
	boolean isConnected() {
		return _client != null && _client.isInitialized();
	}

	/**
	 * A value, with the order in which the request for it started.
	 *
	 * @param order
	 *        Larger for a request that started later.
	 * @param value
	 *        The value.
	 */
	private record Ordered<T>(long order, T value) {

		/**
		 * The value of the request that started later, so that an older request that ends later does
		 * not bring an older value back.
		 */
		static <T> Ordered<T> later(Ordered<T> a, Ordered<T> b) {
			return b.order() > a.order() ? b : a;
		}
	}

	/**
	 * A transport that marks the tool list of its connection as stale when the server says that its
	 * tools changed, and that drops this notification.
	 *
	 * <p>
	 * The notification is dropped, so that the library does not ask for the new list by itself,
	 * with no caller and with headers that can be stale. The next caller of
	 * {@link McpClientConnection#tools()} asks for it with fresh credentials.
	 * </p>
	 *
	 * <p>
	 * Each method of {@link io.modelcontextprotocol.spec.McpTransport} and
	 * {@link McpClientTransport} must be delegated, also a default method: without
	 * {@link #protocolVersions()}, for example, the library would offer the default protocol
	 * versions of the interface instead of those of the transport.
	 * </p>
	 */
	private static final class ToolChangeObserver implements McpClientTransport {

		private final McpClientTransport _transport;

		private final AtomicBoolean _toolsStale;

		ToolChangeObserver(McpClientTransport transport, AtomicBoolean toolsStale) {
			_transport = transport;
			_toolsStale = toolsStale;
		}

		@Override
		public Mono<Void> connect(
				Function<Mono<McpSchema.JSONRPCMessage>, Mono<McpSchema.JSONRPCMessage>> handler) {
			// The transport accepts an empty result of the handler for a message.
			return _transport.connect(message -> handler.apply(message.filter(this::isForTheLibrary)));
		}

		/**
		 * <code>false</code> for the notification that the tools changed, which marks the tool list
		 * as stale.
		 */
		private boolean isForTheLibrary(McpSchema.JSONRPCMessage message) {
			if (message instanceof McpSchema.JSONRPCNotification notification
				&& McpSchema.METHOD_NOTIFICATION_TOOLS_LIST_CHANGED.equals(notification.method())) {
				_toolsStale.set(true);
				return false;
			}
			return true;
		}

		@Override
		public void setExceptionHandler(Consumer<Throwable> handler) {
			_transport.setExceptionHandler(handler);
		}

		@Override
		public Mono<Void> sendMessage(McpSchema.JSONRPCMessage message) {
			return _transport.sendMessage(message);
		}

		@Override
		public <T> T unmarshalFrom(Object data, TypeRef<T> type) {
			return _transport.unmarshalFrom(data, type);
		}

		@Override
		public List<String> protocolVersions() {
			return _transport.protocolVersions();
		}

		@Override
		public Mono<Void> closeGracefully() {
			return _transport.closeGracefully();
		}

		@Override
		public void close() {
			_transport.close();
		}
	}
}
