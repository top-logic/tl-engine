/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.col.TreeView;

/**
 * {@link TreeBuilder} based on a {@link TreeView}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TreeViewTreeBuilder<T> extends AbstractTLTreeNodeBuilder {

	private TreeView<T> base;

	/**
	 * Creates a {@link TreeViewTreeBuilder}.
	 * 
	 * @param base
	 *        The {@link TreeView} to use for child lookup.
	 */
	public TreeViewTreeBuilder(TreeView<T> base) {
		this.base = base;
	}

	@Override
	public List<DefaultMutableTLTreeNode> createChildList(DefaultMutableTLTreeNode node) {
		List<DefaultMutableTLTreeNode> result = new ArrayList<>();

		// Workaround for not being able to enforce a type on user objects.
		@SuppressWarnings("unchecked")
		T businessObject = (T) node.getBusinessObject();

		Iterator<?> childrenUserObjects = base.getChildIterator(businessObject);
		while (childrenUserObjects.hasNext()) {
			result.add(createNode(node.getModel(), node, childrenUserObjects.next()));
		}
		return result;
	}

	@Override
	public boolean isFinite() {
		return base.isFinite();
	}

}