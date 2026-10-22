/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.eval;

import org.junit.Assume;

import com.top_logic.tl.ai.core.junit.AssumptionAwareRunner;

/**
 * The switch that turns the real-model eval on, and the assumption that makes a build without it
 * report as <b>skipped</b>.
 *
 * <p>
 * Everything about the harness itself - the matrix, the runner, the graders, the CSV, the report -
 * is tested against a scripted provider and runs in every build, at no cost. What is behind this
 * switch is the other half: the same scenarios put to real models, which spends real money and takes
 * minutes rather than seconds. Five scenarios times two models times three repeats is thirty agent
 * runs, each of them several provider calls resending the whole conversation.
 * </p>
 *
 * <p>
 * {@link #assumeEnabled()} throws a violated assumption rather than returning early, and
 * {@link AssumptionAwareRunner} - which every gated test class is {@code @RunWith} - reports that as
 * a skip. A test that returned early would be counted as passed, which is how a suite that ran
 * nothing reports green.
 * </p>
 *
 * <h2>Running it</h2>
 *
 * <pre>
 * source env.sh   # the model calls need a key
 * mvn -pl tl-ai-eval test -DskipTests=false -Dtl.ai.eval=true -Dtest='TestEvalPmMatrix'
 * </pre>
 *
 * <p>
 * Without a key in the environment the real arms report as "could not run" rather than as failures,
 * so a machine with the property but no key still produces a readable - and empty - matrix.
 * </p>
 */
public class EvalE2e {

	/**
	 * System property that enables the real-model eval.
	 *
	 * @see #assumeEnabled()
	 */
	public static final String ENABLED_PROPERTY = "tl.ai.eval";

	/** Skips the calling test unless {@link #ENABLED_PROPERTY} is set. */
	public static void assumeEnabled() {
		Assume.assumeTrue("the real-model eval is off, enable with -D" + ENABLED_PROPERTY + "=true",
			enabled());
	}

	/**
	 * Whether {@link #ENABLED_PROPERTY} is set.
	 *
	 * <p>
	 * Gating a test method is {@link #assumeEnabled()}'s job - this is for the setup a class puts in
	 * place <em>around</em> its methods, which is built before any of them runs and therefore cannot
	 * ask an assumption.
	 * </p>
	 */
	public static boolean enabled() {
		return Boolean.getBoolean(ENABLED_PROPERTY);
	}

	/** The key the real arms would be run with, or {@code null} if the environment holds none. */
	public static String apiKey() {
		String key = System.getenv("ANTHROPIC_API_KEY");
		if (key == null || key.isEmpty()) {
			key = System.getenv("tl_anthropic_api_key");
		}
		return key == null || key.isEmpty() ? null : key;
	}
}
