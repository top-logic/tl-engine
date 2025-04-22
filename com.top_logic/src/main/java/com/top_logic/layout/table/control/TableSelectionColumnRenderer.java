/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.control;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.control.SelectionPartControl;
import com.top_logic.layout.form.control.TreeSelectionPartControl;
import com.top_logic.layout.table.AbstractCellRenderer;
import com.top_logic.layout.table.CellRenderer;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.TableRenderer.Cell;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.TreeSelectionModel;

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
		Control ctrl = createSelectionPartControl(selectionModel, rowObject, tableData.getSelectionVetoListeners());
		ctrl.write(context, out);
	}

	/**
	 * Creates a {@link Control} handling the selection of the given row object in the given
	 * {@link SelectionModel}.
	 *
	 * @param vetoListeners
	 *        Optional listeners to prevent selecting or de-selecting the given object.
	 */
	public static Control createSelectionPartControl(SelectionModel selectionModel, Object rowObject,
			Iterable<SelectionVetoListener> vetoListeners) {
		Control ctrl;
		if (selectionModel instanceof TreeSelectionModel) {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			TreeSelectionPartControl<?> selectionPartControl =
				new TreeSelectionPartControl((TreeSelectionModel) selectionModel, rowObject);
			vetoListeners.forEach(selectionPartControl::addSelectionVetoListener);
			ctrl = selectionPartControl;
		} else {
			SelectionPartControl selectionPartControl = new SelectionPartControl(selectionModel, rowObject);
			vetoListeners.forEach(selectionPartControl::addSelectionVetoListener);
			ctrl = selectionPartControl;
		}
		return ctrl;
	}

}
