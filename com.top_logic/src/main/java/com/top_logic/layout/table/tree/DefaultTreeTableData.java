/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.tree;

import com.top_logic.layout.structure.DefaultExpandable;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.layout.table.DefaultTableData;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.TableDataOwner;
import com.top_logic.layout.toolbar.DefaultToolBar;
import com.top_logic.layout.tree.model.AbstractTreeTableModel;
import com.top_logic.layout.tree.model.TreeTableModel;
import com.top_logic.layout.tree.model.TreeUIModel;

/**
 * Default holder of the tree table model.
 * 
 * @see TreeTableComponent
 * @see TreeTableModel
 * @see AbstractTreeTableModel
 * @see TableData
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class DefaultTreeTableData extends DefaultTableData implements TreeTableData {

	/**
	 * Creates a default {@link TreeTableData}.
	 */
	public DefaultTreeTableData(TableDataOwner owner, ConfigKey configKey) {
		super(owner, configKey, false);
	}

	@Override
	public AbstractTreeTableModel<?> getTree() {
		TreeTableModel tableModel = (TreeTableModel) getTableModel();

		return (AbstractTreeTableModel<?>) tableModel.getTreeModel();
	}

	@Override
	public void setTree(AbstractTreeTableModel<?> treeModel) {
		setTableModel(treeModel.getTable());

		treeModel.setViewModel(() -> getViewModel());
	}

	/**
	 * Creates a {@link TreeTableData} for the given {@link TreeUIModel}.
	 */
	public static TreeTableData createTreeTableData(TreeTableDataOwner owner, AbstractTreeTableModel<?> model,
			ConfigKey key) {
		DefaultTreeTableData result = new DefaultTreeTableData(owner, key);

		result.setTree(model);
		result.setToolBar(new DefaultToolBar(result, new DefaultExpandable()));

		return result;
	}

}
