/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.util.List;
import java.util.function.Supplier;

import org.junit.runner.RunWith;

import junit.framework.TestCase;

import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.junit.AssumptionAwareRunner;
import com.top_logic.tl.ai.core.tools.web.BrowserSession.DomainPolicy;
import com.top_logic.tl.ai.core.tools.web.BrowserToolsService.PageView;

/**
 * Tests that every way a browser operation can fail reaches the model as a <b>short, actionable
 * sentence</b>, promptly - never as a raw Playwright error, and never after sitting out a
 * half-minute timeout.
 *
 * <p>
 * The three regressions covered here all had the same shape: an operation that could not possibly
 * succeed still spent the full 30 s page-load timeout, and then handed the model a multi-line
 * Playwright dump (complete with the driver's own JavaScript stack trace) that says nothing it can
 * act on - and which the browser verbs' truncation exemption passes through in full.
 * </p>
 * <ul>
 * <li>A navigation the domain policy refuses - reached by a <em>redirect</em> or by a link, so the
 * pre-flight check in {@link BrowserTool} cannot see it - is stopped by the security route. An
 * aborted main-frame request is indistinguishable from a network failure at the Playwright level, so
 * it surfaced as {@code net::ERR_FAILED} (or, when no document ever committed, as a 30 s
 * {@code TimeoutError}) instead of naming the policy.</li>
 * <li>A click on an element the page has re-rendered away waited for actionability for the whole
 * page-load timeout.</li>
 * <li>A page that genuinely never loads produced the raw {@code TimeoutError}.</li>
 * </ul>
 *
 * <p>
 * The mapping tests are plain unit tests. The end-to-end tests launch a real Chromium (gated behind
 * {@link BrowserE2e}); the click test needs no network, the redirect test uses a public redirect
 * service and skips itself if that service is not answering.
 * </p>
 */
@RunWith(AssumptionAwareRunner.class)
public class TestBrowserFailureSurfaces extends TestCase {

	/** Allow-any policy for fixture-only sessions: nothing is navigated, so it is irrelevant. */
	private static final DomainPolicy ALLOW_ANY = host -> true;

	private static final int TIMEOUT_MILLIS = 30_000;

	/** A short action timeout, so a dead click is asserted to fail fast rather than eventually. */
	private static final int ACTION_TIMEOUT_MILLIS = 3_000;

	/**
	 * A button fully covered by a later, opaque overlay: it is attached and visible, but never
	 * <em>actionable</em>, which is what a control re-rendered behind a suggestion list looks like to
	 * Playwright.
	 */
	private static final String COVERED_BUTTON_FIXTURE =
		"<!doctype html><html><body>"
			+ "<button id='target' style='position:absolute;left:10px;top:10px;width:120px;height:30px'>"
			+ "Search</button>"
			+ "<div id='cover' style='position:absolute;left:0;top:0;width:400px;height:200px;"
			+ "background:#fff;z-index:99'></div>"
			+ "</body></html>";

	// --- Failure-to-message mapping (deterministic, no browser). ---

	/**
	 * A navigation the security route aborted for policy reasons is reported as the policy block it
	 * was: it names the refused origin and the allowlist, states the page was not loaded, and carries
	 * no Playwright noise.
	 */
	public void testPolicyBlockRendersActionablePolicyMessage() {
		String rendered = execute(() -> {
			throw new BrowserPolicyException("Navigation ended at a blocked or off-allowlist origin: "
				+ "https://example.com/ - the domain 'example.com' is not on the browser allowlist "
				+ "configured by the administrator. The page was NOT loaded.");
		});

		assertTrue("reported as an error: " + rendered, rendered.startsWith("Error:"));
		assertTrue("names the refused origin: " + rendered, rendered.contains("https://example.com/"));
		assertTrue("mirrors the final-URL wording: " + rendered,
			rendered.contains("Navigation ended at a blocked or off-allowlist origin"));
		assertTrue("names the policy that refused it: " + rendered, rendered.contains("allowlist"));
		assertTrue("says the page was not loaded: " + rendered, rendered.contains("NOT loaded"));
		assertNoPlaywrightNoise(rendered);
	}

	/**
	 * A dead ref from the <em>current</em> generation is reported as recoverable: it names the ref and
	 * tells the model to re-snapshot, rather than dumping a {@code TimeoutError}.
	 */
	public void testDeadRefRendersRecoverableMessage() {
		String rendered = execute(() -> {
			throw new BrowserRefNotActionableException("s3e7", "clicked", 8_000);
		});

		assertTrue("reported as an error: " + rendered, rendered.startsWith("Error:"));
		assertTrue("names the ref: " + rendered, rendered.contains("s3e7"));
		assertTrue("tells the model how to recover: " + rendered, rendered.contains("browserSnapshot"));
		assertTrue("explains why the ref went dead: " + rendered, rendered.contains("no longer actionable"));
		assertNoPlaywrightNoise(rendered);
	}

