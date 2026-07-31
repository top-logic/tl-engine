/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.tl.ai.core.tools.web.FilteringProxy.AddressResolver;
import com.top_logic.tl.ai.core.tools.web.FilteringProxy.RequestPolicy;
import com.top_logic.tl.ai.core.tools.web.FilteringProxy.Target;

/**
 * Tests for {@link FilteringProxy} - the outer, authoritative layer of the browser tool's SSRF and
 * domain policy.
 *
 * <p>
 * These tests need no browser and no internet: half of them exercise the policy decision
 * ({@link FilteringProxy#decide}) as a pure function, the other half drive the real proxy over a
 * loopback socket. That is deliberate - the property being tested is not "the tool reports an error"
 * (the pre-fix code did that too) but "<b>the forbidden destination is never contacted</b>", and the
 * only honest way to assert that is to put a server on the forbidden address that counts the TCP
 * connections it accepts (see {@link RecordingServer}) and require the count to stay at zero.
 * </p>
 */
public class TestFilteringProxy extends TestCase {

	/** A policy that permits every domain, so only SSRF can refuse. */
	private static final RequestPolicy PERMIT_ALL = host -> null;

	/** Every address that must never be connected to, in the spellings an attacker would use. */
	private static final List<String> FORBIDDEN_HOSTS = List.of(
		"127.0.0.1", "127.1.2.3", "0.0.0.0", "10.0.0.1", "172.16.0.1", "172.31.255.254", "192.168.1.1",
		"169.254.169.254", "100.64.0.1", "192.0.0.1", "192.0.2.1", "192.88.99.1", "198.18.0.1",
		"198.51.100.1",
		"203.0.113.1", "224.0.0.1", "240.0.0.1", "255.255.255.255",
		"::1", "fe80::1", "fc00::1", "fd00::1", "2001:db8::1", "100::1",
		// IPv6 spellings that embed a private/loopback IPv4 address.
		"::ffff:127.0.0.1", "::ffff:169.254.169.254", "::7f00:1", "2002:7f00:1::", "64:ff9b::7f00:1",
		// A registered name that resolves into a forbidden range - the DNS-rebinding shape.
		"localhost");

	/** Public IP literals, so the "permitted" assertions need no DNS. */
	private static final List<String> PUBLIC_HOSTS = List.of("8.8.8.8", "1.1.1.1", "93.184.216.34");

	// --- Policy decisions (pure; no socket, no browser, no network). ---

	/**
	 * Every non-globally-routable address is refused, in every spelling - dotted quad, IPv6 literal,
	 * the IPv6 transition forms that embed an IPv4 address, and a name that resolves into such a range.
	 * This is the check that makes an <b>IP-literal</b> target (which performs no DNS lookup at all, so
	 * no resolver-level control can see it) as unreachable as a name.
	 */
	public void testEveryNonGlobalAddressIsRefused() {
		for (String host : FORBIDDEN_HOSTS) {
			Target target = FilteringProxy.decide(PERMIT_ALL, host, 80);
			assertFalse("must be refused: " + host, target.permitted());
			assertNull("a refused target carries no address to connect to: " + host, target.address());
			assertNotNull("the refusal names a reason: " + host, target.refusal());
		}
	}

	/** A globally routable address is permitted and pinned to the address that was validated. */
	public void testPublicAddressIsPermittedAndPinned() {
		for (String host : PUBLIC_HOSTS) {
			Target target = FilteringProxy.decide(PERMIT_ALL, host, 443);
			assertTrue("must be permitted: " + host, target.permitted());
			assertEquals("the connection is pinned to the port that was asked for", 443,
				target.address().getPort());
			assertEquals("the connection is pinned to the address that was validated", host,
				target.address().getAddress().getHostAddress());
		}
	}

	/**
	 * SSRF cannot be relaxed by configuration: with {@code allow-any-domains} on - the mode in which
	 * every public domain is reachable - the metadata endpoint, loopback and the private ranges stay
	 * refused.
	 */
	public void testAllowAnyDomainsDoesNotRelaxSsrf() {
		RequestPolicy allowAny = newService(List.of(), List.of(), true, false);
		for (String host : List.of("169.254.169.254", "127.0.0.1", "10.1.2.3", "192.168.0.5")) {
			assertFalse("allow-any must not reach " + host,
				FilteringProxy.decide(allowAny, host, 80).permitted());
		}
	}

