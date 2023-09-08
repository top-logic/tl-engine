/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.layout;

import java.util.Arrays;
import java.util.List;

import com.top_logic.layout.form.boxes.model.AbstractBox;
import com.top_logic.layout.form.boxes.model.Box;
import com.top_logic.layout.form.boxes.model.ContentBox;
import com.top_logic.layout.form.boxes.model.Table;

/**
 * {@link BoxLayout} positioning boxes horizontally but wrapping to a next row each fixed number of
 * super columns.
 * 
 * <p>
 * <b>Note:</b> This {@link BoxLayout} is stateful and must not be used in more than one {@link Box}.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FlowLayout implements BoxLayout {

	private final int _superColumns;

	private final int[] _subColumns;

	/**
	 * Creates a {@link FlowLayout}.
	 * 
	 * @param superColumns
	 *        The number of columns to create.
	 */
	public FlowLayout(int superColumns) {
		_superColumns = superColumns;
		_subColumns = new int[_superColumns];
	}

	@Override
	public boolean isHorizontal() {
		return true;
	}

	@Override
	public void layout(AbstractBox container, List<Box> boxes) {
		Arrays.fill(_subColumns, 0);

		int superColumn = 0;
		int rows = 0;
		int subRows = 0;
		for (int n = 0, cnt = boxes.size(); n < cnt; n++) {
			Box box = boxes.get(n);
			box.layout();

			_subColumns[superColumn] = Math.max(_subColumns[superColumn], box.getColumns());
			subRows = Math.max(subRows, box.getRows());

			superColumn++;
			if (superColumn == _superColumns) {
				superColumn = 0;
				rows += subRows;
				subRows = 0;
			}
		}

		rows += subRows;
		int columns = sum(_subColumns);
		container.setDimension(columns, rows);
	}

	private static int sum(int[] values) {
		int result = 0;
		for (int value : values) {
			result += value;
		}
		return result;
	}

	@Override
	public void enter(AbstractBox container, Table<ContentBox> table, int x, int y, List<Box> boxes) {
		int superColumn = 0;
		int row = 0;
		int subRows = 0;

		int cnt = boxes.size();
		int lastBoxIndex = cnt - 1;
		int lastRowStartIndex = ((cnt - 1) / _superColumns) * _superColumns;

		for (int n = 0; n < cnt; n++) {
			Box box = boxes.get(n);

			subRows = Math.max(subRows, box.getRows());

			superColumn++;
			if (superColumn == _superColumns || n == lastBoxIndex) {
				int superColumnsInRow = superColumn;
				int startPos = n + 1 - superColumnsInRow;
				int availableRows = startPos == lastRowStartIndex ? container.getRows() - row : subRows;
				for (int offset = 0, column = 0; offset < superColumnsInRow; column += _subColumns[offset], offset++) {
					int subColumns =
						offset < superColumnsInRow - 1 ? _subColumns[offset] : container.getColumns() - column;
					boxes.get(startPos + offset).enter(x + column, y + row, subColumns, availableRows, table);
				}

				superColumn = 0;
				row += subRows;
				subRows = 0;
			}
		}
	}

}
