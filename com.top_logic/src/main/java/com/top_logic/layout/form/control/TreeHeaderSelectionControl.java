/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.util.Collections;

import com.top_logic.layout.DynamicText;
import com.top_logic.layout.tree.model.TreeUIModel;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.TreeSelectionModel;
import com.top_logic.mig.html.TreeSelectionModel.DescendantState;
import com.top_logic.mig.html.TreeSelectionModel.NodeSelectionState;

/**
 * Control displaying the overall selection state of a tree table in the header of the select
 * column.
 */
public class TreeHeaderSelectionControl extends TableHeaderSelectionControl {

	/**
	 * Creates a {@link TreeHeaderSelectionControl}.
	 */
	public TreeHeaderSelectionControl(SelectionModel selectionModel) {
		super(selectionModel, Collections.emptySet());
	}

	@Override
	protected DynamicText createCheckboxUpdate() {
		TreeSelectionModel selectionModel = selectionModel();
		TreeUIModel treeModel = selectionModel.getTreeModel();
		Object root = treeModel.getRoot();
		NodeSelectionState rootState = selectionModel.getNodeSelectionState(root);

		boolean checked = selectionModel.getTreeModel().isRootVisible() ? rootState == NodeSelectionState.FULL
			: rootState.descendants() == DescendantState.ALL;
		boolean indeterminate = !checked && rootState != NodeSelectionState.NONE;

		return checkboxUpdateScript(checked, indeterminate);
	}

	@Override
	protected void updateSelection(boolean selectAll) {
		TreeSelectionModel selectionModel = selectionModel();
		TreeUIModel treeModel = selectionModel.getTreeModel();
		Object root = treeModel.getRoot();
		NodeSelectionState rootState = selectionModel.getNodeSelectionState(root);

		boolean doSelectAll;
		if (treeModel.isRootVisible()) {
			doSelectAll = rootState == NodeSelectionState.NONE;
		} else {
			doSelectAll = rootState.descendants() != DescendantState.NONE;
		}

		Object update = selectionModel.startBulkUpdate();
		try {
			selectionModel.setSelectedSubtree(root, doSelectAll);
			if (!treeModel.isRootVisible()) {
				selectionModel.setSelected(root, false);
			}
		} finally {
			selectionModel.completeBulkUpdate(update);
		}
	}

	@SuppressWarnings({ "rawtypes" })
	private TreeSelectionModel selectionModel() {
		return (TreeSelectionModel<?>) getSelectionModel();
	}
}
