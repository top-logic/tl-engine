/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

/**
 * Two-dimensional fixed size collection.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Matrix<T> {

	private final int _rows;

	private final int _columns;

	private final T[] _values;

	/**
	 * Creates a {@link Matrix} with the given dimension.
	 * 
	 * @param rows
	 *        See {@link #getRows()}.
	 * @param columns
	 *        See {@link #getColumns()}.
	 */
	public Matrix(int rows, int columns) {
		_rows = rows;
		_columns = columns;
		_values = allocate();
	}

	@SuppressWarnings("unchecked")
	private T[] allocate() {
		return (T[]) new Object[size()];
	}

	/**
	 * The number of rows.
	 */
	public int getRows() {
		return _rows;
	}

	/**
	 * The number of columns.
	 */
	public int getColumns() {
		return _columns;
	}

	/**
	 * Get the element at the given location.
	 * 
	 * @param row
	 *        The row to access.
	 * @param column
	 *        The column to access.
	 * @return The element in the given row at the given column.
	 */
	public T get(int row, int column) {
		checkBounds(row, column);
		return _values[index(row, column)];
	}

	/**
	 * Updates the element at the given location.
	 * 
	 * @param row
	 *        The row to access.
	 * @param column
	 *        The column to access.
	 * @param value
	 *        The new element to set.
	 * @return The element that was replaced by the new one.
	 */
	public T set(int row, int column, T value) {
		checkBounds(row, column);
		int index = index(row, column);
		T before = _values[index];
		_values[index] = value;
		return before;
	}

	/**
	 * Fills the complete {@link Matrix} with the given value.
	 * 
	 * @param value
	 *        The value to set at all locations.
	 */
	public void fill(T value) {
		for (int n = 0, cnt = size(); n < cnt; n++) {
			_values[n] = value;
		}
	}

	/**
	 * Fills the given row with the given value.
	 * 
	 * @param row
	 *        The row to fill.
	 * @param value
	 *        The value to set at all locations of the given row.
	 */
	public void fillRow(int row, T value) {
		fillRow(row, 0, _columns, value);
	}

	/**
	 * Fills the given column range of the given row with the given value.
	 * 
	 * @param row
	 *        The row to fill.
	 * @param columnStart
	 *        The first column to fill (inclusive).
	 * @param columnStop
	 *        The column to stop filling at (exclusive).
	 * @param value
	 *        The value to set at all locations of the given row.
	 */
	public void fillRow(int row, int columnStart, int columnStop, T value) {
		int offset = rowOffset(row);
		for (int n = offset + columnStart, stop = offset + columnStop; n < stop; n++) {
			_values[n] = value;
		}
	}

	/**
	 * Fills the given column with the given value.
	 * 
	 * @param column
	 *        The column to fill.
	 * @param value
	 *        The value to set at all locations of the given column.
	 */
	public void fillColumn(int column, T value) {
		fillColumn(column, 0, _rows, value);
	}

	/**
	 * Fills the given row range of the given column with the given value.
	 * 
	 * @param column
	 *        The column to fill.
	 * @param startRow
	 *        The first row to fill (inclusive).
	 * @param stopRow
	 *        The row to stop filling at (exclusive).
	 * @param value
	 *        The value to set at all locations of the given column.
	 */
	public void fillColumn(int column, int startRow, int stopRow, T value) {
		for (int n = rowOffset(startRow) + column, stop = rowOffset(stopRow); n < stop; n += _columns) {
			_values[n] = value;
		}
	}

	private int size() {
		return _rows * _columns;
	}

	private int index(int row, int column) {
		return rowOffset(row) + column;
	}

	private int rowOffset(int row) {
		return row * _columns;
	}

	private void checkBounds(int row, int column) {
		if (row < 0 || row >= _rows) {
			throw new IndexOutOfBoundsException("Row '" + row + "' out of bounds [0, " + _rows + ").");
		}
		if (column < 0 || column >= _columns) {
			throw new IndexOutOfBoundsException("Column '" + column + "' out of bounds [0, " + _columns + ").");
		}
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append('[');
		result.append('\n');
		int index = 0;
		for (int row = 0; row < _rows; row++) {
			if (row > 0) {
				result.append(',');
				result.append('\n');
			}
			result.append('\t');
			result.append('[');
			for (int column = 0; column < _columns; column++) {
				if (column > 0) {
					result.append(',');
					result.append('\t');
				}
				result.append(_values[index++]);
			}
			result.append(']');
		}
		result.append('\n');
		result.append(']');
		return result.toString();
	}

}
