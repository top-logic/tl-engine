/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table;

import java.util.Comparator;
import java.util.Optional;

/**
 * The sort capability of a {@link Column}: an in-memory comparator and, optionally, a
 * query {@link OrderPushdown}.
 *
 * <p>
 * A column without a {@link Sort} is not sortable. A {@link Sort} without a
 * {@link #pushdown()} can still be applied by an in-memory {@link RowSource}; a
 * query-backed source either uses the pushdown or applies a declared fallback.
 * </p>
 *
 * @param <V>
 *        The cell value type being compared.
 */
public interface Sort<V> {

	/**
	 * The comparator establishing the ascending order of cell values.
	 */
	Comparator<V> comparator();

	/**
	 * Optional translation of this ordering into a data-tier query.
	 */
	default Optional<OrderPushdown> pushdown() {
		return Optional.empty();
	}

}
