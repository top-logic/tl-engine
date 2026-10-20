/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.util.regex.Pattern;

import org.junit.runner.RunWith;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.junit.AssumptionAwareRunner;
import com.top_logic.tl.ai.core.tools.web.BrowserSession.DomainPolicy;
import com.top_logic.tl.ai.core.tools.web.BrowserToolsService.PageView;

/**
 * Gated end-to-end tests for the two browser bot-defence features, exercised against a real Chromium
 * but with <b>no network</b>: fixtures are loaded straight into the page via the test hook, so both
 * the block-page detector (through the real {@link BrowserSession#capture} path) and the evasion
 * fingerprint shim are verified deterministically, without depending on any live bot-protected site.
 *
 * <p>
 * Gated behind {@link BrowserE2e}, exactly like {@link TestBrowserShadowDom} and the
 * other browser E2E tests, because they launch Chromium.
 * </p>
 */
@RunWith(AssumptionAwareRunner.class)
public class TestBrowserBotDefenses extends TestCase {

	/** Navigation never happens (content is injected), so the policy is irrelevant. */
	private static final DomainPolicy ALLOW_ANY = host -> true;

	private static final int TIMEOUT_MILLIS = 30_000;

	/** An Akamai-style access-denied page - the shape a bot wall serves an automated browser. */
	private static final String AKAMAI_ACCESS_DENIED =
		"<!doctype html><html><head><title>Access Denied</title></head><body>"
			+ "<h1>Access Denied</h1>"
			+ "<p>You don't have permission to access \"/\" on this server.</p>"
			+ "<p>Reference #18.abcd1234.1700000000.deadbeef</p>"
			+ "</body></html>";

	/** A normal page whose visible text merely contains the word "denied" (must NOT be flagged). */
	private static final String NORMAL_PAGE_WITH_DENIED_WORD =
		"<!doctype html><html><body>"
			+ "<h1>Loan report</h1>"
			+ "<p>Your application was denied by the reviewer.</p>"
			+ "<button id='ok'>OK</button>"
			+ "</body></html>";

	/** A fixture that reads and displays {@code navigator.webdriver} into the light DOM. */
	private static final String WEBDRIVER_PROBE =
		"<!doctype html><html><body>"
			+ "<p id='wd'>pending</p>"
			+ "<script>document.getElementById('wd').textContent = 'webdriver=' + navigator.webdriver;</script>"
			+ "</body></html>";

	// --- Deliverable 1: block-page detection through the real capture path. ---

	/**
	 * An Akamai-style access-denied body captured through {@link BrowserSession#capture} yields a
	 * distinct block signal, and {@link BrowserTool#render} turns it into the clearly-worded banner an
	 * agent sees - not a silent empty element list.
	 */
	public void testBlockPageDetectedAndRenderedEndToEnd() {
		BrowserE2e.assumeEnabled();
		BrowserSession session = BrowserSession.open("block-detected", TIMEOUT_MILLIS, ALLOW_ANY);
		try {
			PageView view = session.loadHtmlForTest(AKAMAI_ACCESS_DENIED);
			assertNotNull("a block signal is always attached", view.block());
			assertTrue("Akamai access-denied body is detected as a bot-protection block",
				view.block().detected());

			String rendered = BrowserTool.render(view, 5_000_000, 50_000);
			assertTrue("agent sees a distinct block banner, not an unexplained empty page",
				rendered.contains("PAGE BLOCKED BY BOT PROTECTION"));
			assertTrue("banner names the detected reason",
				rendered.contains("You don't have permission to access"));
		} finally {
			session.close();
		}
	}

	/** A normal page containing only the word "denied" is NOT misclassified as blocked. */
	public void testNormalPageNotFlaggedEndToEnd() {
		BrowserE2e.assumeEnabled();
		BrowserSession session = BrowserSession.open("block-normal", TIMEOUT_MILLIS, ALLOW_ANY);
		try {
			PageView view = session.loadHtmlForTest(NORMAL_PAGE_WITH_DENIED_WORD);
			assertFalse("an ordinary page that merely says 'denied' is not a block page",
				view.block().detected());

			String rendered = BrowserTool.render(view, 5_000_000, 50_000);
			assertFalse("no block banner on an ordinary page", rendered.contains("PAGE BLOCKED"));
		} finally {
			session.close();
		}
	}

	// --- Deliverable 2: evasion flag off (default) vs on. ---

	/**
	 * The default session (evasion off) keeps the plain headless fingerprint: {@code navigator.webdriver}
	 * is {@code true} and the user-agent still advertises {@code HeadlessChrome}. This pins the
	 * "byte-for-byte unchanged when off" guarantee.
	 */
	public void testDefaultSessionKeepsHeadlessFingerprint() {
		BrowserE2e.assumeEnabled();
		BrowserSession session = BrowserSession.open("evasion-off", TIMEOUT_MILLIS, ALLOW_ANY);
		try {
			session.loadHtmlForTest("<!doctype html><html><body>hi</body></html>");
			assertEquals("default: navigator.webdriver is true under automation",
				Boolean.TRUE, session.evaluateForTest("navigator.webdriver"));
			String ua = String.valueOf(session.evaluateForTest("navigator.userAgent"));
			assertTrue("default: user-agent still advertises HeadlessChrome (" + ua + ")",
				ua.contains("HeadlessChrome"));
		} finally {
			session.close();
		}
	}