	/** A page that never loaded is reported as a slow/unreachable site, not as a raw timeout. */
	public void testNavigationTimeoutRendersTerseMessage() {
		String rendered = execute(() -> {
			throw new BrowserNavigationTimeoutException("https://slow.example/", 30_000);
		});

		assertTrue("reported as an error: " + rendered, rendered.startsWith("Error:"));
		assertTrue("names the URL: " + rendered, rendered.contains("https://slow.example/"));
		assertTrue("names the elapsed budget in seconds: " + rendered, rendered.contains("30 s"));
		assertTrue("suggests a way forward: " + rendered, rendered.contains("webFetch"));
		assertNoPlaywrightNoise(rendered);
	}

	/** Every actionable failure message stays short enough to be worth its place in the context. */
	public void testFailureMessagesAreSingleLineAndShort() {
		List<String> rendered = List.of(
			execute(() -> {
				throw new BrowserPolicyException("Navigation ended at a blocked address: loopback refused");
			}),
			execute(() -> {
				throw new BrowserRefNotActionableException("s1e1", "typed into", 8_000);
			}),
			execute(() -> {
				throw new BrowserNavigationTimeoutException("https://slow.example/", 30_000);
			}),
			execute(() -> {
				throw new StaleBrowserRefException("s1e1");
			}));
		for (String message : rendered) {
			assertFalse("no embedded newlines (a stack trace would have them): " + message,
				message.contains("\n"));
			assertTrue("stays compact (was " + message.length() + " chars): " + message,
				message.length() < 500);
		}
	}

	// --- End-to-end (gated: launches Chromium). ---

	/**
	 * A top-level navigation the policy refuses - here refused by the per-context security route, which
	 * aborts the request - is reported as the policy block, promptly. This goes through
	 * {@code BrowserToolsService.navigate} rather than the {@code browserNavigate} verb precisely to
	 * get <em>past</em> the tool's pre-flight URL check, so only the route can stop it: exactly the
	 * situation a redirect or a link off the permitted origin creates. Gated behind
	 * {@code -Dtl.ai.browser.e2e=true}.
	 */
	public void testRouteAbortReportsPolicyBlockFastEndToEnd() {
		BrowserE2e.assumeEnabled();
		BrowserToolsService service = newService(List.of("quotes.toscrape.com"));
		service.startUp();
		try {
			long startNanos = System.nanoTime();
			try {
				service.navigate(new Object(), "https://example.com/");
				fail("an off-allowlist top-level navigation must not succeed");
			} catch (BrowserPolicyException expected) {
				long elapsedMillis = (System.nanoTime() - startNanos) / 1_000_000L;
				assertTrue("names the refused origin: " + expected.getMessage(),
					expected.getMessage().contains("example.com"));
				assertTrue("names the policy: " + expected.getMessage(),
					expected.getMessage().contains("allowlist"));
				assertNoPlaywrightNoise(expected.getMessage());
				assertTrue("fails fast, nowhere near the 30 s page-load timeout (was " + elapsedMillis
					+ " ms)", elapsedMillis < 10_000L);
			}
		} finally {
			service.shutDown();
		}
	}

	/**
	 * A server-side redirect off the allowlist is blocked and reported cleanly and promptly - the
	 * regression that returned a raw 30 s {@code Frame.goto} {@code TimeoutError}. Uses a public
	 * redirect service; if that service is not answering with its redirect there is nothing to assert
	 * about the tool, so the test skips itself. Gated behind {@code -Dtl.ai.browser.e2e=true}.
	 */
	public void testOffAllowlistRedirectReportedCleanlyEndToEnd() {
		BrowserE2e.assumeEnabled();
		BrowserToolsService service = newService(List.of("httpbingo.org"));
		service.startUp();
		try {
			BrowserTool tool = new BrowserTool();
			long startNanos = System.nanoTime();
			String result = tool.browserNavigate(
				"https://httpbingo.org/redirect-to?url=https%3A%2F%2Fexample.com%2F",
				ToolContext.EMPTY);
			long elapsedMillis = (System.nanoTime() - startNanos) / 1_000_000L;

			if (!result.contains("example.com")) {
				// The public redirect service did not redirect (it is occasionally unavailable, and
				// httpbin.org - the original fixture - was serving 503 when this was written). Nothing
				// about the tool's behaviour can be concluded from that.
				return;
			}
			assertTrue("the redirect is refused, not rendered: " + result, result.startsWith("Error:"));
			assertTrue("reports the policy, not a network failure: " + result,
				result.contains("blocked or off-allowlist origin"));
			assertTrue("names the domain policy: " + result, result.contains("allowlist"));
			assertNoPlaywrightNoise(result);
			assertTrue("returns promptly instead of timing out (was " + elapsedMillis + " ms)",
				elapsedMillis < 15_000L);
		} finally {
			service.shutDown();
		}
	}

