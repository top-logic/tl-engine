/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

import java.util.List;

/**
 * An {@link AbstractTreeModel} for {@link TLTreeNode}s.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class AbstractTLTreeNodeModel<N extends TLTreeNode<N>> extends AbstractTreeModel<N> {

	@Override
	public boolean hasChild(N parent, Object node) {
		if (!(node instanceof TLTreeNode)) {
			return false;
		}
		TLTreeNode<?> parentOfNode = ((TLTreeNode<?>) node).getParent();
		return parent == parentOfNode;
	}

	@Override
	public List<? extends N> getChildren(N parent) {
		return parent.getChildren();
	}

	@Override
	public N getParent(N node) {
		return node.getParent();
	}

	@Override
	public Object getBusinessObject(N node) {
		return node.getBusinessObject();
	}

	@Override
	public boolean isLeaf(N node) {
		return node.isLeaf();
	}

}
