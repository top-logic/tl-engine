/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.List;

import org.junit.Assume;
import org.junit.runner.RunWith;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.junit.AssumptionAwareRunner;
import com.top_logic.tl.ai.core.tools.web.BrowserSession.DomainPolicy;
import com.top_logic.tl.ai.core.tools.web.BrowserToolsService.InteractiveElement;
import com.top_logic.tl.ai.core.tools.web.BrowserToolsService.PageView;
import com.top_logic.tl.ai.core.tools.web.FilteringProxy.AddressResolver;
import com.top_logic.tl.ai.core.tools.web.FilteringProxy.RequestPolicy;
import com.top_logic.tl.ai.core.tools.web.FilteringProxy.Target;

/**
 * End-to-end tests, with a real Chromium, that a page which <b>never goes network-idle</b> costs a
 * bounded amount of time per browser operation.
 *
 * <p>
 * <b>The defect (Gitea #394).</b> Every {@code browserClick} ran the post-navigation settle, and the
 * first stage of that settle waited for Playwright's {@code networkidle} with the <em>page-load</em>
 * timeout ({@code navigation-timeout-seconds}, 30 s by default). {@code networkidle} needs 500 ms
 * with no network activity at all, which a page that polls, streams or sends beacons never reaches -
 * an SSO login page, most single-page applications. So every click on such a page cost the full 30 s
 * although the click itself had landed within a second, and a three-click login flow cost a minute
 * and a half. {@code browserNavigate} and {@code browserBack} were exposed the same way.
 * </p>
 *
 * <p>
 * <b>The fixture.</b> A page that fetches every {@value #POLL_INTERVAL_MILLIS} ms forever, so it
 * never goes network-idle, served locally (deterministic, offline) while reachable under a
 * <em>public</em> host name - the SSRF layer refuses loopback, as it must - by substituting the
 * proxy's address resolver, the same technique {@link TestBrowserPolicyRecheck} uses and for the same
 * reason: the code under test stays production code. Only DNS resolution of the two names is needed;
 * the tests skip themselves if that is unavailable.
 * </p>
 *
 * <p>
 * <b>What is asserted.</b> Wall time, against budgets that are far below the 30 s page-load timeout
 * the defect spent and far above what the fixed code needs, so a slow agent does not make them flap.
 * That the fixture really does keep the network busy is asserted directly, from the poll requests the
 * fixture server recorded - without that the budgets could be met by a page that simply went idle.
 * The navigating click additionally has to still capture content injected a beat after the new page
 * loaded, which is what proves the page-load settle still runs where a page really was loaded.
 * </p>
 *
 * <p>
 * Gated behind {@link BrowserE2e}.
 * </p>
 */
@RunWith(AssumptionAwareRunner.class)
public class TestBrowserSettleBounds extends TestCase {

	/** The host the first fixture page is served under; only its DNS resolution is needed. */
	private static final String FIRST_HOST = "example.com";

	/** The host the second fixture page - the click target - is served under. */
	private static final String SECOND_HOST = "www.example.com";

	/** Permits both fixture hosts, so the navigating click is not refused by the domain policy. */
	private static final DomainPolicy BOTH_FIXTURE_HOSTS =
		host -> FIRST_HOST.equalsIgnoreCase(host) || SECOND_HOST.equalsIgnoreCase(host);

	/** The proxy refuses nothing by domain; this suite is about timing, not policy. */
	private static final RequestPolicy PERMIT_ALL = host -> null;

	/** The production-default page-load timeout - the budget the defect used to spend per click. */
	private static final int NAVIGATION_TIMEOUT_MILLIS = 30_000;

	/** The production-default settle cap, which now bounds the network-quiet wait as well. */
	private static final int SETTLE_MILLIS = 1500;

	/** How often the fixture page fetches, in milliseconds. Well under Playwright's 500 ms idle window. */
	private static final String POLL_INTERVAL_MILLIS = "200";

	/** The path the fixture polls; recorded by the fixture server and asserted on. */
	private static final String POLL_PATH = "/poll";

	/**
	 * Budget for a click that starts no navigation. It waits for the navigation-detection window and
	 * the DOM-quiet cap and for nothing else, which is about a second; the defect spent
	 * {@link #NAVIGATION_TIMEOUT_MILLIS}.
	 */
	private static final long SAME_PAGE_CLICK_BUDGET_MILLIS = 5_000;

	/**
	 * Budget for an operation that really does load a page: the load itself (a loopback fixture, so
	 * immediate) plus the two settle stages, each bounded by {@link #SETTLE_MILLIS}. Generous enough
	 * for a loaded build agent, and still nowhere near the {@link #NAVIGATION_TIMEOUT_MILLIS} the
	 * defect spent.
	 */
	private static final long PAGE_LOAD_BUDGET_MILLIS = 12_000;

	/** Milliseconds after load at which the second fixture page appends its late button. */
	private static final int LATE_INJECT_MILLIS = 800;

