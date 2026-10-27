/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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
	 * Request headers (lower case) that are never forwarded: the credentials of the proxy, cookies
	 * of the control application, hop-by-hop headers, and the headers the HTTP client sets itself.
	 */
	public static final Set<String> DROPPED_REQUEST_HEADERS = Set.of(
		AUTHORIZATION.toLowerCase(Locale.ROOT), "cookie", "host", CONTENT_LENGTH.toLowerCase(Locale.ROOT), "expect",
		"connection", "keep-alive", "proxy-authenticate", "proxy-authorization", "proxy-connection", "te",
		"trailer", "transfer-encoding", "upgrade", "http2-settings");

	/**
	 * Response headers (lower case) that are not passed back: hop-by-hop headers, the length (the
	 * server chooses the transfer), and cookies of the upstream.
	 */
	public static final Set<String> DROPPED_RESPONSE_HEADERS = Set.of(
		CONTENT_LENGTH.toLowerCase(Locale.ROOT), "set-cookie", "connection", "keep-alive", "proxy-authenticate",
		"proxy-authorization", "proxy-connection", "te", "trailer", "transfer-encoding", "upgrade");

	private static final String PSEUDO_HEADER_PREFIX = ":";

	private static final int BUFFER_SIZE = 8 * 1024;

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
		for (String name : exchange.headerNames()) {
			String lower = name.toLowerCase(Locale.ROOT);
			if (DROPPED_REQUEST_HEADERS.contains(lower) || alsoDropped.contains(lower)) {
				continue;
			}
			for (String value : exchange.headers(name)) {
				request.header(name, value);
			}
		}
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
	 * Passes status, headers and body of the answer of the upstream back to the client, except the
	 * {@link #DROPPED_RESPONSE_HEADERS}; the body is flushed piece by piece as it arrives, so that
	 * a stream reaches the client while it is produced.
	 */
	public static void sendResponse(HttpResponse<InputStream> response, ProxyExchange exchange) throws IOException {
		try (InputStream in = response.body()) {
			for (Map.Entry<String, List<String>> header : response.headers().map().entrySet()) {
				String name = header.getKey();
				String lower = name.toLowerCase(Locale.ROOT);
				if (lower.startsWith(PSEUDO_HEADER_PREFIX) || DROPPED_RESPONSE_HEADERS.contains(lower)) {
					continue;
				}
				for (String value : header.getValue()) {
					exchange.addHeader(name, value);
				}
			}
			exchange.sendHeaders(response.statusCode(), -1);
			OutputStream out = exchange.responseBody();
			byte[] buffer = new byte[BUFFER_SIZE];
			int length;
			while ((length = in.read(buffer)) >= 0) {
				out.write(buffer, 0, length);
				out.flush();
			}
		}
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

	/**
	 * Answers with the given plain text.
	 */
	public static void sendText(ProxyExchange exchange, int status, String message) throws IOException {
		send(exchange, status, TEXT_CONTENT_TYPE, message.getBytes(StandardCharsets.UTF_8));
	}

}
