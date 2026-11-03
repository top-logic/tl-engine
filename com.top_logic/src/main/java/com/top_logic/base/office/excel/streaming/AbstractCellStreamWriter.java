/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.excel.streaming;

import java.io.File;
import java.io.IOException;

/**
 * Base class for {@link CellStreamWriter} implementations.
 *
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public abstract class AbstractCellStreamWriter implements CellStreamWriter {

	/** Index of the current column in the current row. */
	protected int currentColumnIndex = 0;

	/** Index of the current row in the current table. */
	protected int currentRowIndex = 0;

	/** Name of the current table. */
	protected String currentTable = null;

	@Override
	public void newRow() throws IOException {
		checkReady();
		internalNewRow();
		currentColumnIndex = 0;
		currentRowIndex++;
	}

	@Override
	public File close() throws IOException {
		return internalClose();
	}

	@Override
	public void newTable(String tablename) throws IOException {
		internalNewTable(tablename);
		currentColumnIndex = 0;
		currentRowIndex = 0;
		currentTable = tablename;
	}

	@Override
	public void write(Object cellvalue) throws IOException {
		checkReady();
		internalWrite(cellvalue);
		currentColumnIndex++;
	}

	@Override
	public int currentColumn() {
		return currentColumnIndex;
	}

	@Override
	public int currentRow() {
		return currentRowIndex;
	}

	@Override
	public String currentTable() {
		return currentTable;
	}

	/**
	 * Increments the column index.
	 */
	public void newColumn() {
		incColumn(1);
	}

	/**
	 * Increments the column index by the given value.
	 */
	public void incColumn(int cnt) {
		currentColumnIndex += cnt;
	}

	private void checkReady() throws IOException {
		if (currentTable == null) {
			throw new IllegalStateException("Writer is not ready for write access yet! No current table given.");
		}
	}

	/** Finishes writing and returns the written file. */
	protected abstract File internalClose() throws IOException;

	/** Starts a new row. */
	protected abstract void internalNewRow() throws IOException;

	/** Writes the given value into the next cell of the current row. */
	protected abstract void internalWrite(Object cellvalue) throws IOException;

	/** Starts a new table with the given name. */
	protected abstract void internalNewTable(String tablename) throws IOException;
}