	/** The blocklist wins over everything: it refuses even in allow-any mode, and for a public host. */
	public void testBlocklistWinsInEveryMode() {
		RequestPolicy allowAnyButBlocked =
			newService(List.of(), List.of("8.8.8.8"), true, false);
		Target refused = FilteringProxy.decide(allowAnyButBlocked, "8.8.8.8", 80);
		assertFalse("the blocklist refuses a public host even in allow-any mode", refused.permitted());
		assertTrue("names the blocklist: " + refused.refusal(), refused.refusal().contains("blocklist"));

		RequestPolicy allowlistedButBlocked =
			newService(List.of("8.8.8.8"), List.of("8.8.8.8"), false, true);
		assertFalse("the blocklist wins over the allowlist",
			FilteringProxy.decide(allowlistedButBlocked, "8.8.8.8", 80).permitted());
	}

	/**
	 * With the allowlist extended to every request, a host that is not on it is refused fail-closed -
	 * an empty allowlist refuses everything, not nothing.
	 */
	public void testAllowlistIsFailClosedWhenItGovernsEveryRequest() {
		RequestPolicy emptyAllowlist = newService(List.of(), List.of(), false, true);
		assertFalse("an empty allowlist permits nothing",
			FilteringProxy.decide(emptyAllowlist, "8.8.8.8", 80).permitted());

		RequestPolicy allowlisted = newService(List.of("8.8.8.8"), List.of(), false, true);
		assertTrue("an allowlisted host is permitted",
			FilteringProxy.decide(allowlisted, "8.8.8.8", 80).permitted());
		assertFalse("a host that is not on the allowlist is refused",
			FilteringProxy.decide(allowlisted, "1.1.1.1", 80).permitted());
	}

	/**
	 * By default the allowlist stays a <em>top-level-origin</em> control: the proxy applies SSRF and the
	 * blocklist to every connection, but does not refuse a sub-resource host merely for being
	 * off-allowlist - which is what keeps an allowlisted page rendering with its third-party assets.
	 */
	public void testAllowlistDoesNotGovernEveryRequestByDefault() {
		RequestPolicy allowlisted = newService(List.of("8.8.8.8"), List.of(), false, false);
		assertTrue("an off-allowlist sub-resource host is not refused at the proxy by default",
			FilteringProxy.decide(allowlisted, "1.1.1.1", 80).permitted());
		assertFalse("SSRF still applies to every connection",
			FilteringProxy.decide(allowlisted, "127.0.0.1", 80).permitted());
		RequestPolicy blocked = newService(List.of("8.8.8.8"), List.of("1.1.1.1"), false, false);
		assertFalse("the blocklist still applies to every connection",
			FilteringProxy.decide(blocked, "1.1.1.1", 80).permitted());
	}

	/** A request naming no host at all is refused rather than passed on. */
	public void testMissingHostIsRefused() {
		assertFalse(FilteringProxy.decide(PERMIT_ALL, null, 80).permitted());
		assertFalse(FilteringProxy.decide(PERMIT_ALL, "  ", 80).permitted());
		assertFalse("a host of nothing but the root label names no host either",
			FilteringProxy.decide(PERMIT_ALL, ".", 80).permitted());
	}

	/**
	 * <b>The trailing-dot bypass, at the proxy.</b> {@code blocked.example.} is the fully qualified
	 * spelling of a blocklisted host: it resolves to the same server, but it matches neither the
	 * blocklist entry nor a sub-domain of it unless the host is normalised first. The proxy normalises
	 * through the same {@link UrlSafety#normalizeHost(String)} the tool's pre-flight check uses, so
	 * the two layers cannot disagree about which host this is.
	 */
	public void testTrailingDotSpellingIsRefusedByTheDomainPolicy() {
		RequestPolicy blocking = newService(List.of(), List.of("blocked.example"), true, false);

		Target refused = FilteringProxy.decide(blocking, "blocked.example.", 80);
		assertFalse("the fully qualified spelling is the same blocked host", refused.permitted());
		assertTrue("named as a blocklist refusal: " + refused.refusal(),
			refused.refusal().contains("blocklist"));
		assertFalse("a trailing-dot sub-domain of a blocked domain too",
			FilteringProxy.decide(blocking, "www.blocked.example.", 80).permitted());
		assertFalse("upper case plus the root label",
			FilteringProxy.decide(blocking, "BLOCKED.Example.", 443).permitted());
	}

