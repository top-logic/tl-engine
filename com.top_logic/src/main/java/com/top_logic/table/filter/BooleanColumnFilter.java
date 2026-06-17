/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table.filter;

import java.util.function.Predicate;

import com.top_logic.table.ColumnFilter;
import com.top_logic.table.FilterInput;
import com.top_logic.table.FilterState;

/**
 * A tri-state {@link ColumnFilter} over {@link Boolean}-valued cells, accepting any subset
 * of {@code true} / {@code false} / no-value as configured by a {@link BooleanFilterState}.
 */
public class BooleanColumnFilter implements ColumnFilter<Boolean> {

	/** Singleton instance. */
	public static final BooleanColumnFilter INSTANCE = new BooleanColumnFilter();

	@Override
	public FilterInput input() {
		return new FilterInput.Bool();
	}

	@Override
	public Predicate<Boolean> predicate(FilterState state) {
		BooleanFilterState bool = (BooleanFilterState) state;
		return value -> {
			if (value == null) {
				return bool.acceptNull();
			}
			return value ? bool.acceptTrue() : bool.acceptFalse();
		};
	}

}
