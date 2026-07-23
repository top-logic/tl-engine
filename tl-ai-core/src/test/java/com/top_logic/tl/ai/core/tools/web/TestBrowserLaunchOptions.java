/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import junit.framework.TestCase;

import com.microsoft.playwright.BrowserType;

/**
 * Pure unit tests for {@link BrowserSession#buildLaunchOptions(boolean, boolean)} - the launch-option
 * seam that decides which Chromium build is launched. No browser is started: the tests only inspect
 * the constructed {@link BrowserType.LaunchOptions} POJO, so they run in the ordinary (ungated) test
 * suite.
 *
 * <p>
 * The contract: with bot-detection evasion <b>on</b> the launch selects the full Chromium build via
 * {@link BrowserType.LaunchOptions#setChannel(String) channel "chromium"} (new headless); with
 * evasion <b>off</b> no channel is set, so Playwright keeps the light {@code chrome-headless-shell}
 * default byte-for-byte. The channel setting is orthogonal to headless/headed.
 * </p>
 */
public class TestBrowserLaunchOptions extends TestCase {

	/** Evasion on, non-headed: full Chromium (channel "chromium"), still windowless (headless). */
	public void testEvasionSelectsFullChromiumChannelHeadless() {
		BrowserType.LaunchOptions options = BrowserSession.buildLaunchOptions(false, true);

		assertEquals("evasion on selects the full Chromium build via channel \"chromium\"",
			"chromium", options.channel);
		assertEquals("evasion is windowless: new headless, not a visible window",
			Boolean.TRUE, options.headless);
		assertTrue("evasion adds the AutomationControlled blink-feature switch",
			options.args.contains("--disable-blink-features=AutomationControlled"));
	}

	/** Evasion off: no channel (headless-shell default), and the arg set is unchanged. */
	public void testDefaultUsesHeadlessShellNoChannel() {
		BrowserType.LaunchOptions options = BrowserSession.buildLaunchOptions(false, false);

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
		BrowserType.LaunchOptions options = BrowserSession.buildLaunchOptions(false, false);

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
		BrowserType.LaunchOptions options = BrowserSession.buildLaunchOptions(true, true);

		assertEquals("headed still selects the full Chromium build", "chromium", options.channel);
		assertEquals("headed launches a visible window", Boolean.FALSE, options.headless);
	}

	/** Headed without evasion: headed full build (headed already uses the full build), no channel. */
	public void testHeadedWithoutEvasionSetsNoChannel() {
		BrowserType.LaunchOptions options = BrowserSession.buildLaunchOptions(true, false);

		assertNull("headed without evasion sets no channel", options.channel);
		assertEquals(Boolean.FALSE, options.headless);
	}
}
