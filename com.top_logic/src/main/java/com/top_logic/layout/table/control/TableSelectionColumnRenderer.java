/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.control;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.control.SelectionPartControl;
import com.top_logic.layout.table.AbstractCellRenderer;
import com.top_logic.layout.table.CellRenderer;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.TableRenderer.Cell;
import com.top_logic.mig.html.SelectionModel;

/**
 * {@link CellRenderer} creating the technical column for keyboard row selection.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TableSelectionColumnRenderer extends AbstractCellRenderer {

	/**
	 * Singleton {@link TableSelectionColumnRenderer} instance.
	 */
	public static final TableSelectionColumnRenderer INSTANCE = new TableSelectionColumnRenderer();

	private TableSelectionColumnRenderer() {
		// Singleton constructor.
	}

	@Override
	public void writeCell(DisplayContext context, TagWriter out, Cell cell) throws IOException {
		TableData tableData = cell.getView().getTableData();
		SelectionModel selectionModel = tableData.getSelectionModel();

		TableControl view = cell.getView();
		int row = cell.getRowIndex();
		if (!view.isRowSelectable(row)) {
			return;
		}
		Object rowObject = tableData.getViewModel().getRowObject(row);
		SelectionPartControl selectionPartControl = new SelectionPartControl(selectionModel, rowObject);
		tableData.getSelectionVetoListeners().forEach(selectionPartControl::addSelectionVetoListener);
		selectionPartControl.write(context, out);
	}

}
