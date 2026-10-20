/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import static com.top_logic.tl.ai.core.tools.web.TestBrowsingPolicy.origin;
import static com.top_logic.tl.ai.core.tools.web.TestBrowsingPolicy.policy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.tl.ai.core.tools.web.BrowserToolsService.GovernedPolicy;
import com.top_logic.tl.ai.core.tools.web.BrowsingPolicyConfig.Access;
import com.top_logic.tl.ai.core.tools.web.FilteringProxy.AddressResolver;
import com.top_logic.tl.ai.core.tools.web.FilteringProxy.Target;
import com.top_logic.tl.ai.core.tools.web.WebSearchTool.SearchScope;

/**
 * Tests that the browsing governance of issue #347 is actually <b>enforced</b>, at each of the three
 * places a run can leave the application.
 *
 * <p>
 * The proxy half is driven over a real loopback socket, with the destination pinned to a server that
 * counts the TCP connections it accepts: the property worth asserting is not "the tool answered with
 * an error" but "<b>the off-policy origin was never contacted</b>", and only a counting server can
 * tell the two apart. The search half is asserted as a pure function, because what has to hold there
 * is an algebraic property - a model-supplied domain list can only remove origins, never add one.
 * </p>
 *
 * @see TestBrowsingPolicy
 */
public class TestBrowsingPolicyEnforcement extends TestCase {

	/** The configuration items need the module system for their nested type resolution. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestBrowsingPolicyEnforcement.class)));
	}

	// --- The filtering proxy: the outer, authoritative layer. ---

	/**
	 * An origin the browsing policy refuses is answered {@code 403} and <b>never contacted</b>, even
	 * though the installation's own lists permit every domain.
	 */
	public void testTheProxyRefusesAnOffPolicyOriginWithoutContactingIt() throws IOException {
		BrowsingPolicy browsing =
			BrowsingPolicy.of(policy("research", Access.DENY, origin("docs.example.com", Access.ALLOW)));
		GovernedPolicy governed = governed(allowAnyDomains(), browsing);

		try (RecordingServer victim = RecordingServer.serving("<html>off-policy content</html>");
				FilteringProxy proxy = FilteringProxy.start(governed, pinnedTo(victim))) {
			String response = exchange(proxy, "GET http://other.example.com/ HTTP/1.1\r\n"
				+ "Host: other.example.com\r\n\r\n");

			assertTrue("refused with 403: " + statusLine(response), response.startsWith("HTTP/1.1 403"));
			assertEquals("the off-policy origin was never contacted", 0, victim.contactCount());
			assertFalse("nothing from it leaked", response.contains("off-policy content"));
			assertEquals(1, proxy.refusedCount());
			assertEquals(0, proxy.forwardedCount());
		}
	}

	/** The https path is refused before the tunnel, so not even a bare TCP connection is made. */
	public void testTheProxyRefusesAnOffPolicyTunnel() throws IOException {
		BrowsingPolicy browsing =
			BrowsingPolicy.of(policy("research", Access.DENY, origin("docs.example.com", Access.ALLOW)));

		try (RecordingServer victim = RecordingServer.serving("<html>off-policy content</html>");
				FilteringProxy proxy =
					FilteringProxy.start(governed(allowAnyDomains(), browsing), pinnedTo(victim))) {
			String response = exchange(proxy, "CONNECT other.example.com:443 HTTP/1.1\r\n"
				+ "Host: other.example.com\r\n\r\n");

			assertTrue("refused with 403: " + statusLine(response), response.startsWith("HTTP/1.1 403"));
			assertFalse("no tunnel was established", response.contains("200 Connection Established"));
			assertEquals("still never contacted", 0, victim.contactCount());
		}
	}

	/** What the policy permits still goes through - the layer refuses, it does not break browsing. */
	public void testTheProxyForwardsAnOnPolicyOrigin() throws IOException {
		BrowsingPolicy browsing =
			BrowsingPolicy.of(policy("research", Access.DENY, origin("docs.example.com", Access.ALLOW)));

		try (RecordingServer upstream = RecordingServer.serving("<html>on-policy content</html>");
				FilteringProxy proxy =
					FilteringProxy.start(governed(allowAnyDomains(), browsing), pinnedTo(upstream))) {
			String response = exchange(proxy, "GET http://docs.example.com/ HTTP/1.1\r\n"
				+ "Host: docs.example.com\r\n\r\n");

			assertTrue("forwarded: " + statusLine(response), response.startsWith("HTTP/1.1 200"));
			assertTrue("the page came back", response.contains("on-policy content"));
			assertEquals(1, upstream.contactCount());
		}
	}

