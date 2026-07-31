/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import com.top_logic.basic.Logger;

/**
 * A tiny filtering HTTP forward proxy, bound to an ephemeral port on the loopback interface, that
 * every browser session is pinned to - the <b>outer, authoritative</b> layer of the browser tool's
 * SSRF and domain policy.
 *
 * <p>
 * <b>Why this exists.</b> The per-context Playwright security route
 * ({@code BrowserSession.installSecurityRoute}) cannot cancel a <em>redirect</em>: routing is matched
 * once per request, so when a permitted origin answers with a {@code 3xx} pointing at
 * {@code http://169.254.169.254/} (cloud metadata), {@code http://127.0.0.1/} or a private LAN
 * address, Chromium follows the {@code Location} itself and the hop is only <em>observable</em> after
 * the request has already been issued. The route therefore guaranteed "the agent never gets to read
 * forbidden content", not "the browser never touches a forbidden origin". Constraining the browser
 * from <em>outside</em> the routing API closes that: with {@code --proxy-server} pointing here, every
 * connection Chromium makes - top-level navigation, redirect hop, sub-resource, and IP-literal target
 * alike - has to pass this filter before a single packet leaves the machine.
 * </p>
 *
 * <p>
 * <b>How it filters.</b> No TLS interception is needed, because SSRF is a question about the
 * <em>destination</em>, never about the payload:
 * </p>
 * <ul>
 * <li><b>Plain HTTP</b> arrives as a proxy request with an absolute request target
 * ({@code GET http://host/path HTTP/1.1}); the host, port and any embedded credentials are read
 * straight off it.</li>
 * <li><b>HTTPS</b> arrives as {@code CONNECT host:port}; the destination is checked and the
 * connection is then blind-tunnelled, so certificate validation stays entirely between Chromium and
 * the real server.</li>
 * </ul>
 *
 * <p>
 * <b>DNS pinning.</b> {@link UrlSafety#resolveHost(String)} resolves the destination once, refuses it
 * unless <em>every</em> resolved address is globally routable, and returns the address to use; the
 * upstream socket is then connected to <em>that address</em>. Re-connecting by name would re-run the
 * lookup, and a rebinding host that answers public on the first query and private on the second would
 * slip through that TOCTOU window.
 * </p>
 *
 * <p>
 * <b>Precedence.</b> Exactly the order the tool documents: SSRF is unconditional, the administrator
 * blocklist always applies, and the allowlist is consulted only when the operator has asked for it to
 * govern sub-resources too (see {@code BrowserToolsService.Config#isApplyAllowlistToAllRequests()});
 * {@code allow-any-domains} relaxes only the allowlist. A refused plain-HTTP request is answered
 * {@code 403}, a refused {@code CONNECT} is refused before the tunnel is established - the forbidden
 * origin is never contacted at all. The route, the redirect guard and the final-URL re-validation stay
 * in place as defense in depth and are what turn a refusal here into the tool's clean policy message.
 * </p>
 *
 * <p>
 * <b>Connection handling.</b> A permitted request is forwarded verbatim apart from the request line
 * (rewritten to origin form) and the hop-by-hop {@code Connection} / {@code Proxy-Connection}
 * headers, then both directions are pumped byte-for-byte, so an allowed page pays only one extra
 * loopback hop. Plain-HTTP connections are made single-request ({@code Connection: close} upstream):
 * a proxy connection may otherwise be re-used for a <em>different</em> origin, and a blind pump would
 * then deliver the second request to the first request's server. A protocol upgrade
 * ({@code Upgrade:} / {@code Connection: upgrade}, i.e. plain WebSocket) is exempt from that and
 * forwarded as-is, since an upgraded connection belongs to one origin by definition.
 * </p>
 *
 * <p>
 * <b>Why not {@code --host-resolver-rules}.</b> Chromium can be told to blackhole names
 * ({@code MAP * ~NOTFOUND}), but it only ever affects <em>name resolution</em>: an IP-literal target
 * performs no lookup, so {@code http://169.254.169.254/} would sail straight past it. Worse, once a
 * proxy is configured Chromium stops resolving names altogether (it hands the name to the proxy), so
 * the flag is not even consulted for proxied requests. It can supplement this proxy but can never
 * replace it.
 * </p>
 */
