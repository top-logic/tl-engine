/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.environment;

import java.util.List;

import com.top_logic.tl.ai.core.binding.ThreadLocalBinding;

/**
 * Thread-local binding for the facts a host stated for the current run.
 *
 * <p>
 * The chat evaluates its {@code <context>} expressions where the page is - in the browser's
 * request - and the run happens on a background thread minutes later. The facts therefore travel
 * with the send call and are bound here for the duration of the run, so that every agent loop built
 * inside it - the entry-point agent, a delegate, a subagent - is told the same thing about the
 * situation without the facts having to be threaded through every signature in between.
 * </p>
 *
 * <p>
 * Prefer {@link #bind(List)} with try-with-resources. The previous value is captured at bind time
 * and restored on close, which is what makes the binding safe on the cached thread pool the chat
 * runs on.
 * </p>
 */
public class RunFactsBinding {

	private static final ThreadLocalBinding<List<EnvironmentFact>> FACTS = new ThreadLocalBinding<>();

	private RunFactsBinding() {
		// Static utility.
	}

	/**
	 * Binds the given facts on the current thread and returns a handle whose
	 * {@link AutoCloseable#close() close()} restores the previously bound value (or absence).
	 */
	public static Binding bind(List<EnvironmentFact> facts) {
		return new Binding(FACTS.bind(facts == null || facts.isEmpty() ? null : List.copyOf(facts)));
	}

	/** The facts bound to the current thread; never {@code null}, empty when none are bound. */
	public static List<EnvironmentFact> facts() {
		List<EnvironmentFact> facts = FACTS.get();
		return facts == null ? List.of() : facts;
	}

	/** Handle returned by {@link RunFactsBinding#bind(List)}. */
	public static final class Binding implements AutoCloseable {

		private final ThreadLocalBinding<List<EnvironmentFact>>.Handle _handle;

		Binding(ThreadLocalBinding<List<EnvironmentFact>>.Handle handle) {
			_handle = handle;
		}

		@Override
		public void close() {
			_handle.close();
		}
	}
}
