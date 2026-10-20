/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assume;
import org.junit.runner.RunWith;

import junit.framework.TestCase;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.WaitUntilState;

import com.top_logic.tl.ai.core.junit.AssumptionAwareRunner;
import com.top_logic.tl.ai.core.tools.web.BrowserSession.DomainPolicy;
import com.top_logic.tl.ai.core.tools.web.BrowserToolsService.PageView;
import com.top_logic.tl.ai.core.tools.web.FilteringProxy.AddressResolver;
import com.top_logic.tl.ai.core.tools.web.FilteringProxy.RequestPolicy;
import com.top_logic.tl.ai.core.tools.web.FilteringProxy.Target;

/**
 * End-to-end tests, with a real Chromium, that a page can no longer make the browser emit <b>UDP</b>
 * towards an address of its choosing - the SSRF egress path the {@link FilteringProxy} cannot cover,
 * because <em>WebRTC does not honour an HTTP proxy</em>. Gated behind {@link BrowserE2e}.
 *
 * <p>
 * <b>Why the fixture is shaped this way.</b> The proxy tests of {@link TestBrowserProxyEnforcement}
 * measure TCP connections; nothing there can see a datagram, and a page that constructs an
 * {@code RTCPeerConnection} sends STUN/UDP straight out of the browser process. So this fixture points
 * a page's ICE gathering at a {@link RecordingUdpListener} on loopback and counts the datagrams that
 * arrive. {@link RecordingUdpListener#packetCount()} is the assertion that matters throughout: not
 * "did the tool report an error" but "did a packet reach the forbidden destination at all".
 * </p>
 *
 * <p>
 * <b>The negative control is the point.</b> A test that only asserts {@code packetCount() == 0} would
 * pass just as happily against a browser that never ran the page's script, a fixture that mis-spelled
 * the STUN address, or a switch that Chromium silently ignored - and several plausible-looking WebRTC
 * switches <em>are</em> silently ignored on this build. So the same page is loaded twice into the same
 * Chromium build, differing in nothing but the switch under test: without it the listener must be
 * contacted, with it never. Everything is offline and on loopback except
 * {@link #testProductionSessionNeverEmitsWebRtcUdpEndToEnd}, which needs one DNS lookup and skips
 * itself without it.
 * </p>
 */
@RunWith(AssumptionAwareRunner.class)
public class TestBrowserWebRtcEgress extends TestCase {

	/** A public hostname used as the permitted origin of the fixture page; only DNS is needed. */
	private static final String PERMITTED_HOST = "example.com";

	/** Domain policy that permits every top-level origin, so only SSRF and the proxy can refuse. */
	private static final DomainPolicy ALLOW_ANY = host -> true;

	/** Domain policy for the proxy that refuses nothing, so only SSRF can refuse. */
	private static final RequestPolicy PERMIT_ALL = host -> null;

	private static final int TIMEOUT_MILLIS = 30_000;

	/** How long to wait for the page's ICE gathering to run and report back. */
	private static final int BEACON_TIMEOUT_MILLIS = 20_000;

	/** The launch args of a production session, minus the WebRTC switch - the negative control. */
	private static final List<String> BASE_ARGS = List.of("--disable-dev-shm-usage", "--no-sandbox");

	/** The switch under test, as {@code BrowserSession} passes it. */
	private static final String WEBRTC_SWITCH = "--webrtc-ip-handling-policy=disable_non_proxied_udp";

	/**
	 * <b>The negative control.</b> With the switch absent - and nothing else changed - a page really
	 * does push UDP straight out of the browser to an address it chose, past every HTTP-level control.
	 * If this test ever stops seeing packets, the rest of this class proves nothing and the fixture is
	 * what needs fixing, not the product.
	 */
	public void testWebRtcUdpEscapesWithoutTheSwitchNegativeControl() {
		BrowserE2e.assumeEnabled();
		Integer packets = probeUdpEgress(null, BASE_ARGS);
		if (packets == null) {
			return;
		}
		assertTrue("WITHOUT the switch a page reaches an arbitrary UDP address - this is the bypass "
			+ "issue #232 is about; if this is 0 the fixture no longer measures anything", packets > 0);
	}