final class FilteringProxy implements AutoCloseable {

	/**
	 * The administrator-owned domain policy, as the proxy needs it: one question per outbound
	 * connection, regardless of what kind of request it belongs to.
	 *
	 * <p>
	 * Kept separate from {@link BrowserSession.DomainPolicy} (which answers only about a
	 * <em>top-level</em> navigation) because the proxy cannot tell a main document from a
	 * sub-resource: all it sees is a destination.
	 * </p>
	 */
	interface RequestPolicy {

		/**
		 * The reason a connection to {@code host} is refused by the domain policy, or {@code null}
		 * when it is permitted.
		 *
		 * <p>
		 * SSRF is <em>not</em> this method's business - it is enforced unconditionally by the proxy
		 * itself via {@link UrlSafety} - so an implementation only decides blocklist and (where it
		 * applies) allowlist membership.
		 * </p>
		 *
		 * @param host
		 *        The destination host of the connection (a name or an IP literal).
		 */
		String refuseRequest(String host);
	}

	/**
	 * Resolves a destination host to the socket address the proxy must connect to, refusing anything
	 * that is not globally routable.
	 *
	 * <p>
	 * The production implementation is {@link #ssrfResolver()}. It is an injectable seam only so that
	 * the forwarding path can be exercised locally in a test (where every reachable fixture server is,
	 * necessarily, on a loopback address the real resolver refuses).
	 * </p>
	 */
	interface AddressResolver {

		/**
		 * Resolves and validates {@code host}.
		 *
		 * @param host
		 *        The destination host.
		 * @param port
		 *        The destination port.
		 * @return The pinned target, or a refusal; never {@code null}.
		 */
		Target resolve(String host, int port);
	}

	/**
	 * Where one outbound connection may be made, or why it may not.
	 *
	 * @param address
	 *        The pinned socket address to connect to, or {@code null} when refused.
	 * @param refusal
	 *        The human-readable refusal reason, or {@code null} when permitted.
	 */
	record Target(InetSocketAddress address, String refusal) {

		/** A permitted target. */
		static Target to(InetSocketAddress address) {
			return new Target(address, null);
		}

		/** A refused target carrying the reason. */
		static Target refused(String refusal) {
			return new Target(null, refusal);
		}

		/** Whether the connection may be made. */
		boolean permitted() {
			return refusal == null;
		}
	}

	/**
	 * Response header naming this proxy as the layer that refused a request, and why.
	 *
	 * <p>
	 * A {@code 403} the proxy writes is a perfectly ordinary response as far as the browser is
	 * concerned, so a refused <em>top-level</em> request would otherwise commit the refusal page as the
	 * document and the agent would read the proxy's HTML as page content. This header lets
	 * {@code BrowserSession.installProxyRefusalGuard} recognise it and report the policy instead. Lower
	 * case because that is how header names arrive at the Playwright API.
	 * </p>
	 */
	static final String REFUSAL_HEADER = "x-tl-browser-policy-refused";

	/** How long the proxy waits for the client's request head before giving up on the connection. */
	private static final int HEADER_READ_TIMEOUT_MILLIS = 30_000;

	/** How long the proxy waits for the upstream TCP connect. */
	private static final int CONNECT_TIMEOUT_MILLIS = 15_000;

	/** Hard cap on the request head, so a client cannot make the proxy buffer without bound. */
	private static final int MAX_HEAD_BYTES = 64 * 1024;

	/** Hard cap on a single header line. */
	private static final int MAX_LINE_BYTES = 8 * 1024;

	private static final int PUMP_BUFFER_BYTES = 16 * 1024;

	/** How many refused hosts are remembered for diagnostics and tests. */
	private static final int REFUSAL_HISTORY = 64;