	/**
	 * A click on an element that is attached but permanently un-actionable (covered by an overlay -
	 * what a re-rendered search control looks like) fails within the bounded action timeout with the
	 * recoverable "re-snapshot and use a fresh ref" message, instead of stalling for the page-load
	 * timeout. Needs no network. Gated behind {@code -Dtl.ai.browser.e2e=true}.
	 */
	public void testCoveredElementClickFailsFastAndActionablyEndToEnd() {
		BrowserE2e.assumeEnabled();
		BrowserSession session = BrowserSession.open("dead-click", TIMEOUT_MILLIS, ACTION_TIMEOUT_MILLIS,
			0, ALLOW_ANY, null, false, false, null);
		try {
			PageView view = session.loadHtmlForTest(COVERED_BUTTON_FIXTURE);
			String ref = refWithName(view, "Search");
			assertNotNull("the covered button is still captured (it IS visible, just not clickable)", ref);

			long startNanos = System.nanoTime();
			try {
				session.activateRefForTest(ref);
				fail("clicking a fully covered element must not succeed");
			} catch (BrowserRefNotActionableException expected) {
				long elapsedMillis = (System.nanoTime() - startNanos) / 1_000_000L;
				assertTrue("names the ref: " + expected.getMessage(), expected.getMessage().contains(ref));
				assertTrue("tells the model to re-snapshot: " + expected.getMessage(),
					expected.getMessage().contains("browserSnapshot"));
				assertNoPlaywrightNoise(expected.getMessage());
				assertTrue("bounded by the action timeout, not the 30 s page-load timeout (was "
					+ elapsedMillis + " ms)", elapsedMillis < 3 * ACTION_TIMEOUT_MILLIS);
			}
		} finally {
			session.close();
		}
	}

	/**
	 * The session stays usable after a dead click: the failure is a recoverable answer, not a broken
	 * browser, so removing the obstruction and clicking the same page again works. This is what
	 * distinguishes {@link BrowserRefNotActionableException} from
	 * {@link BrowserSessionBrokenException}. Gated behind {@code -Dtl.ai.browser.e2e=true}.
	 */
	public void testSessionSurvivesDeadClickEndToEnd() {
		BrowserE2e.assumeEnabled();
		BrowserSession session = BrowserSession.open("dead-click-recovery", TIMEOUT_MILLIS,
			ACTION_TIMEOUT_MILLIS, 0, ALLOW_ANY, null, false, false, null);
		try {
			PageView view = session.loadHtmlForTest(COVERED_BUTTON_FIXTURE
				.replace("<button id='target'", "<button id='target' onclick=\"document.title='clicked'\""));
			String ref = refWithName(view, "Search");
			try {
				session.activateRefForTest(ref);
				fail("clicking a fully covered element must not succeed");
			} catch (BrowserRefNotActionableException expected) {
				// Expected; the point is what still works afterwards.
			}

			// Remove the overlay and act on the very same page again through the same session.
			session.evaluateForTest("() => document.getElementById('cover').remove()");
			PageView reread = session.snapshot();
			String freshRef = refWithName(reread, "Search");
			assertNotNull("the page is still readable after a dead click", freshRef);
			PageView afterClick = session.activateRefForTest(freshRef);
			assertTrue("the same session can click once the obstruction is gone",
				afterClick.html().contains("clicked") || "clicked".equals(pageTitleOf(afterClick)));
		} finally {
			session.close();
		}
	}

	/** Runs {@link BrowserTool#execute} with an operation that fails, and returns what the model sees. */
	private static String execute(Supplier<PageView> failing) {
		return BrowserTool.execute(newService(List.of("example.com")), failing, false);
	}

	/**
	 * Asserts that {@code message} carries none of the raw Playwright error shape - the driver's
	 * {@code Error {} / message='...'} envelope, its JavaScript stack frames, or a bare
	 * {@code net::}/{@code TimeoutError} token.
	 */
	private static void assertNoPlaywrightNoise(String message) {
		for (String noise : List.of("TimeoutError", "net::ERR", "at Frame.", "at FrameSession",
			"playwright-java", "message='")) {
			assertFalse("no raw Playwright noise (" + noise + ") in: " + message, message.contains(noise));
		}
	}

	private static String refWithName(PageView view, String name) {
		return view.elements().stream()
			.filter(element -> name.equals(element.name()))
			.map(BrowserToolsService.InteractiveElement::ref)
			.findFirst().orElse(null);
	}

	private static String pageTitleOf(PageView view) {
		String html = view.html();
		int start = html.indexOf("<title>");
		int end = html.indexOf("</title>");
		return (start < 0 || end < start) ? null : html.substring(start + "<title>".length(), end);
	}

	private static BrowserToolsService newService(List<String> allowedDomains) {
		BrowserToolsService.Config<?> config =
			TypedConfiguration.newConfigItem(BrowserToolsService.Config.class);
		config.update(config.descriptor().getProperty(BrowserToolsService.Config.ALLOWED_DOMAINS),
			allowedDomains);
		return new BrowserToolsService(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config);
	}
}
