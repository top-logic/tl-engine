/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.treetable.component;

import java.util.List;

import com.top_logic.layout.table.control.IndexViewportState;
import com.top_logic.layout.table.display.ColumnAnchor;
import com.top_logic.layout.table.display.RowIndexAnchor;
import com.top_logic.layout.table.display.ViewportState;

/**
 * Converts {@link IndexViewportState}s to {@link ViewportState} or vice versa.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class ViewportStateConverter {

	/**
	 * {@link IndexViewportState}, based on a {@link ViewportState} and a list of columns.
	 */
	public static IndexViewportState getIndexViewportState(ViewportState viewportState, List<String> columns) {
		IndexViewportState indexViewportState = new IndexViewportState();
		indexViewportState.setRowAnchor(viewportState.getRowAnchor().getIndex());
		indexViewportState.setRowOffset(viewportState.getRowAnchor().getIndexPixelOffset());

		int columnIndex = getColumnIndex(viewportState.getColumnAnchor(), columns);
		if (columnIndex > -1) {
			indexViewportState.setColumnAnchor(columnIndex);
			indexViewportState.setColumnOffset(viewportState.getColumnAnchor().getIndexPixelOffset());
		}
		return indexViewportState;
	}

	private static int getColumnIndex(ColumnAnchor columnAnchor, List<String> columns) {
		if (columnAnchor.getColumnName() != null) {
			return columns.indexOf(columnAnchor.getColumnName());
		} else {
			return -1;
		}
	}

	/**
	 * {@link ViewportState}, based on a {@link IndexViewportState} and a list of columns.
	 */
	public static ViewportState getViewportState(IndexViewportState indexViewportState, List<String> columns, int fixedColumnCount) {
		ViewportState viewportState = new ViewportState();
		viewportState
			.setRowAnchor(RowIndexAnchor.create(indexViewportState.getRowAnchor(), indexViewportState.getRowOffset()));
		if (hasHorizontalScrollPosition(indexViewportState, fixedColumnCount)) {
			viewportState.setColumnAnchor(
				ColumnAnchor.create(getColumnName(indexViewportState, columns), indexViewportState.getColumnOffset()));
		}
		return viewportState;
	}

	private static boolean hasHorizontalScrollPosition(IndexViewportState indexViewportState, int fixedColumnCount) {
		return indexViewportState.getColumnAnchor() > fixedColumnCount;
	}

	private static String getColumnName(IndexViewportState indexViewportState, List<String> columns) {
		int columnAnchor = getColumnAnchorInAllowedRange(indexViewportState.getColumnAnchor(), columns.size());
		return columns.get(columnAnchor);
	}

	private static int getColumnAnchorInAllowedRange(int columnAnchor, int upperBoundary) {
		return Math.max(0, Math.min(columnAnchor, upperBoundary - 1));
	}

}