	/** Hop-by-hop headers the proxy must not pass on. */
	private static final List<String> HOP_BY_HOP =
		List.of("proxy-connection", "connection", "keep-alive", "proxy-authorization", "te", "trailer");

	private final ServerSocket _listener;

	private final RequestPolicy _policy;

	private final AddressResolver _resolver;

	private final ExecutorService _connections;

	/** Every socket currently open, so {@link #close()} can unblock threads pumping a live tunnel. */
	private final Set<Closeable> _open = ConcurrentHashMap.newKeySet();

	private final AtomicLong _refusedCount = new AtomicLong();

	private final AtomicLong _forwardedCount = new AtomicLong();

	private final Deque<String> _refusedHosts = new ArrayDeque<>();

	private volatile boolean _closed;

	private FilteringProxy(ServerSocket listener, RequestPolicy policy, AddressResolver resolver) {
		_listener = listener;
		_policy = policy;
		_resolver = resolver;
		_connections = Executors.newCachedThreadPool(runnable -> {
			Thread thread = new Thread(runnable, "browser-filter-proxy");
			thread.setDaemon(true);
			return thread;
		});
	}

	/**
	 * Starts a proxy enforcing SSRF (via {@link UrlSafety}) plus the given domain policy.
	 *
	 * @param policy
	 *        The administrator-owned domain policy to apply to every connection.
	 * @return The started proxy, listening on an ephemeral loopback port.
	 */
	static FilteringProxy start(RequestPolicy policy) {
		return start(policy, ssrfResolver());
	}

	/**
	 * Starts a proxy with an explicit {@link AddressResolver}.
	 *
	 * @param policy
	 *        The administrator-owned domain policy to apply to every connection.
	 * @param resolver
	 *        How destinations are resolved and SSRF-checked; production code passes
	 *        {@link #ssrfResolver()}.
	 * @return The started proxy, listening on an ephemeral loopback port.
	 */
	static FilteringProxy start(RequestPolicy policy, AddressResolver resolver) {
		ServerSocket listener;
		try {
			// Loopback-only and ephemeral: nothing outside this host can reach the proxy, and no
			// fixed port has to be reserved for it.
			listener = new ServerSocket(0, 64, InetAddress.getByName("127.0.0.1"));
		} catch (IOException ex) {
			throw new RuntimeException("Could not start the browser filtering proxy on the loopback "
				+ "interface; the browser tool cannot enforce its SSRF policy without it.", ex);
		}
		FilteringProxy proxy = new FilteringProxy(listener, policy, resolver);
		Thread acceptor = new Thread(proxy::acceptLoop, "browser-filter-proxy-accept");
		acceptor.setDaemon(true);
		acceptor.start();
		return proxy;
	}

	/** The production {@link AddressResolver}: resolve once, refuse non-global addresses, pin the result. */
	static AddressResolver ssrfResolver() {
		return (host, port) -> {
			UrlSafety.HostResolution resolution = UrlSafety.resolveHost(host);
			if (!resolution.permitted()) {
				return Target.refused(resolution.refusal());
			}
			return Target.to(new InetSocketAddress(resolution.address(), port));
		};
	}

	/**
	 * The {@code --proxy-server} value pinning a browser to this proxy.
	 *
	 * @see BrowserSession#buildLaunchOptions(boolean, boolean, String, boolean)
	 */
	String serverUrl() {
		return "http://127.0.0.1:" + port();
	}

	/** The ephemeral loopback port this proxy listens on. */
	int port() {
		return _listener.getLocalPort();
	}

	/** How many connections this proxy has refused. */
	long refusedCount() {
		return _refusedCount.get();
	}

	/** How many connections this proxy has forwarded. */
	long forwardedCount() {
		return _forwardedCount.get();
	}

	/** The most recently refused hosts (at most {@link #REFUSAL_HISTORY}), oldest first. */
	List<String> refusedHosts() {
		synchronized (_refusedHosts) {
			return new ArrayList<>(_refusedHosts);
		}
	}

