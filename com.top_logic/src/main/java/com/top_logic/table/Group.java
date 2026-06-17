/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table;

import java.util.List;

/**
 * A group of data rows sharing a common {@link GroupKey}, as produced by grouping.
 *
 * <p>
 * Passed to an {@link Aggregator} to compute aggregate/footer cells. For large,
 * pushdown-backed sources {@link #members()} may be a lazily materialized view; prefer
 * {@link #size()} when only the count is needed.
 * </p>
 *
 * @param <R>
 *        The business object type of the member rows.
 */
public interface Group<R> {

	/**
	 * Identity of this group.
	 */
	GroupKey key();

	/**
	 * Number of data rows in this group.
	 */
	int size();

	/**
	 * The member data rows of this group. May be lazily materialized.
	 */
	List<R> members();

}
