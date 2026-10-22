/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.binding;

/**
 * Generic thread-local binding with save/restore semantics.
 *
 * <p>
 * {@link #bind(Object)} sets the value on the current thread and returns a {@link Handle} whose
 * {@link Handle#close() close()} restores the previously bound value (or its absence). This makes
 * the binding safe across cached thread pools, where a thread may already carry a value from an
 * earlier task. A {@code null} value clears the binding via {@link ThreadLocal#remove()}.
 * </p>
 *
 * <p>
 * Every instance registers itself with {@link BindingRegistry} so that {@link BindingSnapshot} can
 * capture all bindings of the current thread and re-establish them on a worker thread without the
 * caller having to enumerate them.
 * </p>
 *
 * @param <T>
 *        the type of the bound value.
 */
public final class ThreadLocalBinding<T> {

	private final ThreadLocal<T> _threadLocal = new ThreadLocal<>();

	/**
	 * Creates a new binding and registers it with the {@link BindingRegistry}.
	 */
	public ThreadLocalBinding() {
		BindingRegistry.register(this);
	}

	/**
	 * The value bound to the current thread, or {@code null} if none is bound.
	 */
	public T get() {
		return _threadLocal.get();
	}

	/**
	 * Binds the given value on the current thread and returns a handle whose
	 * {@link Handle#close() close()} restores the previously bound value (or its absence).
	 */
	public Handle bind(T value) {
		T previous = _threadLocal.get();
		apply(value);
		return new Handle(previous);
	}

	private void apply(T value) {
		if (value == null) {
			_threadLocal.remove();
		} else {
			_threadLocal.set(value);
		}
	}

	/**
	 * Captures the current value so it can be re-established on another thread.
	 *
	 * @see BindingSnapshot
	 */
	Capture<T> captureCurrent() {
		return new Capture<>(this, _threadLocal.get());
	}

	/**
	 * The captured value of a single {@link ThreadLocalBinding}, paired with its binding so it can
	 * be re-established.
	 */
	static final class Capture<T> {
		private final ThreadLocalBinding<T> _binding;

		private final T _value;

		Capture(ThreadLocalBinding<T> binding, T value) {
			_binding = binding;
			_value = value;
		}

		ThreadLocalBinding<T>.Handle establish() {
			return _binding.bind(_value);
		}
	}

	/**
	 * Handle restoring the previously bound value when {@link #close() closed}.
	 *
	 * <p>
	 * {@link #close()} is idempotent.
	 * </p>
	 */
	public final class Handle implements AutoCloseable {
		private final T _previous;

		private boolean _closed;

		Handle(T previous) {
			_previous = previous;
		}

		@Override
		public void close() {
			if (_closed) {
				return;
			}
			_closed = true;
			apply(_previous);
		}
	}
}
