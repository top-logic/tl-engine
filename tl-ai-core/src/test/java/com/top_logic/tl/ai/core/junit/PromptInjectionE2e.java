/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.junit;

import org.junit.Assume;

/**
 * The switch that turns the prompt-injection end-to-end tests on, and the {@link Assume assumption}
 * that makes a run without it report as <b>skipped</b>.
 *
 * <p>
 * Whether the untrusted-content envelope actually holds is not a question about a string: it is a
 * question about what a language model does with that string. So the tests behind this switch put a
 * real model in front of an injected tool result and assert which tools ran - which spends real API
 * calls and cannot be part of an ordinary build. {@link #assumeEnabled()} throws a violated
 * assumption instead of returning early, and {@link AssumptionAwareRunner} - which every gated test
 * class is {@code @RunWith} - turns that into a reported skip rather than a passed test that checked
 * nothing.
 * </p>
 *
 * <p>
 * A third switch beside {@link VisualHarness} and {@code BrowserE2e} rather than a reuse of either:
 * the visual harnesses assert nothing by definition, and these assert the whole point, so folding
 * them together would make the one flag mean two different things.
 * </p>
 *
 * <h2>Running the gated suite locally</h2>
 *
 * <pre>
 * source env.sh   # the model calls need a key
 * mvn -pl tl-ai-core -am test -DskipTests=false -Dtl.ai.injection.e2e=true -Dtest='TestPromptInjection*'
 * </pre>
 *
 * <p>
 * Without {@code ANTHROPIC_API_KEY} in the environment the tests skip for that reason instead. The
 * research tests ({@code TestResearchInjection}) run against an OpenAI model and need
 * {@code OPENAI_API_KEY} instead; {@code -Dtl.ai.injection.openai.model=...} chooses the model.
 * </p>
 */
public class PromptInjectionE2e {

	/**
	 * System property that enables the prompt-injection end-to-end tests.
	 *
	 * @see #assumeEnabled()
	 */
	public static final String ENABLED_PROPERTY = "tl.ai.injection.e2e";

	/**
	 * Skips the calling test unless {@link #ENABLED_PROPERTY} is set, and unless there is an API key
	 * to spend.
	 */
	public static void assumeEnabled() {
		Assume.assumeTrue("prompt-injection end-to-end tests are off, enable with -D"
			+ ENABLED_PROPERTY + "=true", enabled());
		Assume.assumeTrue("no ANTHROPIC_API_KEY in the environment (source env.sh)", apiKey() != null);
	}

	/**
	 * Whether {@link #ENABLED_PROPERTY} is set.
	 *
	 * <p>
	 * Gating a test method is {@link #assumeEnabled()}'s job - this is for the setup a test class puts
	 * in place <em>around</em> its methods, which is built before any of them runs and therefore
	 * cannot ask an assumption.
	 * </p>
	 */
	public static boolean enabled() {
		return Boolean.getBoolean(ENABLED_PROPERTY);
	}

	/** The key the model calls are made with, or {@code null} if there is none. */
	public static String apiKey() {
		return env("ANTHROPIC_API_KEY");
	}

	/**
	 * System property that names the OpenAI model of the tests that run against OpenAI.
	 *
	 * @see #openAiModel()
	 */
	public static final String OPENAI_MODEL_PROPERTY = "tl.ai.injection.openai.model";

	/**
	 * Skips the calling test unless {@link #ENABLED_PROPERTY} is set, and unless there is an OpenAI
	 * key to spend.
	 */
	public static void assumeEnabledForOpenAi() {
		Assume.assumeTrue("prompt-injection end-to-end tests are off, enable with -D"
			+ ENABLED_PROPERTY + "=true", enabled());
		Assume.assumeTrue("no OPENAI_API_KEY in the environment", openAiKey() != null);
	}

	/** The OpenAI key the model calls are made with, or {@code null} if there is none. */
	public static String openAiKey() {
		return env("OPENAI_API_KEY");
	}

	/** The OpenAI model of the tests that run against OpenAI. */
	public static String openAiModel() {
		return System.getProperty(OPENAI_MODEL_PROPERTY, "gpt-5.6-luna");
	}

	private static String env(String name) {
		String value = System.getenv(name);
		return value == null || value.isEmpty() ? null : value;
	}

}
