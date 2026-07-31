/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.tools.web.BrowserSession.DomainPolicy;
import com.top_logic.tl.ai.core.tools.web.BrowserToolsService.PageView;
import com.top_logic.tl.ai.core.tools.web.FilteringProxy.AddressResolver;
import com.top_logic.tl.ai.core.tools.web.FilteringProxy.RequestPolicy;
import com.top_logic.tl.ai.core.tools.web.FilteringProxy.Target;

/**
 * End-to-end tests, with a real Chromium, that <b>every</b> verb re-checks the document it is about to
 * hand to the model - including the two that do not navigate themselves.
 *
 * <p>
 * <b>The defect.</b> {@code browserSnapshot} performed no final-URL re-validation at all, and
 * {@code browserType} never consulted the recorded off-policy hop. A page can take its own top-level
 * frame somewhere the policy forbids at any moment - a timer, a form submit - and the
 * {@link FilteringProxy} answers a refused main-frame request with a {@code 403} page that
 * <em>commits as the document</em>. On those two verbs the result was therefore returned as ordinary
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
 * Gated behind {@code -Dtl.ai.browser.e2e=true}.
 * </p>
 */
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
		if (!e2eEnabled() || !resolvable()) {
			return;
		}
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
	 * A snapshot of a page that did <b>not</b> misbehave is unaffected, and a hop is reported once
	 * rather than poisoning every later snapshot of a page the session is legitimately on.
	 */
	public void testSnapshotOfAPermittedPageStillWorks() {
		if (!e2eEnabled() || !resolvable()) {
			return;
		}
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
				assertTrue("... and not again on every later snapshot of the page the session is on: "
					+ afterwards.finalUrl(), afterwards.html().contains("permitted fixture page"));
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
		if (!e2eEnabled() || !resolvable()) {
			return;
		}
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
		if (!e2eEnabled() || !resolvable()) {
			return;
		}
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
		if (!e2eEnabled()) {
			return;
		}
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
		AddressResolver real = FilteringProxy.ssrfResolver();
		return (host, port) -> {
			if (PERMITTED_HOST.equalsIgnoreCase(host)) {
				return Target.to(new InetSocketAddress(InetAddress.getLoopbackAddress(), fixture.port()));
			}
			return real.resolve(host, port);
		};
	}

	private static BrowserSession openSession(String key, FilteringProxy proxy) {
		return BrowserSession.open(key, TIMEOUT_MILLIS, BrowserSession.DEFAULT_ACTION_TIMEOUT_MILLIS, 0,
			ONLY_PERMITTED_HOST, null, false, false, proxy.serverUrl());
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
	}

	private static boolean e2eEnabled() {
		return Boolean.getBoolean("tl.ai.browser.e2e");
	}
}
