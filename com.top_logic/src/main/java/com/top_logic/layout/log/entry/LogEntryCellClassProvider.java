/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.log.entry;

import com.top_logic.layout.table.AbstractCellClassProvider;
import com.top_logic.layout.table.CellClassProvider;
import com.top_logic.layout.table.TableRenderer.Cell;

/**
 * A {@link CellClassProvider} that adds a CSS class for the severity of the {@link ParsedLogEntry}.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class LogEntryCellClassProvider extends AbstractCellClassProvider {

	/** The {@link LogEntryCellClassProvider} instance. */
	public static final LogEntryCellClassProvider INSTANCE = new LogEntryCellClassProvider();

	@Override
	public String getCellClass(Cell cell) {
		return getLogEntry(cell).getSeverity().getCssClass();
	}

	private ParsedLogEntry getLogEntry(Cell cell) {
		return (ParsedLogEntry) cell.getRowObject();
	}

}
