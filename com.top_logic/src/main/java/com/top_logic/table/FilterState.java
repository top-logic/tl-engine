/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table;

/**
 * The serializable, user-chosen criteria of a single column filter.
 *
 * <p>
 * A {@link FilterState} is pure data - it holds <em>what</em> the user selected (a
 * text pattern, a range, a set of option values, ...) with no dependency on any form or
 * control. A {@link ColumnFilter} turns a {@link FilterState} into a {@code Predicate} (or
 * a query pushdown), and a UI tier renders/edits it based on the column's
 * {@link FilterInput} descriptor.
 * </p>
 *
 * <p>
 * Concrete {@link FilterState} implementations (text, range, options, boolean) are added
 * alongside the corresponding {@link FilterInput} variants. A JSON serialization contract
 * for personalization is layered on later.
 * </p>
 */
public interface FilterState {

	/**
	 * Whether this filter is in its empty/inactive state (matches everything).
	 */
	boolean isEmpty();

}
