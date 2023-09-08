/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.decorator;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.table.AbstractCellRenderer;
import com.top_logic.layout.table.CellRenderer;
import com.top_logic.layout.table.TableRenderer.Cell;

/**
 * {@link CellRenderer} for columns in a compare table.
 * 
 * <p>
 * The {@link SimpleCompareCellRenderer} delegates to the actual {@link CellRenderer} with a
 * {@link Cell} that unwraps the {@link CompareRowObject}.
 * </p>
 * 
 * @see CompareCellRenderer
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SimpleCompareCellRenderer extends AbstractCellRenderer {

	private CellRenderer _finalRenderer;

	private boolean _treeTable;

	/**
	 * Creates a new {@link SimpleCompareCellRenderer}.
	 * 
	 * @param cellRenderer
	 *        Actual {@link CellRenderer}.
	 * @param treeTable
	 *        Whether the table is a tree table.
	 */
	public SimpleCompareCellRenderer(CellRenderer cellRenderer, boolean treeTable) {
		_finalRenderer = cellRenderer;
		_treeTable = treeTable;
	}

	@Override
	public void writeCell(DisplayContext context, TagWriter out, Cell cell) throws IOException {
		_finalRenderer.writeCell(context, out, createCompareCell(cell));
	}

	/**
	 * {@link CompareCell}, that shall be rendered.
	 */
	protected CompareCell createCompareCell(Cell innerCell) {
		return new CompareCell(innerCell, _treeTable);
	}

	/**
	 * true, if a tree table shall be rendered, false otherwise.
	 */
	protected final boolean isTreeTable() {
		return _treeTable;
	}

}

