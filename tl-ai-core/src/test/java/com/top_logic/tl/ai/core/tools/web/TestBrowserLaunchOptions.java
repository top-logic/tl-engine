/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import junit.framework.TestCase;

import com.microsoft.playwright.BrowserType;

import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.tl.ai.core.tools.web.BrowserToolsService.SandboxMode;

/**
 * Pure unit tests for {@link BrowserSession#buildLaunchOptions(boolean, boolean, String, boolean)}
 * - the launch-option seam that decides which Chromium build is launched, whether it keeps its
 * OS-level sandbox, and whether it is pinned to the filtering forward proxy. No browser is started:
 * the tests only inspect the constructed {@link com.microsoft.playwright.BrowserType.LaunchOptions}
 * POJO, so they run in the ordinary (ungated) test suite.
 *
 * <p>
 * The contract: <b>every</b> launch disables WebRTC's UDP egress
 * ({@code --webrtc-ip-handling-policy=disable_non_proxied_udp}) and selects the full Chromium build
 * via {@link com.microsoft.playwright.BrowserType.LaunchOptions#setChannel(String) channel
 * "chromium"} - the two go together, because the switch does not exist in
 * {@code chrome-headless-shell}, which is what a headless launch would otherwise default to.
 * {@code --no-sandbox} is added <b>only</b> when the caller asks for it. Bot-detection evasion adds
 * only its own switch on top. A given proxy server is set as the launch-level proxy (Chromium's
 * {@code --proxy-server}). The evasion, headed, sandbox and proxy settings remain orthogonal.
 * </p>
 */
public class TestBrowserLaunchOptions extends TestCase {

	/** The switch that takes WebRTC's UDP egress away; see {@code TestBrowserWebRtcEgress}. */
	private static final String WEBRTC_SWITCH = "--webrtc-ip-handling-policy=disable_non_proxied_udp";

	/** The switch that gives up Chromium's OS-level renderer sandbox. */
	private static final String NO_SANDBOX_SWITCH = "--no-sandbox";

	/** Sandbox on: the setting every host but a container gets. */
	private static final boolean SANDBOXED = false;

	/** Sandbox given up: the container setting. */
	private static final boolean UNSANDBOXED = true;