	/** The accessible name of the button the second fixture page injects after it loaded. */
	private static final String LATE_BUTTON_NAME = "Accept";

	/** The accessible name of the button that changes the first page without navigating. */
	private static final String STAY_BUTTON_NAME = "Stay here";

	/** The accessible name of the link that navigates to the second fixture page. */
	private static final String NAVIGATE_LINK_NAME = "Go to the second page";

	/** The text the same-page button writes, proving the click landed rather than being swallowed. */
	private static final String CLICK_MARKER = "clicked";

	/** Keeps a page permanently busy on the network, so it never reaches {@code networkidle}. */
	private static final String POLL_SCRIPT =
		"<script>setInterval(function() { fetch('" + POLL_PATH + "?' + Date.now())"
			+ ".catch(function() {}); }, " + POLL_INTERVAL_MILLIS + ");</script>";

	/**
	 * The first fixture page: never idle, with a button that only edits the page and a link that
	 * really navigates - the two clicks whose cost has to differ.
	 */
	private static final String FIRST_PAGE = ""
		+ "<!doctype html><html><head><title>first</title></head><body>"
		+ "<h1>first fixture page</h1>"
		+ "<button id=\"stay\" onclick=\"document.getElementById('mark').textContent = '"
		+ CLICK_MARKER + "';\">" + STAY_BUTTON_NAME + "</button>"
		+ "<p id=\"mark\">not clicked</p>"
		+ "<a id=\"go\" href=\"http://" + SECOND_HOST + "/\">" + NAVIGATE_LINK_NAME + "</a>"
		+ POLL_SCRIPT
		+ "</body></html>";

	/**
	 * The second fixture page: never idle either, and it appends a button a beat after it loaded - the
	 * cookie-consent-banner shape, so a capture that skipped the settle would miss it.
	 */
	private static final String SECOND_PAGE = ""
		+ "<!doctype html><html><head><title>second</title></head><body>"
		+ "<h1>second fixture page</h1>"
		+ "<script>setTimeout(function() {"
		+ "  var b = document.createElement('button');"
		+ "  b.id = 'late';"
		+ "  b.textContent = '" + LATE_BUTTON_NAME + "';"
		+ "  document.body.appendChild(b);"
		+ "}, " + LATE_INJECT_MILLIS + ");</script>"
		+ POLL_SCRIPT
		+ "</body></html>";

	/**
	 * <b>The defect itself.</b> A click that starts no navigation on a page that never goes
	 * network-idle must return in about a second, not in a page-load timeout.
	 */
	public void testSamePageClickIsNotChargedThePageLoadTimeout() {
		BrowserE2e.assumeEnabled();
		Assume.assumeTrue("DNS does not resolve the fixture hosts - no network", resolvable());
		try (RecordingServer first = RecordingServer.serving(FIRST_PAGE);
				RecordingServer second = RecordingServer.serving(SECOND_PAGE);
				FilteringProxy proxy = FilteringProxy.start(PERMIT_ALL, resolvingTo(first, second))) {
			BrowserSession session = openSession("click-same-page", proxy);
			try {
				PageView loaded = session.navigate("http://" + FIRST_HOST + "/");
				String stay = refWithName(loaded.elements(), STAY_BUTTON_NAME);
				assertNotNull("the fixture's same-page button was captured", stay);

				long startNanos = System.nanoTime();
				PageView clicked = session.click(stay);
				long elapsedMillis = (System.nanoTime() - startNanos) / 1_000_000L;

				assertTrue("the click landed (the page edited itself): " + clicked.html(),
					clicked.html().contains(CLICK_MARKER));
				assertPollingKeptTheNetworkBusy(first);
				assertTrue("a click that starts no navigation must not be charged the page-load "
					+ "timeout: took " + elapsedMillis + " ms, budget " + SAME_PAGE_CLICK_BUDGET_MILLIS
					+ " ms, page-load timeout " + NAVIGATION_TIMEOUT_MILLIS + " ms",
					elapsedMillis < SAME_PAGE_CLICK_BUDGET_MILLIS);
			} finally {
				session.close();
			}
		}
	}

