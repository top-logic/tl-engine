/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;


/**
 * {@link DefaultMutableTLTreeModel} that keeps a link from its user objects back to its nodes.
 * 
 * @param <N>
 *        The node type.
 * 
 * @see #updateUserObject(Object)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LazyMappedTreeModel<N extends AbstractMutableTLTreeNode<N>> extends AbstractMutableTLTreeModel<N> {

	private final UserObjectIndex<N> mapping = new UserObjectIndex<>();

	/**
	 * Creates a {@link LazyMappedTreeModel}.
	 * 
	 * @param rootUserObject
	 *        See {@link DefaultMutableTLTreeModel#DefaultMutableTLTreeModel(TreeBuilder, Object)}.
	 * @param builder
	 *        See {@link DefaultMutableTLTreeModel#DefaultMutableTLTreeModel(TreeBuilder, Object)}.
	 */
	public LazyMappedTreeModel(Object rootUserObject, TreeBuilder<N> builder) {
		super(builder, rootUserObject);
	}

	/**
	 * @see UserObjectIndex#updateUserObject(Object)
	 */
	public boolean updateUserObject(Object userObject) {
		return mapping.updateUserObject(userObject);
	}

	@Override
	protected void handleInitNode(N node) {
		super.handleInitNode(node);
		mapping.handleInitNode(node);
	}

	@Override
	protected void handleRemoveNode(N subtreeRootParent, N node) {
		super.handleRemoveNode(subtreeRootParent, node);
		mapping.handleRemoveNode(subtreeRootParent, node);
	}
	
}
