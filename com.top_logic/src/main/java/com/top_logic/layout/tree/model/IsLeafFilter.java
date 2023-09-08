/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.TreeView;

/**
 * {@link IsLeafFilter} is a filter which accepts the leaf nodes (resp. the no
 * leaf nodes) of a given tree.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class IsLeafFilter implements Filter<Object> {

	private final TreeView tree;
	private final boolean acceptLeaf;

	public IsLeafFilter(TreeView tree, boolean acceptLeaf) {
		this.tree = tree;
		this.acceptLeaf = acceptLeaf;
	}

	@Override
	public boolean accept(Object anObject) {
		return tree.isLeaf(anObject) == acceptLeaf;
	}

	/**
	 * Creates a {@link Filter} which accepts the leaf nodes of the given
	 * {@link TreeView tree}.
	 * 
	 * @param tree
	 *        the tree whose leafs must be accepted.
	 */
	public static IsLeafFilter createIsLeafFilter(TreeView tree) {
		return new IsLeafFilter(tree, true);
	}

	/**
	 * Creates a {@link Filter} which accepts the nodes which are not leaf nodes
	 * of the given {@link TreeView tree}.
	 * 
	 * @param tree
	 *        the tree whose non leaf nodes must be accepted.
	 */
	public static IsLeafFilter createIsNotLeafFilter(TreeView tree) {
		return new IsLeafFilter(tree, false);
	}
}
