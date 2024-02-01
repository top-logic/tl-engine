/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.tree;

import com.top_logic.basic.col.Maybe;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.tree.TreeData;
import com.top_logic.layout.tree.TreeRenderer;
import com.top_logic.layout.tree.model.AbstractTreeTableModel;
import com.top_logic.layout.tree.model.TreeUIModel;
import com.top_logic.layout.tree.renderer.DefaultTreeRenderer;

/**
 * Data holder for {@link TreeTableComponent}s.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public interface TreeTableData extends TableData, TreeData {

	@Override
	Maybe<? extends TreeTableDataOwner> getOwner();

	/**
	 * Returns the {@link TreeUIModel}.
	 */
	public AbstractTreeTableModel<?> getTree();

	/**
	 * Sets the {@link AbstractTreeTableModel} on which the table bases.
	 */
	public void setTree(AbstractTreeTableModel<?> treeModel);

	@Override
	default TreeRenderer getTreeRenderer() {
		return DefaultTreeRenderer.INSTANCE;
	}

	@Override
	default TreeUIModel<?> getTreeModel() {
		return getTree();
	}

}
