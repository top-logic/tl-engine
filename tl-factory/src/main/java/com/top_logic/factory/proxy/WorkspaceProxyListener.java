/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * The {@link WorkspaceProxyHandler workspace proxy} on a port of its own, its routes at their
 * {@link ProxyRoute#getPath() paths} below the root.
 *
 * <p>
 * The listener is an HTTP server of the JDK, independent of the server the control application
 * runs in. It is bound to one address, so that the proxy is reachable from the workspaces without
 * being reachable through the public port of the control application. All requests go to one
 * handler, which chooses the route by the path, so that the routes may change without the server
 * and every other path is answered the same way.
 * </p>
 */
public class WorkspaceProxyListener implements AutoCloseable {

	private static final String THREAD_NAME_PREFIX = "workspace-proxy-";

	private static final String ROOT_PATH = "/";

	/**
	 * Seconds the running requests get to finish when the listener is closed.
	 */
	private static final int STOP_DELAY_SECONDS = 1;

	private final WorkspaceProxyHandler _handler;

	private final HttpServer _server;

	private final ExecutorService _executor;

	/**
	 * Starts a {@link WorkspaceProxyListener}.
	 *
	 * @param address
	 *        The address and port to listen on; port {@code 0} chooses a free one.
	 * @param proxy
	 *        Supplier of the {@link WorkspaceProxy} asked for each request.
	 * @throws IOException
	 *         If the address cannot be bound.
	 */
	public WorkspaceProxyListener(InetSocketAddress address, Supplier<? extends WorkspaceProxy> proxy)
			throws IOException {
		_handler = new WorkspaceProxyHandler(proxy);
		_server = HttpServer.create(address, 0);
		AtomicInteger threads = new AtomicInteger();
		ThreadFactory factory = runnable -> {
			Thread thread = new Thread(runnable, THREAD_NAME_PREFIX + threads.incrementAndGet());
			thread.setDaemon(true);
			return thread;
		};
		// A streamed request holds its thread as long as it is read; an answer is written on the
		// threads of the upstream client.
		_executor = Executors.newCachedThreadPool(factory);
		_server.setExecutor(_executor);
		_server.createContext(ROOT_PATH, exchange -> {
			JdkExchange proxyExchange = new JdkExchange(exchange);
			try {
				_handler.handle(proxyExchange);
			} finally {
				if (!proxyExchange.isAsync()) {
					exchange.close();
				}
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
	}

	/**
	 * {@link ProxyExchange} of a request to the HTTP server of the JDK.
	 */
	private static final class JdkExchange implements ProxyExchange {

		private final HttpExchange _exchange;

		private JdkAsyncAnswer _async;

		JdkExchange(HttpExchange exchange) {
			_exchange = exchange;
		}

		@Override
		public String method() {
			return _exchange.getRequestMethod();
		}

		@Override
		public String path() {
			return _exchange.getRequestURI().getPath();
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

		@Override
		public AsyncAnswer startAsync() {
			if (_async == null) {
				_async = new JdkAsyncAnswer();
			}
			return _async;
		}

		boolean isAsync() {
			return _async != null;
		}

		/**
		 * {@link AsyncAnswer} of the HTTP server of the JDK, whose output only blocks: a piece of
		 * the body is written on the thread delivering it, no thread waits for the upstream.
		 */
		private final class JdkAsyncAnswer implements AsyncAnswer, Flow.Subscriber<List<ByteBuffer>> {

			private Flow.Subscription _subscription;

			private boolean _completed;

			@Override
			public void stream(Flow.Publisher<List<ByteBuffer>> body) {
				body.subscribe(this);
			}

			@Override
			public void onSubscribe(Flow.Subscription subscription) {
				_subscription = subscription;
				subscription.request(1);
			}

			@Override
			public void onNext(List<ByteBuffer> item) {
				try {
					OutputStream out = _exchange.getResponseBody();
					for (ByteBuffer buffer : item) {
						byte[] bytes = new byte[buffer.remaining()];
						buffer.get(bytes);
						out.write(bytes);
					}
					out.flush();
				} catch (IOException ex) {
					// The client went away.
					_subscription.cancel();
					complete();
					return;
				}
				_subscription.request(1);
			}

			@Override
			public void onError(Throwable throwable) {
				complete();
			}

			@Override
			public void onComplete() {
				complete();
			}

			@Override
			public synchronized void complete() {
				if (!_completed) {
					_completed = true;
					_exchange.close();
				}
			}
		}
	}

}