	/**
	 * The policy decision for one outbound connection, applied to <em>every</em> connection the
	 * browser makes.
	 *
	 * <p>
	 * The domain policy is asked first because it is a pure, in-memory decision: a blocklisted host
	 * is refused without even performing (and thereby leaking) a DNS lookup for it. Which of the two
	 * layers speaks first cannot change the outcome - both only ever refuse - and SSRF stays
	 * unconditional either way.
	 * </p>
	 *
	 * @param policy
	 *        The domain policy to apply.
	 * @param resolver
	 *        The SSRF-checking resolver.
	 * @param host
	 *        The destination host.
	 * @param port
	 *        The destination port.
	 * @return The pinned target, or a refusal; never {@code null}.
	 */
	static Target decide(RequestPolicy policy, AddressResolver resolver, String host, int port) {
		if (host == null || host.isBlank()) {
			return Target.refused("the request names no host.");
		}
		// Lower-case and drop the trailing dot of the fully qualified form, through the SAME
		// normalisation the tool's pre-flight check and UrlSafety use - otherwise "blocked.example.com."
		// would reach the domain policy as a host it does not recognise and be forwarded.
		String target = UrlSafety.normalizeHost(host);
		// java.net.URI keeps the brackets of an IPv6 literal while a CONNECT target arrives without
		// them; normalize so the policy and the resolver always see the same spelling.
		if (target.length() > 1 && target.charAt(0) == '[' && target.charAt(target.length() - 1) == ']') {
			target = target.substring(1, target.length() - 1);
		}
		if (target.isEmpty()) {
			return Target.refused("the request names no host.");
		}
		String refusal = policy.refuseRequest(target);
		if (refusal != null) {
			return Target.refused(refusal);
		}
		return resolver.resolve(target, port);
	}

	/**
	 * The policy decision for one outbound connection, using the production SSRF resolver.
	 *
	 * @see #decide(RequestPolicy, AddressResolver, String, int)
	 */
	static Target decide(RequestPolicy policy, String host, int port) {
		return decide(policy, ssrfResolver(), host, port);
	}

	@Override
	public void close() {
		_closed = true;
		closeQuietly(_listener);
		// Close every live socket so threads blocked in a tunnel pump return instead of lingering
		// until their peer happens to hang up.
		for (Closeable socket : _open) {
			closeQuietly(socket);
		}
		_open.clear();
		_connections.shutdownNow();
	}

	private void acceptLoop() {
		while (!_closed) {
			Socket client;
			try {
				client = _listener.accept();
			} catch (IOException ex) {
				if (!_closed) {
					Logger.warn("The browser filtering proxy stopped accepting connections.", ex,
						FilteringProxy.class);
				}
				return;
			}
			try {
				_connections.execute(() -> handle(client));
			} catch (RuntimeException ex) {
				// Shutting down, or out of threads: drop the connection rather than leak it.
				closeQuietly(client);
			}
		}
	}

	private void handle(Socket client) {
		_open.add(client);
		try {
			client.setTcpNoDelay(true);
			client.setSoTimeout(HEADER_READ_TIMEOUT_MILLIS);
			BufferedInputStream in = new BufferedInputStream(client.getInputStream(), PUMP_BUFFER_BYTES);
			OutputStream out = client.getOutputStream();

			String requestLine = readLine(in);
			if (requestLine == null || requestLine.isBlank()) {
				return;
			}
			String[] parts = requestLine.split(" ");
			if (parts.length < 3) {
				respond(out, 400, "Bad Request", "The proxy could not parse the request line.");
				return;
			}
			List<String> headers = readHeaders(in);
			if ("CONNECT".equalsIgnoreCase(parts[0])) {
				tunnel(client, in, out, parts[1]);
			} else {
				forward(client, in, out, parts[0], parts[1], parts[2], headers);
			}
		} catch (IOException ex) {
			// A client that hung up, or a peer that reset: nothing to report, the browser will.
		} catch (RuntimeException ex) {
			Logger.warn("The browser filtering proxy failed to handle a connection.", ex,
				FilteringProxy.class);
		} finally {
			_open.remove(client);
			closeQuietly(client);
		}
	}

