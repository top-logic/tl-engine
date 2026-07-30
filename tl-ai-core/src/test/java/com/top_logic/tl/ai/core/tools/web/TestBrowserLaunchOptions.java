/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import junit.framework.TestCase;

import com.microsoft.playwright.BrowserType;

/**
 * Pure unit tests for {@link BrowserSession#buildLaunchOptions(boolean, boolean, String)} - the
 * launch-option seam that decides which Chromium build is launched and whether the browser is pinned
 * to the filtering forward proxy. No browser is started: the tests only inspect the constructed
 * {@link BrowserType.LaunchOptions} POJO, so they run in the ordinary (ungated) test suite.
 *
 * <p>
 * The contract: with bot-detection evasion <b>on</b> the launch selects the full Chromium build via
 * {@link BrowserType.LaunchOptions#setChannel(String) channel "chromium"} (new headless); with
 * evasion <b>off</b> no channel is set, so Playwright keeps the light {@code chrome-headless-shell}
 * default byte-for-byte. A given proxy server is set as the launch-level proxy (Chromium's
 * {@code --proxy-server}). All three settings are orthogonal.
 * </p>
 */
public class TestBrowserLaunchOptions extends TestCase {

	/** Evasion on, non-headed: full Chromium (channel "chromium"), still windowless (headless). */
	public void testEvasionSelectsFullChromiumChannelHeadless() {
		BrowserType.LaunchOptions options = BrowserSession.buildLaunchOptions(false, true, null);

		assertEquals("evasion on selects the full Chromium build via channel \"chromium\"",
			"chromium", options.channel);
		assertEquals("evasion is windowless: new headless, not a visible window",
			Boolean.TRUE, options.headless);
		assertTrue("evasion adds the AutomationControlled blink-feature switch",
			options.args.contains("--disable-blink-features=AutomationControlled"));
	}

	/** Evasion off: no channel (headless-shell default), and the arg set is unchanged. */
	public void testDefaultUsesHeadlessShellNoChannel() {
		BrowserType.LaunchOptions options = BrowserSession.buildLaunchOptions(false, false, null);

		assertNull("evasion off sets no channel, so Playwright launches chrome-headless-shell",
			options.channel);
		assertEquals("evasion off stays headless", Boolean.TRUE, options.headless);
		assertFalse("evasion off does not add the AutomationControlled switch",
			options.args.contains("--disable-blink-features=AutomationControlled"));
	}

	/**
	 * The off path is byte-for-byte the previous default launch: exactly the two sandbox args, no
	 * channel, headless true.
	 */
	public void testOffPathUnchanged() {
		BrowserType.LaunchOptions options = BrowserSession.buildLaunchOptions(false, false, null);

		assertNull(options.channel);
		assertNull("no executable-path override on the default path", options.executablePath);
		assertEquals("exactly the two default sandbox args", 2, options.args.size());
		assertEquals("--disable-dev-shm-usage", options.args.get(0));
		assertEquals("--no-sandbox", options.args.get(1));
		assertEquals(Boolean.TRUE, options.headless);
	}

	/**
	 * Headed + evasion compose cleanly: the full-build channel is still selected and it does not
	 * flip the launch back to headless - the visible-window request wins ({@code headless == false}),
	 * so the headed launch toggle is not broken.
	 */
	public void testHeadedEvasionDoesNotConflict() {
		BrowserType.LaunchOptions options = BrowserSession.buildLaunchOptions(true, true, null);

		assertEquals("headed still selects the full Chromium build", "chromium", options.channel);
		assertEquals("headed launches a visible window", Boolean.FALSE, options.headless);
	}

	/** Headed without evasion: headed full build (headed already uses the full build), no channel. */
	public void testHeadedWithoutEvasionSetsNoChannel() {
		BrowserType.LaunchOptions options = BrowserSession.buildLaunchOptions(true, false, null);

		assertNull("headed without evasion sets no channel", options.channel);
		assertEquals(Boolean.FALSE, options.headless);
	}

	/**
	 * A proxy server is pinned as the launch-level proxy, which Playwright turns into Chromium's
	 * {@code --proxy-server}. This is what makes the SSRF policy apply to every connection the browser
	 * makes, so it is the one launch option a regression here would silently disarm.
	 */
	public void testProxyServerIsPinnedAtLaunch() {
		BrowserType.LaunchOptions options =
			BrowserSession.buildLaunchOptions(false, false, "http://127.0.0.1:41234");

		assertNotNull("the filtering proxy is pinned at launch level", options.proxy);
		assertEquals("http://127.0.0.1:41234", options.proxy.server);
		assertNull("pinning a proxy does not change the build selection", options.channel);
		assertEquals("pinning a proxy does not change the headless toggle", Boolean.TRUE,
			options.headless);
		assertEquals("pinning a proxy does not change the launch args", 2, options.args.size());
	}

	/** The proxy composes with evasion and with the headed toggle; none of them displaces another. */
	public void testProxyComposesWithEvasionAndHeaded() {
		BrowserType.LaunchOptions options =
			BrowserSession.buildLaunchOptions(true, true, "http://127.0.0.1:41235");

		assertEquals("http://127.0.0.1:41235", options.proxy.server);
		assertEquals("evasion still selects the full Chromium build", "chromium", options.channel);
		assertEquals("headed still launches a visible window", Boolean.FALSE, options.headless);
		assertTrue("evasion still adds its blink-feature switch",
			options.args.contains("--disable-blink-features=AutomationControlled"));
	}

	/** No proxy given (test fixtures with no network at all): the browser is left unproxied. */
	public void testNoProxyLeavesBrowserUnproxied() {
		assertNull(BrowserSession.buildLaunchOptions(false, false, null).proxy);
		assertNull("a blank proxy server is treated as none",
			BrowserSession.buildLaunchOptions(false, false, "  ").proxy);
	}
}
