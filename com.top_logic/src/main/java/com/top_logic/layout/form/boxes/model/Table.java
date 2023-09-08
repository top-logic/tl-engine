/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.model;

/**
 * Two-dimensional collection of elements.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Table<T> {

	private final int _columns;

	private final int _rows;

	private final T[] _data;

	/**
	 * Creates a {@link Table}.
	 * 
	 * @param columns
	 *        See {@link #getColumns()}.
	 * @param rows
	 *        See {@link #getRows()}.
	 */
	public Table(int columns, int rows) {
		_columns = columns;
		_rows = rows;
		_data = allocate(columns, rows);
	}

	/**
	 * The number of columns in this collection.
	 */
	public int getColumns() {
		return _columns;
	}

	/**
	 * The number of rows in this collection.
	 */
	public int getRows() {
		return _rows;
	}

	/**
	 * The element at the given column and row.
	 * 
	 * @param column
	 *        The column to access.
	 * @param row
	 *        The row to access.
	 * @return The element at the given position.
	 */
	public T get(int column, int row) {
		return _data[index(column, row)];
	}

	/**
	 * Updates the element at the given column and row.
	 * 
	 * @param column
	 *        The column to access.
	 * @param row
	 *        The row to access.
	 * @param newValue
	 *        The new value to set.
	 * @return The value that was stored at the given position before.
	 */
	public T set(int column, int row, T newValue) {
		int index = index(column, row);
		T oldValue = _data[index];
		_data[index] = newValue;
		return oldValue;
	}

	private int index(int x, int y) {
		if (x < 0 || x >= _columns) {
			throw new IndexOutOfBoundsException("0 <= (x=" + x + ") < " + _columns);
		}
		if (y < 0 || y >= _rows) {
			throw new IndexOutOfBoundsException("0 <= (y=" + y + ") < " + _rows);
		}
		return x + y * _columns;
	}

	@SuppressWarnings("unchecked")
	private T[] allocate(int columns, int rows) {
		return (T[]) new Object[columns * rows];
	}
}
