/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table;

import java.util.List;

/**
 * Identity of a row {@link Group}: the tuple of grouping values that defines the group.
 *
 * <p>
 * For a grouping by columns {@code [region, year]}, a {@link GroupKey} holds the concrete
 * {@code region} and {@code year} values of one group. The list length equals the number
 * of active grouping columns; nested groups extend the tuple.
 * </p>
 *
 * @param values
 *        The grouping values, in grouping-column order.
 */
public record GroupKey(List<Object> values) {

	/**
	 * Creates a {@link GroupKey} with a defensive, immutable copy of the values.
	 */
	public GroupKey {
		values = List.copyOf(values);
	}

}
