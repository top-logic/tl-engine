/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.excel.streaming;

import java.io.File;
import java.io.IOException;

/**
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public abstract class AbstractCellStreamWriter implements CellStreamWriter {

	private int _currentColumn = 0;

	private int _currentRow = 0;

	private String _currentTable = null;

	@Override
	public void newRow() throws IOException {
		checkReady();
		internalNewRow();
		_currentColumn = 0;
		_currentRow++;
	}

	@Override
	public File close() throws IOException {
		return internalClose();
	}

	@Override
	public void newTable(String tablename) throws IOException {
		internalNewTable(tablename);
		_currentColumn = 0;
		_currentRow = 0;
		_currentTable = tablename;
	}

	@Override
	public void write(Object cellvalue) throws IOException {
		checkReady();
		internalWrite(cellvalue);
		_currentColumn++;
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
		_currentColumn += cnt;
	}

	@Override
	public int currentColumn() {
		return _currentColumn;
	}

	@Override
	public int currentRow() {
		return _currentRow;
	}

	@Override
	public String currentTable() {
		return _currentTable;
	}

	private void checkReady() throws IOException {
		if (_currentTable == null) {
			throw new IllegalStateException("Writer is not ready for write access yet! No current table given.");
		}
	}

	protected abstract File internalClose() throws IOException;

	protected abstract void internalNewRow() throws IOException;

	protected abstract void internalWrite(Object cellvalue) throws IOException;

	protected abstract void internalNewTable(String tablename) throws IOException;
}