	/**
	 * And the blocked origin is <b>never contacted</b> under that spelling - the property that
	 * distinguishes a real refusal from an error message. The resolver is pinned to a local recording
	 * server, so if the policy failed to recognise the host the proxy would forward to it and the
	 * connection count would rise; it must stay at zero.
	 */
	public void testTrailingDotSpellingNeverReachesTheBlockedOrigin() throws IOException {
		RequestPolicy blocking = newService(List.of(), List.of("blocked.example"), true, false);
		try (RecordingServer victim = RecordingServer.serving("<html>blocked site content</html>");
				FilteringProxy proxy = FilteringProxy.start(blocking, pinnedTo(victim))) {
			String response = exchange(proxy, "GET http://blocked.example./ HTTP/1.1\r\n"
				+ "Host: blocked.example.\r\n\r\n");

			assertTrue("refused with 403: " + statusLine(response), response.startsWith("HTTP/1.1 403"));
			assertEquals("the blocklisted origin was never contacted", 0, victim.contactCount());
			assertFalse("no content from the blocklisted origin leaked",
				response.contains("blocked site content"));

			String tunnel = exchange(proxy, "CONNECT blocked.example.:443 HTTP/1.1\r\n"
				+ "Host: blocked.example.\r\n\r\n");

			assertTrue("the https path is refused too: " + statusLine(tunnel),
				tunnel.startsWith("HTTP/1.1 403"));
			assertFalse("no tunnel was established", tunnel.contains("200 Connection Established"));
			assertEquals("still never contacted", 0, victim.contactCount());
			assertEquals("nothing was forwarded", 0, proxy.forwardedCount());
		}
	}

	// --- The real proxy over a socket: the forbidden destination is never contacted. ---

	/**
	 * A plain-HTTP proxy request for a loopback address is answered {@code 403} and the destination is
	 * <b>never contacted</b>. This is the shape a redirect hop to {@code http://127.0.0.1/} takes once
	 * it reaches the proxy - the hop a Playwright route provably cannot cancel.
	 */
	public void testPlainHttpToLoopbackIsRefusedWithoutContactingIt() throws IOException {
		try (RecordingServer victim = RecordingServer.serving("<html>secret</html>");
				FilteringProxy proxy = FilteringProxy.start(PERMIT_ALL)) {
			String response = exchange(proxy, "GET " + victim.origin() + "/ HTTP/1.1\r\n"
				+ "Host: 127.0.0.1:" + victim.port() + "\r\n\r\n");

			assertTrue("refused with 403: " + statusLine(response), response.startsWith("HTTP/1.1 403"));
			assertEquals("the forbidden destination was never contacted", 0, victim.contactCount());
			assertFalse("no content from the forbidden destination leaked", response.contains("secret"));
			assertEquals(1, proxy.refusedCount());
			assertEquals(0, proxy.forwardedCount());
		}
	}

	/**
	 * A {@code CONNECT} (the https path) to a loopback address is refused <b>before the tunnel is
	 * established</b>, so not even a bare TCP connection reaches the destination.
	 */
	public void testConnectToLoopbackIsRefusedBeforeTheTunnel() throws IOException {
		try (RecordingServer victim = RecordingServer.serving("<html>secret</html>");
				FilteringProxy proxy = FilteringProxy.start(PERMIT_ALL)) {
			String response = exchange(proxy,
				"CONNECT 127.0.0.1:" + victim.port() + " HTTP/1.1\r\n"
					+ "Host: 127.0.0.1:" + victim.port() + "\r\n\r\n");

			assertTrue("refused with 403: " + statusLine(response), response.startsWith("HTTP/1.1 403"));
			assertFalse("no tunnel was established", response.contains("200 Connection Established"));
			assertEquals("the forbidden destination was never contacted", 0, victim.contactCount());
		}
	}

	/**
	 * The cloud-metadata endpoint is refused as an IP literal, in the default configuration and with
	 * {@code allow-any-domains} on alike. An IP literal triggers no DNS lookup, so this is precisely the
	 * target that Chromium's {@code --host-resolver-rules} cannot catch and only the proxy can.
	 */
	public void testMetadataIpLiteralIsRefusedInEveryConfiguration() throws IOException {
		for (RequestPolicy policy : List.of(newService(List.of(), List.of(), false, false),
			newService(List.of(), List.of(), true, false))) {
			try (FilteringProxy proxy = FilteringProxy.start(policy)) {
				String response = exchange(proxy,
					"GET http://169.254.169.254/latest/meta-data/ HTTP/1.1\r\nHost: 169.254.169.254\r\n\r\n");
				assertTrue("metadata refused: " + statusLine(response), response.startsWith("HTTP/1.1 403"));

				String tunnel = exchange(proxy,
					"CONNECT 169.254.169.254:443 HTTP/1.1\r\nHost: 169.254.169.254\r\n\r\n");
				assertTrue("metadata tunnel refused: " + statusLine(tunnel),
					tunnel.startsWith("HTTP/1.1 403"));
			}
		}
	}

