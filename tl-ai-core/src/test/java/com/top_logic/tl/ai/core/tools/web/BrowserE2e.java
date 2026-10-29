/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import org.junit.Assume;

import com.top_logic.tl.ai.core.junit.AssumptionAwareRunner;

/**
 * The switch that turns the browser end-to-end tests on, and the {@link Assume assumption} that
 * makes a run without it report as <b>skipped</b>.
 *
 * <p>
 * The tests behind this switch launch a real Chromium, and some of them additionally need the
 * internet, so they cannot run on every build. They used to opt out by returning from the test
 * method - which surefire counts as a passed test, so the whole browser suite reported green
 * without having started a browser or asserted anything. {@link #assumeEnabled()} instead throws a
 * violated assumption, and {@link AssumptionAwareRunner} - which every gated test class is
 * {@code @RunWith} - turns that into a reported skip.
 * </p>
 *
 * <h2>Running the gated suite locally</h2>
 *
 * <pre>
 * mvn -pl tl-ai-core -am test -DskipTests=false -Dtl.ai.browser.e2e=true -Dtest='TestBrowser*'
 * </pre>
 *
 * <p>
 * Playwright provisions the Chromium it needs on first use, into its own browser cache
 * ({@code ~/.cache/ms-playwright} on Linux). It must be the <b>full</b> Chromium build, not the
 * stripped {@code chrome-headless-shell} - see {@code BrowserSession#FULL_CHROMIUM_CHANNEL} for why
 * the WebRTC egress guard depends on it. A plain {@code playwright install chromium} supplies both.
 * The tests that reach public sites additionally assume DNS resolves; where it does not, they skip
 * for that reason instead.
 * </p>
 */
public class BrowserE2e {

	/**
	 * System property that enables the browser end-to-end tests.
	 *
	 * @see #assumeEnabled()
	 */
	public static final String ENABLED_PROPERTY = "tl.ai.browser.e2e";

	/**
	 * Skips the calling test unless {@link #ENABLED_PROPERTY} is set.
	 */
	public static void assumeEnabled() {
		Assume.assumeTrue("browser end-to-end tests are off, enable with -D" + ENABLED_PROPERTY + "=true",
			Boolean.getBoolean(ENABLED_PROPERTY));
	}

}
