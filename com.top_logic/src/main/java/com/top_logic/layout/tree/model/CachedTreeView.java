/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;


import com.top_logic.basic.col.TreeView;

/**
 * {@link TLTreeModel} based on a {@link TreeView}.
 * 
 * <p>
 * This implementation wraps user objects (on which the {@link TreeView} algorithm operates) into
 * node objects implementing the {@link TLTreeNode} interface.
 * </p>
 * 
 * @param <T>
 *        The type of the base business objects.
 * 
 * @see ConstantTreeViewTreeModelAdapter An adapter that can be used more efficiently, if the tree
 *      structure is guaranteed not to change and each object of the underlying tree is mapped to
 *      exactly one node.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CachedTreeView<T> extends LazyMappedTreeModel<DefaultMutableTLTreeNode> {
	
	/**
	 * Creates a {@link CachedTreeView}.
	 * @param base
	 *        The algorithm that creates the tree structure on user objects.
	 * @param rootBusinessObject
	 *        The root object to use as {@link TLTreeNode#getBusinessObject()} of the
	 *        {@link #getRoot()} node.
	 */
	public CachedTreeView(TreeView<T> base, T rootBusinessObject) {
		super(rootBusinessObject, new TreeViewTreeBuilder<>(base));
	}
	
}