	/**
	 * <b>The hole is closed.</b> The identical page, in the identical Chromium build, with the single
	 * added switch: not one datagram leaves towards the listener.
	 */
	public void testWebRtcUdpIsSuppressedByTheSwitch() {
		BrowserE2e.assumeEnabled();
		Integer packets = probeUdpEgress(null, concat(BASE_ARGS, WEBRTC_SWITCH));
		if (packets == null) {
			return;
		}
		assertEquals("WITH the switch the UDP listener is never contacted", 0, packets.intValue());
	}

	/**
	 * The switch is <b>compiled out of {@code chrome-headless-shell}</b>: passing it to the shell
	 * changes nothing and the datagrams still escape. That is the whole reason
	 * {@code BrowserSession.buildLaunchOptions} pins the channel to the full Chromium build rather
	 * than letting Playwright default a headless launch to the shell - so this test guards the pin: if
	 * it is removed, the WebRTC hole silently reopens on the default path.
	 *
	 * <p>
	 * A failure here would be good news rather than a defect - it would mean a newer Chromium honours
	 * the switch in the shell too, and that the channel pin (and its deployment prerequisite) could be
	 * reconsidered.
	 * </p>
	 */
	public void testHeadlessShellCannotHonourTheSwitchWhichIsWhyTheChannelIsPinned() {
		BrowserE2e.assumeEnabled();
		Integer packets = probeUdpEgress("chromium-headless-shell", concat(BASE_ARGS, WEBRTC_SWITCH));
		if (packets == null) {
			return;
		}
		assertTrue("the headless shell ignores the switch, which is why the full-Chromium channel is "
			+ "pinned; if this is now 0, revisit whether the pin is still needed", packets > 0);
	}

	/**
	 * The whole production path, through {@link BrowserSession} and the {@link FilteringProxy}: a page
	 * served from a permitted origin emits no UDP at all, <b>and still renders</b>. The second half
	 * matters as much as the first - turning WebRTC off must not cost ordinary browsing, so the same
	 * navigation that fails to leak is asserted to have produced a real, readable page.
	 */
	public void testProductionSessionNeverEmitsWebRtcUdpEndToEnd() {
		BrowserE2e.assumeEnabled();
		Assume.assumeTrue("DNS does not resolve " + PERMITTED_HOST + " - no network", resolves(PERMITTED_HOST));
		try (RecordingUdpListener stun = RecordingUdpListener.startOrNull()) {
			if (stun == null) {
				return;
			}
			try (RecordingServer origin = RecordingServer.serving(probePage(stun))) {
				try (FilteringProxy proxy = FilteringProxy.start(PERMIT_ALL, resolvingTo(origin))) {
					BrowserSession session = BrowserSession.open("webrtc-egress", TIMEOUT_MILLIS,
						BrowserSession.DEFAULT_ACTION_TIMEOUT_MILLIS, 0, ALLOW_ANY, null, false, false,
						proxy);
					try {
						PageView view = session.navigate("http://" + PERMITTED_HOST + "/");

						assertTrue("ordinary browsing is unaffected by WebRTC being off: " + view.html(),
							view.html().contains("webrtc egress probe"));
						assertEquals("the page was served normally", 200, view.status());
					} finally {
						awaitBeacon(origin);
						session.close();
					}

					assertTrue("the page really ran (otherwise the measurement is vacuous): "
						+ origin.requestLines(), reportedOutcome(origin) != null);
					assertEquals("a production session emits NO WebRTC UDP - issue #232", 0,
						stun.packetCount());
				}
			}
		}
	}

	// --- Fixture helpers. ---

