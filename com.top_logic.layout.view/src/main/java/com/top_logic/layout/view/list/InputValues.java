/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.list;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.model.TLObject;

/**
 * The values an {@link ObjectListElement} applies its functions to.
 *
 * <p>
 * The functions of a list are applied to what its inputs hold at the moment they run, and an object
 * that was deleted meanwhile holds nothing: it has no elements to list, and nothing can be attached
 * to it. Reading the inputs through this mapping turns such an object into no value at all, which
 * is what the functions receive and what tells the list that there is no place to add to.
 * </p>
 */
final class InputValues {

	private InputValues() {
		// Static utility.
	}

	/**
	 * The current values of the given channels, in declaration order, each of them
	 * {@link #aliveOrNull(Object) alive or nothing}.
	 *
	 * @param inputs
	 *        The channels the values are read from.
	 */
	static List<Object> of(List<ViewChannel> inputs) {
		List<Object> result = new ArrayList<>(inputs.size());
		for (ViewChannel input : inputs) {
			result.add(aliveOrNull(input.get()));
		}
		return result;
	}

	/**
	 * The given values, each of them {@link #aliveOrNull(Object) alive or nothing}.
	 *
	 * @param values
	 *        The values as delivered by the input channels.
	 */
	static Object[] alive(Object[] values) {
		Object[] result = new Object[values.length];
		for (int n = 0; n < values.length; n++) {
			result[n] = aliveOrNull(values[n]);
		}
		return result;
	}

	/**
	 * Whether every one of the given values is a value, so that the list has a place to add to.
	 *
	 * <p>
	 * A list without inputs is complete: it computes its elements from nothing and has nothing that
	 * could be missing.
	 * </p>
	 *
	 * @param values
	 *        The values to check, see {@link #of(List)}.
	 */
	static boolean complete(List<Object> values) {
		return !values.contains(null);
	}

	/**
	 * The given value while it is alive, {@code null} for a deleted object.
	 *
	 * <p>
	 * A deleted object is no value: it holds no elements, and no function can be evaluated on it.
	 * Reading an input through this mapping makes a deleted object differ from the alive one it was,
	 * so that a list built for it is rebuilt without it and the element being composed for it is
	 * dropped.
	 * </p>
	 *
	 * @param value
	 *        The value as delivered by an input channel, may be {@code null}.
	 * @return The given value, or {@code null} if it is no longer {@link TLObject#tValid() valid}.
	 */
	static Object aliveOrNull(Object value) {
		if (value instanceof TLObject object && !object.tValid()) {
			return null;
		}
		return value;
	}

}
