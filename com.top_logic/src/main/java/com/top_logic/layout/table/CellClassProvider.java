/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table;

import com.top_logic.layout.table.TableRenderer.Cell;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.provider.DefaultCellClassProvider;

/**
 * Provider for CSS classes for individual table cells.
 * 
 * <p>
 * Custom implementations must use {@link AbstractCellClassProvider} as base class.
 * </p>
 * 
 * @see DefaultCellClassProvider
 * @see ColumnConfiguration#getCssClassProvider()
 * @see RowClassProvider
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface CellClassProvider extends Unimplementable {

	/**
	 * Look up the CSS class for the given individual cell.
	 * 
	 * @param cell
	 *        Context identifying the cell to look up the CSS class for.
	 * @return The individual CSS class for the given cell, or <code>null</code> to write no custom
	 *         CSS class for that cell.
	 */
	String getCellClass(Cell cell);

}