	/**
	 * The refusal names the origin, the role and where the run may go instead, so the agent can act on
	 * it rather than retry the same address.
	 */
	public void testTheProxyRefusalIsReadableByTheAgent() throws IOException {
		BrowsingPolicy browsing =
			BrowsingPolicy.of(policy("research", Access.DENY, origin("docs.example.com", Access.ALLOW)));

		try (RecordingServer victim = RecordingServer.serving("<html>x</html>");
				FilteringProxy proxy =
					FilteringProxy.start(governed(allowAnyDomains(), browsing), pinnedTo(victim))) {
			String response = exchange(proxy, "GET http://other.example.com/ HTTP/1.1\r\n"
				+ "Host: other.example.com\r\n\r\n");

			assertTrue("names the refused origin", response.contains("other.example.com"));
			assertTrue("names the role as the reason", response.contains("not allowed for your role"));
			assertTrue("names where the run may go", response.contains("docs.example.com"));
		}
	}

	/**
	 * The installation's own lists keep binding: a blocklisted origin stays refused although the
	 * browsing policy permits every origin.
	 */
	public void testTheInstallationListsStillBind() {
		GovernedPolicy governed =
			governed(newService(List.of(), List.of("blocked.example"), true, false),
				BrowsingPolicy.of(policy("open", Access.ALLOW)));

		assertNotNull("the blocklist is not relaxed by a permissive browsing policy",
			governed.refuseRequest("blocked.example"));
		assertFalse(governed.isNavigationPermitted("blocked.example"));
		assertNull("and an origin neither of them refuses is reachable",
			governed.refuseRequest("example.com"));
	}

	/** Neither layer can relax the other: both have to permit an origin. */
	public void testBothLayersHaveToPermit() {
		BrowsingPolicy narrow =
			BrowsingPolicy.of(policy("research", Access.DENY, origin("docs.example.com", Access.ALLOW)));
		GovernedPolicy governed = governed(allowAnyDomains(), narrow);

		assertNull(governed.refuseRequest("docs.example.com"));
		assertNotNull(governed.refuseRequest("example.com"));

		GovernedPolicy other = governed(
			newService(List.of("example.com"), List.of(), false, true),
			BrowsingPolicy.of(policy("open", Access.ALLOW)));
		assertNull("what the installation allowlists (host or sub-domain) stays reachable",
			other.refuseRequest("docs.example.com"));
		assertNotNull("an origin off the installation's allowlist stays off it",
			other.refuseRequest("elsewhere.test"));
	}

	// --- The browser's navigation policy. ---

	/** A top-level navigation to an off-policy origin is refused. */
	public void testNavigationToAnOffPolicyOriginIsRefused() {
		GovernedPolicy governed = governed(allowAnyDomains(),
			BrowsingPolicy.of(policy("research", Access.DENY, origin("docs.example.com", Access.ALLOW))));

		assertTrue(governed.isNavigationPermitted("docs.example.com"));
		assertFalse(governed.isNavigationPermitted("other.example.com"));
	}

	/** The download gate is the policy's second axis, asked per origin. */
	public void testTheDownloadGateFollowsThePolicy() {
		BrowsingPolicyConfig.OriginRule readOnly = origin("docs.example.com", Access.ALLOW);
		readOnly.setDownloads(BrowsingPolicyConfig.DownloadAccess.DENY);
		GovernedPolicy governed =
			governed(allowAnyDomains(), BrowsingPolicy.of(policy("research", Access.DENY, readOnly)));

		assertTrue("the page is readable", governed.isNavigationPermitted("docs.example.com"));
		assertNotNull("a file from it is not saved", governed.refuseDownload("docs.example.com"));
	}

	/** With nothing configured no download is refused, which is what the browser has always done. */
	public void testNothingConfiguredRefusesNoDownload() {
		GovernedPolicy governed = governed(allowAnyDomains(), BrowsingPolicy.unrestricted());

		assertNull(governed.refuseDownload("example.com"));
	}

	// --- Web search: model-supplied narrowing under a configured ceiling. ---

