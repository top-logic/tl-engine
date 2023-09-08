/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.gui;

import com.top_logic.layout.form.FormField;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.tree.model.TreeTableModel;
import com.top_logic.layout.tree.model.TreeUIModel;

/**
 * {@link TableCell} representing the tree node aspect of a tree table cell.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TreeTableCell<N> extends TableCell implements AssertionTreeNode<N> {

	/**
	 * Creates a new {@link TreeTableCell}.
	 */
	public TreeTableCell(Object value, TableData tableData, N rowObject, String columnName) {
		super(value, tableData, rowObject, columnName);
	}

	@Override
	public boolean isSelected() {
		return getTableData().getSelectionModel().getSelection().contains(getNode());
	}

	@Override
	public N getNode() {
		return (N) getRowObject();
	}

	@Override
	public Object getContext() {
		return getTableData();
	}

	@Override
	public TreeUIModel<N> treeModel() {
		return (TreeUIModel<N>) ((TreeTableModel) getTableData().getTableModel()).getTreeModel();
	}

	@Override
	public String getNodeLabel() {
		Object value = getValue();
		if (value instanceof FormField) {
			value = ((FormField) value).getValue();
		}
		return String.valueOf(value);
	}
}

