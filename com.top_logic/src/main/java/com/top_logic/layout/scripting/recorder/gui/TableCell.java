/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.gui;

import com.top_logic.layout.table.TableData;

/**
 * Represents a {@link #getValue() value} in a table cell. The cell is identified by the
 * {@link #getRowObject() row} and {@link #getColumnName() column index}. The table is represented
 * by the {@link TableData} of that table.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TableCell implements Cloneable {

	private Object value;
	private final TableData tableData;
	private final Object rowObject;
	private final String columnName;

	public TableCell(Object value, TableData tableData, Object rowObject, String columnName) {
		this.value = value;
		this.tableData = tableData;
		this.rowObject = rowObject;
		this.columnName = columnName;
	}

	public final Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public final TableData getTableData() {
		return tableData;
	}

	public final Object getRowObject() {
		return rowObject;
	}

	public final String getColumnName() {
		return columnName;
	}

	public boolean hasCellInformation() {
		return getColumnName() != null;
	}

	public boolean hasRowInformation() {
		return getRowObject() != null;
	}

	@Override
	public TableCell clone() {
		try {
			return (TableCell) super.clone();
		} catch (CloneNotSupportedException ex) {
			throw new AssertionError("Must not occur because implementation of Cloneable.");
		}
	}

}