	/** A domain the model asks for that the policy forbids does not make it reachable. */
	public void testAModelSuppliedAllowListCannotWiden() {
		BrowsingPolicy browsing =
			BrowsingPolicy.of(policy("research", Access.DENY, origin("docs.oracle.com", Access.ALLOW)));

		SearchScope widened = WebSearchTool.narrow(browsing, List.of("evil.example.com"), null);
		assertNotNull("the search is refused rather than run on the whole web", widened.refusal());
		assertTrue(widened.refusal(), widened.refusal().contains("docs.oracle.com"));

		SearchScope mixed =
			WebSearchTool.narrow(browsing, List.of("evil.example.com", "docs.oracle.com"), null);
		assertNull(mixed.refusal());
		assertEquals("only the permitted half survives", List.of("docs.oracle.com"), mixed.include());
	}

	/** Naming nothing gets the policy's own ceiling, so an unrestricted search cannot happen either. */
	public void testAnUnfilteredSearchInheritsTheCeiling() {
		BrowsingPolicy browsing = BrowsingPolicy.of(policy("research", Access.DENY,
			origin("docs.oracle.com", Access.ALLOW), origin("*.top-logic.com", Access.ALLOW)));

		SearchScope scope = WebSearchTool.narrow(browsing, null, null);
		assertNull(scope.refusal());
		assertEquals(List.of("docs.oracle.com", "top-logic.com"), scope.include());
	}

	/** A model-supplied domain that is narrower than the ceiling is kept as asked. */
	public void testAModelSuppliedAllowListMayNarrow() {
		BrowsingPolicy browsing =
			BrowsingPolicy.of(policy("research", Access.DENY, origin("*.top-logic.com", Access.ALLOW)));

		SearchScope scope = WebSearchTool.narrow(browsing, List.of("dev.top-logic.com"), null);
		assertNull(scope.refusal());
		assertEquals(List.of("dev.top-logic.com"), scope.include());
	}

	/** What the policy refuses is added to the engine's exclude list beside what the model asked. */
	public void testDeniedOriginsReachTheEngineAsExclusions() {
		BrowsingPolicy browsing =
			BrowsingPolicy.of(policy("open", Access.ALLOW, origin("pastebin.com", Access.DENY)));

		SearchScope scope = WebSearchTool.narrow(browsing, null, List.of("spam.example.com"));
		assertNull(scope.refusal());
		assertEquals(List.of(), scope.include());
		assertEquals(List.of("spam.example.com", "pastebin.com"), scope.exclude());
	}

	/**
	 * An engine that ignores the domain filter does not get past the policy: the hits are filtered
	 * again on the way back, which is the half that actually binds.
	 */
	public void testOffPolicyHitsAreDroppedFromWhatTheEngineAnswered() {
		BrowsingPolicy browsing =
			BrowsingPolicy.of(policy("research", Access.DENY, origin("docs.oracle.com", Access.ALLOW)));
		WebSearchResults answered = new WebSearchResults("an answer", List.of(
			hit("https://docs.oracle.com/a"),
			hit("https://evil.example.com/b"),
			hit("not a url at all")));

		WebSearchResults filtered = WebSearchTool.onPolicyOnly(browsing, answered);

		assertEquals(1, filtered.hits().size());
		assertEquals("https://docs.oracle.com/a", filtered.hits().get(0).url());
		assertEquals("the engine's answer text is kept", "an answer", filtered.answer());
	}

	/** With nothing configured the results come back exactly as the engine answered them. */
	public void testNothingConfiguredFiltersNothing() {
		WebSearchResults answered =
			new WebSearchResults(null, List.of(hit("https://example.com/a")));

		assertSame(answered, WebSearchTool.onPolicyOnly(BrowsingPolicy.unrestricted(), answered));

		SearchScope scope = WebSearchTool.narrow(BrowsingPolicy.unrestricted(), null, null);
		assertEquals(List.of(), scope.include());
		assertEquals(List.of(), scope.exclude());
		assertNull(scope.refusal());
	}

	// --- Fixture. ---

	private static WebSearchHit hit(String url) {
		return new WebSearchHit("title", url, "snippet", null);
	}

	private static GovernedPolicy governed(BrowserToolsService lists, BrowsingPolicy browsing) {
		return new GovernedPolicy(lists, lists, browsing);
	}

	/** An installation whose own lists refuse nothing, so only the browsing policy can. */
	private static BrowserToolsService allowAnyDomains() {
		return newService(List.of(), List.of(), true, false);
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

	/** Pins every destination to the local fixture, so the forwarding path can be exercised offline. */
	private static AddressResolver pinnedTo(RecordingServer upstream) {
		return (host, port) -> {
			try {
				return Target.to(new InetSocketAddress(InetAddress.getByName("127.0.0.1"),
					upstream.port()));
			} catch (IOException ex) {
				return Target.refused("fixture unreachable: " + ex.getMessage());
			}
		};
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
}
