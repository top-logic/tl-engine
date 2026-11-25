/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table;

import java.util.List;
import java.util.Objects;

/**
 * The filter state of a column offering several {@link Column#filters() kinds of filter}, or
 * filtered {@link #inverted() inverted}: the active kind, its state and whether its match is
 * inverted.
 *
 * <p>
 * A column is filtered by exactly one of its kinds at a time. A plain {@link FilterState} of a
 * column - one that is no {@link ColumnFilterState} - is a state of its first kind, not inverted.
 * {@link #of(List, String, FilterState, boolean)} builds the state in that canonical form, so the
 * same criteria compare equal however they were reached.
 * </p>
 *
 * <p>
 * Inversion is the column's, so every kind of filter can be inverted: a row matches an inverted
 * state iff it does not match the {@link #state() inner state}, empty cells included.
 * </p>
 *
 * @param kind
 *        The {@link ColumnFilter#kind()} of the active filter, or {@code null} for the first
 *        filter of the column.
 * @param state
 *        The state of the active filter; never a {@link ColumnFilterState} itself.
 * @param inverted
 *        Whether the match of {@link #state()} is inverted.
 */
public record ColumnFilterState(String kind, FilterState state, boolean inverted) implements FilterState {

	/**
	 * Creates a {@link ColumnFilterState}.
	 */
	public ColumnFilterState {
		Objects.requireNonNull(state, "state");
		if (state instanceof ColumnFilterState) {
			throw new IllegalArgumentException("A column filter state does not nest: " + state);
		}
	}

	@Override
	public boolean isEmpty() {
		// Inverting an empty (accept-all) selection leaves the column unfiltered: checking "invert"
		// without choosing anything is a no-op, not a reject-all.
		return state.isEmpty();
	}

	/**
	 * The inverted state of the first filter of a column.
	 */
	public static ColumnFilterState invert(FilterState state) {
		return new ColumnFilterState(null, state, true);
	}

	/**
	 * The state of a column in canonical form.
	 *
	 * <p>
	 * The state of the first kind that is not inverted is the plain inner state; otherwise the
	 * result is a {@link ColumnFilterState} whose {@link #kind()} is {@code null} for the first
	 * kind.
	 * </p>
	 *
	 * @param kinds
	 *        The {@link Column#filters() kinds of filter} of the column.
	 * @param kind
	 *        The {@link ColumnFilter#kind()} of the active filter, or {@code null} for the first.
	 * @param state
	 *        The state of the active filter.
	 * @param inverted
	 *        Whether its match is inverted.
	 */
	public static FilterState of(List<? extends ColumnFilter<?>> kinds, String kind, FilterState state,
			boolean inverted) {
		String effectiveKind = kind == null || (!kinds.isEmpty() && kinds.get(0).kind().equals(kind)) ? null : kind;
		if (effectiveKind == null && !inverted) {
			return state;
		}
		return new ColumnFilterState(effectiveKind, state, inverted);
	}

	/**
	 * The {@link #kind()} the given column state is a state of, {@code null} for the first kind.
	 */
	public static String kindOf(FilterState columnState) {
		return columnState instanceof ColumnFilterState wrapper ? wrapper.kind() : null;
	}

	/**
	 * The state of the active filter of the given column state.
	 */
	public static FilterState innerOf(FilterState columnState) {
		return columnState instanceof ColumnFilterState wrapper ? wrapper.state() : columnState;
	}

	/**
	 * Whether the given column state is {@link #inverted()}.
	 */
	public static boolean isInverted(FilterState columnState) {
		return columnState instanceof ColumnFilterState wrapper && wrapper.inverted();
	}

}
