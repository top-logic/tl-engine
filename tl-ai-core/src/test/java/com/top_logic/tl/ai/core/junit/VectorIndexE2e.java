/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.junit;

import org.junit.Assume;

/**
 * The switch that turns the vector-index end-to-end test on, and the {@link Assume assumption} that
 * makes a run without it report as <b>skipped</b>.
 *
 * <p>
 * Semantic search is not a question about this repository's code alone: the test behind this switch
 * embeds text with a real Mistral model and stores the vectors in a real JVector index, so it needs
 * an embedding key and spends a call per indexed object. Until #369 it was never run by a build at
 * all; run now without the key, it fails in its setup with "Unable to instantiate service
 * VectorIndex", which says nothing about the code under test.
 * </p>
 *
 * <p>
 * A fourth switch beside {@link VisualHarness}, {@link PromptInjectionE2e} and {@code BrowserE2e}
 * rather than a reuse of one of them: each names one thing a build may not have, and a flag that
 * meant two of them could not be turned on for one and not the other.
 * </p>
 *
 * <h2>Running the gated test locally</h2>
 *
 * <pre>
 * source env.sh   # the embedding calls need a key
 * mvn -pl tl-ai-demo test -DskipTests=false -Dtl.ai.vector.e2e=true -Dtest='VectorIndexIntegrationTest'
 * </pre>
 */
public class VectorIndexE2e {

	/**
	 * System property that enables the vector-index end-to-end test.
	 *
	 * @see #assumeEnabled()
	 */
	public static final String ENABLED_PROPERTY = "tl.ai.vector.e2e";

	/**
	 * Skips the calling test unless {@link #ENABLED_PROPERTY} is set.
	 */
	public static void assumeEnabled() {
		Assume.assumeTrue("vector-index end-to-end tests are off, enable with -D" + ENABLED_PROPERTY + "=true",
			enabled());
	}

	/**
	 * Whether {@link #ENABLED_PROPERTY} is set.
	 *
	 * <p>
	 * Gating a test method is {@link #assumeEnabled()}'s job - this is for the setup the test class
	 * puts in place <em>around</em> its methods. That setup boots the embedding service itself, so it
	 * has to be left out entirely rather than asked an assumption it is built before.
	 * </p>
	 */
	public static boolean enabled() {
		return Boolean.getBoolean(ENABLED_PROPERTY);
	}

}
