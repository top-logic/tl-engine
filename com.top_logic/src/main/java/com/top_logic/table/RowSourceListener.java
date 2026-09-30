/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table;

/**
 * Notified when a {@link RowSource}'s displayed rows change (insert, delete, update,
 * re-sort, re-filter, expansion change).
 */
@FunctionalInterface
public interface RowSourceListener {

	/**
	 * The displayed rows in the half-open index range {@code [from, to)} changed; cached
	 * windows overlapping that range must be refetched.
	 *
	 * <p>
	 * The total {@link RowSource#size()} may also have changed; a full invalidation is
	 * signalled with {@code from == 0} and {@code to == Integer.MAX_VALUE}.
	 * </p>
	 */
	void rowsInvalidated(int from, int to);

}
