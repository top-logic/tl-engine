/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table;

import java.util.List;

/**
 * The grouping order of a table: the columns to group rows by, outermost first.
 *
 * <p>
 * An empty {@link GroupSpec} means no grouping (a plain flat or tree table). A non-empty
 * grouping makes the {@link RowSource} introduce {@link RowKind#GROUP_HEADER} (and
 * optionally {@link RowKind#AGGREGATE}) rows.
 * </p>
 *
 * @param columns
 *        The grouping columns, outermost group first.
 */
public record GroupSpec(List<String> columns) {

	/** No grouping. */
	public static final GroupSpec NONE = new GroupSpec(List.of());

	/**
	 * Creates a {@link GroupSpec} with a defensive, immutable copy of the columns.
	 */
	public GroupSpec {
		columns = List.copyOf(columns);
	}

}