	/**
	 * With evasion on, {@code navigator.webdriver} is masked (undefined/false) - both when read
	 * directly and as a fixture displays it - and the user-agent is a realistic desktop Chrome UA
	 * with no {@code HeadlessChrome} token.
	 */
	public void testEvasionMasksWebdriverAndSetsChromeUserAgent() {
		BrowserE2e.assumeEnabled();
		BrowserSession session = BrowserSession.open("evasion-on", TIMEOUT_MILLIS, 0, ALLOW_ANY,
			null, false, true);
		try {
			PageView view = session.loadHtmlForTest(WEBDRIVER_PROBE);

			// The fixture's own script observed a masked navigator.webdriver.
			assertTrue("fixture reads navigator.webdriver as undefined/false: " + view.html(),
				view.html().contains("webdriver=undefined") || view.html().contains("webdriver=false"));

			// Reading it directly agrees: undefined (our shim) surfaces as a Java null.
			Object webdriver = session.evaluateForTest("navigator.webdriver");
			assertTrue("navigator.webdriver masked to undefined/false, was: " + webdriver,
				webdriver == null || Boolean.FALSE.equals(webdriver));

			String ua = String.valueOf(session.evaluateForTest("navigator.userAgent"));
			assertFalse("evasion UA drops the HeadlessChrome token (" + ua + ")", ua.contains("HeadlessChrome"));
			assertTrue("evasion UA is a realistic desktop Chrome UA (" + ua + ")", ua.contains("Chrome/"));

			// The masked plugins / window.chrome tells are also patched.
			assertEquals("navigator.plugins is non-empty", Boolean.TRUE,
				session.evaluateForTest("navigator.plugins.length > 0"));
			assertEquals("window.chrome is present", Boolean.TRUE,
				session.evaluateForTest("!!window.chrome"));
		} finally {
			session.close();
		}
	}

	// --- Deliverable 3: evasion launches the FULL Chromium build (channel "chromium"). ---

	/**
	 * Matches a User-Agent-Reduced Chrome version token ({@code Chrome/<major>.0.0.0} or
	 * {@code HeadlessChrome/<major>.0.0.0}): the minor/build/patch are frozen to {@code 0.0.0}. The
	 * <b>full</b> Chromium build applies UA Reduction and so matches; the stripped
	 * {@code chrome-headless-shell} does not - it reports its exact patch version (e.g.
	 * {@code .../131.0.6778.33 ...}). This is the deterministic, network-free signal that tells the
	 * two builds apart here.
	 */
	private static final Pattern REDUCED_UA_VERSION = Pattern.compile("Chrome/\\d+\\.0\\.0\\.0");

	/**
	 * The <b>full</b> Chromium build (channel "chromium", new headless) is launched, whether evasion
	 * is on or off - the light {@code chrome-headless-shell} is never used any more. Evasion needs the
	 * full build because the shell is itself a detectable fingerprint, but the reason it is now
	 * unconditional is security rather than evasion: the switch that disables WebRTC's UDP egress does
	 * not exist in the shell (see {@code TestBrowserWebRtcEgress}), so a shell launch would silently
	 * reopen the SSRF hole. This test is therefore also the evasion-side guard on that pin.
	 *
	 * <p>
	 * The signal is UA Reduction: the full build's native user-agent freezes the version to
	 * {@code .../<major>.0.0.0 ...}, whereas the shell reports its exact patch version. Deterministic
	 * and network-free.
	 * </p>
	 *
	 * <p>
	 * Note: in this Playwright-Java 1.49 / Chromium 131 build the full new-headless build still emits
	 * a {@code HeadlessChrome} token, so "UA lacks HeadlessChrome" is <em>not</em> a valid
	 * discriminator here; UA Reduction is.
	 * </p>
	 */
	public void testEveryLaunchUsesTheFullChromiumBuild() {
		BrowserE2e.assumeEnabled();
		BrowserSession defaultSession = BrowserSession.open("build-default", TIMEOUT_MILLIS, ALLOW_ANY);
		String defaultUa;
		try {
			defaultUa = defaultSession.rawBuildUserAgentForTest();
		} finally {
			defaultSession.close();
		}

		BrowserSession evasionSession = BrowserSession.open("build-evasion", TIMEOUT_MILLIS, 0, ALLOW_ANY,
			null, false, true);
		String evasionUa;
		try {
			// Launching with channel "chromium" itself succeeds (no error) and the session is usable.
			PageView view = evasionSession.loadHtmlForTest("<!doctype html><html><body>ok</body></html>");
			assertTrue("full-build session renders normally", view.html().contains("ok"));
			evasionUa = evasionSession.rawBuildUserAgentForTest();
		} finally {
			evasionSession.close();
		}

		assertNotNull(defaultUa);
		assertNotNull(evasionUa);
		assertTrue("the default (evasion-off) path runs the full Chromium build, which is what makes the "
			+ "WebRTC switch effective: " + defaultUa, REDUCED_UA_VERSION.matcher(defaultUa).find());
		assertTrue("the evasion path runs the full Chromium build too: " + evasionUa,
			REDUCED_UA_VERSION.matcher(evasionUa).find());
		assertEquals("both paths launch the very same binary now (raw build user-agent)", defaultUa,
			evasionUa);
	}}
