/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

import java.util.List;

import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.tree.model.AbstractTreeTableModel.AbstractTreeTableNode;

/**
 * {@link DefaultTreeTableModel} also implementing {@link IndexedTLTreeModel}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class IndexedTreeTableModel<N extends AbstractTreeTableNode<N>> extends AbstractTreeTableModel<N>
		implements IndexedTLTreeModel<N> {

	private UserObjectIndex<N> _index = new UserObjectIndex<>();

	/**
	 * Creates a {@link IndexedTreeTableModel}.
	 *
	 * @see AbstractTreeTableModel#AbstractTreeTableModel(TreeBuilder, Object, List,
	 *      TableConfiguration)
	 */
	public IndexedTreeTableModel(TreeBuilder<N> builder, Object rootUserObject,
			List<String> columnNames, TableConfiguration config) {
		super(builder, rootUserObject, columnNames, config);
	}

	@Override
	protected void handleInitNode(N node) {
		super.handleInitNode(node);
		_index.handleInitNode(node);
	}

	@Override
	protected void handleRemoveNode(N subtreeRootParent, N node) {
		super.handleRemoveNode(subtreeRootParent, node);
		_index.handleRemoveNode(subtreeRootParent, node);
	}

	@Override
	public UserObjectIndex<N> getIndex() {
		return _index;
	}

}
