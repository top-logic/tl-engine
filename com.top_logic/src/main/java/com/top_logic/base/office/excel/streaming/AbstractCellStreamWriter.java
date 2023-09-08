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

	protected int currentColumnIndex = 0;

	protected int currentRowIndex = 0;

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

	private void checkReady() throws IOException {
		if (currentTable == null) {
			throw new IllegalStateException("Writer is not ready for write access yet! No current table given.");
		}
	}

	protected abstract File internalClose() throws IOException;

	protected abstract void internalNewRow() throws IOException;

	protected abstract void internalWrite(Object cellvalue) throws IOException;

	protected abstract void internalNewTable(String tablename) throws IOException;
}
