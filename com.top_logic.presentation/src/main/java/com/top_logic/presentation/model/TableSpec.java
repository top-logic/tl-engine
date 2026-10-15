/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.model;

import java.util.List;

/**
 * A table.
 *
 * <p>
 * The column widths are either given in points or as weights distributing the width of the table.
 * Without either, all columns have the same width.
 * </p>
 *
 * @param bounds
 *        See {@link ElementSpec#bounds()}.
 * @param columnWeights
 *        The relative widths of the columns, <code>null</code> if not given.
 * @param columnWidths
 *        The widths of the columns in points, <code>null</code> if not given.
 * @param rows
 *        The rows of cells, all rows having the same number of cells.
 * @param headerRows
 *        The number of rows at the top that are formatted as header.
 * @param rowHeight
 *        The minimum height of a row in points, <code>null</code> for the height of the text.
 */
public record TableSpec(Bounds bounds, List<Double> columnWeights, List<Double> columnWidths,
		List<List<CellSpec>> rows, int headerRows, Double rowHeight) implements ElementSpec {

	/**
	 * The number of columns.
	 */
	public int columnCount() {
		return rows.isEmpty() ? 0 : rows.get(0).size();
	}

}
