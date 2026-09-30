/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.overlay;

/**
 * Result of a modal dialog interaction.
 *
 * <p>
 * A dialog result is either "OK" (confirmed with a value) or "cancelled" (dismissed without a
 * value). Use the static factories {@link #ok(Object)} and {@link #cancelled()} to create
 * instances.
 * </p>
 *
 * @param <T>
 *        The type of the result value.
 */
public final class DialogResult<T> {

	private static final DialogResult<?> CANCELLED = new DialogResult<>(false, null);

	private final boolean _ok;

	private final T _value;

	private DialogResult(boolean ok, T value) {
		_ok = ok;
		_value = value;
	}

	/**
	 * Creates a successful result with the given value.
	 *
	 * @param value
	 *        The result value.
	 * @return A new OK result.
	 */
	public static <T> DialogResult<T> ok(T value) {
		return new DialogResult<>(true, value);
	}

	/**
	 * Creates a cancelled result with no value.
	 *
	 * @return A cancelled result.
	 */
	@SuppressWarnings("unchecked")
	public static <T> DialogResult<T> cancelled() {
		return (DialogResult<T>) CANCELLED;
	}

	/**
	 * Whether the dialog was confirmed.
	 */
	public boolean isOk() {
		return _ok;
	}

	/**
	 * Whether the dialog was cancelled.
	 */
	public boolean isCancelled() {
		return !_ok;
	}

	/**
	 * The result value, or {@code null} if cancelled.
	 */
	public T getValue() {
		return _value;
	}

}
