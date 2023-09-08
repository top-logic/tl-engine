/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.grid;

import java.util.List;

import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.tree.model.AbstractTreeTableModel;
import com.top_logic.layout.tree.model.TreeBuilder;

/**
 * {@link AbstractTreeTableModel} for tree grids.
 * 
 * <p>
 * Keeps a reference back to the component to be able to produce
 * {@link ModelName}s for {@link GridTreeTableNode}s.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class GridTreeTableModel extends AbstractTreeTableModel<GridTreeTableNode> {

	private final GridComponent _grid;

	/**
	 * Creates a {@link GridTreeTableModel}.
	 *
	 * @param grid See {@link #getGrid()}.
	 * 
	 * @see AbstractTreeTableModel#AbstractTreeTableModel(TreeBuilder, Object, List, TableConfiguration)
	 */
	public GridTreeTableModel(GridComponent grid, TreeBuilder<GridTreeTableNode> builder,
			Object rootUserObject, List<String> columnNames,
			TableConfiguration config) {
		super(builder, rootUserObject, columnNames, config);
		_grid = grid;
	}

	/**
	 * The context component.
	 */
	public GridComponent getGrid() {
		return _grid;
	}

	@Override
	protected void handleRootVisible(boolean newVisibility) {
		super.handleRootVisible(newVisibility);

		/* Fix the selection. Changing the number of rows breaks it: The selection is index-based,
		 * but the FormFields are bound to the selected _object_. Neither of that is updated, when
		 * the number of rows change. */
		_grid.invalidateSelection();
	}

}