	/**
	 * A name that resolves into a private range is refused at connect time - the DNS-rebinding shape.
	 * The destination is resolved once, every answer is validated, and the socket is connected to that
	 * address, so there is no window in which a second lookup could return something else.
	 */
	public void testNameResolvingToPrivateAddressIsRefusedAtConnectTime() throws IOException {
		try (RecordingServer victim = RecordingServer.serving("<html>secret</html>");
				FilteringProxy proxy = FilteringProxy.start(PERMIT_ALL)) {
			String response = exchange(proxy, "GET http://localhost:" + victim.port() + "/ HTTP/1.1\r\n"
				+ "Host: localhost:" + victim.port() + "\r\n\r\n");

			assertTrue("refused with 403: " + statusLine(response), response.startsWith("HTTP/1.1 403"));
			assertEquals("the forbidden destination was never contacted", 0, victim.contactCount());
		}
	}

	/** A URL carrying credentials is refused, exactly as {@link UrlSafety} refuses one. */
	public void testCredentialsInUrlAreRefused() throws IOException {
		try (FilteringProxy proxy = FilteringProxy.start(PERMIT_ALL)) {
			String response = exchange(proxy,
				"GET http://user:secret@8.8.8.8/ HTTP/1.1\r\nHost: 8.8.8.8\r\n\r\n");

			assertTrue("refused with 403: " + statusLine(response), response.startsWith("HTTP/1.1 403"));
			assertTrue("names the reason: " + response, response.contains("credentials"));
			assertFalse("the credentials are not echoed back", response.contains("secret"));
		}
	}

	/** A non-http(s) scheme is refused as policy, not forwarded. */
	public void testNonHttpSchemeIsRefused() throws IOException {
		try (FilteringProxy proxy = FilteringProxy.start(PERMIT_ALL)) {
			String response = exchange(proxy, "GET ftp://8.8.8.8/x HTTP/1.1\r\nHost: 8.8.8.8\r\n\r\n");

			assertTrue("refused with 403: " + statusLine(response), response.startsWith("HTTP/1.1 403"));
			assertEquals(1, proxy.refusedCount());
		}
	}

	/** A request without an absolute target is rejected: this is a forward proxy, not an origin server. */
	public void testOriginFormRequestIsRejected() throws IOException {
		try (FilteringProxy proxy = FilteringProxy.start(PERMIT_ALL)) {
			String response = exchange(proxy, "GET /admin HTTP/1.1\r\nHost: 8.8.8.8\r\n\r\n");

			assertTrue("rejected with 400: " + statusLine(response), response.startsWith("HTTP/1.1 400"));
			assertEquals("a malformed request is not counted as a policy refusal", 0, proxy.refusedCount());
		}
	}

	// --- The forwarding path: allowed traffic still works, and is rewritten correctly. ---

	/**
	 * A permitted request is forwarded to the pinned destination, rewritten from the proxy's
	 * absolute-target form to the origin form a server expects, and the response comes back intact.
	 *
	 * <p>
	 * The address resolver is substituted here (the production one refuses loopback, and every fixture
	 * server a test can reach <em>is</em> on loopback); the transport, the rewrite and the relay under
	 * test are the production ones.
	 * </p>
	 */
	public void testPermittedRequestIsForwardedAndRewritten() throws IOException {
		try (RecordingServer upstream = RecordingServer.serving("<html>fixture body</html>");
				FilteringProxy proxy = FilteringProxy.start(PERMIT_ALL, pinnedTo(upstream))) {
			String response = exchange(proxy, "GET http://fixture.example/a/b?q=1 HTTP/1.1\r\n"
				+ "Host: fixture.example\r\nProxy-Connection: keep-alive\r\nX-Probe: kept\r\n\r\n");

			assertTrue("the response is relayed back: " + statusLine(response),
				response.startsWith("HTTP/1.1 200"));
			assertTrue("the body is relayed back", response.contains("fixture body"));
			assertEquals("the destination was contacted exactly once", 1, upstream.contactCount());
			assertEquals("the request line is rewritten to origin form", "GET /a/b?q=1 HTTP/1.1",
				upstream.requestLines().get(0));

			String head = upstream.heads().get(0);
			assertTrue("the Host header is preserved", head.contains("Host: fixture.example"));
			assertTrue("unrelated headers are preserved", head.contains("X-Probe: kept"));
			assertFalse("the hop-by-hop Proxy-Connection header is stripped",
				head.contains("Proxy-Connection"));
			assertTrue("plain-HTTP connections are made single-request", head.contains("Connection: close"));
			assertEquals(1, proxy.forwardedCount());
			assertEquals(0, proxy.refusedCount());
		}
	}