	/** Handles {@code CONNECT host:port} - the HTTPS path; checked, then blind-tunnelled. */
	private void tunnel(Socket client, InputStream in, OutputStream out, String authority)
			throws IOException {
		String host = hostOf(authority);
		int port = portOf(authority, 443);
		if (host == null || port < 0) {
			respond(out, 400, "Bad Request", "The proxy could not parse the CONNECT target.");
			return;
		}
		Target target = decide(_policy, _resolver, host, port);
		if (!target.permitted()) {
			refuse(out, host, target.refusal());
			return;
		}
		Socket upstream = connect(out, target, host);
		if (upstream == null) {
			return;
		}
		relay(client, in, out, upstream,
			"HTTP/1.1 200 Connection Established\r\n\r\n".getBytes(StandardCharsets.ISO_8859_1), null);
	}

	/**
	 * Handles a plain-HTTP proxy request ({@code GET http://host/path HTTP/1.1}) - checked, rewritten
	 * to origin form, then relayed.
	 */
	private void forward(Socket client, InputStream in, OutputStream out, String method, String target,
			String version, List<String> headers) throws IOException {
		URI uri;
		try {
			uri = new URI(target);
		} catch (Exception ex) {
			respond(out, 400, "Bad Request", "The proxy could not parse the request target.");
			return;
		}
		String scheme = uri.getScheme();
		if (scheme == null || !uri.isAbsolute()) {
			// Someone is talking to the proxy as if it were an origin server; it serves nothing.
			respond(out, 400, "Bad Request",
				"This is a forward proxy; requests must carry an absolute target.");
			return;
		}
		if (scheme.equalsIgnoreCase("https")) {
			// Forwarding https in absolute form would mean terminating TLS here; Chromium never does
			// this (it sends CONNECT), so this is a client error rather than a policy refusal.
			respond(out, 400, "Bad Request", "https requests must be tunnelled with CONNECT.");
			return;
		}
		if (!scheme.equalsIgnoreCase("http")) {
			// ftp:, gopher:, file: and friends: refused as policy, exactly as UrlSafety refuses them.
			refuse(out, uri.getHost(), "only http and https URLs are supported.");
			return;
		}
		if (uri.getUserInfo() != null) {
			refuse(out, uri.getHost(), "URLs containing credentials are not allowed.");
			return;
		}
		String host = uri.getHost();
		int port = uri.getPort() < 0 ? 80 : uri.getPort();
		Target pinned = decide(_policy, _resolver, host, port);
		if (!pinned.permitted()) {
			refuse(out, host, pinned.refusal());
			return;
		}
		Socket upstream = connect(out, pinned, host);
		if (upstream == null) {
			return;
		}
		relay(client, in, out, upstream, null,
			head(method, originForm(uri), version, headers).getBytes(StandardCharsets.ISO_8859_1));
	}

	/**
	 * The request head to send upstream: the request line rewritten to origin form, the client's
	 * headers minus the hop-by-hop ones, and - unless this is a protocol upgrade - an explicit
	 * {@code Connection: close}.
	 *
	 * <p>
	 * Forcing the connection closed is what makes the byte-for-byte relay below safe: an HTTP proxy
	 * connection may carry consecutive requests for <em>different</em> origins, and a blind pump
	 * would send the second one to the first one's (already policy-checked) server. One request per
	 * upstream connection removes that possibility entirely, at the cost of plain-HTTP keep-alive -
	 * which real traffic barely uses, since https is tunnelled per origin anyway. An upgrade request
	 * is exempt: its connection belongs to a single origin for its whole life, and stripping the
	 * upgrade headers would break plain WebSocket outright.
	 * </p>
	 */
	private static String head(String method, String target, String version, List<String> headers) {
		boolean upgrade = isUpgrade(headers);
		StringBuilder head = new StringBuilder();
		head.append(method).append(' ').append(target).append(' ').append(version).append("\r\n");
		for (String header : headers) {
			String name = nameOf(header);
			if (HOP_BY_HOP.contains(name) && !(upgrade && ("connection".equals(name) || "te".equals(name)))) {
				continue;
			}
			head.append(header).append("\r\n");
		}
		if (!upgrade) {
			head.append("Connection: close\r\n");
		}
		head.append("\r\n");
		return head.toString();
	}

