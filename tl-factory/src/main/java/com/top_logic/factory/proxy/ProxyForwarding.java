/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.proxy;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Flow;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * Forwarding of a {@link ProxyExchange} to an upstream with the HTTP client of the JDK, shared by
 * the {@link ProxyRoute routes}: the headers that pass in either direction, the request body
 * streamed to the upstream, and the answer streamed back.
 */
public final class ProxyForwarding {

	/**
	 * Header carrying the credentials of a request: the token of the workspace for the proxy.
	 */
	public static final String AUTHORIZATION = "Authorization";

	/**
	 * Header naming the content type of a body.
	 */
	public static final String CONTENT_TYPE = "Content-Type";

	/**
	 * Header naming the length of a body.
	 */
	public static final String CONTENT_LENGTH = "Content-Length";

	/**
	 * Content type of {@link #sendText(ProxyExchange, int, String)}.
	 */
	public static final String TEXT_CONTENT_TYPE = "text/plain; charset=utf-8";

	/**
	 * Header carrying the cookies of a request.
	 */
	public static final String COOKIE = "Cookie";

	/**
	 * Header of an answer setting a cookie.
	 */
	public static final String SET_COOKIE = "Set-Cookie";

	/**
	 * Header naming the encoding of the body in transfer.
	 */
	public static final String TRANSFER_ENCODING = "Transfer-Encoding";

	/**
	 * Request headers (lower case) a proxy never forwards: hop-by-hop headers and the headers the
	 * HTTP client sets itself.
	 */
	public static final Set<String> HOP_BY_HOP_REQUEST_HEADERS = Set.of(
		"host", CONTENT_LENGTH.toLowerCase(Locale.ROOT), "expect", "connection", "keep-alive", "proxy-authenticate",
		"proxy-authorization", "proxy-connection", "te", "trailer", TRANSFER_ENCODING.toLowerCase(Locale.ROOT),
		"upgrade", "http2-settings");

	/**
	 * Request headers (lower case) the routes of the workspace proxy never forward: the
	 * {@link #HOP_BY_HOP_REQUEST_HEADERS}, the credentials of the proxy and cookies of the control
	 * application.
	 */
	public static final Set<String> DROPPED_REQUEST_HEADERS =
		union(HOP_BY_HOP_REQUEST_HEADERS, AUTHORIZATION.toLowerCase(Locale.ROOT), COOKIE.toLowerCase(Locale.ROOT));

	/**
	 * Response headers (lower case) a proxy never passes back: hop-by-hop headers and the length
	 * (the server chooses the transfer).
	 */
	public static final Set<String> HOP_BY_HOP_RESPONSE_HEADERS = Set.of(
		CONTENT_LENGTH.toLowerCase(Locale.ROOT), "connection", "keep-alive", "proxy-authenticate",
		"proxy-authorization", "proxy-connection", "te", "trailer", TRANSFER_ENCODING.toLowerCase(Locale.ROOT),
		"upgrade");

	/**
	 * Response headers (lower case) the routes of the workspace proxy do not pass back: the
	 * {@link #HOP_BY_HOP_RESPONSE_HEADERS} and cookies of the upstream.
	 */
	public static final Set<String> DROPPED_RESPONSE_HEADERS =
		union(HOP_BY_HOP_RESPONSE_HEADERS, SET_COOKIE.toLowerCase(Locale.ROOT));

	private static final String PSEUDO_HEADER_PREFIX = ":";

	private ProxyForwarding() {
		// Static utility.
	}

	/**
	 * Copies the headers of the request to the forwarded request, except the
	 * {@link #DROPPED_REQUEST_HEADERS} and the given ones.
	 *
	 * @param exchange
	 *        The request of the client.
	 * @param request
	 *        The request to the upstream.
	 * @param alsoDropped
	 *        Further headers (lower case) of the route that are not forwarded, e.g. credentials of
	 *        its protocol.
	 */
	public static void copyRequestHeaders(ProxyExchange exchange, HttpRequest.Builder request,
			Set<String> alsoDropped) {
		forwardRequestHeaders(exchange, request,
			lower -> DROPPED_REQUEST_HEADERS.contains(lower) || alsoDropped.contains(lower));
	}

	/**
	 * Copies the headers of the request to the forwarded request, except the given ones.
	 *
	 * @param exchange
	 *        The request of the client.
	 * @param request
	 *        The request to the upstream.
	 * @param dropped
	 *        Whether the header of the given name (lower case) is not forwarded; it must drop at
	 *        least the {@link #HOP_BY_HOP_REQUEST_HEADERS}.
	 */
	public static void forwardRequestHeaders(ProxyExchange exchange, HttpRequest.Builder request,
			Predicate<String> dropped) {
		for (String name : exchange.headerNames()) {
			if (dropped.test(name.toLowerCase(Locale.ROOT))) {
				continue;
			}
			for (String value : exchange.headers(name)) {
				request.header(name, value);
			}
		}
	}

	/**
	 * Whether the request has a body: it names its {@link #CONTENT_LENGTH} above zero or a
	 * {@link #TRANSFER_ENCODING}.
	 */
	public static boolean hasRequestBody(ProxyExchange exchange) {
		return contentLength(exchange) > 0 || !exchange.headers(TRANSFER_ENCODING).isEmpty();
	}