	/**
	 * <b>The security control that must never be dropped.</b> WebRTC ignores {@code --proxy-server},
	 * so without this switch a page can send UDP past the filtering proxy to any address it likes
	 * (issue #232). It is unconditional: no combination of the launch flags may omit it.
	 */
	public void testWebRtcUdpIsDisabledOnEveryLaunch() {
		for (boolean headed : new boolean[] { false, true }) {
			for (boolean evade : new boolean[] { false, true }) {
				for (boolean noSandbox : new boolean[] { false, true }) {
					for (String proxy : new String[] { null, "http://127.0.0.1:41234" }) {
						BrowserType.LaunchOptions options =
							BrowserSession.buildLaunchOptions(headed, evade, proxy, noSandbox);

						assertTrue("WebRTC UDP egress is disabled for headed=" + headed + " evade=" + evade
							+ " noSandbox=" + noSandbox + " proxy=" + proxy,
							options.args.contains(WEBRTC_SWITCH));
					}
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
				BrowserType.LaunchOptions options =
					BrowserSession.buildLaunchOptions(headed, evade, null, SANDBOXED);

				assertEquals("the full Chromium build is what honours the WebRTC switch (headed="
					+ headed + " evade=" + evade + ")", "chromium", options.channel);
			}
		}
	}

	// --- The sandbox: page JavaScript stays inside an OS-level boundary unless it provably cannot. ---

	/**
	 * <b>The regression this pins down (issue: {@code --no-sandbox} was unconditional).</b> Chromium's
	 * sandbox is what stops attacker-controlled page JavaScript in a renderer from being code running
	 * with the application's own privileges. Nothing about being headed, evading bot detection or
	 * being proxied has any bearing on it, so no combination of those may drop it.
	 */
	public void testSandboxIsKeptOnEveryLaunchThatDoesNotAskToDropIt() {
		for (boolean headed : new boolean[] { false, true }) {
			for (boolean evade : new boolean[] { false, true }) {
				for (String proxy : new String[] { null, "http://127.0.0.1:41234" }) {
					BrowserType.LaunchOptions options =
						BrowserSession.buildLaunchOptions(headed, evade, proxy, SANDBOXED);

					assertFalse("the OS-level sandbox must be kept for headed=" + headed + " evade="
						+ evade + " proxy=" + proxy + ": " + options.args,
						options.args.contains(NO_SANDBOX_SWITCH));
				}
			}
		}
	}

	/** Asked to drop the sandbox (the container case), the switch is added - and nothing else changes. */
	public void testSandboxIsDroppedOnlyWhenAsked() {
		BrowserType.LaunchOptions options =
			BrowserSession.buildLaunchOptions(false, false, null, UNSANDBOXED);

		assertTrue("the container branch passes --no-sandbox: " + options.args,
			options.args.contains(NO_SANDBOX_SWITCH));
		assertTrue("dropping the sandbox does not touch the WebRTC control",
			options.args.contains(WEBRTC_SWITCH));
		assertEquals("dropping the sandbox does not change the build selection", "chromium",
			options.channel);
		assertEquals("dropping the sandbox does not change the headless toggle", Boolean.TRUE,
			options.headless);
	}

	/**
	 * The mode resolution itself, for both branches and both forced settings. {@code AUTO} follows the
	 * detected environment; the two explicit settings ignore it, which is what makes them an escape
	 * hatch rather than a hint.
	 */
	public void testSandboxModeResolution() {
		String previous = System.getProperty(ContainerEnvironment.FORCE_PROPERTY);
		try {
			System.setProperty(ContainerEnvironment.FORCE_PROPERTY, "true");
			assertTrue("AUTO gives the sandbox up inside a container",
				BrowserToolsService.isSandboxDisabled(SandboxMode.AUTO));
			assertFalse("ENABLED keeps the sandbox even inside a container",
				BrowserToolsService.isSandboxDisabled(SandboxMode.ENABLED));

			System.setProperty(ContainerEnvironment.FORCE_PROPERTY, "false");
			assertFalse("AUTO keeps the sandbox outside a container",
				BrowserToolsService.isSandboxDisabled(SandboxMode.AUTO));
			assertTrue("DISABLED gives the sandbox up outside a container too",
				BrowserToolsService.isSandboxDisabled(SandboxMode.DISABLED));
		} finally {
			restore(ContainerEnvironment.FORCE_PROPERTY, previous);
		}
	}

	/** The configured mode reaches the service unchanged, and {@code AUTO} is what a fresh config gets. */
	public void testSandboxModeDefaultsToAutoDetection() {
		BrowserToolsService.Config<?> config =
			TypedConfiguration.newConfigItem(BrowserToolsService.Config.class);
		assertEquals("a fresh configuration auto-detects", SandboxMode.AUTO, config.getBrowserSandbox());

		BrowserToolsService service =
			new BrowserToolsService(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config);
		String previous = System.getProperty(ContainerEnvironment.FORCE_PROPERTY);
		try {
			System.setProperty(ContainerEnvironment.FORCE_PROPERTY, "true");
			assertTrue("the default configuration drops the sandbox in a container",
				service.isSandboxDisabled());
			System.setProperty(ContainerEnvironment.FORCE_PROPERTY, "false");
			assertFalse("the default configuration keeps it everywhere else", service.isSandboxDisabled());
		} finally {
			restore(ContainerEnvironment.FORCE_PROPERTY, previous);
		}
	}

	/** Outside a container - which is where this test suite runs - nothing is detected. */
	public void testNoContainerIsDetectedOnAnOrdinaryHost() {
		String previous = System.getProperty(ContainerEnvironment.FORCE_PROPERTY);
		try {
			System.clearProperty(ContainerEnvironment.FORCE_PROPERTY);
			// The detection reads only files a container runtime creates, so on a developer machine or
			// an ordinary CI host it must answer "no" - the whole point of the AUTO branch.
			assertFalse("no container marker may be reported on the host running this suite",
				ContainerEnvironment.isContainer());
		} finally {
			restore(ContainerEnvironment.FORCE_PROPERTY, previous);
		}
	}

	// --- Evasion, headed and proxy: unchanged behaviour. ---

	/** Evasion on, non-headed: full Chromium (channel "chromium"), still windowless (headless). */
	public void testEvasionSelectsFullChromiumChannelHeadless() {
		BrowserType.LaunchOptions options = BrowserSession.buildLaunchOptions(false, true, null, SANDBOXED);

		assertEquals("evasion runs on the full Chromium build via channel \"chromium\"",
			"chromium", options.channel);
		assertEquals("evasion is windowless: new headless, not a visible window",
			Boolean.TRUE, options.headless);
		assertTrue("evasion adds the AutomationControlled blink-feature switch",
			options.args.contains("--disable-blink-features=AutomationControlled"));
	}

	/** Evasion off: the evasion-only switch is absent, everything else is the common baseline. */
	public void testDefaultOmitsTheEvasionSwitch() {
		BrowserType.LaunchOptions options = BrowserSession.buildLaunchOptions(false, false, null, SANDBOXED);

		assertEquals("evasion off stays headless", Boolean.TRUE, options.headless);
		assertFalse("evasion off does not add the AutomationControlled switch",
			options.args.contains("--disable-blink-features=AutomationControlled"));
	}

	/** The default path is the shared-memory workaround plus the WebRTC control - and nothing else. */
	public void testDefaultArgSet() {
		BrowserType.LaunchOptions options = BrowserSession.buildLaunchOptions(false, false, null, SANDBOXED);

		assertNull("no executable-path override on the default path", options.executablePath);
		assertEquals("the shared-memory workaround plus the WebRTC control: " + options.args, 2,
			options.args.size());
		assertEquals("--disable-dev-shm-usage", options.args.get(0));
		assertEquals(WEBRTC_SWITCH, options.args.get(1));
		assertEquals(Boolean.TRUE, options.headless);
	}

	/** The container path adds exactly one further switch to the default set. */
	public void testContainerArgSet() {
		BrowserType.LaunchOptions options =
			BrowserSession.buildLaunchOptions(false, false, null, UNSANDBOXED);

		assertEquals("one switch more than the default set: " + options.args, 3, options.args.size());
		assertEquals("--disable-dev-shm-usage", options.args.get(0));
		assertEquals(WEBRTC_SWITCH, options.args.get(1));
		assertEquals(NO_SANDBOX_SWITCH, options.args.get(2));
	}

	/**
	 * Headed + evasion compose cleanly: the full-build channel is still selected and it does not
	 * flip the launch back to headless - the visible-window request wins ({@code headless == false}),
	 * so the headed launch toggle is not broken.
	 */
	public void testHeadedEvasionDoesNotConflict() {
		BrowserType.LaunchOptions options = BrowserSession.buildLaunchOptions(true, true, null, SANDBOXED);

		assertEquals("headed still selects the full Chromium build", "chromium", options.channel);
		assertEquals("headed launches a visible window", Boolean.FALSE, options.headless);
	}

	/** Headed without evasion: same full build, visible window, no evasion switch. */
	public void testHeadedWithoutEvasion() {
		BrowserType.LaunchOptions options = BrowserSession.buildLaunchOptions(true, false, null, SANDBOXED);

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
			BrowserSession.buildLaunchOptions(false, false, "http://127.0.0.1:41234", SANDBOXED);

		assertNotNull("the filtering proxy is pinned at launch level", options.proxy);
		assertEquals("http://127.0.0.1:41234", options.proxy.server);
		assertEquals("pinning a proxy does not change the build selection", "chromium", options.channel);
		assertEquals("pinning a proxy does not change the headless toggle", Boolean.TRUE,
			options.headless);
		assertEquals("pinning a proxy does not change the launch args", 2, options.args.size());
	}

	/** The proxy composes with evasion and with the headed toggle; none of them displaces another. */
	public void testProxyComposesWithEvasionAndHeaded() {
		BrowserType.LaunchOptions options =
			BrowserSession.buildLaunchOptions(true, true, "http://127.0.0.1:41235", SANDBOXED);

		assertEquals("http://127.0.0.1:41235", options.proxy.server);
		assertEquals("evasion still selects the full Chromium build", "chromium", options.channel);
		assertEquals("headed still launches a visible window", Boolean.FALSE, options.headless);
		assertTrue("evasion still adds its blink-feature switch",
			options.args.contains("--disable-blink-features=AutomationControlled"));
	}

	/** No proxy given (test fixtures with no network at all): the browser is left unproxied. */
	public void testNoProxyLeavesBrowserUnproxied() {
		assertNull(BrowserSession.buildLaunchOptions(false, false, null, SANDBOXED).proxy);
		assertNull("a blank proxy server is treated as none",
			BrowserSession.buildLaunchOptions(false, false, "  ", SANDBOXED).proxy);
	}

	private static void restore(String property, String previous) {
		if (previous == null) {
			System.clearProperty(property);
		} else {
			System.setProperty(property, previous);
		}
	}
}