	private static boolean isUpgrade(List<String> headers) {
		for (String header : headers) {
			if ("upgrade".equals(nameOf(header))) {
				return true;
			}
		}
		return false;
	}

	private static String nameOf(String header) {
		int colon = header.indexOf(':');
		return (colon < 0 ? header : header.substring(0, colon)).trim().toLowerCase(Locale.ROOT);
	}

	private static String originForm(URI uri) {
		StringBuilder form = new StringBuilder();
		String path = uri.getRawPath();
		form.append(path == null || path.isEmpty() ? "/" : path);
		if (uri.getRawQuery() != null) {
			form.append('?').append(uri.getRawQuery());
		}
		return form.toString();
	}

	/**
	 * Connects to the <em>pinned</em> address, answering the client with a {@code 502} when the
	 * destination cannot be reached. Connecting to the address rather than re-resolving the name is
	 * what closes the DNS-rebinding window.
	 */
	private Socket connect(OutputStream out, Target target, String host) throws IOException {
		Socket upstream = new Socket();
		try {
			upstream.setTcpNoDelay(true);
			upstream.connect(target.address(), CONNECT_TIMEOUT_MILLIS);
		} catch (IOException ex) {
			closeQuietly(upstream);
			respond(out, 502, "Bad Gateway", "The proxy could not reach " + host + ".");
			return null;
		}
		_open.add(upstream);
		return upstream;
	}

	/**
	 * Takes over a connected pair: writes the opening bytes each side is owed, then pumps both
	 * directions byte-for-byte until either side hangs up - upstream-to-client on this thread,
	 * client-to-upstream on a second one. Owns the teardown of <em>both</em> sockets, including when a
	 * greeting cannot be written, so no socket can be left behind in {@link #_open}; closing them is
	 * also what unblocks the second thread.
	 *
	 * @param clientGreeting
	 *        Bytes to send to the client first (the {@code CONNECT} success line), or {@code null}.
	 * @param upstreamHead
	 *        Bytes to send upstream first (the rewritten request head), or {@code null}.
	 */
	private void relay(Socket client, InputStream in, OutputStream out, Socket upstream,
			byte[] clientGreeting, byte[] upstreamHead) {
		try {
			// A tunnel or a slow-trickling response must not be killed by the head-read timeout.
			client.setSoTimeout(0);
			upstream.setSoTimeout(0);
			if (upstreamHead != null) {
				upstream.getOutputStream().write(upstreamHead);
				upstream.getOutputStream().flush();
			}
			if (clientGreeting != null) {
				out.write(clientGreeting);
				out.flush();
			}
		} catch (IOException ex) {
			_open.remove(upstream);
			closeQuietly(upstream);
			return;
		}
		_forwardedCount.incrementAndGet();
		_connections.execute(() -> {
			try {
				copy(in, upstream.getOutputStream());
				upstream.shutdownOutput();
			} catch (IOException | RuntimeException ex) {
				// The client hung up or the tunnel was closed; the other direction handles teardown.
			}
		});
		try {
			copy(upstream.getInputStream(), out);
		} catch (IOException ex) {
			// Upstream reset or the client went away mid-response.
		} finally {
			_open.remove(upstream);
			closeQuietly(upstream);
			closeQuietly(client);
		}
	}

	private static void copy(InputStream from, OutputStream to) throws IOException {
		byte[] buffer = new byte[PUMP_BUFFER_BYTES];
		int read;
		while ((read = from.read(buffer)) != -1) {
			to.write(buffer, 0, read);
			to.flush();
		}
	}

