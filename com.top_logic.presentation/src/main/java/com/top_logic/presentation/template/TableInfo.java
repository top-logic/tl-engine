/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.template;

import java.util.List;
import java.util.Map;

import com.top_logic.presentation.data.PresentationData;

/**
 * Description of a table in a presentation template.
 *
 * @param rows
 *        The number of rows.
 * @param columns
 *        The number of columns.
 * @param columnWidths
 *        The column widths in points.
 * @param style
 *        The name of the table style, <code>null</code> if the table has none.
 * @param headerCell
 *        The first cell of the first row, <code>null</code> for an empty table.
 * @param bodyCell
 *        The first cell of the second row, <code>null</code> if the table has a single row.
 */
public record TableInfo(int rows, int columns, List<Double> columnWidths, String style, CellInfo headerCell,
		CellInfo bodyCell) {

	/**
	 * The style of a table cell.
	 *
	 * @param fill
	 *        The fill defined at the cell itself, <code>null</code> if the fill is taken from the
	 *        table style.
	 * @param text
	 *        The text of the cell.
	 */
	public record CellInfo(PaintInfo fill, TextInfo text) {

		/**
		 * The data representation with the keys {@link PresentationData#FILL} and
		 * {@link PresentationData#TEXT}.
		 */
		public Map<String, Object> toData() {
			Map<String, Object> result = PresentationData.map();
			PresentationData.put(result, PresentationData.FILL, fill == null ? null : fill.toData());
			PresentationData.put(result, PresentationData.TEXT, text == null ? null : text.toData());
			return result;
		}
	}

	/**
	 * The data representation with the keys {@link PresentationData#ROWS}, {@link PresentationData#COLUMNS},
	 * {@link PresentationData#COLUMN_WIDTHS}, {@link PresentationData#STYLE},
	 * {@link PresentationData#HEADER_CELL} and {@link PresentationData#BODY_CELL}.
	 */
	public Map<String, Object> toData() {
		Map<String, Object> result = PresentationData.map();
		result.put(PresentationData.ROWS, Double.valueOf(rows));
		result.put(PresentationData.COLUMNS, Double.valueOf(columns));
		result.put(PresentationData.COLUMN_WIDTHS, PresentationData.list(columnWidths, PresentationData::number));
		PresentationData.put(result, PresentationData.STYLE, style);
		PresentationData.put(result, PresentationData.HEADER_CELL, headerCell == null ? null : headerCell.toData());
		PresentationData.put(result, PresentationData.BODY_CELL, bodyCell == null ? null : bodyCell.toData());
		return result;
	}

}
