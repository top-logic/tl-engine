/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.sun.net.httpserver.HttpServer;

import junit.framework.TestCase;

import com.top_logic.tl.ai.tool.api.CancellationToken;

/**
 * Verifies that a streaming provider aborts an in-flight request promptly when the run's
 * {@link CancellationToken} is cancelled, instead of blocking on the network until the model
 * finishes its response.
 *
 * <p>
 * A local HTTP server sends the response headers and a first chunk, then holds the body open
 * (never sending the rest), mimicking a model still generating tokens. Without the fix, the
 * provider's stream read blocks here until the connection times out; with it, cancelling the
 * token closes the response stream and the call returns within a fraction of a second.
 * </p>
 */
public class TestStreamCancellation extends TestCase {

	/** Time budget within which a cancelled stream must return; the server holds the body ~60s. */
	private static final long PROMPT_LIMIT_MS = 5_000;

	/** A minimal concrete provider whose {@code processStream} blocks reading the body to EOF. */
	private static final class BlockingReadProvider extends AbstractStreamingProvider {

		BlockingReadProvider(String baseUrl, java.net.http.HttpClient httpClient) {
			super(null, baseUrl, httpClient);
		}

		@Override
		protected String endpointPath(ProviderRequest request) {
			return "/stream";
		}

		@Override
		protected String buildRequestJson(ProviderRequest request) {
			return "{}";
		}

		@Override
		protected void processStream(InputStream input, StreamHandler handler) throws IOException {
			byte[] buffer = new byte[256];
			while (input.read(buffer) != -1) {
				// Consume until EOF or the stream is closed by cancellation.
			}
		}

		@Override
		protected void handleErrorResponse(HttpResponse<InputStream> response) throws ProviderException {
			throw new ProviderException(ProviderException.Kind.UNKNOWN, response.statusCode(), "unexpected error");
		}
	}

	/** */
	public void testCancelAbortsStreamPromptly() throws Exception {
		CountDownLatch bodyOpened = new CountDownLatch(1);
		HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		ExecutorService serverPool = Executors.newCachedThreadPool();
		server.setExecutor(serverPool);
		server.createContext("/stream", exchange -> {
			exchange.getResponseHeaders().add("Content-Type", "text/event-stream");
			exchange.sendResponseHeaders(200, 0);
			try (OutputStream body = exchange.getResponseBody()) {
				body.write("data: hello\n\n".getBytes());
				body.flush();
				bodyOpened.countDown();
				// Hold the body open, as a model still generating its answer would. Interruptible
				// so test teardown (serverPool.shutdownNow) returns immediately instead of waiting.
				try {
					Thread.sleep(60_000);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
				}
			} catch (IOException expectedWhenClientAborts) {
				// The client closed the connection on cancellation; expected.
			}
		});
		server.start();
		try {
			String baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
			BlockingReadProvider provider = new BlockingReadProvider(baseUrl, AbstractStreamingProvider.defaultHttpClient());

			CancellationToken token = new CancellationToken();
			StreamHandler handler = new StreamHandler() {
				@Override
				public void onEvent(StreamEvent event) {
					// Not needed for this test.
				}

				@Override
				public CancellationToken cancellation() {
					return token;
				}
			};

			ProviderRequest request = new ProviderRequest("test-model", null, List.of(), List.of(), 100);
			AtomicReference<Throwable> failure = new AtomicReference<>();
			CountDownLatch done = new CountDownLatch(1);
			Thread worker = new Thread(() -> {
				try {
					provider.stream(request, handler);
				} catch (Throwable t) {
					failure.set(t);
				} finally {
					done.countDown();
				}
			}, "stream-worker");
			worker.start();

			// Wait until the request is in flight and blocked reading the held-open body.
			assertTrue("Response body must have been opened by the server",
				bodyOpened.await(PROMPT_LIMIT_MS, TimeUnit.MILLISECONDS));

			long start = System.nanoTime();
			token.cancel();

			assertTrue("Cancelled stream must return well before the server's 60s hold",
				done.await(PROMPT_LIMIT_MS, TimeUnit.MILLISECONDS));
			long elapsedMs = (System.nanoTime() - start) / 1_000_000L;
			assertTrue("Cancel must take effect promptly; took " + elapsedMs + "ms",
				elapsedMs < PROMPT_LIMIT_MS);

			// The aborted stream surfaces as a ProviderException, not a hang or a stray error type.
			Throwable thrown = failure.get();
			assertTrue("A cancelled stream must throw ProviderException; got: " + thrown,
				thrown instanceof ProviderException);
		} finally {
			serverPool.shutdownNow();
			server.stop(0);
		}
	}

	/** A stream that is never cancelled must not be aborted by the cancellation plumbing. */
	public void testUncancelledStreamCompletesNormally() throws Exception {
		HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		server.createContext("/stream", exchange -> {
			exchange.getResponseHeaders().add("Content-Type", "text/event-stream");
			byte[] payload = "data: done\n\n".getBytes();
			exchange.sendResponseHeaders(200, payload.length);
			try (OutputStream body = exchange.getResponseBody()) {
				body.write(payload);
			}
		});
		server.start();
		try {
			String baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
			BlockingReadProvider provider = new BlockingReadProvider(baseUrl, AbstractStreamingProvider.defaultHttpClient());
			ProviderRequest request = new ProviderRequest("test-model", null, List.of(), List.of(), 100);
			// Default handler cancellation() is UNCANCELLABLE; the call must return normally.
			provider.stream(request, event -> { /* ignore */ });
		} finally {
			server.stop(0);
		}
	}
}
