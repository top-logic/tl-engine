/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.grid;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.element.layout.grid.GridTreeTableNodeLabelNaming.GridNodeLabelName;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.tree.TreeTableData;
import com.top_logic.layout.table.tree.TreeTableNodeLabelNaming;

/**
 * A {@link ModelNamingScheme} for identifying a row in the tree grid by the label path to it.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class GridTreeTableNodeLabelNaming
		extends TreeTableNodeLabelNaming<GridTreeTableNode, GridNodeLabelName, GridComponent> {

	/**
	 * {@link ModelName} for the {@link GridTreeTableNodeLabelNaming}.
	 */
	@Label("{foreach(path, \" > \")}")
	public interface GridNodeLabelName extends TreeTableNodeLabelNaming.Name {

		// No properties here.

	}

	/**
	 * Creates a new {@link GridTreeTableNodeLabelNaming}.
	 */
	public GridTreeTableNodeLabelNaming() {
		super(GridTreeTableNode.class, GridNodeLabelName.class);
	}

	@Override
	protected GridComponent getOwner(GridTreeTableNode node) {
		GridTreeTableModel treeModel = (GridTreeTableModel) node.getModel();
		return treeModel.getGrid();
	}

	@Override
	protected TreeTableData getTreeTable(GridComponent owner) {
		TableData tableData = owner.getTableData();
		return (TreeTableData) tableData;
	}

}
