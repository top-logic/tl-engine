/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.table;

import com.top_logic.layout.react.ReactControl;

/**
 * Provider that creates {@link ReactControl}s for table cells.
 *
 * <p>
 * The table delegates all cell rendering to this provider. Implementations map column types to
 * appropriate React controls (e.g. text display, links, checkboxes, select fields).
 * </p>
 */
public interface ReactCellControlProvider {

	/**
	 * Creates a {@link ReactControl} for the given cell.
	 *
	 * @param rowObject
	 *        The application object for the row.
	 * @param columnName
	 *        The column identifier.
	 * @param cellValue
	 *        The cell value as extracted by the column's accessor.
	 * @return A {@link ReactControl} to render in the cell. Must not be {@code null}.
	 */
	ReactControl createCellControl(Object rowObject, String columnName, Object cellValue);
}
