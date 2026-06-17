/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table;

/**
 * Tests whether a cell logically exists for a row, used to gate filter visibility: a
 * column whose cells never exist for the current rows offers no useful filter.
 *
 * @param <R>
 *        The row business object type.
 */
@FunctionalInterface
public interface CellExistence<R> {

	/**
	 * Whether the column's cell exists (is applicable) for the given row.
	 */
	boolean cellExists(R row);

}
