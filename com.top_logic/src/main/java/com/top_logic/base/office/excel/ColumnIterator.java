/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.excel;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 * Instances of this class are responsible for iterating over the logical columns in a row. If a
 * cell does not exist, a new one is created.
 * 
 * @author <a href=mailto:wta@top-logic.com>wta</a>
 */
public class ColumnIterator implements Iterator<Cell> {

	/**
	 * The total number of logical columns.
	 */
	private final int _width;

	/**
	 * The cell we're currently at.
	 */
	private Cell _cell;

	/**
	 * The logical column we're currently at.
	 */
	private int _column;

	/**
	 * The header row defining the table's layout.
	 */
	private Row _row;

	/**
	 * Creates an instance of this class which allows iteration over the logical cell in the
	 * specified row starting at the specified cell.
	 * 
	 * @param row
	 *        the {@link Row} to iterate over
	 * @param start
	 *        the {@link Cell} to start iterating at
	 * @param width
	 *        the total number of logical columns to iterate over
	 */
	public ColumnIterator(final Row row, final Cell start, final int width) {
		_row = row;
		_cell = row.getCell(start.getColumnIndex(), MissingCellPolicy.CREATE_NULL_AS_BLANK);
		_width = width;
		_column = 0;
	}

	@Override
	public boolean hasNext() {
		return _column < _width;
	}

	@Override
	public Cell next() {
		// check again if we have more elements.
		// In case we've reached the end, throw the appropriate
		// Exception just as the interface demands.
		if (!hasNext()) {
			throw new NoSuchElementException("No more elements");
		}

		// remember the cell itself before returning it
		// since we're going to advance now.
		final Cell current = _cell;

		// increment the column
		_column++;

		// check if the current cell is included in any
		// cell range.
		final CellRangeAddress range = POIExcelUtil.getRange(_cell);
		if (range != null) {
			_cell = _row.getCell(range.getLastColumn() + 1, MissingCellPolicy.CREATE_NULL_AS_BLANK);
		} else {
			_cell = _row.getCell(current.getColumnIndex() + 1, MissingCellPolicy.CREATE_NULL_AS_BLANK);
		}

		// return the PREVIOUSLY remembered cell!
		return current;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Removing cells is not supported.");
	}
}