	/**
	 * Loads the probe page into a bare Chromium launched with exactly {@code args}, and returns how
	 * many datagrams reached the STUN listener.
	 *
	 * <p>
	 * Deliberately a raw Playwright launch rather than a {@link BrowserSession}: the control and the
	 * treatment must differ in the switch and nothing else, which the production seam - which always
	 * adds the switch - cannot express.
	 * </p>
	 *
	 * @param channel
	 *        The Playwright channel to launch, or {@code null} for the full-Chromium default this
	 *        product pins.
	 * @return The datagram count, or {@code null} when the harness cannot run the probe at all (no UDP
	 *         socket, or that browser build is not installed), so the caller skips cleanly.
	 */
	private static Integer probeUdpEgress(String channel, List<String> args) {
		try (RecordingUdpListener stun = RecordingUdpListener.startOrNull()) {
			if (stun == null) {
				return null;
			}
			try (RecordingServer origin = RecordingServer.serving(probePage(stun))) {
				try (Playwright playwright = Playwright.create()) {
					BrowserType.LaunchOptions options = new BrowserType.LaunchOptions()
						.setHeadless(true)
						.setArgs(args)
						.setChannel(channel == null ? "chromium" : channel);
					Browser browser;
					try {
						browser = playwright.chromium().launch(options);
					} catch (PlaywrightException ex) {
						// That build is not installed in this browser cache: not a product failure.
						return null;
					}
					try {
						Page page = browser.newPage();
						page.navigate(origin.origin() + "/", new Page.NavigateOptions()
							.setWaitUntil(WaitUntilState.DOMCONTENTLOADED).setTimeout(TIMEOUT_MILLIS));
						awaitBeacon(origin);
						assertNotNull("the probe page must have run, else nothing is being measured: "
							+ origin.requestLines(), reportedOutcome(origin));
					} finally {
						browser.close();
					}
				}
				return Integer.valueOf(stun.packetCount());
			}
		}
	}

	/**
	 * The probe page: it asks for a peer connection whose only ICE server is the loopback listener,
	 * then starts gathering, which is what makes Chromium send STUN over UDP. It reports back over
	 * HTTP once gathering has finished or a few seconds have passed - the latter is the normal case
	 * for the negative control, where the listener never answers, so gathering stays pending.
	 */
	private static String probePage(RecordingUdpListener stun) {
		return "<!doctype html><html><body><h1>webrtc egress probe</h1><script>\n"
			+ "(async () => {\n"
			+ "  let outcome = 'none';\n"
			+ "  try {\n"
			+ "    const pc = new RTCPeerConnection("
			+ "{iceServers:[{urls:'stun:" + stun.address() + "'}]});\n"
			+ "    pc.createDataChannel('probe');\n"
			+ "    await pc.setLocalDescription(await pc.createOffer());\n"
			+ "    await new Promise(done => {\n"
			+ "      pc.onicegatheringstatechange = () => "
			+ "{ if (pc.iceGatheringState === 'complete') done(); };\n"
			+ "      setTimeout(done, 6000);\n"
			+ "    });\n"
			+ "    outcome = pc.iceGatheringState;\n"
			+ "  } catch (e) { outcome = 'error'; }\n"
			+ "  fetch('/probe-done?' + outcome);\n"
			+ "})();\n"
			+ "</script></body></html>";
	}

	/** Waits until the probe page reports back, or the timeout elapses. */
	private static void awaitBeacon(RecordingServer origin) {
		long deadline = System.currentTimeMillis() + BEACON_TIMEOUT_MILLIS;
		while (System.currentTimeMillis() < deadline) {
			if (reportedOutcome(origin) != null) {
				return;
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
				return;
			}
		}
	}

	/** The probe page's self-reported ICE gathering outcome, or {@code null} if it has not reported. */
	private static String reportedOutcome(RecordingServer origin) {
		for (String line : origin.requestLines()) {
			int marker = line.indexOf("/probe-done?");
			if (marker >= 0) {
				return line.substring(marker + "/probe-done?".length()).split(" ")[0];
			}
		}
		return null;
	}

	/** {@code args} plus one more switch - the treatment, next to the untouched control. */
	private static List<String> concat(List<String> args, String extra) {
		List<String> result = new ArrayList<>(args);
		result.add(extra);
		return result;
	}

	/**
	 * An address resolver that maps {@link #PERMITTED_HOST} to the local fixture server and leaves
	 * every other destination to the real, production SSRF resolver.
	 */
	private static AddressResolver resolvingTo(RecordingServer fixture) {
		AddressResolver real = FilteringProxy.ssrfResolver();
		return (host, port) -> {
			if (PERMITTED_HOST.equalsIgnoreCase(host)) {
				return Target.to(new InetSocketAddress(InetAddress.getLoopbackAddress(), fixture.port()));
			}
			return real.resolve(host, port);
		};
	}

	/** Whether {@code host} can be resolved at all; the in-browser route needs that independently. */
	private static boolean resolves(String host) {
		try {
			InetAddress.getByName(host);
			return true;
		} catch (UnknownHostException ex) {
			return false;
		}
	}}