	/**
	 * A path-less target is forwarded as {@code /}, which is what an HTTP server requires; a bare
	 * {@code GET  HTTP/1.1} would be a malformed request.
	 */
	public void testPathlessTargetIsForwardedAsRoot() throws IOException {
		try (RecordingServer upstream = RecordingServer.serving("<html>root</html>");
				FilteringProxy proxy = FilteringProxy.start(PERMIT_ALL, pinnedTo(upstream))) {
			exchange(proxy, "GET http://fixture.example HTTP/1.1\r\nHost: fixture.example\r\n\r\n");

			assertEquals("GET / HTTP/1.1", upstream.requestLines().get(0));
		}
	}

	/**
	 * A protocol upgrade (plain WebSocket) keeps its {@code Connection: upgrade} / {@code Upgrade}
	 * headers: forcing {@code Connection: close} on it - which is right for an ordinary request, so a
	 * blind relay can never carry a second request for a different origin - would break the handshake
	 * outright. The destination is still policy-checked like any other.
	 */
	public void testUpgradeRequestKeepsItsUpgradeHeaders() throws IOException {
		try (RecordingServer upstream = RecordingServer.serving("<html>ws</html>");
				FilteringProxy proxy = FilteringProxy.start(PERMIT_ALL, pinnedTo(upstream))) {
			exchange(proxy, "GET http://fixture.example/socket HTTP/1.1\r\nHost: fixture.example\r\n"
				+ "Connection: Upgrade\r\nUpgrade: websocket\r\n\r\n");

			String head = upstream.heads().get(0);
			assertTrue("the upgrade is preserved", head.contains("Upgrade: websocket"));
			assertTrue("the upgrade's Connection header is preserved", head.contains("Connection: Upgrade"));
			assertFalse("no Connection: close is forced onto an upgrade", head.contains("Connection: close"));
		}
	}

	/** The refusal history names the host that was refused, for diagnostics and for E2E assertions. */
	public void testRefusalsAreRecordedWithTheirHost() throws IOException {
		try (FilteringProxy proxy = FilteringProxy.start(PERMIT_ALL)) {
			exchange(proxy, "GET http://169.254.169.254/ HTTP/1.1\r\nHost: 169.254.169.254\r\n\r\n");
			exchange(proxy, "CONNECT 10.0.0.1:443 HTTP/1.1\r\nHost: 10.0.0.1\r\n\r\n");

			assertEquals(2, proxy.refusedCount());
			assertEquals(List.of("169.254.169.254", "10.0.0.1"), proxy.refusedHosts());
		}
	}

	// --- Resource bounds. ---

	/** The connection bound is derived from the session cap, and is never zero. */
	public void testConnectionBoundIsDerivedFromTheSessionCap() throws IOException {
		assertTrue("more than one connection per session, or a page cannot load",
			FilteringProxy.maxConnections(1) > 1);
		assertEquals("and it scales with the number of browsers", 2 * FilteringProxy.maxConnections(1),
			FilteringProxy.maxConnections(2));
		assertEquals("a nonsensical cap still leaves a usable proxy", FilteringProxy.maxConnections(1),
			FilteringProxy.maxConnections(0));

		try (FilteringProxy proxy = FilteringProxy.start(PERMIT_ALL, 3)) {
			assertEquals(FilteringProxy.maxConnections(3), proxy.maxConnections());
		}
	}

