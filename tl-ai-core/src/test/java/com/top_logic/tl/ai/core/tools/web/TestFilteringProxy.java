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
import java.net.Socket;
import java.nio.charset.StandardCharsets;
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
		"169.254.169.254", "100.64.0.1", "192.0.0.1", "192.0.2.1", "198.18.0.1", "198.51.100.1",
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
		return (host, port) -> Target
			.to(new InetSocketAddress(InetAddress.getLoopbackAddress(), upstream.port()));
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
