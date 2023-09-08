/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.component;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import com.top_logic.layout.tree.model.StructureTreeModel;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Subclass of {@link AbstractTreeModelBuilder} whose nodes are {@link TLTreeNode}.
 * 
 * @see StructureTreeModel Tree model implementation with nodes implementing
 *      {@link Object#equals(Object)} based on their {@link TLTreeNode#getBusinessObject() business
 *      objects}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class TreeNodeBasedTreeModelBuilder<N extends TLTreeNode<? extends N>> extends
		AbstractTreeModelBuilder<N> {

	@Override
	public Collection<? extends N> getParents(LayoutComponent contextComponent, N node) {
		N parent = node.getParent();
		if (parent == null) {
			// node is root
			return Collections.emptyList();
		} else {
			return Collections.singletonList(parent);
		}
	}

	@Override
	public Iterator<? extends N> getChildIterator(N node) {
		return node.getChildren().iterator();
	}

	@Override
	public boolean supportsNode(LayoutComponent contextComponent, Object node) {
		return node instanceof TLTreeNode<?>;
	}

}

