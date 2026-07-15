/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.model.util;

/**
 * Simple pair of two values.
 *
 * @param <S>
 *        The type of the first value.
 * @param <T>
 *        The type of the second value.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class Pair<S, T> {

	private final S _first;

	private final T _second;

	/**
	 * Creates a new {@link Pair}.
	 *
	 * @param first
	 *        See {@link #getFirst()}.
	 * @param second
	 *        See {@link #getSecond()}.
	 */
	public Pair(S first, T second) {
		_first = first;
		_second = second;
	}

	/**
	 * The first element of this {@link Pair}.
	 */
	public S getFirst() {
		return _first;
	}

	/**
	 * The second element of this {@link Pair}.
	 */
	public T getSecond() {
		return _second;
	}

}
