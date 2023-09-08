/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table;

import com.top_logic.layout.table.control.TableControl;

/**
 * Provider of CSS classes for table content rows.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface RowClassProvider {

	/**
	 * Creates a CSS class for the specified table content row.
	 * 
	 * @param view
	 *        The currently rendered table.
	 * @param rowOptions
	 *        Bitmask that determines the kind of the given row (e.g. whether it is the last row or
	 *        the row is selected), see {@link TableRenderer#SELECTED}.
	 * @param displayedRow
	 *        The number of the row on the client, i.e. it is not the row in the model but the row
	 *        on the GUI.
	 * @param row
	 *        The absolute number of the row in the {@link TableViewModel}.
	 * 
	 * @return the CSS class for the given <code>displayedRow</code>. May be <code>null</code>.
	 */
	public String getTRClass(TableControl view, int rowOptions, int displayedRow, int row);

}
