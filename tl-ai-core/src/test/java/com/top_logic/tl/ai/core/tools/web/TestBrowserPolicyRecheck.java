/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import org.junit.Assume;
import org.junit.runner.RunWith;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.junit.AssumptionAwareRunner;
import com.top_logic.tl.ai.core.tools.web.BrowserSession.DomainPolicy;
import com.top_logic.tl.ai.core.tools.web.BrowserToolsService.PageView;
import com.top_logic.tl.ai.core.tools.web.BrowserToolsService.ScreenshotCapture;
import com.top_logic.tl.ai.core.tools.web.FilteringProxy.AddressResolver;
import com.top_logic.tl.ai.core.tools.web.FilteringProxy.RequestPolicy;
import com.top_logic.tl.ai.core.tools.web.FilteringProxy.Target;

/**
 * End-to-end tests, with a real Chromium, that <b>every</b> verb re-checks the document it is about to
 * hand to the model - including the three that do not navigate themselves.
 *
 * <p>
 * <b>The defect.</b> {@code browserSnapshot} performed no final-URL re-validation at all,
 * {@code browserType} never consulted the recorded off-policy hop, and {@code browserScreenshot} did
 * neither - it captured the image and persisted it, so the allowlist was bypassed for the image path
 * even though the text path caught the very same page. A page can take its own top-level frame
 * somewhere the policy forbids at any moment - a timer, a form submit, a server-side redirect - and the
 * {@link FilteringProxy} answers a refused main-frame request with a {@code 403} page that
 * <em>commits as the document</em>. On those verbs the result was therefore returned as ordinary
 * page content, which is exactly what the navigating verbs' policy re-check and the proxy-refusal
 * guard exist to prevent.
 * </p>
 *
 * <p>
 * <b>The fixture.</b> A page must be served locally (deterministic, offline) while being reachable
 * under a <em>public</em> host name (the SSRF layer refuses loopback, as it must), so the proxy's
 * address resolver is substituted to pin one public name to a local fixture - the same technique
 * {@link TestBrowserProxyEnforcement} uses, and for the same reason: the policy decision under test
 * stays production code. The domain policy permits that one host only, so the hop the fixture page
 * performs is off-policy. Only DNS resolution of the two names is needed; the tests skip themselves
 * if that is unavailable.
 * </p>
 *
 * <p>
 * Gated behind {@link BrowserE2e}.
 * </p>
 */
@RunWith(AssumptionAwareRunner.class)
public class TestBrowserPolicyRecheck extends TestCase {

	/** The one host the policy permits; only its DNS resolution is needed. */
	private static final String PERMITTED_HOST = "example.com";

	/** A resolvable host the policy does not permit - where the fixture page tries to go. */
	private static final String OFF_POLICY_HOST = "www.example.com";

	/** Permits the fixture's own origin and nothing else. */
	private static final DomainPolicy ONLY_PERMITTED_HOST =
		host -> PERMITTED_HOST.equalsIgnoreCase(host);

	/** The proxy refuses nothing by domain, so the in-browser route is what has to catch the hop. */
	private static final RequestPolicy PERMIT_ALL = host -> null;

	private static final int TIMEOUT_MILLIS = 30_000;

	/**
	 * How long after the page loads the hop is attempted. Comfortably after the navigation has
	 * returned, so the hop belongs to the verb under test rather than to {@code navigate}.
	 */
	private static final int HOP_DELAY_MILLIS = 3_000;

	/** A page that takes its own top-level frame off-policy a few seconds after it loaded. */
	private static final String DELAYED_HOP_FIXTURE = ""
		+ "<!doctype html><html><head><title>fixture</title></head><body>"
		+ "<h1>permitted fixture page</h1>"
		+ "<script>setTimeout(function() { location.href = 'http://" + OFF_POLICY_HOST + "/'; }, "
		+ HOP_DELAY_MILLIS + ");</script>"
		+ "</body></html>";

	/** The page behind the off-policy host - what a leaked capture would show. */
	private static final String OFF_POLICY_FIXTURE = ""
		+ "<!doctype html><html><head><title>off-allowlist</title></head><body>"
		+ "<h1>off-allowlist page content</h1></body></html>";

	/** A page whose text field takes the top-level frame off-policy as soon as it is filled. */
	private static final String HOP_ON_INPUT_FIXTURE = ""
		+ "<!doctype html><html><head><title>fixture</title></head><body>"
		+ "<h1>permitted fixture page</h1>"
		+ "<input id=\"q\" name=\"q\" aria-label=\"query\" "
		+ "oninput=\"location.href = 'http://" + OFF_POLICY_HOST + "/';\">"
		+ "</body></html>";

