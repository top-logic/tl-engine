/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

import com.top_logic.layout.tree.model.DefaultTreeUINodeModel.DefaultTreeUINode;

/**
 * {@link AbstractTreeUINodeModel} managing an index of {@link DefaultTreeUINode}s by their business
 * object.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class DefaultIndexedTreeModel extends DefaultTreeUINodeModel implements IndexedTLTreeModel<DefaultTreeUINode> {

	private UserObjectIndex<DefaultTreeUINode> _index = new UserObjectIndex<>();

	/**
	 * Creates a {@link DefaultIndexedTreeModel}.
	 */
	public DefaultIndexedTreeModel(TreeBuilder<DefaultTreeUINode> builder, Object rootUserObject) {
		super(builder, rootUserObject);
	}

	@Override
	protected void handleInitNode(DefaultTreeUINode node) {
		super.handleInitNode(node);
		_index.handleInitNode(node);
	}

	@Override
	protected void handleRemoveNode(DefaultTreeUINode subtreeRootParent, DefaultTreeUINode node) {
		super.handleRemoveNode(subtreeRootParent, node);
		_index.handleRemoveNode(subtreeRootParent, node);
	}

	@Override
	public UserObjectIndex<DefaultTreeUINode> getIndex() {
		return _index;
	}

}
