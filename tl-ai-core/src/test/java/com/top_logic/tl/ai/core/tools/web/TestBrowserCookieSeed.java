/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.tools.web.BrowserSession.DomainPolicy;

/**
 * Gated end-to-end test that a {@link BrowserSession} opened with an initial {@code storageState}
 * actually carries those cookies: the session's context is seeded from the JSON at creation, so
 * {@link BrowserSession#captureStorageState()} reads the seeded cookie straight back - proving the
 * pool-seed-on-open wiring without any network fixture.
 *
 * <p>
 * Launches a real Chromium, so it is gated behind {@code -Dtl.ai.browser.e2e=true} like the other
 * browser E2E tests. It needs <b>no network</b>: seeding and re-reading {@code storageState} never
 * navigates.
 * </p>
 */
public class TestBrowserCookieSeed extends TestCase {

	/** Allow-any policy: no navigation happens, so the policy is irrelevant. */
	private static final DomainPolicy ALLOW_ANY = host -> true;

	private static final int TIMEOUT_MILLIS = 30_000;

	private static final String COOKIE_NAME = "tl_seed_session";

	private static final String COOKIE_VALUE = "seed-value-abc123";

	private static final String COOKIE_DOMAIN = "example.org";

	/** A minimal, valid Playwright {@code storageState} with a single cookie. */
	private static final String SEED_STATE =
		"{\"cookies\":[{"
			+ "\"name\":\"" + COOKIE_NAME + "\","
			+ "\"value\":\"" + COOKIE_VALUE + "\","
			+ "\"domain\":\"" + COOKIE_DOMAIN + "\","
			+ "\"path\":\"/\",\"expires\":-1,\"httpOnly\":false,\"secure\":false,\"sameSite\":\"Lax\""
			+ "}],\"origins\":[]}";

	/**
	 * A session opened with a seed {@code storageState} exposes that cookie via
	 * {@link BrowserSession#captureStorageState()}; an unseeded session does not.
	 */
	public void testSeededSessionCarriesCookies() {
		if (!e2eEnabled()) {
			return;
		}
		BrowserSession seeded =
			BrowserSession.open("cookie-seed", TIMEOUT_MILLIS, 0, ALLOW_ANY, SEED_STATE, false, false);
		try {
			String captured = seeded.captureStorageState();
			assertNotNull(captured);
			assertTrue("seeded cookie name must round-trip: " + captured, captured.contains(COOKIE_NAME));
			assertTrue("seeded cookie value must round-trip: " + captured, captured.contains(COOKIE_VALUE));
			assertTrue("seeded cookie domain must round-trip: " + captured, captured.contains(COOKIE_DOMAIN));
		} finally {
			seeded.close();
		}

		BrowserSession clean = BrowserSession.open("cookie-clean", TIMEOUT_MILLIS, ALLOW_ANY);
		try {
			String captured = clean.captureStorageState();
			assertNotNull(captured);
			assertFalse("a clean session must not carry the seed cookie", captured.contains(COOKIE_VALUE));
		} finally {
			clean.close();
		}
	}

	private static boolean e2eEnabled() {
		return Boolean.getBoolean("tl.ai.browser.e2e");
	}
}
