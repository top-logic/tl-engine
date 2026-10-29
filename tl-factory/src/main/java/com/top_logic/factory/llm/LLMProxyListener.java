/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.llm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * The {@link LLMProxyHandler LLM proxy} on a port of its own, at {@link LLMProxyHandler#PATH}.
 *
 * <p>
 * The listener is an HTTP server of the JDK, independent of the server the control application
 * runs in. It is bound to one address, so that the proxy is reachable from the workspaces without
 * being reachable through the public port of the control application.
 * </p>
 */
public class LLMProxyListener implements AutoCloseable {

	private static final String THREAD_NAME_PREFIX = "llm-proxy-";

	/**
	 * Seconds the running requests get to finish when the listener is closed.
	 */
	private static final int STOP_DELAY_SECONDS = 1;

	private final LLMProxyHandler _handler;

	private final HttpServer _server;

	private final ExecutorService _executor;

	/**
	 * Starts a {@link LLMProxyListener}.
	 *
	 * @param address
	 *        The address and port to listen on; port {@code 0} chooses a free one.
	 * @param proxy
	 *        Supplier of the {@link LLMProxy} asked for each request.
	 * @throws IOException
	 *         If the address cannot be bound.
	 */
	public LLMProxyListener(InetSocketAddress address, Supplier<LLMProxy> proxy) throws IOException {
		_handler = new LLMProxyHandler(proxy);
		_server = HttpServer.create(address, 0);
		AtomicInteger threads = new AtomicInteger();
		ThreadFactory factory = runnable -> {
			Thread thread = new Thread(runnable, THREAD_NAME_PREFIX + threads.incrementAndGet());
			thread.setDaemon(true);
			return thread;
		};
		// A streamed answer holds its thread as long as the model writes.
		_executor = Executors.newCachedThreadPool(factory);
		_server.setExecutor(_executor);
		_server.createContext(LLMProxyHandler.PATH, exchange -> {
			try (exchange) {
				_handler.handle(new JdkExchange(exchange));
			}
		});
		_server.start();
	}

	/**
	 * The address the listener is bound to, with the port chosen.
	 */
	public InetSocketAddress getAddress() {
		return _server.getAddress();
	}

	@Override
	public void close() {
		_server.stop(STOP_DELAY_SECONDS);
		_executor.shutdownNow();
		try {
			_executor.awaitTermination(STOP_DELAY_SECONDS, TimeUnit.SECONDS);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
		_handler.close();
	}

	/**
	 * {@link LLMProxyExchange} of a request to the HTTP server of the JDK.
	 */
	private static final class JdkExchange implements LLMProxyExchange {

		private final HttpExchange _exchange;

		JdkExchange(HttpExchange exchange) {
			_exchange = exchange;
		}

		@Override
		public String method() {
			return _exchange.getRequestMethod();
		}

		@Override
		public String path() {
			String path = _exchange.getRequestURI().getPath();
			if (path == null || !path.startsWith(LLMProxyHandler.PATH)) {
				return null;
			}
			String rest = path.substring(LLMProxyHandler.PATH.length());
			return rest.isEmpty() ? null : rest;
		}

		@Override
		public String query() {
			return _exchange.getRequestURI().getRawQuery();
		}

		@Override
		public String remoteAddress() {
			InetSocketAddress remote = _exchange.getRemoteAddress();
			return remote == null || remote.getAddress() == null ? null : remote.getAddress().getHostAddress();
		}

		@Override
		public List<String> headerNames() {
			return new ArrayList<>(_exchange.getRequestHeaders().keySet());
		}

		@Override
		public List<String> headers(String name) {
			for (Map.Entry<String, List<String>> header : _exchange.getRequestHeaders().entrySet()) {
				if (header.getKey().equalsIgnoreCase(name)) {
					return header.getValue();
				}
			}
			return Collections.emptyList();
		}

		@Override
		public InputStream body() {
			return _exchange.getRequestBody();
		}

		@Override
		public void addHeader(String name, String value) {
			_exchange.getResponseHeaders().add(name, value);
		}

		@Override
		public void sendHeaders(int status, long length) throws IOException {
			// The JDK server takes 0 for a streamed body of unknown length, -1 for none.
			_exchange.sendResponseHeaders(status, length < 0 ? 0 : (length == 0 ? -1 : length));
		}

		@Override
		public OutputStream responseBody() {
			return _exchange.getResponseBody();
		}
	}

}
