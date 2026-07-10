/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.binding;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.tl.ai.core.binding.ThreadLocalBinding.Capture;

/**
 * Immutable snapshot of every registered {@link ThreadLocalBinding} of the current thread.
 *
 * <p>
 * Worker threads do not inherit thread-locals. Instead of capturing and re-establishing each
 * binding by hand (once per binding, on both the capturing and the establishing side), a caller
 * captures all bindings at once via {@link #capture()} and re-establishes them on the worker thread
 * via {@link #establish()}:
 * </p>
 *
 * <pre>
 * BindingSnapshot bindings = BindingSnapshot.capture();
 * pool.submit(() -&gt; {
 *     try (BindingSnapshot.Established established = bindings.establish()) {
 *         // runs with the caller's bindings re-established
 *     }
 * });
 * </pre>
 */
public final class BindingSnapshot {

	private final List<Capture<?>> _captures;

	private BindingSnapshot(List<Capture<?>> captures) {
		_captures = captures;
	}

	/**
	 * Captures the current value of every registered {@link ThreadLocalBinding} on the calling
	 * thread.
	 */
	public static BindingSnapshot capture() {
		List<Capture<?>> captures = new ArrayList<>();
		for (ThreadLocalBinding<?> binding : BindingRegistry.all()) {
			captures.add(binding.captureCurrent());
		}
		return new BindingSnapshot(captures);
	}

	/**
	 * Re-establishes all captured bindings on the current thread and returns a handle whose
	 * {@link Established#close() close()} restores the previous values.
	 */
	public Established establish() {
		List<ThreadLocalBinding<?>.Handle> handles = new ArrayList<>(_captures.size());
		for (Capture<?> capture : _captures) {
			handles.add(capture.establish());
		}
		return new Established(handles);
	}

	/**
	 * Handle restoring the previous binding values when {@link #close() closed}.
	 */
	public static final class Established implements AutoCloseable {
		private final List<ThreadLocalBinding<?>.Handle> _handles;

		Established(List<ThreadLocalBinding<?>.Handle> handles) {
			_handles = handles;
		}

		@Override
		public void close() {
			for (int i = _handles.size() - 1; i >= 0; i--) {
				_handles.get(i).close();
			}
		}
	}
}