	/**
	 * <b>The bound holds.</b> The proxy served every connection from an unbounded thread pool, so
	 * anything able to open connections to it - a page with many sub-resource hosts, or any other
	 * process on the loopback interface, since the proxy authenticates nobody - could make it spawn
	 * threads without limit.
	 *
	 * <p>
	 * Past the bound a connection is <b>dropped</b> rather than queued: every task here lives as long as
	 * the connection it serves, so a queued one would not start until some page finished. The fixture
	 * connections say nothing, which parks each handler where a real one waits for the request head and
	 * keeps its slot occupied.
	 * </p>
	 */
	public void testConnectionsBeyondTheBoundAreDroppedRatherThanQueued() throws IOException {
		int bound = FilteringProxy.maxConnections(1);
		List<Socket> clients = new ArrayList<>();
		try (FilteringProxy proxy = FilteringProxy.start(PERMIT_ALL, 1)) {
			try {
				for (int i = 0; i < bound + 4; i++) {
					clients.add(connectTo(proxy.port()));
				}

				assertTrue("connections past the bound of " + bound + " are dropped (dropped "
					+ proxy.droppedCount() + ")", awaitDropped(proxy));
				Socket last = clients.get(clients.size() - 1);
				last.setSoTimeout(5_000);
				assertEquals("a dropped connection is closed, not served", -1,
					last.getInputStream().read());
			} finally {
				for (Socket client : clients) {
					closeQuietly(client);
				}
			}
		}
	}

	/**
	 * <b>An upgrade that was not granted does not get a reused connection.</b> An upgrade request is
	 * exempt from the forced {@code Connection: close}, because a connection that really switched
	 * protocols belongs to one origin for its whole life - but a server may answer an upgrade request
	 * with an ordinary {@code 200}, and then the connection is an ordinary keep-alive connection with the
	 * safeguard left out. A blind pump would deliver the client's next request - which may name a
	 * <em>different</em> origin - to this origin's already-checked server, which is precisely the hazard
	 * {@code Connection: close} exists to remove.
	 */
	public void testUpgradeRefusedByUpstreamDoesNotCarryASecondRequest() throws IOException {
		try (KeepAliveServer upstream = KeepAliveServer.start();
				FilteringProxy proxy = FilteringProxy.start(PERMIT_ALL, pinnedTo(upstream.port()))) {
			try (Socket client = connectTo(proxy.port())) {
				client.setSoTimeout(10_000);
				OutputStream out = client.getOutputStream();
				out.write(("GET http://first.example/socket HTTP/1.1\r\nHost: first.example\r\n"
					+ "Connection: Upgrade\r\nUpgrade: websocket\r\n\r\n")
						.getBytes(StandardCharsets.ISO_8859_1));
				out.flush();

				// Only the head: the connection has to still be open for the second request below,
				// which is the whole point - a browser sends it on the connection it thinks is reusable.
				String head = readHead(client.getInputStream());
				assertTrue("the ordinary answer is relayed back: " + statusLine(head),
					head.startsWith("HTTP/1.1 200"));
				assertTrue("and the client is told the connection ends here: " + head,
					head.contains("Connection: close"));

				try {
					out.write("GET http://second.example/secret HTTP/1.1\r\nHost: second.example\r\n\r\n"
						.getBytes(StandardCharsets.ISO_8859_1));
					out.flush();
				} catch (IOException alreadyClosed) {
					// Equally correct: the proxy had already closed the connection.
				}
			}
			upstream.awaitDone();
			assertEquals("only the upgrade request was ever forwarded: " + upstream.requestLines(),
				List.of("GET /socket HTTP/1.1"), upstream.requestLines());
		}
	}

	/**
	 * A granted upgrade ({@code 101}) is still blind-pumped in both directions, so plain WebSocket keeps
	 * working: the exemption is withdrawn only where the answer says the connection did not switch.
	 */
	public void testGrantedUpgradeIsTunnelledInBothDirections() throws IOException {
		try (KeepAliveServer upstream = KeepAliveServer.switchingProtocols();
				FilteringProxy proxy = FilteringProxy.start(PERMIT_ALL, pinnedTo(upstream.port()))) {
			try (Socket client = connectTo(proxy.port())) {
				client.setSoTimeout(10_000);
				OutputStream out = client.getOutputStream();
				out.write(("GET http://ws.example/socket HTTP/1.1\r\nHost: ws.example\r\n"
					+ "Connection: Upgrade\r\nUpgrade: websocket\r\n\r\n")
						.getBytes(StandardCharsets.ISO_8859_1));
				out.flush();

				String head = readHead(client.getInputStream());
				assertTrue("the switch is relayed verbatim: " + statusLine(head),
					head.startsWith("HTTP/1.1 101"));
				assertFalse("no Connection: close is forced onto a switched connection: " + head,
					head.contains("Connection: close"));

				// The tunnel carries client bytes upstream, as a WebSocket frame would be.
				out.write("ping".getBytes(StandardCharsets.ISO_8859_1));
				out.flush();
				assertTrue("the tunnel is pumped in the client-to-upstream direction too",
					upstream.awaitTunnelledBytes("ping"));
			}
		}
	}

