/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.excel.streaming;

import java.io.File;
import java.io.IOException;

/**
 * Write objects to an underlying table-like stream.
 * 
 * @author <a href=mailto:fsc@top-logic.com>fsc</a>
 */
public interface CellStreamWriter {


	/**
	 * Begin a new table. Subsequent calls of {@link #write(Object)} will write the object to
	 * the new table.
	 */
	void newTable(String tablename) throws IOException;

	/**
	 * sets the values for a freeze pane
	 */
	void setFreezePane(int col, int row);

	/**
	 * Append a new row in the current table.
	 */
	void newRow() throws IOException;

	/**
	 * Append a new cell in the current row.
	 */
	void write(Object cellvalue) throws IOException;

	/**
	 * Return index of the current row.
	 */
	int currentRow();

	/**
	 * Return index of the current column.
	 */
	int currentColumn();

	/**
	 * Return name of the current table.
	 */
	String currentTable();

	/**
	 * Close the writer
	 */
	File close() throws IOException;
}