	/** Records and answers a refusal: a {@code 403} the forbidden origin never learns about. */
	private void refuse(OutputStream out, String host, String reason) throws IOException {
		_refusedCount.incrementAndGet();
		synchronized (_refusedHosts) {
			if (_refusedHosts.size() >= REFUSAL_HISTORY) {
				_refusedHosts.removeFirst();
			}
			_refusedHosts.addLast(host == null ? "" : host);
		}
		Logger.info("Browser filtering proxy refused a connection to " + host + ": " + reason,
			FilteringProxy.class);
		respond(out, 403, "Forbidden", "Refused by the browser security policy: " + reason,
			REFUSAL_HEADER + ": " + oneLine(reason));
	}

	private static void respond(OutputStream out, int status, String phrase, String body,
			String... extraHeaders) throws IOException {
		byte[] bytes = ("<!doctype html><html><body><p>" + escape(body) + "</p></body></html>")
			.getBytes(StandardCharsets.UTF_8);
		StringBuilder head = new StringBuilder();
		head.append("HTTP/1.1 ").append(status).append(' ').append(phrase).append("\r\n");
		for (String header : extraHeaders) {
			head.append(header).append("\r\n");
		}
		head.append("Content-Type: text/html; charset=utf-8\r\n");
		head.append("Content-Length: ").append(bytes.length).append("\r\n");
		head.append("Connection: close\r\n\r\n");
		out.write(head.toString().getBytes(StandardCharsets.ISO_8859_1));
		out.write(bytes);
		out.flush();
	}

	private static String escape(String text) {
		return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}

	/** A reason reduced to a single header-safe line (no CR/LF, so no header injection). */
	private static String oneLine(String reason) {
		return reason.replace('\r', ' ').replace('\n', ' ').strip();
	}

	/** The host part of an {@code host:port} authority, unbracketing an IPv6 literal. */
	private static String hostOf(String authority) {
		if (authority == null || authority.isEmpty()) {
			return null;
		}
		if (authority.charAt(0) == '[') {
			int end = authority.indexOf(']');
			return end < 0 ? null : authority.substring(1, end);
		}
		int colon = authority.lastIndexOf(':');
		String host = colon < 0 ? authority : authority.substring(0, colon);
		return host.isEmpty() ? null : host;
	}

	/** The port part of an {@code host:port} authority, or {@code fallback} when absent, or {@code -1}. */
	private static int portOf(String authority, int fallback) {
		if (authority == null) {
			return -1;
		}
		int colon = authority.charAt(0) == '[' ? authority.indexOf(':', authority.indexOf(']'))
			: authority.lastIndexOf(':');
		if (colon < 0) {
			return fallback;
		}
		try {
			int port = Integer.parseInt(authority.substring(colon + 1));
			return port > 0 && port <= 65535 ? port : -1;
		} catch (NumberFormatException ex) {
			return -1;
		}
	}

	/** Reads one CRLF-terminated line, or {@code null} at end of stream. */
	private static String readLine(InputStream in) throws IOException {
		StringBuilder line = new StringBuilder();
		int read;
		while ((read = in.read()) != -1) {
			if (read == '\n') {
				int end = line.length();
				if (end > 0 && line.charAt(end - 1) == '\r') {
					line.setLength(end - 1);
				}
				return line.toString();
			}
			if (line.length() >= MAX_LINE_BYTES) {
				throw new IOException("Request header line exceeds " + MAX_LINE_BYTES + " bytes.");
			}
			line.append((char) read);
		}
		return line.length() == 0 ? null : line.toString();
	}

	/** Reads the header lines up to (and consuming) the blank line that ends the request head. */
	private static List<String> readHeaders(InputStream in) throws IOException {
		List<String> headers = new ArrayList<>();
		int total = 0;
		String line;
		while ((line = readLine(in)) != null && !line.isEmpty()) {
			total += line.length();
			if (total > MAX_HEAD_BYTES) {
				throw new IOException("Request head exceeds " + MAX_HEAD_BYTES + " bytes.");
			}
			headers.add(line);
		}
		return headers;
	}

	private static void closeQuietly(Closeable closeable) {
		try {
			closeable.close();
		} catch (IOException ex) {
			// Nothing to do; the socket is going away either way.
		}
	}
}
