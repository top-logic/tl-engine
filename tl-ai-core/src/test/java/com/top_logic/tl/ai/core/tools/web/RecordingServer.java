/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * A minimal loopback HTTP server for the browser-security tests that <b>records whether it was
 * contacted at all</b>.
 *
 * <p>
 * The point of an SSRF test is not "did the tool return an error" - the pre-fix code returned an error
 * too - but "did a packet reach the forbidden destination". So the counter that matters is
 * {@link #contactCount()}, and it counts accepted <em>TCP connections</em>, not parsed HTTP requests:
 * a tunnel that is established and then carries nothing at all has still touched the destination, and
 * must be counted as a breach.
 * </p>
 *
 * <p>
 * Deliberately hand-rolled rather than {@code com.sun.net.httpserver}: this needs accept-level
 * counting and the ability to answer a redirect with a byte-exact response.
 * </p>
 */
final class RecordingServer implements AutoCloseable {

	/** A response: the status line phrase, extra headers and the body. */
	record Reply(int status, String phrase, List<String> headers, byte[] content) {

		/** A response with a text body, sent as UTF-8. */
		Reply(int status, String phrase, List<String> headers, String body) {
			this(status, phrase, headers, body.getBytes(StandardCharsets.UTF_8));
		}

		/** A {@code 200} reply with a binary body of the given type. */
		static Reply binary(String contentType, byte[] content) {
			return new Reply(200, "OK", List.of("Content-Type: " + contentType), content);
		}

		/** A {@code 200} HTML reply. */
		static Reply html(String body) {
			return new Reply(200, "OK", List.of("Content-Type: text/html; charset=utf-8"), body);
		}

		/** A {@code 302} redirect to {@code location}. */
		static Reply redirect(String location) {
			return new Reply(302, "Found", List.of("Location: " + location), "redirecting");
		}
	}

	private final ServerSocket _listener;

	private final Function<String, Reply> _replies;

	private final AtomicInteger _contacts = new AtomicInteger();

	private final List<String> _requestLines = Collections.synchronizedList(new ArrayList<>());

	private final List<String> _heads = Collections.synchronizedList(new ArrayList<>());

	private final List<String> _bodies = Collections.synchronizedList(new ArrayList<>());

	private volatile boolean _closed;

	private RecordingServer(ServerSocket listener, Function<String, Reply> replies) {
		_listener = listener;
		_replies = replies;
	}

	/**
	 * Starts a server on an ephemeral loopback port.
	 *
	 * @param replies
	 *        Maps the request target (the path, or the absolute URL if one is sent) to the reply.
	 * @return The started server.
	 */
	static RecordingServer start(Function<String, Reply> replies) {
		ServerSocket listener;
		try {
			listener = new ServerSocket(0, 16, InetAddress.getByName("127.0.0.1"));
		} catch (IOException ex) {
			throw new RuntimeException("Could not start the recording fixture server.", ex);
		}
		RecordingServer server = new RecordingServer(listener, replies);
		Thread acceptor = new Thread(server::acceptLoop, "recording-fixture-server");
		acceptor.setDaemon(true);
		acceptor.start();
		return server;
	}

	/** A server that always answers with the same HTML body. */
	static RecordingServer serving(String html) {
		return start(target -> Reply.html(html));
	}

	/** The ephemeral loopback port. */
	int port() {
		return _listener.getLocalPort();
	}

	/** {@code http://127.0.0.1:<port>} - the origin a test navigates to (or must never reach). */
	String origin() {
		return "http://127.0.0.1:" + port();
	}

	/**
	 * How many TCP connections reached this server. {@code 0} is the assertion an SSRF test needs: the
	 * forbidden origin was never contacted.
	 */
	int contactCount() {
		return _contacts.get();
	}

	/** The request lines received so far. */
	List<String> requestLines() {
		synchronized (_requestLines) {
			return new ArrayList<>(_requestLines);
		}
	}

	/** The complete request heads received so far (request line and headers, newline separated). */
	List<String> heads() {
		synchronized (_heads) {
			return new ArrayList<>(_heads);
		}
	}

	/**
	 * The request bodies received so far, one per request that announced a {@code Content-Length}.
	 *
	 * <p>
	 * That the body is read <em>before</em> the reply is written is what makes this fixture the shape a
	 * real server has: a proxy that waited for the response head before forwarding the request body
	 * would deadlock against it rather than quietly work.
	 * </p>
	 */
	List<String> bodies() {
		synchronized (_bodies) {
			return new ArrayList<>(_bodies);
		}
	}

	@Override
	public void close() {
		_closed = true;
		try {
			_listener.close();
		} catch (IOException ex) {
			// Going away either way.
		}
	}

	private void acceptLoop() {
		while (!_closed) {
			try (Socket client = _listener.accept()) {
				_contacts.incrementAndGet();
				serve(client);
			} catch (IOException ex) {
				if (!_closed) {
					continue;
				}
				return;
			}
		}
	}

	/** The {@code Content-Length} a request head announces, or {@code 0} when it announces none. */
	private static int announcedLength(String head) {
		for (String line : head.split("\n")) {
			if (line.toLowerCase(Locale.ROOT).startsWith("content-length:")) {
				try {
					return Integer.parseInt(line.substring(line.indexOf(':') + 1).trim());
				} catch (NumberFormatException malformed) {
					return 0;
				}
			}
		}
		return 0;
	}

	/** Reads exactly {@code length} characters, which are bytes because the stream is ISO-8859-1. */
	private static String read(BufferedReader in, int length) throws IOException {
		char[] body = new char[length];
		int filled = 0;
		while (filled < length) {
			int read = in.read(body, filled, length - filled);
			if (read < 0) {
				break;
			}
			filled += read;
		}
		return new String(body, 0, filled);
	}

	private void serve(Socket client) throws IOException {
		client.setSoTimeout(5_000);
		BufferedReader in =
			new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.ISO_8859_1));
		String requestLine = in.readLine();
		if (requestLine == null) {
			return;
		}
		_requestLines.add(requestLine);
		StringBuilder received = new StringBuilder(requestLine);
		String line;
		while ((line = in.readLine()) != null && !line.isEmpty()) {
			received.append('\n').append(line);
		}
		_heads.add(received.toString());
		int bodyLength = announcedLength(received.toString());
		if (bodyLength > 0) {
			_bodies.add(read(in, bodyLength));
		}
		String[] parts = requestLine.split(" ");
		Reply reply = _replies.apply(parts.length > 1 ? parts[1] : "/");
		byte[] body = reply.content();
		StringBuilder head = new StringBuilder();
		head.append("HTTP/1.1 ").append(reply.status()).append(' ').append(reply.phrase()).append("\r\n");
		for (String header : reply.headers()) {
			head.append(header).append("\r\n");
		}
		head.append("Content-Length: ").append(body.length).append("\r\n");
		head.append("Connection: close\r\n\r\n");
		OutputStream out = client.getOutputStream();
		out.write(head.toString().getBytes(StandardCharsets.ISO_8859_1));
		out.write(body);
		out.flush();
	}
}