	/**
	 * <b>browserSnapshot.</b> The page hops off-policy after the navigation returned; the snapshot that
	 * follows must report the refusal rather than hand back the page it is sitting on.
	 */
	public void testSnapshotReportsAnOffPolicyHopInsteadOfContent() {
		BrowserE2e.assumeEnabled();
		Assume.assumeTrue("DNS does not resolve the fixture hosts - no network", resolvable());
		try (RecordingServer origin = RecordingServer.serving(DELAYED_HOP_FIXTURE);
				FilteringProxy proxy = FilteringProxy.start(PERMIT_ALL, resolvingTo(origin))) {
			BrowserSession session = openSession("snapshot-recheck", proxy);
			try {
				PageView loaded = session.navigate("http://" + PERMITTED_HOST + "/");
				assertTrue("the permitted page itself still loads: " + loaded.finalUrl(),
					loaded.html().contains("permitted fixture page"));

				// Let the page attempt its hop; the route aborts it and records it.
				sleep(HOP_DELAY_MILLIS + 2_000);

				try {
					PageView snapshot = session.snapshot();
					fail("a snapshot taken after an off-policy hop must not return page content: "
						+ snapshot.finalUrl());
				} catch (BrowserPolicyException expected) {
					assertTrue("names the origin the policy refused: " + expected.getMessage(),
						expected.getMessage().contains(OFF_POLICY_HOST));
					assertTrue("and says the page was not loaded: " + expected.getMessage(),
						expected.getMessage().contains("NOT loaded"));
				}
			} finally {
				session.close();
			}
		}
	}

	/**
	 * A snapshot of a page that did <b>not</b> misbehave is unaffected, and a refused hop is reported
	 * once rather than on every later snapshot.
	 *
	 * <p>
	 * Reporting it repeatedly is the failure mode that consuming the record avoids, and there is no way
	 * back to the page for the second snapshot to return either: aborting a main-frame navigation makes
	 * Chromium commit its own error document ({@code chrome-error://chromewebdata/}), so the session is
	 * left on a page with no origin at all. What the second snapshot must not do is keep failing with a
	 * refusal that has already been reported - the model would have no way to tell that it is looking at
	 * a stale verdict rather than a fresh one.
	 * </p>
	 */
	public void testAnOffPolicyHopIsReportedOnceNotOnEverySnapshot() {
		BrowserE2e.assumeEnabled();
		Assume.assumeTrue("DNS does not resolve the fixture hosts - no network", resolvable());
		try (RecordingServer origin = RecordingServer.serving(DELAYED_HOP_FIXTURE);
				FilteringProxy proxy = FilteringProxy.start(PERMIT_ALL, resolvingTo(origin))) {
			BrowserSession session = openSession("snapshot-clean", proxy);
			try {
				session.navigate("http://" + PERMITTED_HOST + "/");

				// Before the hop is due: an ordinary re-capture of an ordinary page.
				PageView snapshot = session.snapshot();
				assertTrue("the page is returned as before: " + snapshot.finalUrl(),
					snapshot.html().contains("permitted fixture page"));

				sleep(HOP_DELAY_MILLIS + 2_000);
				try {
					session.snapshot();
					fail("the hop must be reported");
				} catch (BrowserPolicyException expected) {
					// Reported once ...
				}
				PageView afterwards = session.snapshot();
				assertFalse("... and the session is not stuck reporting it: " + afterwards.finalUrl(),
					afterwards.finalUrl().contains(OFF_POLICY_HOST));
			} finally {
				session.close();
			}
		}
	}

