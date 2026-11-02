/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.a2a.client;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BooleanSupplier;

import com.top_logic.ai.a2a.protocol.A2AError;
import com.top_logic.ai.a2a.protocol.Json;
import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.basic.credential.RequestAuthentication;
import com.top_logic.tl.ai.core.agent.RunCancellationBinding;
import com.top_logic.tl.ai.core.provider.SSEReader;

/**
 * A minimal A2A v1.0 JSON-RPC client: agent cards, unary calls, and streaming calls read as
 * server-sent events. Redirects are not followed: an agent card URL is configuration, not something a
 * peer may move.
 *
 * <p>
 * Each request is authenticated by the given {@link CredentialProvider} at the moment it is sent. An
 * answer {@code 401 Unauthorized} invalidates the credentials - a cached access token may have
 * expired or been revoked - and the request is sent once more with fresh ones.
 * </p>
 */
public final class A2AClient {

	/**
	 * Receives the results of a stream.
	 */
	public interface StreamListener {

		/**
		 * One stream result ({@code task}, {@code statusUpdate}, {@code artifactUpdate} or
		 * {@code message}).
		 *
		 * @return Whether to go on reading.
		 */
		boolean onResult(Map<String, Object> result);
	}

	/** Closes a stream whose wait ended or whose caller was cancelled. */
	private static final ScheduledExecutorService WATCHDOG = Executors.newSingleThreadScheduledExecutor(job -> {
		Thread thread = new Thread(job, "a2a-client-watchdog");
		thread.setDaemon(true);
		return thread;
	});

	/**
	 * {@link CredentialProvider} for requests sent without credentials.
	 */
	public static final CredentialProvider NO_CREDENTIALS = request -> {
		// No credentials.
	};

	private static final AtomicLong IDS = new AtomicLong();

	private final HttpClient _http = HttpClient.newBuilder()
		.connectTimeout(Duration.ofSeconds(10))
		.followRedirects(HttpClient.Redirect.NEVER)
		.version(HttpClient.Version.HTTP_1_1)
		.build();

	/**
	 * Fetches a JSON document.
	 *
	 * @param credentials
	 *        Authenticates the request.
	 * @param notFoundIsAnswer
	 *        Whether a {@code 404} carries a JSON explanation to return, as the artifact endpoint's
	 *        answer to an unknown section does.
	 */
	public Object getJson(String url, CredentialProvider credentials, boolean notFoundIsAnswer)
			throws IOException, InterruptedException {
		HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(url)).GET()
			.timeout(Duration.ofSeconds(60)).header("Accept", "application/json");
		HttpResponse<String> response =
			send(request, credentials, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		if (!(notFoundIsAnswer && response.statusCode() == 404)) {
			checkStatus(response.statusCode(), response.body());
		}
		return parse(response.body());
	}