	/** A closed proxy stops listening, so no session can keep using a proxy the service shut down. */
	public void testClosedProxyStopsListening() throws IOException {
		FilteringProxy proxy = FilteringProxy.start(PERMIT_ALL);
		int port = proxy.port();
		proxy.close();

		try (Socket socket = new Socket()) {
			socket.connect(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), port), 2_000);
			// A stale listen backlog can still accept once; what must not happen is a served request.
			socket.setSoTimeout(2_000);
			socket.getOutputStream().write("GET http://8.8.8.8/ HTTP/1.1\r\nHost: 8.8.8.8\r\n\r\n"
				.getBytes(StandardCharsets.ISO_8859_1));
			socket.getOutputStream().flush();
			assertEquals("a closed proxy serves nothing", "", drain(socket.getInputStream()));
		} catch (IOException expected) {
			// Connection refused - equally correct.
		}
	}

	// --- Fixture helpers. ---

	/** An {@link AddressResolver} that pins every host to the fixture server. */
	private static AddressResolver pinnedTo(RecordingServer upstream) {
		return pinnedTo(upstream.port());
	}

	/** An {@link AddressResolver} that pins every host to a fixture on the given loopback port. */
	private static AddressResolver pinnedTo(int fixturePort) {
		return (host, port) -> Target
			.to(new InetSocketAddress(InetAddress.getLoopbackAddress(), fixturePort));
	}

	private static Socket connectTo(int port) throws IOException {
		Socket socket = new Socket();
		socket.connect(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), port), 5_000);
		return socket;
	}

	/** Waits briefly for the proxy to have dropped at least one connection. */
	private static boolean awaitDropped(FilteringProxy proxy) {
		for (int i = 0; i < 100; i++) {
			if (proxy.droppedCount() > 0) {
				return true;
			}
			sleep(50);
		}
		return proxy.droppedCount() > 0;
	}

	/** Reads an HTTP head (status line plus headers), stopping at the blank line that ends it. */
	private static String readHead(InputStream in) throws IOException {
		StringBuilder head = new StringBuilder();
		StringBuilder line = new StringBuilder();
		int read;
		while ((read = in.read()) != -1) {
			if (read == '\n') {
				if (line.length() == 0) {
					break;
				}
				head.append(line).append('\n');
				line.setLength(0);
				continue;
			}
			if (read != '\r') {
				line.append((char) read);
			}
		}
		return head.toString();
	}

	private static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}

	private static void closeQuietly(Socket socket) {
		try {
			socket.close();
		} catch (IOException ex) {
			// Going away either way.
		}
	}

	/**
	 * An upstream fixture that keeps its connection open after answering, so what the proxy does with a
	 * <em>reusable</em> connection can be observed - which {@link RecordingServer} cannot show, since it
	 * answers {@code Connection: close} and hangs up.
	 */
	private static final class KeepAliveServer implements AutoCloseable {

		/** How long the fixture waits for a second request before giving up on one. */
		private static final int SECOND_REQUEST_WINDOW_MILLIS = 1_500;

		private final ServerSocket _listener;

		private final boolean _switchProtocols;

		private final List<String> _requestLines = Collections.synchronizedList(new ArrayList<>());

		private final StringBuilder _tunnelled = new StringBuilder();

		private final Thread _worker;

		private KeepAliveServer(ServerSocket listener, boolean switchProtocols) {
			_listener = listener;
			_switchProtocols = switchProtocols;
			_worker = new Thread(this::serve, "keep-alive-fixture");
			_worker.setDaemon(true);
		}

		/** A fixture answering an upgrade request with an ordinary, keep-alive {@code 200}. */
		static KeepAliveServer start() throws IOException {
			return started(false);
		}

		/** A fixture answering an upgrade request with {@code 101 Switching Protocols}. */
		static KeepAliveServer switchingProtocols() throws IOException {
			return started(true);
		}

		private static KeepAliveServer started(boolean switchProtocols) throws IOException {
			KeepAliveServer server = new KeepAliveServer(
				new ServerSocket(0, 16, InetAddress.getByName("127.0.0.1")), switchProtocols);
			server._worker.start();
			return server;
		}

		int port() {
			return _listener.getLocalPort();
		}

		/** Every request line this fixture received, in order. */
		List<String> requestLines() {
			synchronized (_requestLines) {
				return new ArrayList<>(_requestLines);
			}
		}

		/** Waits for the fixture to have finished serving its connection. */
		void awaitDone() {
			try {
				_worker.join(30_000);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		}

		/** Waits briefly for {@code expected} to arrive through an established tunnel. */
		boolean awaitTunnelledBytes(String expected) {
			for (int i = 0; i < 100; i++) {
				synchronized (_tunnelled) {
					if (_tunnelled.indexOf(expected) >= 0) {
						return true;
					}
				}
				sleep(50);
			}
			return false;
		}

		private void serve() {
			try (Socket client = _listener.accept()) {
				client.setSoTimeout(SECOND_REQUEST_WINDOW_MILLIS);
				InputStream in = client.getInputStream();
				OutputStream out = client.getOutputStream();
				recordHead(in);
				byte[] body = "ok".getBytes(StandardCharsets.UTF_8);
				String head = _switchProtocols
					? "HTTP/1.1 101 Switching Protocols\r\nUpgrade: websocket\r\nConnection: Upgrade\r\n\r\n"
					// No Connection header at all: an ordinary HTTP/1.1 answer is reusable by default,
					// which is the state this fixture exists to create.
					: "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: " + body.length
						+ "\r\n\r\n";
				out.write(head.getBytes(StandardCharsets.ISO_8859_1));
				if (!_switchProtocols) {
					out.write(body);
				}
				out.flush();
				// Stay open for a while: whatever arrives now is what the proxy forwarded onto a
				// connection it had already used for another request.
				if (_switchProtocols) {
					readTunnelled(in);
				} else {
					recordHead(in);
				}
			} catch (IOException ex) {
				// The window closed, or the peer hung up; whatever was recorded is the result.
			}
		}

		private void recordHead(InputStream in) throws IOException {
			String head = readHead(in);
			if (!head.isBlank()) {
				_requestLines.add(head.split("\n")[0].trim());
			}
		}

		private void readTunnelled(InputStream in) throws IOException {
			byte[] buffer = new byte[256];
			int read;
			while ((read = in.read(buffer)) != -1) {
				synchronized (_tunnelled) {
					_tunnelled.append(new String(buffer, 0, read, StandardCharsets.ISO_8859_1));
				}
			}
		}

		@Override
		public void close() {
			try {
				_listener.close();
			} catch (IOException ex) {
				// Going away either way.
			}
		}
	}

	/** Sends one request head to the proxy and returns everything it answers, as text. */
	private static String exchange(FilteringProxy proxy, String head) throws IOException {
		try (Socket socket = new Socket()) {
			socket.connect(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), proxy.port()), 5_000);
			socket.setSoTimeout(10_000);
			OutputStream out = socket.getOutputStream();
			out.write(head.getBytes(StandardCharsets.ISO_8859_1));
			out.flush();
			return drain(socket.getInputStream());
		}
	}

	private static String drain(InputStream in) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		byte[] chunk = new byte[4096];
		int read;
		try {
			while ((read = in.read(chunk)) != -1) {
				buffer.write(chunk, 0, read);
			}
		} catch (IOException ex) {
			// A reset after the response is written is fine; return what arrived.
		}
		return buffer.toString(StandardCharsets.UTF_8);
	}

	private static String statusLine(String response) {
		int end = response.indexOf('\r');
		return end < 0 ? response : response.substring(0, end);
	}

	private static BrowserToolsService newService(List<String> allowedDomains, List<String> blockedDomains,
			boolean allowAny, boolean applyAllowlistToAllRequests) {
		BrowserToolsService.Config<?> config =
			TypedConfiguration.newConfigItem(BrowserToolsService.Config.class);
		config.update(config.descriptor().getProperty(BrowserToolsService.Config.ALLOWED_DOMAINS),
			allowedDomains);
		config.update(config.descriptor().getProperty(BrowserToolsService.Config.BLOCKED_DOMAINS),
			blockedDomains);
		config.update(config.descriptor().getProperty(BrowserToolsService.Config.ALLOW_ANY_DOMAINS),
			allowAny);
		config.update(
			config.descriptor().getProperty(BrowserToolsService.Config.APPLY_ALLOWLIST_TO_ALL_REQUESTS),
			applyAllowlistToAllRequests);
		return new BrowserToolsService(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config);
	}
}
