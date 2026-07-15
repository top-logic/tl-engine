/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table;

/**
 * A {@link FilterState} that inverts the match of an inner state: a row matches iff it would
 * <em>not</em> match the {@link #inner() inner} filter.
 *
 * <p>
 * Inversion is orthogonal to the concrete filter type, so it is modelled once as this generic
 * wrapper rather than as a flag on every concrete state. A UI offers it for filters that
 * {@link ColumnFilter#supportsInversion() support} it; the negation itself is applied centrally
 * where a column's predicate is built.
 * </p>
 *
 * @param inner
 *        The state whose match is inverted.
 */
public record NegatedFilterState(FilterState inner) implements FilterState {

	@Override
	public boolean isEmpty() {
		// Inverting an empty (accept-all) selection leaves the column unfiltered: checking "invert"
		// without choosing anything is a no-op, not a reject-all.
		return inner.isEmpty();
	}

}
