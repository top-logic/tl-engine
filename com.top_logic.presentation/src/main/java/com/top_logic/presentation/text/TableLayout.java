/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.text;

import java.awt.geom.Rectangle2D;

import org.apache.poi.sl.usermodel.Insets2D;
import org.apache.poi.xslf.usermodel.XSLFTable;
import org.apache.poi.xslf.usermodel.XSLFTableCell;

/**
 * Computes the row heights of a table and places its cells as PowerPoint does.
 *
 * <ul>
 * <li>Each row is as high as the highest text of its cells, measured by
 * {@link TextMeasure#measure(org.apache.poi.sl.usermodel.TextShape)} at the width of the cell,
 * plus the top and bottom insets of the cell, but at least as high as the row height stored in
 * the table. A cell spanning several rows does not contribute to the height of a row.</li>
 * <li>The cells are placed side by side and one below the other without any distance, a cell
 * spanning several columns or rows covers their whole width or height.</li>
 * </ul>
 *
 * <p>
 * Apache POI computes the row heights with its own text layout when a cell position is requested
 * first, and inserts a distance between the cells. Its row heights deviate from the
 * {@link TextMeasure measured} heights, so that a table written with measured row heights would be
 * rendered with different ones. A table laid out here keeps the stored row heights of its rows
 * when it is written and rendered.
 * </p>
 */
public final class TableLayout {

	private TableLayout() {
		// Static utility.
	}

	/**
	 * Sets the row heights of the given table and places its cells.
	 *
	 * <p>
	 * The table keeps the position of its anchor, its size results from the column widths and the
	 * computed row heights.
	 * </p>
	 *
	 * @param table
	 *        The table, whose stored row heights are the minimum heights of its rows.
	 */
	public static void layout(XSLFTable table) {
		int rowCount = table.getNumberOfRows();
		int columnCount = table.getNumberOfColumns();
		double[] widths = new double[columnCount];
		for (int column = 0; column < columnCount; column++) {
			widths[column] = table.getColumnWidth(column);
		}
		double[] heights = new double[rowCount];
		for (int row = 0; row < rowCount; row++) {
			double height = table.getRowHeight(row);
			for (int column = 0; column < columnCount; column++) {
				XSLFTableCell cell = table.getCell(row, column);
				if (cell == null || cell.isMerged()) {
					continue;
				}
				// The cell text is wrapped at the width of the cell.
				cell.setAnchor(new Rectangle2D.Double(0, 0, span(widths, column, cell.getGridSpan()), 0));
				if (cell.getRowSpan() <= 1) {
					height = Math.max(height, contentHeight(cell));
				}
			}
			heights[row] = height;
			table.setRowHeight(row, height);
		}

		Rectangle2D anchor = table.getAnchor();
		double y = anchor.getY();
		for (int row = 0; row < rowCount; row++) {
			double x = anchor.getX();
			for (int column = 0; column < columnCount; column++) {
				XSLFTableCell cell = table.getCell(row, column);
				if (cell != null) {
					int columnSpan = cell.isMerged() ? 1 : cell.getGridSpan();
					int rowSpan = cell.isMerged() ? 1 : cell.getRowSpan();
					cell.setAnchor(new Rectangle2D.Double(x, y, span(widths, column, columnSpan),
						span(heights, row, rowSpan)));
				}
				x += widths[column];
			}
			y += heights[row];
		}
		table.setAnchor(
			new Rectangle2D.Double(anchor.getX(), anchor.getY(), span(widths, 0, columnCount), y - anchor.getY()));
	}

	/**
	 * The height the text of the given cell needs at the width of the cell's anchor, including the
	 * top and bottom insets of the cell.
	 */
	public static double contentHeight(XSLFTableCell cell) {
		Insets2D insets = cell.getInsets();
		return TextMeasure.measure(cell).height() + insets.top + insets.bottom;
	}

	/**
	 * The sum of the given number of sizes starting at the given index.
	 */
	private static double span(double[] sizes, int start, int count) {
		double result = 0;
		for (int n = start, end = Math.min(sizes.length, start + Math.max(1, count)); n < end; n++) {
			result += sizes[n];
		}
		return result;
	}

}