	/**
	 * <b>browserScreenshot.</b> An image of the page is page content too: when the session sits on a
	 * document the policy forbids, the capture must be refused rather than handed back.
	 *
	 * <p>
	 * Nothing can be persisted from a refused capture, and that is what the assertion checks: the verb
	 * that stores the {@code Screenshot} object ({@code BrowserTool.browserScreenshot}) creates it from
	 * the returned {@link ScreenshotCapture}, so as long as no capture is ever returned there are no
	 * bytes to persist and none to read back later through {@code viewDocument}. A screenshot taken
	 * before the page misbehaves is unaffected, which is asserted first.
	 * </p>
	 */
	public void testScreenshotReportsAnOffPolicyHopInsteadOfImageBytes() {
		BrowserE2e.assumeEnabled();
		Assume.assumeTrue("DNS does not resolve the fixture hosts - no network", resolvable());
		try (RecordingServer origin = RecordingServer.serving(DELAYED_HOP_FIXTURE);
				FilteringProxy proxy = FilteringProxy.start(PERMIT_ALL, resolvingTo(origin))) {
			BrowserSession session = openSession("screenshot-recheck", proxy);
			try {
				PageView loaded = session.navigate("http://" + PERMITTED_HOST + "/");
				assertTrue("the permitted page itself still loads: " + loaded.finalUrl(),
					loaded.html().contains("permitted fixture page"));

				// Before the hop is due: an ordinary capture of an ordinary page.
				ScreenshotCapture permitted = session.screenshot(false);
				assertTrue("the permitted page is captured as before: " + permitted.url(),
					permitted.png() != null && permitted.png().length > 0);

				// Let the page attempt its hop; the route aborts it and records it.
				sleep(HOP_DELAY_MILLIS + 2_000);

				try {
					ScreenshotCapture offPolicy = session.screenshot(false);
					fail("a screenshot taken after an off-policy hop must not return image bytes: "
						+ offPolicy.png().length + " bytes from " + offPolicy.url());
				} catch (BrowserPolicyException expected) {
					assertTrue("names the origin the policy refused: " + expected.getMessage(),
						expected.getMessage().contains(OFF_POLICY_HOST));
					assertTrue("and says the page was not loaded: " + expected.getMessage(),
						expected.getMessage().contains("NOT loaded"));
				}
			} finally {
				session.close();
			}
		}
	}

	/**
	 * <b>browserScreenshot, on a document the browser really did commit.</b> A screenshot of an
	 * off-allowlist document the session is sitting on must be refused on that document's own origin.
	 *
	 * <p>
	 * The shipped default configuration ({@code apply-allowlist-to-all-requests} off) lets the proxy
	 * forward a server-side redirect off the allowlist, so the off-allowlist document is fetched and
	 * <em>commits</em>. The navigation that pulled it in is refused - and in being refused it consumes
	 * the recorded hop, leaving the session on that document. The screenshot that follows therefore has
	 * nothing recorded to go by, only the current URL, which is the second half of the re-check.
	 * </p>
	 */
	public void testScreenshotOfACommittedOffPolicyDocumentIsRefused() {
		BrowserE2e.assumeEnabled();
		Assume.assumeTrue("DNS does not resolve the fixture hosts - no network", resolvable());
		try (RecordingServer offPolicy = RecordingServer.serving(OFF_POLICY_FIXTURE);
				RecordingServer origin = RecordingServer
					.start(target -> RecordingServer.Reply.redirect("http://" + OFF_POLICY_HOST + "/"));
				FilteringProxy proxy = FilteringProxy.start(PERMIT_ALL, resolvingTo(origin, offPolicy))) {
			BrowserSession session = openSession("screenshot-redirect-recheck", proxy);
			try {
				try {
					session.navigate("http://" + PERMITTED_HOST + "/");
					fail("a server-side redirect off the allowlist must be refused");
				} catch (BrowserPolicyException expected) {
					// Reported once, and consumed with it - which is what leaves the session on the
					// document below.
				}
				assertTrue("the off-allowlist document really was fetched and committed; without that "
					+ "this test would not be exercising the leak at all", offPolicy.contactCount() > 0);

				try {
					ScreenshotCapture leaked = session.screenshot(false);
					fail("a screenshot of the committed off-allowlist document must not return image "
						+ "bytes: " + leaked.png().length + " bytes from " + leaked.url());
				} catch (BrowserPolicyException expected) {
					assertTrue("names the origin the policy refused: " + expected.getMessage(),
						expected.getMessage().contains(OFF_POLICY_HOST));
					assertTrue("and says the page was not loaded: " + expected.getMessage(),
						expected.getMessage().contains("NOT loaded"));
				}
			} finally {
				session.close();
			}
		}
	}

	/**
	 * <b>browserType.</b> Filling a field can submit a form; when that takes the top-level frame
	 * off-policy the fill must be reported as the policy refusal it caused, not as a capture of the page
	 * the browser has not left yet.
	 */
	public void testTypeReportsAnOffPolicyHopInsteadOfContent() {
		BrowserE2e.assumeEnabled();
		Assume.assumeTrue("DNS does not resolve the fixture hosts - no network", resolvable());
		try (RecordingServer origin = RecordingServer.serving(HOP_ON_INPUT_FIXTURE);
				FilteringProxy proxy = FilteringProxy.start(PERMIT_ALL, resolvingTo(origin))) {
			BrowserSession session = openSession("type-recheck", proxy);
			try {
				PageView loaded = session.navigate("http://" + PERMITTED_HOST + "/");
				String ref = refOfFirstTextField(loaded);
				assertNotNull("the fixture's input must be addressable: " + loaded.elements(), ref);

				try {
					session.type(ref, "anything");
					fail("typing that navigates off-policy must not return page content");
				} catch (BrowserPolicyException expected) {
					assertTrue("names the origin the policy refused: " + expected.getMessage(),
						expected.getMessage().contains(OFF_POLICY_HOST));
				}
			} finally {
				session.close();
			}
		}
	}

