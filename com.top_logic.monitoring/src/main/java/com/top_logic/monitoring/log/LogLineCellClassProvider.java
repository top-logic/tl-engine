/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.log;

import com.top_logic.layout.table.AbstractCellClassProvider;
import com.top_logic.layout.table.CellClassProvider;
import com.top_logic.layout.table.TableRenderer.Cell;

/**
 * A {@link CellClassProvider} that adds a CSS class for the severity of the {@link LogLine}.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class LogLineCellClassProvider extends AbstractCellClassProvider {

	/** The {@link LogLineCellClassProvider} instance. */
	public static final LogLineCellClassProvider INSTANCE = new LogLineCellClassProvider();

	@Override
	public String getCellClass(Cell cell) {
		return getLogLine(cell).getSeverity().getCssClass();
	}

	private LogLine getLogLine(Cell cell) {
		return (LogLine) cell.getRowObject();
	}

}
