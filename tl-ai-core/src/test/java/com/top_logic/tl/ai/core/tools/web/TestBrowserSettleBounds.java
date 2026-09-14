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
 * fixture server recorded <em>across</em> the operation under test - a count taken before and after,
 * because a cumulative count would already be satisfied by the navigation that preceded the click.
 * The navigating click additionally has to still capture content injected a beat after the new page
 * loaded, which is what proves the page-load settle still runs where a page really was loaded.
 * </p>
 *
 * <p>
 * <b>The deferred navigation.</b> The second group of tests covers the case a click-detection window
 * is easy to get wrong: a handler that starts the hop itself, later than the click
 * ({@code setTimeout(() => location.href = …)}, the shape {@code fetch(…).then(() => location.href = …)}
 * has against a backend that takes about a second). Capturing across that hop hands the model a
 * <em>torn</em> page view - the old page's URL with the new page's HTML, and refs minted on the new
 * document under the old page generation, so a ref the model still held would be accepted against a
 * different element on a different page. These tests therefore assert the three things that have to
 * agree - reported URL, captured content, and an advanced page generation - rather than only a
 * budget.
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

	/** Marks the second fixture page in its own HTML, so a torn capture is visible in the content. */
	private static final String SECOND_PAGE_MARKER = "second fixture page";

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
		+ "<h1>" + SECOND_PAGE_MARKER + "</h1>"
		+ "<script>setTimeout(function() {"
		+ "  var b = document.createElement('button');"
		+ "  b.id = 'late';"
		+ "  b.textContent = '" + LATE_BUTTON_NAME + "';"
		+ "  document.body.appendChild(b);"
		+ "}, " + LATE_INJECT_MILLIS + ");</script>"
		+ POLL_SCRIPT
		+ "</body></html>";

	/**
	 * Delays, in milliseconds after the click, at which the deferred-navigation fixture starts its
	 * hop. All three sit past {@code CLICK_NAVIGATION_DETECTION_MILLIS} (500 ms), so none of them is
	 * caught by the click's first look at the flag; they are caught by the second one, taken after the
	 * DOM-quiet wait.
	 */
	private static final int[] LATE_NAVIGATION_DELAYS_MILLIS = { 600, 800, 1000 };

	/** The accessible name of the button that navigates a beat after it is clicked. */
	private static final String LATE_NAVIGATION_BUTTON_NAME = "Navigate in a moment";

	/**
	 * Keeps a page permanently mutating as well as permanently busy on the network, so its DOM-quiet
	 * stage runs to the settle cap rather than resolving at the debounce window.
	 *
	 * <p>
	 * This is not stacking the deck, it is the page class under test: the pages that never go
	 * network-idle - an SSO login page, a single-page application - are the same ones that keep
	 * repainting. It matters here because the click's navigation-detection window is the 500 ms
	 * constant <em>plus</em> however long the DOM-quiet wait runs, so on such a page the window is the
	 * full two seconds and all three delays below sit inside it deterministically.
	 * </p>
	 */
	private static final String RESTLESS_SCRIPT =
		"<p id=\"tick\">0</p>"
			+ "<script>var n = 0; setInterval(function() {"
			+ "  document.getElementById('tick').textContent = String(++n);"
			+ "}, " + POLL_INTERVAL_MILLIS + ");</script>";

	/**
	 * A never-idle, never-still page whose button starts a top-level navigation {@code delayMillis}
	 * after it is clicked - the {@code setTimeout} / {@code fetch().then()} shape - rather than with
	 * the click.
	 */
	private static String lateNavigationPage(int delayMillis) {
		return "<!doctype html><html><head><title>first</title></head><body>"
			+ "<h1>first fixture page</h1>"
			+ "<button id=\"later\" onclick=\"setTimeout(function() {"
			+ " location.href = 'http://" + SECOND_HOST + "/'; }, " + delayMillis + ");\">"
			+ LATE_NAVIGATION_BUTTON_NAME + "</button>"
			+ RESTLESS_SCRIPT
			+ POLL_SCRIPT
			+ "</body></html>";
	}

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

				long pollsBefore = pollCount(first);
				long startNanos = System.nanoTime();
				PageView clicked = session.click(stay);
				long elapsedMillis = (System.nanoTime() - startNanos) / 1_000_000L;

				assertTrue("the click landed (the page edited itself): " + clicked.html(),
					clicked.html().contains(CLICK_MARKER));
				assertPollingContinued(first, pollsBefore);
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

				long pollsBefore = pollCount(second);
				long startNanos = System.nanoTime();
				PageView clicked = session.click(go);
				long elapsedMillis = (System.nanoTime() - startNanos) / 1_000_000L;

				assertTrue("the click navigated to the second page: " + clicked.finalUrl(),
					clicked.finalUrl().contains(SECOND_HOST));
				assertNotNull("a navigating click still settles, so the button the new page injects "
					+ LATE_INJECT_MILLIS + " ms after its load is captured",
					refWithName(clicked.elements(), LATE_BUTTON_NAME));
				assertPollingContinued(second, pollsBefore);
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
				long pollsBefore = pollCount(first);
				long startNanos = System.nanoTime();
				PageView loaded = session.navigate("http://" + FIRST_HOST + "/");
				long elapsedMillis = (System.nanoTime() - startNanos) / 1_000_000L;

				assertTrue("the fixture page itself loaded: " + loaded.html(),
					loaded.html().contains("first fixture page"));
				assertPollingContinued(first, pollsBefore);
				assertTrue("a navigation onto a never-idle page is bounded by the settle cap, not by "
					+ "the page-load timeout: took " + elapsedMillis + " ms, budget "
					+ PAGE_LOAD_BUDGET_MILLIS + " ms, page-load timeout " + NAVIGATION_TIMEOUT_MILLIS
					+ " ms", elapsedMillis < PAGE_LOAD_BUDGET_MILLIS);
			} finally {
				session.close();
			}
		}
	}

	/**
	 * <b>The deferred navigation.</b> A click whose handler starts the hop <em>later</em> than the
	 * detection window must still be settled as a page load: the returned page view has to report the
	 * new page's URL, carry the new page's content, say that it navigated, and mint its refs under a
	 * fresh page generation - all four, because it is their disagreement that makes a torn capture
	 * dangerous rather than merely wrong.
	 *
	 * <p>
	 * Run for each of {@link #LATE_NAVIGATION_DELAYS_MILLIS}, every one of them past the 500 ms first
	 * window, on a page that never goes network-idle and never goes DOM-quiet - the shape that used to
	 * make this go wrong. The URL assertion is the one that carries the commit wait: without it the
	 * click reports the page it started on while its HTML is already the page it ended on.
	 * </p>
	 */
	public void testANavigationStartedAfterTheDetectionWindowIsStillSettled() {
		BrowserE2e.assumeEnabled();
		Assume.assumeTrue("DNS does not resolve the fixture hosts - no network", resolvable());
		for (int delayMillis : LATE_NAVIGATION_DELAYS_MILLIS) {
			assertLateNavigationIsSettled(delayMillis);
		}
	}

	/** One case of {@link #testANavigationStartedAfterTheDetectionWindowIsStillSettled()}. */
	private static void assertLateNavigationIsSettled(int delayMillis) {
		String when = "navigation started " + delayMillis + " ms after the click: ";
		try (RecordingServer first = RecordingServer.serving(lateNavigationPage(delayMillis));
				RecordingServer second = RecordingServer.serving(SECOND_PAGE);
				FilteringProxy proxy = FilteringProxy.start(PERMIT_ALL, resolvingTo(first, second))) {
			BrowserSession session = openSession("click-late-" + delayMillis, proxy);
			try {
				PageView loaded = session.navigate("http://" + FIRST_HOST + "/");
				String later = refWithName(loaded.elements(), LATE_NAVIGATION_BUTTON_NAME);
				assertNotNull(when + "the fixture's deferred-navigation button was captured", later);
				long generationBefore = generationOf(later);

				long startNanos = System.nanoTime();
				PageView clicked = session.click(later);
				long elapsedMillis = (System.nanoTime() - startNanos) / 1_000_000L;

				assertTrue(when + "the reported URL is the page the click ended on, not the one it "
					+ "started on: " + clicked.finalUrl(), clicked.finalUrl().contains(SECOND_HOST));
				assertTrue(when + "the captured content is that same page",
					clicked.html().contains(SECOND_PAGE_MARKER));
				assertTrue(when + "the hop is reported as a navigation, so the model is not told the "
					+ "page stayed put", clicked.navigated());

				// The new page injects this button 800 ms after its own load, so finding it proves the
				// page-load settle ran on the new document rather than the capture racing it.
				String late = refWithName(clicked.elements(), LATE_BUTTON_NAME);
				assertNotNull(when + "the new page was settled and captured in full", late);
				assertTrue(when + "the page generation advanced, so a ref from the old page is refused "
					+ "instead of resolving against the new document (was " + generationBefore
					+ ", now " + generationOf(late) + ")", generationOf(late) > generationBefore);

				assertTrue(when + "the whole click is still bounded: took " + elapsedMillis
					+ " ms, budget " + PAGE_LOAD_BUDGET_MILLIS + " ms, page-load timeout "
					+ NAVIGATION_TIMEOUT_MILLIS + " ms", elapsedMillis < PAGE_LOAD_BUDGET_MILLIS);
			} finally {
				session.close();
			}
		}
	}

	// --- Fixture helpers. ---

	/** How many poll requests this fixture server has answered so far. */
	private static long pollCount(RecordingServer fixture) {
		return fixture.requestLines().stream().filter(line -> line.contains(POLL_PATH)).count();
	}

	/**
	 * Asserts that the fixture kept the network busy <b>during</b> the operation just measured, so a
	 * met budget means the waits are bounded rather than that the page went quiet after all.
	 *
	 * <p>
	 * The growth is what carries the argument, not the total: the navigation that precedes a click
	 * already spends a second or two, so a cumulative count is satisfied long before the click even
	 * happens and would prove only that the fixture once polled.
	 * </p>
	 *
	 * @param before
	 *        {@link #pollCount(RecordingServer)} taken immediately before the operation.
	 */
	private static void assertPollingContinued(RecordingServer fixture, long before) {
		long after = pollCount(fixture);
		assertTrue("the fixture page has to keep polling across the operation for this test to mean "
			+ "anything, but the fixture server answered " + before + " poll request(s) before it and "
			+ after + " after it", after > before);
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

	/**
	 * The page generation encoded in a {@code s<generation>e<index>} ref - what
	 * {@code BrowserSession.requireCurrentRef} compares against, and therefore what decides whether a
	 * ref the model kept from an earlier snapshot is still accepted.
	 */
	private static long generationOf(String ref) {
		int end = ref.indexOf('e');
		assertTrue("not a ref of the documented s<generation>e<index> shape: " + ref,
			ref.startsWith("s") && end > 1);
		return Long.parseLong(ref.substring(1, end));
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
