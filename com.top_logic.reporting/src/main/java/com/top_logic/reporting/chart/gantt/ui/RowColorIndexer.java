/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.gantt.ui;

import com.top_logic.reporting.chart.gantt.model.GanttRow;

/**
 * This class calculates the color index for each row during a row iteration using the row itself
 * and its row index (in said iteration). Either it can increment the row color index (and as such
 * toggle the background color) for row groups consisting of the orginal row and calculated rows or
 * it just uses the row index as color index, as such toggles the background color for each
 * individual row.
 * 
 * @author <a href="mailto:tri@top-logic.com">tri</a>
 */
public class RowColorIndexer {

	// current color index and row number
	private int _colorIndex, _lastRowIndex;

	// flag to indicate wether to toggle color for row groups (true) or individual rows (false)
	private final boolean _onlyOrigRows;

	/**
	 * Creates a new {@link RowColorIndexer}.
	 * 
	 * @return a new instance incrementing the color index for row groups
	 */
	public static RowColorIndexer newInstanceToogleRowGroups() {
		return new RowColorIndexer(true);
	}

	/**
	 * Creates a new {@link RowColorIndexer}.
	 * 
	 * @return a new instance incrementing the color index for each row, as such directly using the row
	 *         index as color index
	 */
	public static RowColorIndexer newInstanceToogleRows() {
		return new RowColorIndexer(false);
	}

	/**
	 * Creates a new {@link RowColorIndexer}.
	 * 
	 * @param onlyOriginalRows
	 *        Flag to indicate wether to increment the color index for row groups or for each row
	 */
	private RowColorIndexer(boolean onlyOriginalRows) {
		_colorIndex = -1;
		_lastRowIndex = -1;
		_onlyOrigRows = onlyOriginalRows;
	}

	/**
	 * Calculate the color index for the given row. Depending on this instance, this might be identical
	 * to the given row index or the index of the given rows rowgroup
	 * 
	 * @param row
	 *        {@link GanttRow} for which the color index is to be retrieved
	 * @param rowIndex
	 *        index of the given row during the row iteration
	 * @return a color index to be used to calculate the background color for the given row
	 */
	int getColorIndex(GanttRow row, int rowIndex) {
		if (_onlyOrigRows) {
			if (!row.isProgrammatic()) {
				if (_colorIndex < 0) {
					_colorIndex = rowIndex;
					_lastRowIndex = rowIndex;
				} else {
					if (rowIndex > _lastRowIndex) {
						_colorIndex++;
						_lastRowIndex = rowIndex;
					}
				}
			}
		} else {
			_colorIndex = rowIndex;
		}
		return _colorIndex;
	}
}