	/**
	 * The body of the request as it is forwarded: streamed from the client to the upstream as it
	 * arrives, never held as a whole.
	 *
	 * <p>
	 * With the {@link #CONTENT_LENGTH} of the client, the upstream gets that length; without, the
	 * body is sent in chunks.
	 * </p>
	 */
	public static HttpRequest.BodyPublisher requestBody(ProxyExchange exchange) {
		long length = contentLength(exchange);
		if (length == 0) {
			return HttpRequest.BodyPublishers.noBody();
		}
		HttpRequest.BodyPublisher stream = HttpRequest.BodyPublishers.ofInputStream(() -> {
			try {
				return exchange.body();
			} catch (IOException ex) {
				throw new UncheckedIOException(ex);
			}
		});
		return length > 0 ? HttpRequest.BodyPublishers.fromPublisher(stream, length) : stream;
	}

	/**
	 * The {@link #CONTENT_LENGTH} of the request, {@code -1} if it has none or an invalid one.
	 */
	private static long contentLength(ProxyExchange exchange) {
		List<String> values = exchange.headers(CONTENT_LENGTH);
		if (values.size() != 1) {
			return -1;
		}
		try {
			long length = Long.parseLong(values.get(0).strip());
			return length < 0 ? -1 : length;
		} catch (NumberFormatException ex) {
			return -1;
		}
	}

	/**
	 * Sends the request to the upstream without waiting: the answer arrives with its status and
	 * headers, its body is published as it arrives, see
	 * {@link #passBack(HttpResponse, ProxyExchange, AsyncAnswer)}.
	 */
	public static CompletableFuture<HttpResponse<Flow.Publisher<List<ByteBuffer>>>> sendAsync(HttpClient client,
			HttpRequest request) {
		return client.sendAsync(request, HttpResponse.BodyHandlers.ofPublisher());
	}

	/**
	 * Passes status, headers and body of the answer of the upstream back to the client, except the
	 * {@link #DROPPED_RESPONSE_HEADERS}; the body is passed on piece by piece as it arrives, so
	 * that a stream reaches the client while it is produced.
	 *
	 * @param answer
	 *        The {@link ProxyExchange#startAsync() asynchronous answer} of the exchange, completed
	 *        at the end of the body.
	 */
	public static void passBack(HttpResponse<Flow.Publisher<List<ByteBuffer>>> response, ProxyExchange exchange,
			AsyncAnswer answer) throws IOException {
		passBack(response, exchange, answer, (lower, value) -> !DROPPED_RESPONSE_HEADERS.contains(lower));
	}

	/**
	 * Passes status, headers and body of the answer of the upstream back to the client, except the
	 * headers the given filter drops; the body is passed on piece by piece as it arrives, so that a
	 * stream reaches the client while it is produced.
	 *
	 * @param answer
	 *        The {@link ProxyExchange#startAsync() asynchronous answer} of the exchange, completed
	 *        at the end of the body.
	 * @param passed
	 *        Whether the header of the given name (lower case) with the given value is passed
	 *        back; it must drop at least the {@link #HOP_BY_HOP_RESPONSE_HEADERS}.
	 */
	public static void passBack(HttpResponse<Flow.Publisher<List<ByteBuffer>>> response, ProxyExchange exchange,
			AsyncAnswer answer, BiPredicate<String, String> passed) throws IOException {
		for (Map.Entry<String, List<String>> header : response.headers().map().entrySet()) {
			String name = header.getKey();
			String lower = name.toLowerCase(Locale.ROOT);
			if (lower.startsWith(PSEUDO_HEADER_PREFIX)) {
				continue;
			}
			for (String value : header.getValue()) {
				if (passed.test(lower, value)) {
					exchange.addHeader(name, value);
				}
			}
		}
		exchange.sendHeaders(response.statusCode(), -1);
		answer.stream(response.body());
	}

	/**
	 * Drops the body of an answer of the upstream that is not passed back, releasing its
	 * connection.
	 */
	public static void discard(HttpResponse<Flow.Publisher<List<ByteBuffer>>> response) {
		response.body().subscribe(new Flow.Subscriber<>() {
			@Override
			public void onSubscribe(Flow.Subscription subscription) {
				subscription.cancel();
			}

			@Override
			public void onNext(List<ByteBuffer> item) {
				// Cancelled.
			}

			@Override
			public void onError(Throwable throwable) {
				// Dropped anyway.
			}

			@Override
			public void onComplete() {
				// Dropped anyway.
			}
		});
	}

	/**
	 * The failure of an asynchronous send, without the wrapping of the future.
	 */
	public static Throwable cause(Throwable failure) {
		Throwable result = failure;
		while ((result instanceof CompletionException || result instanceof ExecutionException)
			&& result.getCause() != null) {
			result = result.getCause();
		}
		return result;
	}

	/**
	 * Answers with the given body of known length.
	 */
	public static void send(ProxyExchange exchange, int status, String contentType, byte[] body) throws IOException {
		exchange.addHeader(CONTENT_TYPE, contentType);
		exchange.sendHeaders(status, body.length);
		OutputStream out = exchange.responseBody();
		out.write(body);
		out.flush();
	}

	private static Set<String> union(Set<String> headers, String... more) {
		Set<String> result = new HashSet<>(headers);
		result.addAll(Arrays.asList(more));
		return Set.copyOf(result);
	}

	/**
	 * Answers with the given plain text.
	 */
	public static void sendText(ProxyExchange exchange, int status, String message) throws IOException {
		send(exchange, status, TEXT_CONTENT_TYPE, message.getBytes(StandardCharsets.UTF_8));
	}

}
