/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.junit;

import org.junit.Assume;

/**
 * The switch that turns the visual harnesses on, and the {@link Assume assumption} that makes a run
 * without it report as <b>skipped</b>.
 *
 * <p>
 * A visual harness asserts nothing: it renders a transcript to stdout for a human to read, and some
 * of them spend a real API call doing it. So they cannot run on every build. Opting out by
 * returning from the test method would make surefire count a passed test that checked nothing -
 * the defect {@code BrowserE2e} removed for the browser end-to-end tests.
 * {@link #assumeEnabled()} instead throws a violated assumption, and {@link AssumptionAwareRunner} -
 * which every harness class is {@code @RunWith} - turns that into a reported skip.
 * </p>
 *
 * <p>
 * This sits beside {@link AssumptionAwareRunner} rather than beside {@code BrowserE2e}, because its
 * callers are spread over the {@code agent}, {@code memory} and {@code provider} packages, while
 * the browser gate is used only within {@code tools.web}.
 * </p>
 *
 * <h2>Running the harnesses locally</h2>
 *
 * <pre>
 * mvn -pl tl-ai-core -am test -DskipTests=false -Dtl.ai.visual=true -Dtest='Visual*'
 * </pre>
 *
 * <p>
 * The harnesses that talk to Anthropic additionally need {@code ANTHROPIC_API_KEY} in the
 * environment ({@code source env.sh}); without it they skip for that reason instead.
 * </p>
 */
public class VisualHarness {

	/**
	 * System property that enables the visual harnesses.
	 *
	 * @see #assumeEnabled()
	 */
	public static final String ENABLED_PROPERTY = "tl.ai.visual";

	/**
	 * Skips the calling harness method unless {@link #ENABLED_PROPERTY} is set.
	 */
	public static void assumeEnabled() {
		Assume.assumeTrue("visual harnesses are off, enable with -D" + ENABLED_PROPERTY + "=true",
			enabled());
	}

	/**
	 * Whether {@link #ENABLED_PROPERTY} is set.
	 *
	 * <p>
	 * Gating a harness method is {@link #assumeEnabled()}'s job - this is for the setup a harness
	 * class puts in place <em>around</em> its methods, which is built before any of them runs and
	 * therefore cannot ask an assumption.
	 * </p>
	 */
	public static boolean enabled() {
		return Boolean.getBoolean(ENABLED_PROPERTY);
	}

}
