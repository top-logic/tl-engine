/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.util.List;

import org.junit.runner.RunWith;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.junit.AssumptionAwareRunner;
import com.top_logic.tl.ai.core.tools.web.BrowserSession.DomainPolicy;
import com.top_logic.tl.ai.core.tools.web.BrowserToolsService.InteractiveElement;
import com.top_logic.tl.ai.core.tools.web.BrowserToolsService.PageView;

/**
 * Gated end-to-end tests for the post-navigation <b>settle</b>: after a page load and before the
 * capture, the browser waits until the DOM goes quiet (no mutation for a short debounce) or a hard
 * cap elapses, so content injected shortly <em>after</em> navigation returns - the common
 * cookie-consent-banner / lazy-widget pattern - is present in the snapshot without a manual
 * re-snapshot.
 *
 * <p>
 * These launch a real Chromium (gated behind {@link BrowserE2e}, like the other
 * browser E2E tests) but need <b>no network</b>: a fixture whose script appends a
 * {@code <button id="late">} ~800 ms after load is injected straight into the page and taken
 * through the real {@link BrowserSession#settleAfterLoad() settle} path. With the settle on (default)
 * the button is captured; with it disabled ({@code navigation-settle-millis=0}) the same load misses
 * it - proving the settle is what closes the gap. A separate quiet-page test proves a still page
 * settles at ~the debounce window, far under the cap, so fast pages are not meaningfully slowed.
 * </p>
 */
@RunWith(AssumptionAwareRunner.class)
public class TestBrowserSettle extends TestCase {

	/** Allow-any policy: no real navigation happens (content is injected), so it is irrelevant. */
	private static final DomainPolicy ALLOW_ANY = host -> true;

	private static final int TIMEOUT_MILLIS = 30_000;

	/** The production-default settle cap. */
	private static final int DEFAULT_SETTLE_MILLIS = 1500;

	/** Milliseconds after load at which the fixture appends its late button. */
	private static final int LATE_INJECT_MILLIS = 800;

	/** The accessible name (and marker) of the late-injected button. */
	private static final String LATE_BUTTON_NAME = "Accept";

	/**
	 * A page that appends a {@code <button id="late">Accept</button>} ~800 ms after load - the shape
	 * of a consent banner or lazy widget that arrives a beat after navigation returns.
	 */
	private static final String LATE_INJECTION_FIXTURE =
		"<!doctype html><html><body>"
			+ "<h1>Loaded</h1>"
			+ "<script>"
			+ "  setTimeout(function() {"
			+ "    var b = document.createElement('button');"
			+ "    b.id = 'late';"
			+ "    b.textContent = '" + LATE_BUTTON_NAME + "';"
			+ "    document.body.appendChild(b);"
			+ "  }, " + LATE_INJECT_MILLIS + ");"
			+ "</script>"
			+ "</body></html>";

	/** A static page that never mutates after load - it should settle at ~the debounce window. */
	private static final String QUIET_FIXTURE =
		"<!doctype html><html><body>"
			+ "<h1>Quiet</h1>"
			+ "<button id='ready'>Ready</button>"
			+ "</body></html>";

	/**
	 * With the settle on (default cap), a single page load captures a button injected ~800 ms after
	 * load: the late button is present in the elements and has been assigned a ref.
	 */
	public void testLateInjectedContentCapturedWhenSettleOn() {
		BrowserE2e.assumeEnabled();
		BrowserSession session = open(DEFAULT_SETTLE_MILLIS);
		try {
			PageView view = session.loadHtmlWithSettleForTest(LATE_INJECTION_FIXTURE);
			assertNotNull("with the settle on, the late-injected button is captured (has a ref)",
				refWithName(view.elements(), LATE_BUTTON_NAME));
			assertTrue("the late button is present in the rendered HTML", view.html().contains("id=\"late\""));
		} finally {
			session.close();
		}
	}

	/**
	 * With the settle disabled ({@code navigation-settle-millis=0}), the very same page load captures
	 * <em>before</em> the ~800 ms injection, so the late button is absent - this is exactly the gap
	 * the settle closes (the previous immediate-capture behaviour).
	 */
	public void testLateInjectedContentMissedWhenSettleOff() {
		BrowserE2e.assumeEnabled();
		BrowserSession session = open(0);
		try {
			PageView view = session.loadHtmlWithSettleForTest(LATE_INJECTION_FIXTURE);
			assertNull("with the settle off, the capture happens before the late injection",
				refWithName(view.elements(), LATE_BUTTON_NAME));
			assertFalse("the late button is not yet in the DOM at capture time",
				view.html().contains("id=\"late\""));
		} finally {
			session.close();
		}
	}

	/**
	 * A still page settles fast: with a deliberately large cap (5 s), a page that never mutates
	 * resolves at ~the debounce window, well under the cap - so the settle does not meaningfully slow
	 * quiet pages. Also asserts the page was captured normally (its static button got a ref).
	 */
	public void testQuietPageSettlesWellUnderCap() {
		BrowserE2e.assumeEnabled();
		int largeCap = 5_000;
		BrowserSession session = open(largeCap);
		try {
			long startNanos = System.nanoTime();
			PageView view = session.loadHtmlWithSettleForTest(QUIET_FIXTURE);
			long elapsedMillis = (System.nanoTime() - startNanos) / 1_000_000L;

			assertNotNull("the quiet page is captured normally", refWithName(view.elements(), "Ready"));
			assertTrue("a quiet page settles well under the cap (debounce-bound, not cap-bound): was "
				+ elapsedMillis + " ms, cap " + largeCap + " ms", elapsedMillis < 3_000L);
		} finally {
			session.close();
		}
	}

	/** Opens a session with the given settle cap (ms); {@code 0} disables the settle. */
	private static BrowserSession open(int settleMillis) {
		return BrowserSession.open("settle-" + settleMillis, TIMEOUT_MILLIS, settleMillis, ALLOW_ANY,
			null, false, false);
	}

	private static String refWithName(List<InteractiveElement> elements, String name) {
		for (InteractiveElement element : elements) {
			if (name.equals(element.name())) {
				return element.ref();
			}
		}
		return null;
	}}
