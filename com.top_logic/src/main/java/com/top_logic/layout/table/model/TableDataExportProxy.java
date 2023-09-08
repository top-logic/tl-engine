/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import java.io.IOException;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.table.TableData;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Proxy for a {@link TableDataExport}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class TableDataExportProxy implements TableDataExport {

	@Override
	public HandlerResult exportTableData(DisplayContext context, TableData table) {
		return impl().exportTableData(context, table);
	}

	@Override
	public ExecutableState getExecutability(TableData table) {
		return impl().getExecutability(table);
	}

	@Override
	public BinaryData createExportData(TableData table, String name) throws IOException {
		return impl().createExportData(table, name);
	}

	/**
	 * The {@link TableDataExport} to delegate to.
	 */
	protected abstract TableDataExport impl();

}