	/** Typing into a field that does nothing is unaffected. */
	public void testTypeIntoAnOrdinaryFieldStillWorks() {
		BrowserE2e.assumeEnabled();
		Assume.assumeTrue("DNS does not resolve the fixture hosts - no network", resolvable());
		String fixture = "<!doctype html><html><head><title>fixture</title></head><body>"
			+ "<h1>permitted fixture page</h1>"
			+ "<input id=\"q\" name=\"q\" aria-label=\"query\"></body></html>";
		try (RecordingServer origin = RecordingServer.serving(fixture);
				FilteringProxy proxy = FilteringProxy.start(PERMIT_ALL, resolvingTo(origin))) {
			BrowserSession session = openSession("type-clean", proxy);
			try {
				PageView loaded = session.navigate("http://" + PERMITTED_HOST + "/");
				String ref = refOfFirstTextField(loaded);
				assertNotNull(ref);

				PageView typed = session.type(ref, "hello");

				assertTrue("the same page is captured as before: " + typed.finalUrl(),
					typed.html().contains("permitted fixture page"));
			} finally {
				session.close();
			}
		}
	}

	/**
	 * A session that has not navigated anywhere is on {@code about:blank}, which has no origin for a
	 * domain policy to judge. Snapshotting it must stay possible - the re-check governs documents
	 * fetched from somewhere, not the browser's own starting state.
	 */
	public void testSnapshotOfABlankSessionIsNotAPolicyError() {
		BrowserE2e.assumeEnabled();
		BrowserSession session = BrowserSession.open("blank-recheck", TIMEOUT_MILLIS,
			BrowserSession.DEFAULT_ACTION_TIMEOUT_MILLIS, 0, ONLY_PERMITTED_HOST, null, false, false, null);
		try {
			PageView blank = session.snapshot();

			assertTrue("about:blank is snapshotted, not refused: " + blank.finalUrl(),
				blank.finalUrl().startsWith("about:"));
		} finally {
			session.close();
		}
	}

	// --- Fixture helpers. ---

	/**
	 * An address resolver that maps {@link #PERMITTED_HOST} to the local fixture and leaves every other
	 * destination to the real, production SSRF resolver.
	 */
	private static AddressResolver resolvingTo(RecordingServer fixture) {
		return resolvingTo(fixture, null);
	}

	/**
	 * As {@link #resolvingTo(RecordingServer)}, additionally pinning {@link #OFF_POLICY_HOST} to a
	 * second local fixture so a hop that the policy forbids stays offline too - needed where the hop is
	 * not aborted in the browser but forwarded, and the document it answers with is the point.
	 */
	private static AddressResolver resolvingTo(RecordingServer fixture, RecordingServer offPolicyFixture) {
		AddressResolver real = FilteringProxy.ssrfResolver();
		return (host, port) -> {
			if (PERMITTED_HOST.equalsIgnoreCase(host)) {
				return Target.to(new InetSocketAddress(InetAddress.getLoopbackAddress(), fixture.port()));
			}
			if (offPolicyFixture != null && OFF_POLICY_HOST.equalsIgnoreCase(host)) {
				return Target
					.to(new InetSocketAddress(InetAddress.getLoopbackAddress(), offPolicyFixture.port()));
			}
			return real.resolve(host, port);
		};
	}

	private static BrowserSession openSession(String key, FilteringProxy proxy) {
		return BrowserSession.open(key, TIMEOUT_MILLIS, BrowserSession.DEFAULT_ACTION_TIMEOUT_MILLIS, 0,
			ONLY_PERMITTED_HOST, null, false, false, proxy);
	}

	/** The ref of the first text field the capture found, or {@code null}. */
	private static String refOfFirstTextField(PageView view) {
		return view.elements().stream()
			.filter(element -> "input".equalsIgnoreCase(element.tag()))
			.map(BrowserToolsService.InteractiveElement::ref)
			.findFirst().orElse(null);
	}

	/** Whether both fixture host names can be resolved; the in-browser route needs that independently. */
	private static boolean resolvable() {
		for (String host : new String[] { PERMITTED_HOST, OFF_POLICY_HOST }) {
			try {
				InetAddress.getByName(host);
			} catch (UnknownHostException ex) {
				return false;
			}
		}
		return true;
	}

	private static void sleep(long millis) {
		try {
			TimeUnit.MILLISECONDS.sleep(millis);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}}
