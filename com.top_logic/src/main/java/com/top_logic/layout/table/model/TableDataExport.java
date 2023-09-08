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
 * Exporter of a {@link TableData} to Excel.
 * 
 * @see TableConfig#getExporter()
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface TableDataExport {

	/**
	 * Exports the given table to excel.
	 * 
	 * @param context
	 *        Context in which command is executed.
	 * @param table
	 *        The {@link TableData} to export.
	 * 
	 * @return Result of the export.
	 * 
	 * @see #createExportData(TableData, String)
	 */
	HandlerResult exportTableData(DisplayContext context, TableData table);

	/**
	 * Creates a {@link BinaryData} containing the result of the table export.
	 * 
	 * @param table
	 *        The {@link TableData} to export.
	 * @param name
	 *        Name of the resulting binary data.
	 * 
	 * @throws IOException
	 *         when exporting fails.
	 */
	BinaryData createExportData(TableData table, String name) throws IOException;

	/**
	 * Defines whether the given {@link TableData} can be exported or not.
	 * 
	 * @param table
	 *        The {@link TableData} to export.
	 * @return {@link ExecutableState} deciding whether the given table can be exported.
	 */
	ExecutableState getExecutability(TableData table);

}