	/**
	 * A click that <em>does</em> navigate still gets the page-load settle - it lands on the new page
	 * and captures content injected a beat after that page loaded - but the settle is bounded by the
	 * settle cap even though the new page never goes network-idle either.
	 */
	public void testNavigatingClickStillSettlesAndStaysBounded() {
		BrowserE2e.assumeEnabled();
		Assume.assumeTrue("DNS does not resolve the fixture hosts - no network", resolvable());
		try (RecordingServer first = RecordingServer.serving(FIRST_PAGE);
				RecordingServer second = RecordingServer.serving(SECOND_PAGE);
				FilteringProxy proxy = FilteringProxy.start(PERMIT_ALL, resolvingTo(first, second))) {
			BrowserSession session = openSession("click-navigating", proxy);
			try {
				PageView loaded = session.navigate("http://" + FIRST_HOST + "/");
				String go = refWithName(loaded.elements(), NAVIGATE_LINK_NAME);
				assertNotNull("the fixture's navigating link was captured", go);

				long startNanos = System.nanoTime();
				PageView clicked = session.click(go);
				long elapsedMillis = (System.nanoTime() - startNanos) / 1_000_000L;

				assertTrue("the click navigated to the second page: " + clicked.finalUrl(),
					clicked.finalUrl().contains(SECOND_HOST));
				assertNotNull("a navigating click still settles, so the button the new page injects "
					+ LATE_INJECT_MILLIS + " ms after its load is captured",
					refWithName(clicked.elements(), LATE_BUTTON_NAME));
				assertPollingKeptTheNetworkBusy(second);
				assertTrue("a navigating click onto a never-idle page is bounded by the settle cap, "
					+ "not by the page-load timeout: took " + elapsedMillis + " ms, budget "
					+ PAGE_LOAD_BUDGET_MILLIS + " ms, page-load timeout " + NAVIGATION_TIMEOUT_MILLIS
					+ " ms", elapsedMillis < PAGE_LOAD_BUDGET_MILLIS);
			} finally {
				session.close();
			}
		}
	}

	/**
	 * {@code browserNavigate} onto a page that never goes network-idle is bounded the same way: the
	 * network-quiet wait gives up at the settle cap instead of at the page-load timeout.
	 */
	public void testNavigateToANeverIdlePageIsBounded() {
		BrowserE2e.assumeEnabled();
		Assume.assumeTrue("DNS does not resolve the fixture hosts - no network", resolvable());
		try (RecordingServer first = RecordingServer.serving(FIRST_PAGE);
				RecordingServer second = RecordingServer.serving(SECOND_PAGE);
				FilteringProxy proxy = FilteringProxy.start(PERMIT_ALL, resolvingTo(first, second))) {
			BrowserSession session = openSession("navigate-never-idle", proxy);
			try {
				long startNanos = System.nanoTime();
				PageView loaded = session.navigate("http://" + FIRST_HOST + "/");
				long elapsedMillis = (System.nanoTime() - startNanos) / 1_000_000L;

				assertTrue("the fixture page itself loaded: " + loaded.html(),
					loaded.html().contains("first fixture page"));
				assertPollingKeptTheNetworkBusy(first);
				assertTrue("a navigation onto a never-idle page is bounded by the settle cap, not by "
					+ "the page-load timeout: took " + elapsedMillis + " ms, budget "
					+ PAGE_LOAD_BUDGET_MILLIS + " ms, page-load timeout " + NAVIGATION_TIMEOUT_MILLIS
					+ " ms", elapsedMillis < PAGE_LOAD_BUDGET_MILLIS);
			} finally {
				session.close();
			}
		}
	}

	// --- Fixture helpers. ---

	/**
	 * Asserts that the fixture really did keep the network busy, so a met budget means the waits are
	 * bounded rather than that the page went quiet after all.
	 */
	private static void assertPollingKeptTheNetworkBusy(RecordingServer fixture) {
		long polls = fixture.requestLines().stream().filter(line -> line.contains(POLL_PATH)).count();
		assertTrue("the fixture page has to poll for this test to mean anything, but the fixture "
			+ "server saw " + polls + " poll request(s)", polls >= 2);
	}

	/**
	 * An address resolver that maps the two fixture hosts to the two local fixture servers and leaves
	 * every other destination to the real, production SSRF resolver.
	 */
	private static AddressResolver resolvingTo(RecordingServer first, RecordingServer second) {
		AddressResolver real = FilteringProxy.ssrfResolver();
		return (host, port) -> {
			if (FIRST_HOST.equalsIgnoreCase(host)) {
				return Target.to(new InetSocketAddress(InetAddress.getLoopbackAddress(), first.port()));
			}
			if (SECOND_HOST.equalsIgnoreCase(host)) {
				return Target.to(new InetSocketAddress(InetAddress.getLoopbackAddress(), second.port()));
			}
			return real.resolve(host, port);
		};
	}

	private static BrowserSession openSession(String key, FilteringProxy proxy) {
		return BrowserSession.open(key, NAVIGATION_TIMEOUT_MILLIS,
			BrowserSession.DEFAULT_ACTION_TIMEOUT_MILLIS, SETTLE_MILLIS, BOTH_FIXTURE_HOSTS, null, false,
			false, proxy.serverUrl());
	}

	private static String refWithName(List<InteractiveElement> elements, String name) {
		for (InteractiveElement element : elements) {
			if (name.equals(element.name())) {
				return element.ref();
			}
		}
		return null;
	}

	/** Whether both fixture host names can be resolved; the in-browser route needs that independently. */
	private static boolean resolvable() {
		for (String host : new String[] { FIRST_HOST, SECOND_HOST }) {
			try {
				InetAddress.getByName(host);
			} catch (UnknownHostException ex) {
				return false;
			}
		}
		return true;
	}

}
