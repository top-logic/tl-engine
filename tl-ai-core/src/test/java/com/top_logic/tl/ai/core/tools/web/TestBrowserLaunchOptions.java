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
 * The contract: <b>every</b> launch disables WebRTC's UDP egress
 * ({@code --webrtc-ip-handling-policy=disable_non_proxied_udp}) and selects the full Chromium build
 * via {@link BrowserType.LaunchOptions#setChannel(String) channel "chromium"} - the two go together,
 * because the switch does not exist in {@code chrome-headless-shell}, which is what a headless launch
 * would otherwise default to. Bot-detection evasion adds only its own switch on top. A given proxy
 * server is set as the launch-level proxy (Chromium's {@code --proxy-server}). The evasion, headed and
 * proxy settings remain orthogonal.
 * </p>
 */
public class TestBrowserLaunchOptions extends TestCase {

	/** The switch that takes WebRTC's UDP egress away; see {@code TestBrowserWebRtcEgress}. */
	private static final String WEBRTC_SWITCH = "--webrtc-ip-handling-policy=disable_non_proxied_udp";

	/**
	 * <b>The security control that must never be dropped.</b> WebRTC ignores {@code --proxy-server},
	 * so without this switch a page can send UDP past the filtering proxy to any address it likes
	 * (issue #232). It is unconditional: no combination of the launch flags may omit it.
	 */
	public void testWebRtcUdpIsDisabledOnEveryLaunch() {
		for (boolean headed : new boolean[] { false, true }) {
			for (boolean evade : new boolean[] { false, true }) {
				for (String proxy : new String[] { null, "http://127.0.0.1:41234" }) {
					BrowserType.LaunchOptions options =
						BrowserSession.buildLaunchOptions(headed, evade, proxy);

					assertTrue("WebRTC UDP egress is disabled for headed=" + headed + " evade=" + evade
						+ " proxy=" + proxy, options.args.contains(WEBRTC_SWITCH));
				}
			}
		}
	}

	/**
	 * The full Chromium build is pinned on every launch. This is not a preference: the WebRTC switch
	 * above is compiled out of {@code chrome-headless-shell}, so a launch that let Playwright default
	 * to the shell would silently carry the switch without effect and reopen the hole.
	 */
	public void testFullChromiumBuildIsPinnedOnEveryLaunch() {
		for (boolean headed : new boolean[] { false, true }) {
			for (boolean evade : new boolean[] { false, true }) {
				BrowserType.LaunchOptions options = BrowserSession.buildLaunchOptions(headed, evade, null);

				assertEquals("the full Chromium build is what honours the WebRTC switch (headed="
					+ headed + " evade=" + evade + ")", "chromium", options.channel);
			}
		}
	}

	/** Evasion on, non-headed: full Chromium (channel "chromium"), still windowless (headless). */
	public void testEvasionSelectsFullChromiumChannelHeadless() {
		BrowserType.LaunchOptions options = BrowserSession.buildLaunchOptions(false, true, null);

		assertEquals("evasion runs on the full Chromium build via channel \"chromium\"",
			"chromium", options.channel);
		assertEquals("evasion is windowless: new headless, not a visible window",
			Boolean.TRUE, options.headless);
		assertTrue("evasion adds the AutomationControlled blink-feature switch",
			options.args.contains("--disable-blink-features=AutomationControlled"));
	}

	/** Evasion off: the evasion-only switch is absent, everything else is the common baseline. */
	public void testDefaultOmitsTheEvasionSwitch() {
		BrowserType.LaunchOptions options = BrowserSession.buildLaunchOptions(false, false, null);

		assertEquals("evasion off stays headless", Boolean.TRUE, options.headless);
		assertFalse("evasion off does not add the AutomationControlled switch",
			options.args.contains("--disable-blink-features=AutomationControlled"));
	}

	/** The default path is exactly the two sandbox args plus the WebRTC control - nothing else. */
	public void testDefaultArgSet() {
		BrowserType.LaunchOptions options = BrowserSession.buildLaunchOptions(false, false, null);

		assertNull("no executable-path override on the default path", options.executablePath);
		assertEquals("the two sandbox args plus the WebRTC control", 3, options.args.size());
		assertEquals("--disable-dev-shm-usage", options.args.get(0));
		assertEquals("--no-sandbox", options.args.get(1));
		assertEquals(WEBRTC_SWITCH, options.args.get(2));
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

	/** Headed without evasion: same full build, visible window, no evasion switch. */
	public void testHeadedWithoutEvasion() {
		BrowserType.LaunchOptions options = BrowserSession.buildLaunchOptions(true, false, null);

		assertEquals("headed runs the full Chromium build too", "chromium", options.channel);
		assertEquals(Boolean.FALSE, options.headless);
		assertFalse("no evasion switch without evasion",
			options.args.contains("--disable-blink-features=AutomationControlled"));
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
		assertEquals("pinning a proxy does not change the build selection", "chromium", options.channel);
		assertEquals("pinning a proxy does not change the headless toggle", Boolean.TRUE,
			options.headless);
		assertEquals("pinning a proxy does not change the launch args", 3, options.args.size());
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