	/**
	 * Calls a JSON-RPC method and returns its {@code result}.
	 *
	 * @param credentials
	 *        Authenticates the request.
	 * @throws A2AError
	 *         If the server answers with a JSON-RPC error, or with something that is not JSON-RPC.
	 */
	public Object call(String endpoint, CredentialProvider credentials, String method, Map<String, Object> params,
			Duration timeout) throws IOException, InterruptedException {
		HttpRequest.Builder httpRequest = request(endpoint, method, params, "application/json").timeout(timeout);
		HttpResponse<String> response =
			send(httpRequest, credentials, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		checkStatus(response.statusCode(), response.body());
		return result(parse(response.body()));
	}

	/**
	 * Calls a streaming JSON-RPC method ({@code SendStreamingMessage}, {@code SubscribeToTask}) and
	 * hands each event's result to the listener, until the stream or the listener ends, the wait
	 * passes or the caller is cancelled.
	 *
	 * @param credentials
	 *        Authenticates the request.
	 * @param cancelled
	 *        Asked about once a second; <code>true</code> ends the stream.
	 * @throws A2AError
	 *         If the server answers with a JSON-RPC error.
	 */
	public void stream(String endpoint, CredentialProvider credentials, String method, Map<String, Object> params,
			StreamListener listener, long waitMillis, BooleanSupplier cancelled) throws IOException, InterruptedException {
		HttpRequest.Builder httpRequest = request(endpoint, method, params, "text/event-stream")
			.timeout(Duration.ofSeconds(60));
		HttpResponse<InputStream> response =
			send(httpRequest, credentials, HttpResponse.BodyHandlers.ofInputStream());
		InputStream body = response.body();
		if (!response.headers().firstValue("Content-Type").orElse("").startsWith("text/event-stream")) {
			// A plain JSON-RPC answer: an error, or a result without a stream.
			String text;
			try (InputStream in = body) {
				text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
			}
			checkStatus(response.statusCode(), text);
			Map<String, Object> result = Json.asObject(result(parse(text)));
			if (result != null) {
				listener.onResult(result);
			}
			return;
		}

		long deadline = System.currentTimeMillis() + waitMillis;
		AtomicBoolean stopped = new AtomicBoolean();
		ScheduledFuture<?> watchdog = WATCHDOG.scheduleWithFixedDelay(() -> {
			if (cancelled.getAsBoolean() || System.currentTimeMillis() > deadline) {
				stopped.set(true);
				try {
					body.close();
				} catch (IOException ex) {
					// Closing is all that is wanted.
				}
			}
		}, 1, 1, TimeUnit.SECONDS);
		try (SSEReader reader = new SSEReader(body)) {
			for (SSEReader.Event event; (event = reader.next()) != null;) {
				Map<String, Object> result = Json.asObject(result(parse(event.data())));
				if (result != null && !listener.onResult(result)) {
					return;
				}
			}
		} catch (IOException ex) {
			if (!stopped.get()) {
				throw ex;
			}
		} finally {
			watchdog.cancel(false);
		}
	}

	/**
	 * Sends the request with the current credentials, once more with fresh ones if the server
	 * rejects them.
	 *
	 * @param request
	 *        The request without credentials; each attempt adds the current ones to a copy of it.
	 * @return The first answer that is no rejection of the credentials, or the second rejection.
	 */
	private <T> HttpResponse<T> send(HttpRequest.Builder request, CredentialProvider credentials,
			HttpResponse.BodyHandler<T> handler) throws IOException, InterruptedException {
		boolean renewed = false;
		while (true) {
			HttpRequest.Builder attempt = request.copy();
			credentials.authenticate(RequestAuthentication.of(attempt));
			HttpRequest built = attempt.build();
			HttpResponse<T> response = RunCancellationBinding.whileBlocked(() -> _http.send(built, handler));
			if (response.statusCode() != HttpURLConnection.HTTP_UNAUTHORIZED) {
				return response;
			}
			credentials.invalidate();
			if (renewed) {
				return response;
			}
			renewed = true;
			if (response.body() instanceof Closeable body) {
				body.close();
			}
		}
	}

	private static HttpRequest.Builder request(String endpoint, String method, Map<String, Object> params,
			String accept) {
		String body = Json.write(Json.map("jsonrpc", "2.0", "id", "tl-" + IDS.incrementAndGet(), "method", method,
			"params", params));
		HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(endpoint))
			.POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
			.header("Content-Type", "application/json")
			.header("Accept", accept)
			.header("A2A-Version", "1.0");
		return request;
	}

	private static void checkStatus(int status, String body) {
		if (status == 401 || status == 403) {
			throw new A2AError(A2AError.INVALID_REQUEST,
				"The remote agent refused the credentials (HTTP " + status + "). Check the configured credentials.");
		}
		if (status != 200) {
			String detail = body == null ? "" : body.length() > 300 ? body.substring(0, 300) + "…" : body;
			throw new A2AError(A2AError.INVALID_AGENT_RESPONSE, "The remote agent answered HTTP " + status + ": " + detail);
		}
	}

	private static Object parse(String text) {
		try {
			return Json.parse(text);
		} catch (IllegalArgumentException ex) {
			throw new A2AError(A2AError.INVALID_AGENT_RESPONSE, "The remote agent answered no valid JSON: " + ex.getMessage());
		}
	}

	private static Object result(Object response) {
		Object error = Json.get(response, "error");
		if (error != null) {
			throw A2AError.fromJson(error);
		}
		if (!(response instanceof Map<?, ?> map) || !map.containsKey("result")) {
			throw new A2AError(A2AError.INVALID_AGENT_RESPONSE, "The remote agent answered no JSON-RPC result.");
		}
		return map.get("result");
	}
}
