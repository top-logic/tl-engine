/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.memory;

import com.top_logic.tl.ai.core.binding.ThreadLocalBinding;

/**
 * Thread-local binding for the {@link MemoryConsent} of the turn that is running.
 *
 * <p>
 * Established by the agent loop at the start of a run, so that the {@code remember} tool and
 * {@code askUser} - which run on the same thread, or on a worker that re-establishes the run's
 * bindings - see the same record without it being threaded through every signature in between.
 * </p>
 *
 * <h2>Why a nested run does not get one of its own</h2>
 *
 * <p>
 * {@link #bindIfAbsent(MemoryConsent)} establishes a record only where none is bound yet, so the
 * outermost run of a turn - the one the user's message started - is the one whose record counts,
 * and a delegate or a subagent inherits it. That is deliberate: a delegate's "user message" is the
 * instruction its parent wrote, so a run allowed to build its own record could agree with itself by
 * delegating with the words "remember that ...". A detached subagent runs long after the turn
 * ended, on a thread of its own, and builds its own record from its task - which its lead wrote, not
 * the user ({@link com.top_logic.tl.ai.core.message.UserMessage.Origin#AGENT}). So it has no consent
 * and can keep nothing, which is the safe default.
 * </p>
 */
public class MemoryConsentBinding {

	private static final ThreadLocalBinding<MemoryConsent> CONSENT = new ThreadLocalBinding<>();

	private MemoryConsentBinding() {
		// Static utility.
	}

	/**
	 * Binds the given record unless one is already bound, and returns a handle whose
	 * {@link AutoCloseable#close() close()} restores the previous state either way.
	 */
	public static Binding bindIfAbsent(MemoryConsent consent) {
		MemoryConsent current = CONSENT.get();
		return new Binding(CONSENT.bind(current != null ? current : consent));
	}

	/** The record of the running turn, or {@code null} when none is bound. */
	public static MemoryConsent get() {
		return CONSENT.get();
	}

	/** Whether the user agreed, in this turn, to the agent keeping something. */
	public static boolean granted() {
		MemoryConsent consent = CONSENT.get();
		return consent != null && consent.granted();
	}

	/**
	 * Records an answer on the bound record, if there is one.
	 *
	 * @see MemoryConsent#recordAnswer(String, String)
	 */
	public static void recordAnswer(String question, String answer) {
		MemoryConsent consent = CONSENT.get();
		if (consent != null) {
			consent.recordAnswer(question, answer);
		}
	}

	/** Handle returned by {@link MemoryConsentBinding#bindIfAbsent(MemoryConsent)}. */
	public static final class Binding implements AutoCloseable {

		private final ThreadLocalBinding<MemoryConsent>.Handle _handle;

		Binding(ThreadLocalBinding<MemoryConsent>.Handle handle) {
			_handle = handle;
		}

		@Override
		public void close() {
			_handle.close();
		}
	}
}
