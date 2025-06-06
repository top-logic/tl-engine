/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.grid;

import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.table.tree.TreeTableNodeLabelNaming;

/**
 * A {@link ModelNamingScheme} for identifying a row in the tree grid by the label path to it.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class GridTreeTableNodeLabelNaming extends TreeTableNodeLabelNaming<GridTreeTableNode> {

	/**
	 * {@link ModelName} for the {@link GridTreeTableNodeLabelNaming}.
	 * 
	 * @deprecated Use {@link com.top_logic.layout.table.tree.TreeTableNodeLabelNaming.Name}. This
	 *             class just exists for compatibility with existing scripts.
	 */
	@Deprecated
	public interface GridNodeLabelName extends TreeTableNodeLabelNaming.Name {

		// Compatibility with existing scripts.

	}

	/**
	 * Creates a new {@link GridTreeTableNodeLabelNaming}.
	 */
	public GridTreeTableNodeLabelNaming() {
		super(GridTreeTableNode.class);
	}

	@Override
	protected GridComponent getOwner(GridTreeTableNode node) {
		GridTreeTableModel treeModel = (GridTreeTableModel) node.getModel();
		return treeModel.getGrid();
	}

}
