/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.environment;

import java.util.List;

import com.top_logic.tl.ai.core.binding.ThreadLocalBinding;

/**
 * Thread-local binding for what the entry point stated about the current run.
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
 * The turn number rides along for the same reason and no other: it is what the entry point counted,
 * it belongs in the {@link TurnContext per-turn block}, and nothing between the send call and
 * {@code AgentFactory.buildConfig} would otherwise carry it. A run whose entry point counts no turns
 * - a headless or a scheduled one - binds none, and the block then states no turn.
 * </p>
 *
 * <p>
 * Prefer {@link #bind(List, int)} with try-with-resources. The previous value is captured at bind
 * time and restored on close, which is what makes the binding safe on the cached thread pool the
 * chat runs on.
 * </p>
 */
public class RunFactsBinding {

	/**
	 * What one entry point stated about one run.
	 *
	 * @param facts
	 *        The facts, never {@code null}.
	 * @param turnNumber
	 *        The 1-based turn of the conversation the run answers, or {@code 0} when unknown.
	 */
	record RunFacts(List<EnvironmentFact> facts, int turnNumber) {
		// Purely a carrier: the binding holds one value, and these two travel together.
	}

	private static final ThreadLocalBinding<RunFacts> FACTS = new ThreadLocalBinding<>();

	private RunFactsBinding() {
		// Static utility.
	}

	/** Binds the given facts for a run whose entry point counts no turns. */
	public static Binding bind(List<EnvironmentFact> facts) {
		return bind(facts, 0);
	}

	/**
	 * Binds the given facts on the current thread and returns a handle whose
	 * {@link AutoCloseable#close() close()} restores the previously bound value (or absence).
	 *
	 * @param turnNumber
	 *        The 1-based turn of the conversation this run answers; {@code 0} for none.
	 */
	public static Binding bind(List<EnvironmentFact> facts, int turnNumber) {
		boolean nothing = (facts == null || facts.isEmpty()) && turnNumber <= 0;
		RunFacts bound = nothing
			? null
			: new RunFacts(facts == null ? List.of() : List.copyOf(facts), Math.max(turnNumber, 0));
		return new Binding(FACTS.bind(bound));
	}

	/** The facts bound to the current thread; never {@code null}, empty when none are bound. */
	public static List<EnvironmentFact> facts() {
		RunFacts bound = FACTS.get();
		return bound == null ? List.of() : bound.facts();
	}

	/** The conversation turn bound to the current thread, or {@code 0} when none is. */
	public static int turnNumber() {
		RunFacts bound = FACTS.get();
		return bound == null ? 0 : bound.turnNumber();
	}

	/** Handle returned by {@link RunFactsBinding#bind(List, int)}. */
	public static final class Binding implements AutoCloseable {

		private final ThreadLocalBinding<RunFacts>.Handle _handle;

		Binding(ThreadLocalBinding<RunFacts>.Handle handle) {
			_handle = handle;
		}

		@Override
		public void close() {
			_handle.close();
		}
	}
}
